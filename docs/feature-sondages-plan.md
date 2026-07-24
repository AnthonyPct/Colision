# Plan — Fonctionnalité Sondages (polls)

## Contexte

Le conseil de **Caulnes** (premiers testeurs) demande une fonctionnalité de **sondages**. Un membre crée un
sondage avec une question et ≥2 réponses, une date de clôture, et un périmètre : soit **une ou plusieurs
commissions** (seuls leurs membres votent), soit **public** (tous les membres du projet votent). Après
clôture (date dépassée) le vote est impossible. On voit les résultats d'un sondage clos, ou d'un sondage
en cours dès qu'on a voté (ou si on n'est pas éligible = lecture seule). On peut changer son vote tant que
le sondage est ouvert. Un onglet sépare Ouverts / Clos.

Le design est déjà fait et récupéré depuis Claude Design (`screens-polls.jsx` + `app.jsx` + `data.jsx`) :
3 sous-écrans (liste, détail+vote, création) + un **5ᵉ onglet « Sondages »** dans la bottom-nav
(ordre : Agenda · Commissions · **Sondages** · Membres · Projet).

> ⚠️ **Note scope** : les sondages sont marqués *V2 / hors-périmètre* dans `docs/prd.md` (§V2) et le
> product-brief (la doctrine produit était « on propose un créneau + détection de conflit, pas un sondage »).
> C'est donc un **ajout de périmètre assumé** suite au retour terrain de Caulnes — aucune épopée/story
> existante ne le couvre.

### Décisions produit (validées)

- **Public** : toggle « Tout le projet 🏛️ » en tête de « Qui peut voter ? » ; actif = public (grise les puces
  commissions). Modèle `target_type ∈ (public, commissions)`.
- **Notifications** : push aux membres éligibles **à la création** du sondage (réutilise l'infra Edge Function).
- **Menu ⋯ (poll-detail)** : **Supprimer** (créateur uniquement, avec confirmation). Pas d'édition.
- **Clôture** : dérivée de `closes_at` vs maintenant (pas de colonne `status`, pas de cron). Le vote est refusé
  côté serveur après `closes_at`.
- **Anonymat** : seuls les compteurs agrégés sont affichés. `poll_vote` stocke `member_id` (1 vote/membre +
  changement de vote) mais l'UI ne montre jamais qui a voté quoi.

---

## 1. Backend Supabase — nouvelle migration `supabase/migrations/20260723_015_polls.sql`

Suivre les conventions des migrations existantes (SQL numéroté, `create ... if not exists`, trigger
`set_updated_at`, commentaires « pourquoi », RLS via `public.is_project_member(project_id)`).

**Tables**

- `poll(id uuid pk, project_id uuid → project cascade, created_by_member_id uuid → member set null,
  question text not null, target_type text check (target_type in ('public','commissions')) not null,
  closes_at timestamptz not null, created_at, updated_at)`.
- `poll_option(id uuid pk, poll_id uuid → poll cascade, label text not null, position int not null)`.
- `poll_commission(poll_id, commission_id)` — junction, PK composite, FKs cascade (miroir de
  `meeting_commission`). Renseignée seulement si `target_type='commissions'`.
- `poll_vote(poll_id uuid → poll cascade, member_id uuid → member cascade, option_id uuid → poll_option
  cascade, created_at, updated_at, PK (poll_id, member_id))` — 1 vote/membre ; changer de vote = update
  `option_id`.

**Indexes** (inline) : `poll.project_id`, `poll_option.poll_id`, `poll_commission.commission_id`,
`poll_vote.poll_id`, `poll_vote.option_id`.

**RLS** : `select` gaté par `is_project_member(project_id)` (sous-requête sur le poll parent pour
option/commission/vote). Les écritures passent par des RPC `security definer` (comme
`create_meeting_with_commissions`) plutôt que par des policies d'insert directes.

**RPC** (grant execute to anon, authenticated) :

- `create_poll(p_project_id, p_question, p_target_type, p_commission_ids uuid[], p_option_labels text[],
  p_closes_at, p_created_by_member_id) returns public.poll` — insère poll + options (`position` = ordre du
  tableau) + `poll_commission` dans **une transaction** (miroir exact de `create_meeting_with_commissions`,
  `20260525_012_conflict_push.sql:100`). `raise exception` si <2 options ou (commissions et liste vide).
- `cast_vote(p_poll_id, p_option_id, p_member_id)` — valide : poll ouvert (`closes_at > now()`), option
  appartient au poll, membre éligible (public → membre du projet ; commissions → présent dans une commission
  ciblée via `member_commission ∩ poll_commission`). Upsert `poll_vote` (conflit sur PK `(poll_id,member_id)`
  → update `option_id`). Sinon `raise exception`.
- `delete_poll(p_poll_id)` — vérifie `created_by_member_id = current member` (via `current_device_id()` →
  member), supprime (cascade options/votes/links).

**Push à la création** : miroir de `20260525_012` —

- Edge Function `supabase/functions/dispatch_poll_push/` (copier la structure de `dispatch_meeting_push`) :
  résout les membres éligibles (public = tous les membres du projet ; commissions = jointure
  `member → member_commission → poll_commission`), récupère les tokens FCM/APNs, envoie
  « Nouveau sondage : <question> », retry/backoff comme l'existant.
- `create constraint trigger ... deferrable initially deferred after insert on public.poll` →
  `fn_dispatch_poll_push()` qui `net.http_post` vers `dispatch_poll_push` au COMMIT (les options/links sont
  alors visibles). Réutilise `app.edge_functions_base_url` / `app.service_role_key`.

Appliquer via le MCP Supabase (`apply_migration`) sur le projet **staging** (flavor development) et déployer
l'Edge Function (`deploy_edge_function`). Vérifier avec `get_advisors` (RLS/perf).

## 2. Room (cache local) — `core/database/`

- Entities `core/database/entity/` : `PollEntity`, `PollOptionEntity`, `PollCommissionEntity`,
  `PollVoteEntity` (mêmes conventions que `MeetingEntity`/`MeetingCommissionEntity` : `@PrimaryKey String`,
  timestamps ISO `String`, FKs `CASCADE`, `@Index`). Échelle municipale (~19 membres) → on cache **tous** les
  `poll_vote` : compteurs par option + `myVote` + `voters` dérivés côté client.
- DAO `core/database/dao/PollDao.kt` : `observeByProject(projectId): Flow<...>`, `observeById(pollId)`,
  `observeOptions/Commissions/Votes`, et `@Transaction replaceForProject(...)` (delete-missing + upsert)
  comme `MeetingDao`.
- `ColisionDatabase.kt` : ajouter les 4 entities, **bump `version` 3 → 4**, ajouter `abstract fun pollDao()`.
  Ajouter la migration Room + schéma JSON exporté via la skill **`migrate-room`**.
- `core/di/CoreModule.kt` : `single { get<ColisionDatabase>().pollDao() }` + brancher `PollsRepository` dans
  le `DefaultProjectSyncManager` (pour que `refresh(projectId)` sync les sondages).

## 3. Feature `feature/poll/` (nommage anglais, comme agenda/meeting/arbitrage)

Utiliser la skill **`scaffold-feature`** pour le graphe + les 3 Route/Screen/ViewModel, puis remplir.
Chaque sous-écran = 4 fichiers (`*Contract.kt` / `*Route.kt` / `*Screen.kt` / `*ViewModel.kt`) ; ViewModel
avec les blocs de section (`UI STATE` / `EVENT` / `PUBLIC API` / `INIT` / `HELPER`) ; le Route est le seul à
appeler `koinViewModel()` et à collecter les events.

- **`pollslist/`** — onglet. Filtres puces **Ouverts (n) / Clos (n)** ; cartes de sondage (icône, question,
  puces commissions ou pastille « Tout le projet », état perso : voté / pas voté / lecture seule / résultat si
  clos, footer `n votes · clôture <label>` + `DeadlineChip`) ; FAB « Nouveau sondage ». (cf. `PollsScreen`/
  `PollCard` du design).
- **`polldetail/`** — bandeau d'éligibilité (clos / réservé / vote enregistré / à voter), options en **radio**
  (mode vote) ou **barres de résultat %** avec surbrillance gagnant (mode résultats), règle d'affichage des
  résultats = `clos || aVoté || nonÉligible`, bouton bas « Valider mon vote » / « Changer mon vote », menu ⋯
  → **Supprimer** (créateur). (cf. `PollDetailScreen`).
- **`createpoll/`** — plein écran (hors bottom-nav) : textarea question, réponses (min 2, ajouter/supprimer),
  toggle **« Tout le projet »** + puces commissions (grisées si public), `DateStrip` date limite, compteur
  « n membre(s) pourra(ont) voter », bouton « Lancer le sondage ». (cf. `CreatePollScreen`).
- **`data/PollDto.kt`** (DTO `@SerialName` snake_case + `toEntity()`), **`data/PollsRepository.kt`**
  (interface + `DefaultPollsRepository(supabase, pollDao)` : lectures = Room ; `create`/`vote`/`delete` =
  RPC Supabase puis upsert/delete dans Room ; `refresh(projectId)` = select + `replaceForProject`). Entrées :
  `projectId = activeProjectProvider.current()?.id`, `memberId = currentMemberProvider.current()?.id`
  (comme `CreateMeetingViewModel`).
- **`navigation/PollDestination.kt`** (`@Serializable sealed interface` : `PollsList`, `PollDetail(pollId)`,
  `CreatePoll`) + **`navigation/PollGraph.kt`** (`fun NavGraphBuilder.pollDestinations(navController)`).
- **`di/PollModule.kt`** (`single<PollsRepository>` + `viewModelOf` × 3).

## 4. Wiring (3 fichiers)

- `app/HomeGraph.kt` : ajouter `pollDestinations(navController)` dans `navigation<RootGraph.Home>`.
- `app/App.kt` : insérer dans `HOME_TABS` (index 2, entre Commissions et Membres)
  `HomeTab("Sondages", PollDestination.PollsList, PollDestination.PollsList::class)`. `CreatePoll` reste
  hors bottom-nav (couvert par `isTopLevelHomeTab()` qui ne matche que les routes de `HOME_TABS`). L'icône
  d'onglet reste la 1ʳᵉ lettre (« S »), cohérent avec l'existant.
- `core/di/AppModule.kt` : ajouter `pollModule` à `featureModules` (skill **`add-koin-module`**).

## 5. Design & i18n

- **Tokens** : réutiliser `MaterialTheme.colorScheme`, `Spacing`, `colisionTypography()`, `colisionShapes`
  (forest, `core/design/`). Respecter le design au pixel (rayons, couleurs `primaryContainer`/`errorContainer`
  pour DeadlineChip urgent, barres de résultat, etc.).
- **Composants** : il n'y a **pas** de librairie partagée — répliquer en privé dans les Screens du feature les
  primitives déjà dupliquées ailleurs (`TopBar`/AppBar, `Chip` filter, `FAB`, `Banner`, `SectionLabel`,
  `CommissionPill`, `DateStrip`) en s'alignant sur `feature/meeting/createmeeting/CreateMeetingScreen.kt` et
  `feature/projecthub/commissions/CommissionsListScreen.kt`.
- **i18n** (obligatoire) : ajouter les blocs `<!-- PollsListScreen -->`, `<!-- PollDetailScreen -->`,
  `<!-- CreatePollScreen -->` dans `commonMain/composeResources/values/strings.xml` **et** le miroir
  `androidMain/res/values/strings.xml`, préfixes `polls_*` / `poll_detail_*` / `create_poll_*`. Pluriels
  (`n votes`, `n j restants`, `n membre(s)`) dans `plurals.xml`. Copie FR reprise du design. Le ViewModel
  émet des clés/events typés, le Screen résout via `stringResource`.

---

## Vérification (end-to-end)

1. `./gradlew :composeApp:assembleDevelopmentDebug` — compile Android (flavor development → Supabase staging).
2. `./gradlew :composeApp:testDebugUnitTest` — tests unitaires (dont mapping DTO→entity, dérivation
   open/closed, éligibilité/compteurs si testés).
3. Appliquer la migration + déployer l'Edge Function sur staging (MCP Supabase), puis `get_advisors`.
4. `run-android` (skill) ou `installDevelopmentDebug` sur device/emulateur, puis via le MCP `mobile` :
   créer un sondage public et un sondage ciblé commission → vérifier onglet Ouverts, vote, changement de vote,
   affichage résultats, lecture seule pour non-éligible, suppression par le créateur, passage en Clos une fois
   `closes_at` dépassé (tester avec une date proche). Vérifier réception du push à la création sur un 2ᵉ device.
5. iOS : `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64` (skill `run-ios`) puis run Xcode pour
   vérifier la parité UI.

## Fichiers clés touchés / créés

- **Créés** : `supabase/migrations/20260723_015_polls.sql`, `supabase/functions/dispatch_poll_push/`,
  `core/database/entity/Poll*Entity.kt` (×4), `core/database/dao/PollDao.kt`,
  `feature/poll/**` (data, di, navigation, pollslist, polldetail, createpoll).
- **Modifiés** : `core/database/ColisionDatabase.kt` (v3→v4 + migration Room), `core/di/CoreModule.kt`,
  `core/di/AppModule.kt`, `app/HomeGraph.kt`, `app/App.kt`,
  `commonMain/.../values/strings.xml` + `plurals.xml`, `androidMain/res/values/strings.xml`.
