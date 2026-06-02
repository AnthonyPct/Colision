---
stepsCompleted: [1, 2, 3, 4]
inputDocuments:
  - docs/product-brief-Colision-2026-05-10.md
  - docs/prd.md
  - docs/architecture.md
  - docs/design/README.md
  - docs/design/chats/chat1.md
  - docs/design/project/tokens.jsx
  - CLAUDE.md
workflowType: 'epics-and-stories'
workflowStatus: 'complete'
project_name: 'Colision'
user_name: 'Anthony Picquet'
date: '2026-05-16'
completed: '2026-05-16'
epic_count: 6
story_count: 35
fr_coverage: '43/43'
---

# Colision — Epic Breakdown

## Overview

Ce document décompose les requirements fonctionnelles du PRD en **epics par feature**, alignés sur la structure du dossier `feature/` de l'architecture et sur les 4 écrans livrés en `docs/design/`. Les NFRs et les patterns d'implémentation ne sont pas re-décrits ici — ils sont des contraintes transverses appliquées à toutes les stories (cf. `docs/architecture.md` sections "Non-Functional Requirements" et "Implementation Patterns").

## Requirements Inventory (par capability area)

### Functional Requirements — 43 FRs

Project Management :
- **FR1** : Un membre peut créer un projet en fournissant un nom de projet.
- **FR2** : Le système génère automatiquement un code de partage unique à 6 caractères à la création d'un projet.
- **FR3** : Un utilisateur peut rejoindre un projet existant en saisissant un code de partage valide.
- **FR4** : Un membre peut consulter le code de partage de son projet pour le copier ou le transmettre.
- **FR5** : Un membre peut quitter un projet ; son identité et ses affectations sont retirées du projet.

Commission Management :
- **FR6** : Un membre peut créer une commission au sein de son projet en fournissant uniquement un nom.
- **FR7** : Un membre peut modifier le nom d'une commission existante.
- **FR8** : Un membre peut supprimer une commission ; les réunions rattachées sont supprimées (avec confirmation).
- **FR9** : Un membre peut consulter la liste des commissions de son projet.

Member Management :
- **FR10** : Un membre peut ajouter un nouveau membre au projet (prénom seul).
- **FR11** : Un utilisateur rejoignant peut sélectionner son identité dans la liste existante ou en créer une.
- **FR12** : Un membre peut confirmer son identité après sélection.
- **FR13** : Un membre peut assigner ou désassigner un membre à des commissions.
- **FR14** : Un membre peut consulter la liste des membres et leurs commissions d'appartenance.

Meeting Scheduling :
- **FR15** : Un membre peut créer une réunion (date, heure, durée, commission(s) concernée(s)).
- **FR16** : Un membre peut associer un titre optionnel à une réunion.
- **FR17** : Un membre peut modifier une réunion existante.
- **FR18** : Un membre peut supprimer une réunion existante (avec confirmation).
- **FR19** : Un membre peut consulter le détail d'une réunion.

Conflict Detection & Resolution :
- **FR20** : Avant validation, le système identifie tous les membres des commissions sélectionnées déjà mobilisés sur le créneau.
- **FR21** : Le système affiche pour chaque conflit : nom du membre, commission concurrente, créneau.
- **FR22** : Le créateur peut choisir : décaler, voir des suggestions, créer malgré tout.
- **FR23** : Le système peut calculer et proposer des créneaux alternatifs libres.
- **FR24** : Lorsqu'une réunion est créée malgré conflits, le système enregistre les conflits et déclenche une notification aux conflictés.
- **FR25** : Un membre conflicté peut choisir une des deux réunions, ou reporter.
- **FR26** : L'arbitrage est enregistré et notifié aux créateurs des deux réunions.
- **FR27** : Un créateur consulte le statut d'arbitrage consolidé de ses réunions.

Notifications :
- **FR28** : Push aux membres d'une commission à la création d'une réunion sans conflit.
- **FR29** : Push aux conflictés à la création malgré l'alerte.
- **FR30** : Push aux créateurs des deux réunions à un arbitrage.
- **FR31** : Push aux membres à la modification/annulation d'une réunion.
- **FR32** : Push tap ouvre l'app sur l'écran cible via deep-link.
- **FR33** : Un membre peut accepter ou refuser l'autorisation de notifications.

Calendar & Discovery :
- **FR34** : Vue calendrier personnelle agrégée en vue semaine.
- **FR35** : Vue calendrier personnelle agrégée en vue mois.
- **FR36** : Liste des réunions à venir d'une commission.

Offline & Sync :
- **FR37** : Cache local du dernier état connu, consultation intégrale sans réseau.
- **FR38** : Indication visuelle du mode hors-ligne.
- **FR39** : Actions d'écriture désactivées hors-ligne avec message explicite.
- **FR40** : Synchronisation automatique au retour de connexion.
- **FR41** : Propagation des changements via push notifications (réunions) + pull-on-foreground (autres). *(Reformulé suite à la décision archi "Pas de Realtime".)*

Privacy & Data Management :
- **FR42** : Un membre peut supprimer son identité d'un projet.
- **FR43** : Un membre peut supprimer un projet qu'il a créé (cascading delete).

### Additional Requirements (Architecture)

Les contraintes techniques majeures, à respecter dans toutes les stories :

- **Stack figée** : KMP 2.3.21 + Compose Multiplatform 1.10.3 + Material3 1.10.0-alpha05 + Supabase + Room KMP + Koin + Ktor (cf. `architecture.md` Décision 1-10).
- **Pattern MVI strict** : ViewModel + Route + Screen + Repository pour chaque feature (cf. `CLAUDE.md`).
- **Folder structure imposée** : `feature/<name>/{data,domain,ui,di}/` (cf. `architecture.md`).
- **Design contract non-négociable** : tous les écrans implémentent le design system `docs/design/project/tokens.jsx` mappé en Compose dans `core/design/` (cf. `CLAUDE.md`).
- **Backend Supabase** : schema Postgres + RLS + Edge Functions de dispatch push.
- **CI dès J1** : GH Actions (build + tests + lint) sur chaque push.
- **Localisation française uniquement** au MVP, toutes chaînes via `composeResources`.

### Action Items issus de l'archi à intégrer comme stories

- **Foundation (Epic 1)** : Bootstrap dépendances, folder structure, design tokens, flavors dev/prod, Koin setup, CI.
- **Foundation (Epic 1)** : Schema Postgres + RLS policies + Postgres functions (`create_project`, `try_resolve_code`, `detect_conflicts`).
- **Foundation (Epic 1)** : Supabase Anonymous Auth (session manager par défaut de supabase-kt) + stratégie refresh JWT proactif au foreground.
- **Push pipeline (intégré aux Epics fonctionnels)** : Edge Functions FCM/APNs + retry exponentiel 3x.
- **Launch (Epic 6)** : Rédaction Privacy Policy française + hosting (GitHub Pages).

## Epic List

### Epic 1 — Foundation (le projet est prêt à recevoir des features)

**Epic Goal:**
Au terme de cet epic, le projet `composeApp` est entièrement câblé sur les décisions architecturales : dépendances ajoutées et versionnées, structure de dossiers en place, design system mappé en Compose, flavors `development` et `production` configurés, Koin opérationnel, wrappers `core/common/` en place (au minimum en NoopImpl), schéma Postgres + RLS déployés sur Supabase, authentification anonyme fonctionnelle, CI verte sur GH Actions. Aucune feature utilisateur livrée — mais toute story suivante peut être attaquée sans détour technique.

**FRs covered:** —
**Action items archi intégrés:**
- Bootstrap libs + folder structure + tokens design + flavors + Koin + CI
- Schema Postgres + RLS policies + Postgres functions (`create_project`, `try_resolve_code`, `detect_conflicts`)
- Supabase Anonymous Auth (session manager par défaut de supabase-kt) + stratégie refresh JWT proactif au foreground (Action item archi #4)
- Edge Functions skeleton (déploiement squelette, dispatchers réels en Epic 4 / 5)
- Reformulation de FR41 dans le PRD (Action item archi #1)

**Anchors architecture:** "Starter Template & Foundation" + "Initial Implementation Story" + Décisions §1/§2/§10 + section "Project Structure & Boundaries".

**Dependencies:** aucune (premier epic).
**Unlocks:** tous les epics suivants.

---

### Epic 2 — Onboarding : créer ou rejoindre un projet, devenir membre

**Epic Goal:**
Au terme de cet epic, Antoine peut **créer un projet** ("Conseil municipal de Saint-Machin"), recevoir un code à 6 caractères, ajouter les commissions et les membres pré-remplis. Sophie peut **rejoindre via le code** d'Antoine, sélectionner son identité dans la liste pré-remplie (ou s'ajouter), cocher ses commissions d'appartenance, et atterrir sur l'app prête à l'usage — en moins de 60 secondes du téléchargement à la première utilisation. N'importe quel membre peut ensuite gérer les commissions, les membres, et quitter le projet (FR42) ou le supprimer (FR43, créateur uniquement).

**FRs covered:** FR1, FR2, FR3, FR4, FR5, FR6, FR7, FR8, FR9, FR10, FR11, FR12, FR13, FR14, FR33, FR42, FR43

**Anchors architecture:** Postgres functions `create_project`, `try_resolve_code` ; tables `project`, `commission`, `member`, `member_commission` ; design `screens-onboarding.jsx` ; journeys J1 (Antoine) + J2 (Sophie). Permission notification (FR33) demandée en fin d'onboarding avec écran explicatif.

**Dependencies:** Epic 1 (Foundation).
**Unlocks:** Epic 3 (Agenda), Epic 4 (Meeting Scheduling).

---

### Epic 3 — Agenda : voir et consulter son planning

**Epic Goal:**
Au terme de cet epic, Sophie peut consulter son **agenda personnel agrégé** (toutes ses commissions) en vue semaine (FR34) et vue mois (FR35), consulter la **liste des réunions de n'importe quelle commission** (FR36), et ouvrir le détail d'une réunion (FR19) avec ses commissions, son créateur, les conflits associés et les arbitrages déjà rendus. L'app fonctionne **en mode hors-ligne en lecture** : Sophie peut consulter son agenda dans le métro, avec un indicateur visuel discret du mode dégradé (FR37-FR41). Au retour de connexion, la sync est automatique. Sans encore de meetings créés (Epic 4), l'agenda affiche un **empty state** sympathique.

**FRs covered:** FR19, FR34, FR35, FR36, FR37, FR38, FR39, FR40, FR41

**Anchors architecture:** Décision archi 4 (sync : pull-on-foreground + écritures optimistes) ; Room KMP DAOs `MeetingDao`, `CommissionDao`, `MemberDao` ; design `screens-agenda.jsx` ; journey J2 ongoing ; cross-cutting concern #3.

**Dependencies:** Epic 1 (Foundation), Epic 2 (Onboarding).
**Unlocks:** Epic 4 (Meeting Scheduling), Epic 5 (Arbitration).

---

### Epic 4 — Meeting Scheduling : poser un créneau sans collision

**Epic Goal:**
Au terme de cet epic, Marc peut **créer une réunion** (date, heure, durée, une ou plusieurs commissions, titre optionnel). Au moment de valider, l'app exécute la **détection de conflit cross-commissions en temps réel** : si des membres des commissions sélectionnées sont déjà mobilisés ailleurs sur le créneau, l'app les affiche par nom avec leur commission concurrente et leur créneau. Marc peut alors choisir de **décaler**, de **voir des suggestions de créneaux libres**, ou de **créer la réunion malgré les conflits** — auquel cas le système enregistre les conflits et déclenche les **notifications push curatives**. Marc peut également **modifier ou supprimer** une réunion qu'il a créée, avec push aux membres concernés. À la création sans conflit, push standard envoyé à la commission. Le créateur peut consulter le **statut d'arbitrage consolidé** de chacune de ses réunions conflictuelles.

**FRs covered:** FR15, FR16, FR17, FR18, FR20, FR21, FR22, FR23, FR27, FR28, FR29, FR31

**Anchors architecture:** Décision archi 3 (conflict detection hybride) ; Postgres function `detect_conflicts` + index ; Edge Functions `dispatch_meeting_push` + `dispatch_conflict_push` avec retry 3x backoff (Action item archi #3) ; design `screens-meeting.jsx` ; journey J3. Tests miroirs SQL ↔ Kotlin.

**Dependencies:** Epic 1, Epic 2, Epic 3.
**Unlocks:** Epic 5 (Arbitration).

---

### Epic 5 — Arbitration : décider et notifier après un conflit

**Epic Goal:**
Au terme de cet epic, Sophie qui reçoit une **notification push de conflit** (envoyée par Epic 4) peut taper la notif et atterrir directement sur l'**écran d'arbitrage** via deep-link (FR32). L'écran affiche les deux réunions en conflit côte à côte. Sophie peut choisir d'**aller à l'une des deux** ou de **reporter sa décision** (FR25). L'arbitrage est enregistré côté serveur et **notifie automatiquement les créateurs des deux réunions** en conflit via push (FR26, FR30). Sur leur dashboard (Epic 4 FR27), les créateurs voient apparaître les arbitrages au fur et à mesure.

**FRs covered:** FR25, FR26, FR30, FR32
*(NB: FR24 — enregistrement des conflits — est dans Epic 4 ; FR27 — vue consolidée côté créateur — est dans Epic 4.)*

**Anchors architecture:** Table `arbitration` + RLS policies ; Edge Function `dispatch_arbitration_push` ; design `screens-arbitrage.jsx` ; journey J4 ; cross-cutting concern #4 (deep-linking).

**Dependencies:** Epic 1, Epic 2, Epic 3, Epic 4.
**Unlocks:** Epic 6 (Launch).

---

### Epic 6 — Launch : MVP soumis aux stores

**Epic Goal:**
Au terme de cet epic, Colision est **publié sur Google Play et l'App Store** (Internal Testing / TestFlight d'abord pour le conseil municipal de Sophie, puis production). L'app a sa **politique de confidentialité française hébergée et liée**, ses **assets store** (icons, screenshots, descriptions), sa **déclaration de privacy nutrition** (iOS) et **Data safety form** (Android) renseignée selon le PRD. Les flavors `production` pointent sur le projet Supabase de prod (avec son propre schéma migré et seeded). La version `1.0.0 (build 1)` passe le review. Sophie installe l'app via le lien de partage TestFlight ou le code Play Internal — sans aide.

**FRs covered:** —
**NFRs adressés:** NFR-S6 (privacy policy), NFR-S5 (hosting EU production confirmé), conformité stores.
**Action items archi intégrés:** Story Privacy Policy française + hosting (Action item archi #2).

**Anchors architecture:** Section "Store Compliance" ; section "Environments" ; section "Build & Distribution Structure".

**Dependencies:** Epic 1, 2, 3, 4, 5.
**Unlocks:** lancement V1 et début de la roadmap V1.1.

## FR Coverage Map

Trace de chaque FR vers son epic, pour garantir qu'aucun ne tombe entre les mailles :

| FR | Epic | Bref |
|---|---|---|
| FR1 | Epic 2 | Créer un projet (nom) |
| FR2 | Epic 2 | Génération automatique du code 6 chars |
| FR3 | Epic 2 | Rejoindre via code |
| FR4 | Epic 2 | Consulter le code de partage |
| FR5 | Epic 2 | Quitter un projet |
| FR6 | Epic 2 | Créer une commission |
| FR7 | Epic 2 | Modifier le nom d'une commission |
| FR8 | Epic 2 | Supprimer une commission (cascade meetings) |
| FR9 | Epic 2 | Consulter la liste des commissions |
| FR10 | Epic 2 | Ajouter un membre (prénom) |
| FR11 | Epic 2 | Sélectionner son identité ou en créer une |
| FR12 | Epic 2 | Confirmer son identité |
| FR13 | Epic 2 | Assigner/désassigner membre ↔ commissions |
| FR14 | Epic 2 | Consulter la liste des membres + commissions |
| FR15 | Epic 4 | Créer une réunion |
| FR16 | Epic 4 | Titre optionnel sur une réunion |
| FR17 | Epic 4 | Modifier une réunion |
| FR18 | Epic 4 | Supprimer une réunion |
| FR19 | Epic 3 | Consulter le détail d'une réunion |
| FR20 | Epic 4 | Détection de conflit en amont (Postgres function) |
| FR21 | Epic 4 | Affichage des conflits détectés |
| FR22 | Epic 4 | 3 choix face aux conflits (décaler / suggérer / créer) |
| FR23 | Epic 4 | Suggestion de créneaux alternatifs libres |
| FR24 | Epic 4 | Enregistrement des conflits + déclenchement notif |
| FR25 | Epic 5 | Choix d'arbitrage par le conflicté |
| FR26 | Epic 5 | Enregistrement arbitrage + notif aux créateurs |
| FR27 | Epic 4 | Vue consolidée des arbitrages côté créateur |
| FR28 | Epic 4 | Push à la création sans conflit |
| FR29 | Epic 4 | Push aux conflictés à la création avec conflit |
| FR30 | Epic 5 | Push aux créateurs lors d'un arbitrage |
| FR31 | Epic 4 | Push aux membres à la modif/annulation |
| FR32 | Epic 5 | Deep-link push → écran d'arbitrage *(pattern défini en Epic 1, usage en Epic 5)* |
| FR33 | Epic 2 | Demande de permission notification en fin d'onboarding |
| FR34 | Epic 3 | Vue calendrier semaine |
| FR35 | Epic 3 | Vue calendrier mois |
| FR36 | Epic 3 | Liste réunions par commission |
| FR37 | Epic 3 | Cache local + consultation hors-ligne |
| FR38 | Epic 3 | Indicateur visuel mode hors-ligne |
| FR39 | Epic 3 | Écritures désactivées hors-ligne |
| FR40 | Epic 3 | Sync auto au retour de connexion |
| FR41 | Epic 3 | Propagation via push + pull-on-foreground (à reformuler dans PRD) |
| FR42 | Epic 2 | Supprimer son identité d'un projet |
| FR43 | Epic 2 | Supprimer un projet (créateur) |

**43 / 43 FRs mappés ✅** — aucun orphelin.

## Dépendances inter-Epics

```
Epic 1 (Foundation)
   │
   ▼
Epic 2 (Onboarding) ──────┐
                            │
                            ▼
                     Epic 3 (Agenda) ──┐
                                          │
                                          ▼
                                   Epic 4 (Meeting Scheduling)
                                          │
                                          ▼
                                   Epic 5 (Arbitration)
                                          │
                                          ▼
                                   Epic 6 (Launch)
```

---

## Epic 1: Foundation

**Epic Goal:** Au terme de cet epic, le projet `composeApp` est entièrement câblé sur les décisions architecturales : dépendances ajoutées et versionnées, structure de dossiers en place, design system mappé en Compose, flavors `development` et `production` configurés, Koin opérationnel, wrappers `core/common/` en place, schéma Postgres + RLS déployés sur Supabase, authentification anonyme fonctionnelle, observabilité branchée, CI verte sur GH Actions. Aucune feature utilisateur livrée — mais toute story suivante peut être attaquée sans détour technique.

### Story 1.1: Bootstrap dépendances, KSP et flavors

As a Colision developer,
I want to add all required dependencies to `libs.versions.toml` and configure the build system with KSP, the two product flavors (`development` and `production`), and a flavor-aware `BuildConfig`,
So that all subsequent stories can pull in the libraries they need without build-system friction.

**Acceptance Criteria:**

**Given** the current scaffolding has only Compose Multiplatform basics, **When** I run `./gradlew :composeApp:dependencies`, **Then** the resolved tree includes : Koin (core, compose, viewmodel, android), Ktor (client core + content-negotiation + json + logging + okhttp + darwin), supabase-kt (postgrest, functions, auth, realtime), Room (runtime + compiler + sqlite-bundled), kotlinx-serialization-json, kotlinx-datetime, kotlinx-coroutines-core, androidx.navigation:navigation-compose (KMP version), sentry-kotlin-multiplatform, posthog-android, Firebase BoM + firebase-messaging-ktx (Android only), Turbine + kotlin-test for tests, **And** all versions are declared in `gradle/libs.versions.toml`.

**Given** the `composeApp/build.gradle.kts`, **When** I run `./gradlew :composeApp:tasks --all | grep -E "Development|Production"`, **Then** I see Gradle tasks for both flavors : `assembleDevelopmentDebug`, `assembleProductionDebug`, `testDevelopmentDebugUnitTest`, etc.

**Given** the new flavor source sets, **When** I look at the project structure, **Then** `composeApp/src/development/kotlin/com/anthooop/colision/config/BuildConfig.kt` and `composeApp/src/production/kotlin/com/anthooop/colision/config/BuildConfig.kt` exist, each defining `actual class BuildConfig` with placeholder values for `supabaseUrl`, `supabaseAnonKey`, `sentryDsn`, `posthogApiKey`, `isDevelopmentFlavor`.

**Given** the build, **When** I run `./gradlew :composeApp:assembleDevelopmentDebug` and `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`, **Then** both succeed without errors, **And** the resulting Android APK contains the suffix `.dev` on the applicationId.

### Story 1.2: Structure de dossiers + app shell + Koin minimal

As a Colision developer,
I want to create the by-feature folder structure declared in CLAUDE.md and wire up a minimal Koin setup that boots on both platforms,
So that every subsequent feature story has a place to land.

**Acceptance Criteria:**

**Given** the empty starter project, **When** I inspect `composeApp/src/commonMain/kotlin/com/anthooop/colision/`, **Then** I see the directories `app/`, `core/{common,design,network,database,di}/`, and `feature/` (initially empty), **And** each `core/*` directory contains at least a placeholder so it's not lost.

**Given** the new structure, **When** I open `app/App.kt`, **Then** the file declares a root `@Composable fun App()` that wraps the content in a placeholder `ColisionTheme` (real one in story 1.3) and shows a single `Text("Colision")`.

**Given** Koin wiring, **When** the app starts on Android, **Then** `MainActivity` calls `startKoin { modules(appModule, androidPlatformModule) }` before `setContent { App() }`, **And** Koin starts without exception with placeholder modules.

**Given** Koin wiring iOS, **When** the app starts on iOS, **Then** `MainViewController` triggers Koin initialization with `appModule + iosPlatformModule`.

**Given** the smoke test, **When** I run the app on an Android emulator or an iOS simulator, **Then** I see "Colision" displayed without crash.

### Story 1.3: Design system — tokens.jsx → Compose Theme

As a Colision developer,
I want to translate `docs/design/project/tokens.jsx` into Compose `ColorScheme`, `Typography`, `Shapes` and `Spacing` in `core/design/`,
So that every UI story can consume `MaterialTheme.*` and produce pixel-perfect screens matching the design.

**Acceptance Criteria:**

**Given** `docs/design/project/tokens.jsx`, **When** I inspect `core/design/ColorScheme.kt`, **Then** I find `lightForestColorScheme` and `darkForestColorScheme` correctly mapping the M3 color names (primary, primaryContainer, surface, etc.) including `error` mapped to the coral token.

**Given** the typography tokens, **When** I inspect `core/design/Typography.kt`, **Then** I find a Material3 `Typography` instance using `DM Sans` (loaded from `commonMain/composeResources/font/`), with each scale level matching tokens.jsx.

**Given** the shape and spacing tokens, **When** I inspect `core/design/Shapes.kt` and `core/design/Spacing.kt`, **Then** the radius scale matches (small=10dp, medium=14dp, large=18dp, extraLarge=24dp), and `Spacing` exposes `SP1=4.dp ... SP16=64.dp`.

**Given** the new design system, **When** I inspect `app/ColisionTheme.kt`, **Then** there's a `@Composable fun ColisionTheme(darkTheme: Boolean = isSystemInDarkTheme(), content)` applying the right `ColorScheme`, `Typography`, `Shapes`, **And** `App.kt` wraps content in `ColisionTheme { }`.

**Given** the smoke test, **When** I run the app on Android and iOS, **Then** the placeholder Text renders in DM Sans on background `#FAF7F2`, and switching the OS to dark mode flips the theme correctly.

### Story 1.4: Wrappers core/common (interfaces + NoopImpl)

As a Colision developer,
I want cross-cutting concerns (Logger, CrashReporter, Analytics, NotificationPermissionManager, DispatcherProvider, AppError) modeled as Kotlin interfaces in `core/common/` with `Noop` or basic implementations injected via Koin,
So that every feature story consumes these abstractions cleanly, and real impls (Sentry) can be swapped in story 1.9.

**Acceptance Criteria:**

**Given** `core/common/`, **When** I list the files, **Then** I find : `Logger.kt`, `CrashReporter.kt` + `NoopCrashReporter.kt`, `Analytics.kt` + `NoopAnalytics.kt`, `NotificationPermissionManager.kt` (expect), `DispatcherProvider.kt`, `AppError.kt` (sealed class), `AppErrorThrowable.kt`, `ResultHelpers.kt` (`appErrorResult`, `foldAppError`).

**Given** Koin, **When** the app starts, **Then** `core/di/CoreModule.kt` registers `single<Logger>`, `single<CrashReporter> { NoopCrashReporter() }`, `single<Analytics> { NoopAnalytics() }`, `single<DispatcherProvider> { DefaultDispatcherProvider() }`. Platform modules register `NotificationPermissionManager` placeholders.

**Given** Logger calls, **When** a debug log is emitted on Android `development`, **Then** it appears in Logcat; on iOS `development`, in `os_log`. On `production`, `debug` and `info` are no-op.

**Given** AppError, **When** I write the unit test `appErrorResult(AppError.NetworkUnavailable).foldAppError(...)`, **Then** the `when` block on sealed `AppError` is exhaustive at compile time.

### Story 1.5: CI GitHub Actions (build Android + build iOS + lint)

As a Colision developer,
I want three GitHub Actions workflows running on every push and pull request,
So that no regression slips into `main` without me knowing, even working solo.

**Acceptance Criteria:**

**Given** `.github/workflows/`, **When** I list files, **Then** I find `build-android.yml`, `build-ios.yml`, `lint.yml`, **And** `config/detekt/detekt.yml` exists with a starter config.

**Given** `build-android.yml` running on `ubuntu-latest`, **When** it triggers, **Then** it sets up Java 17 + Gradle, runs `./gradlew :composeApp:assembleDevelopmentDebug :composeApp:testDevelopmentDebugUnitTest`, and succeeds on the current empty app.

**Given** `build-ios.yml` running on `macos-14`, **When** it triggers, **Then** it runs `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64 :composeApp:iosSimulatorArm64Test` and succeeds.

**Given** `lint.yml` running on `ubuntu-latest`, **When** it triggers, **Then** it runs `./gradlew detekt ktlintCheck` and succeeds.

**Given** branch protection on `main`, **When** a PR is opened, **Then** all three workflows must pass before merge is enabled.

### Story 1.6: Supabase project + schéma Postgres + RLS

As a Colision developer,
I want the Supabase `development` project provisioned in EU Frankfurt with the complete Colision schema (8 tables) and RLS policies applied via versioned migrations,
So that the data foundation is ready for Epic 2-5, with strict isolation between projects enforced at the database level.

**Acceptance Criteria:**

**Given** the Supabase CLI, **When** I run `supabase init` and configure `supabase/config.toml`, **Then** the project links to a new Supabase project in Frankfurt EU region (free tier), **And** `supabase/migrations/` is versioned in git.

**Given** the migrations, **When** I list `supabase/migrations/`, **Then** I find : `20260516_000_init_auth.sql` (trigger `on_auth_user_created` + table `device`), `20260516_001_init_schema.sql` (tables `project`, `commission`, `member`, `member_commission`, `meeting`, `meeting_commission`, `arbitration` + FKs + CHECK + `updated_at` triggers), `20260516_002_indexes.sql`, `20260516_003_rls_policies.sql`.

**Given** the dev project, **When** I run `supabase db push`, **Then** all migrations apply cleanly, **And** I can verify in the dashboard that 8 tables exist with expected schema.

**Given** the RLS policies, **When** I authenticate two anonymous devices A and B, device A inserts a project P, and device B tries `SELECT * FROM project`, **Then** device B sees an empty result, **And** if A then makes B a member of P, B sees P on next query.

**Given** the dev URL and anon key, **When** I update the `development` flavor's `BuildConfig.kt`, **Then** `supabaseUrl` and `supabaseAnonKey` hold real values pointing to the dev project.

### Story 1.7: Postgres functions (create_project, try_resolve_code, detect_conflicts)

As a Colision developer,
I want the three Postgres functions central to Colision business logic implemented as `security definer` functions accessible via `supabase.postgrest.rpc(...)`,
So that Epic 2 (Onboarding) and Epic 4 (Meeting Scheduling) can consume them directly.

**Acceptance Criteria:**

**Given** the migrations, **When** I list `supabase/migrations/`, **Then** I find `20260516_004_functions.sql` containing : `generate_share_code()`, `create_project(p_name text)`, `try_resolve_code(p_code text)` raising `P0002` on miss, `detect_conflicts(p_project_id, p_commission_ids, p_start, p_end)` returning `setof` of conflicted members.

**Given** a script calling `create_project('Test')` 1000 times, **When** it runs, **Then** every call returns a unique 6-char code from the unambiguous alphabet, with no collision exception.

**Given** a seeded project with two commissions A and B sharing one member, and a meeting in B at 20h-21h30, **When** I call `detect_conflicts(p_project, ARRAY[A_id], '2026-05-21 20:00+02', '2026-05-21 21:30+02')`, **Then** the result contains the shared member's row with the B meeting details, **And** `EXPLAIN ANALYZE` shows execution under 200 ms on 1 000 seeded meetings.

**Given** `try_resolve_code('ZZZZZZ')`, **When** the code doesn't exist, **Then** the function raises `P0002` (mapped client-side to `AppError.ProjectCodeInvalid`).

### Story 1.8: Anonymous Auth + JWT refresh proactif

As a Colision developer,
I want Supabase Anonymous Auth plugged in : at first launch `signInAnonymously()` is called and the session is refreshed proactively on every foreground,
So that every request has a valid `auth.uid()` and Sophie never sees a "session expired" screen.

**Acceptance Criteria:**

**Given** `core/network/SupabaseClientProvider.kt`, **When** I inspect the file, **Then** the Supabase client is built with `Auth` + `Postgrest` + `Functions` modules. Session storage uses supabase-kt's default `SessionManager` — no custom secure-storage wrapper is shipped at MVP (the SecureStorage abstraction was dropped 2026-05-18; if cross-cold-start session persistence is needed later, plug a custom SessionManager at that point).

**Given** `core/common/AnonymousAuthManager.kt`, **When** I inspect its API, **Then** it exposes `suspend fun ensureSession(): Result<Unit>` and `suspend fun refreshIfNeeded(): Result<Unit>`.

**Given** cold start with no stored session, **When** `ensureSession()` runs, **Then** `signInAnonymously()` is called and a row appears in `auth.users` + `device` (via story 1.6 trigger).

**Given** the app returns to foreground, **When** `refreshIfNeeded()` runs, **Then** the current session is refreshed unconditionally (architecture gap #4 option b — refresh on every foreground is cheaper to maintain than tracking refresh-token expiry, costs one auth round-trip).

**Given** the refresh fails (refresh token expired or invalid), **Then** old session is wiped and a new `signInAnonymously()` is called automatically (member rows in previous projects are not auto-recovered at MVP — V2 concern).

### Story 1.9: Sentry integration (real impls)

As a Colision developer,
I want `NoopCrashReporter` swapped for `SentryCrashReporter` and `NoopAnalytics` for `SentryAnalytics`,
So that crashes AND product events are captured from day 1 — both routed through Sentry per the revised decision #9 (PostHog dropped on 2026-05-18).

**Acceptance Criteria:**

**Given** BuildKonfig configured in `composeApp/build.gradle.kts`, **When** I inspect the generated `BuildKonfig` object, **Then** it exposes `SENTRY_DSN` (Sentry Cloud EU, region DE), differentiating dev/prod via the `IS_DEVELOPMENT_FLAVOR` boolean (auto-switched by the BuildKonfig flavor `dev` / `prod`).

**Given** Android `ColisionApplication.onCreate`, **When** the app starts, **Then** Sentry is initialized with the correct DSN + `environment = "development" | "production"` driven by `BuildKonfig.IS_DEVELOPMENT_FLAVOR`. On iOS the same init runs via the `io.sentry.kotlin.multiplatform.gradle` plugin's bootstrap (Sentry Cocoa resolved via SPM in `iosApp/iosApp.xcodeproj`, pinned to 8.58.2).

**Given** the Koin swap in `core/di/CoreModule.kt`, **When** the bindings resolve, **Then** `CrashReporter` is bound to `SentryCrashReporter` (commonMain) and `Analytics` to `SentryAnalytics` (commonMain — events go through `Sentry.captureMessage("event:$name")` at INFO level).

**Given** a deliberate exception thrown on dev flavor, **When** it propagates, **Then** within ~1 minute the event appears in the Sentry project (org `anthooop`) under environment `development`.

**Given** an `Analytics.track("app_opened")` call, **When** it executes, **Then** an Issue titled `event:app_opened` (level INFO) appears in Sentry with the event properties attached as tags.

---

## Epic 2: Onboarding

**Epic Goal:** Au terme de cet epic, Antoine peut créer un projet, recevoir un code à 6 caractères, ajouter les commissions et les membres pré-remplis. Sophie peut rejoindre via le code, sélectionner son identité, cocher ses commissions, et atterrir sur l'app prête à l'usage — en moins de 60 secondes. N'importe quel membre peut gérer les commissions, les membres, quitter le projet, et le créateur peut le supprimer.

### Story 2.1: Écran d'accueil + entry routing (Créer / Rejoindre)

As Antoine or Sophie (any first-time user),
I want a welcome screen with two clear actions (Créer un projet / Rejoindre un projet),
So that I immediately understand my two options and pick the right path without confusion.

**Acceptance Criteria:**

**Given** the app launches and no project is yet associated with this device, **When** the splash completes, **Then** I land on a Welcome screen matching `docs/design/project/screens-onboarding.jsx`, with two prominent buttons : "Créer un projet" and "Rejoindre un projet".

**Given** the Welcome screen, **When** I tap "Créer un projet", **Then** I navigate to the project creation flow (story 2.2).

**Given** the Welcome screen, **When** I tap "Rejoindre un projet", **Then** I navigate to the code entry flow (story 2.5).

**Given** an already-joined project on this device, **When** I cold-start the app, **Then** I skip the Welcome screen and land directly on the Agenda (Epic 3).

### Story 2.2: Flow "Créer un projet"

As Antoine (the initiator),
I want to enter a project name and receive a generated 6-character share code,
So that I have a project shell ready to which I can add commissions and members.

**Acceptance Criteria:**

**Given** I tapped "Créer un projet", **When** the create screen displays, **Then** I see an input field for the project name with placeholder "Ex. Conseil municipal de Saint-Machin" and a "Créer" button.

**Given** I enter "Conseil municipal de Saint-Machin" and tap "Créer", **When** the request completes, **Then** the app calls Postgres function `create_project(p_name)` via `supabase.postgrest.rpc`, receives the new project row, and stores it in Room. The current device's `auth.uid()` is associated with a `member` row in the new project.

**Given** the project is created, **When** the next screen displays, **Then** I see the 6-character share code prominently with a "Copier" button and explanatory copy ("Partage ce code aux autres membres de ton conseil").

**Given** I'm on the code display screen, **When** I tap "Copier", **Then** the code is placed on the system clipboard and I see a brief confirmation snackbar.

**Given** a network error during creation, **When** the call fails, **Then** I see a clear French error message ("Impossible de créer le projet — vérifie ta connexion") with a retry button, and the AppError captured (`AppError.NetworkUnavailable` or `AppError.Unknown`).

### Story 2.3: CRUD commissions

As any member of a project,
I want to create, rename and delete commissions in my project,
So that the project's group structure can be set up and adjusted over time.

**Acceptance Criteria:**

**Given** I'm on the project settings or commission list screen, **When** I tap "+ Commission", **Then** an input dialog opens. After typing a name and confirming, the commission is inserted via supabase-kt, propagated to Room, and appears in the list.

**Given** an existing commission, **When** I tap "Modifier", **Then** I can edit the name and save; the update is propagated.

**Given** an existing commission with meetings attached, **When** I tap "Supprimer" and confirm the warning dialog ("Cette commission a 3 réunions qui seront supprimées."), **Then** the commission and its meetings are removed (cascade), the lists update.

**Given** offline mode, **When** I attempt any CRUD action on commissions, **Then** the action is disabled with the message "Connexion requise pour cette action" (FR39).

### Story 2.4: CRUD membres + assignation aux commissions

As any member of a project,
I want to add new members (first name only), see the list of all members and their commission assignments, and update assignments (mine or others'),
So that the project's membership roster reflects reality.

**Acceptance Criteria:**

**Given** the project, **When** I tap "+ Membre", **Then** a dialog asks for a first name (optionally last name). On confirm, a `member` row is inserted with `device_id = null` (will be claimed when that person joins via code in story 2.5).

**Given** the member list, **When** I tap on a member, **Then** I see a checkbox list of all commissions in the project, with the member's current assignments pre-cocked. Tapping a checkbox toggles the `member_commission` link.

**Given** the member list screen, **When** the data loads, **Then** I see all members with their associated commissions displayed compactly (e.g., "Sophie Picquet — Jeunesse, École").

**Given** offline mode, **When** I attempt to add a member or change an assignment, **Then** the action is disabled with the message "Connexion requise pour cette action".

### Story 2.5: Flow "Rejoindre via code"

As Sophie (an invited member),
I want to enter the 6-character code I received, see the project name to confirm I'm in the right place, and then move to identity selection,
So that I can join my conseil without typing my email, password, or anything technical.

**Acceptance Criteria:**

**Given** I tapped "Rejoindre un projet", **When** the code entry screen displays, **Then** I see a single input optimized for 6-character entry (auto-uppercase, easy mobile keyboard) and a "Valider" button.

**Given** I enter `KQ7H2P` and tap "Valider", **When** the request completes, **Then** the app calls Postgres function `try_resolve_code(p_code)` via supabase-kt. On success, the project row is received; on `P0002` (CODE_NOT_FOUND), `AppError.ProjectCodeInvalid` is surfaced with a clear French message ("Code introuvable. Vérifie auprès de la personne qui te l'a partagé.").

**Given** the code resolves, **When** the next screen displays, **Then** I see "Conseil municipal de Saint-Machin" with a confirmation prompt ("C'est bien ton conseil ?") and a "Oui, continuer" button.

**Given** I confirm the project, **When** I tap "Continuer", **Then** I navigate to the identity selection screen (story 2.6).

### Story 2.6: Sélection / création de son identité + confirmation

As Sophie joining a project,
I want to see the list of members already in the project and either pick my name or add a new identity for myself,
So that I'm associated with the right `member` row that has my pre-checked commission assignments.

**Acceptance Criteria:**

**Given** I confirmed the project (story 2.5), **When** the identity screen loads, **Then** I see the list of all `member` rows in the project, sorted alphabetically, with a search input. At the bottom, a "+ Ajouter mon nom" option exists for cases where I'm not pre-listed.

**Given** I tap on my name (e.g., "Sophie Picquet"), **When** the confirmation dialog displays, **Then** I see "Tu es bien Sophie Picquet ?" with "Oui" / "Non" buttons.

**Given** I confirm, **When** the request completes, **Then** the `member` row's `device_id` is updated to my current `auth.uid()`'s device, persisting the association. I am then routed to story 2.7 (commissions + notification permission).

**Given** my name isn't in the list, **When** I tap "+ Ajouter mon nom", **Then** I can enter a first name (+ optional last name) and confirm; a new `member` row is created with my `device_id` immediately associated.

### Story 2.7: Compléter ses commissions + demande permission notification

As Sophie completing onboarding,
I want to verify or adjust the commissions I belong to and grant notification permission with a clear explanation,
So that the app can immediately show me a relevant agenda and notify me of conflicts.

**Acceptance Criteria:**

**Given** my identity is confirmed (story 2.6), **When** the commissions screen loads, **Then** I see a checkbox list of all commissions in the project, with my pre-assigned ones already checked. I can tick/untick freely.

**Given** I confirm my commissions, **When** I tap "Continuer", **Then** the `member_commission` table is updated to reflect my choices, and a native-feeling explainer screen presents the notification permission prompt ("Colision te prévient quand une réunion entre en conflit avec ton agenda — accepte pour ne plus rater une coordination.") with an "Activer les notifications" button.

**Given** the permission screen, **When** I tap "Activer", **Then** the system permission dialog (`POST_NOTIFICATIONS` on Android 13+, `requestAuthorization` on iOS) is triggered via `NotificationPermissionManager`.

**Given** I accept or refuse the permission, **When** the prompt closes, **Then** the device's FCM token (Android) or APNs token (iOS) is registered and PUT to Supabase on the `device` row (only if permission granted), **And** I am routed to the Agenda (Epic 3).

**Given** I refused the permission, **When** I land on the Agenda, **Then** the app remains fully functional but a subtle "Active les notifications dans les réglages" banner appears at the top of the Agenda.

### Story 2.8: Quitter un projet

As any member,
I want to leave a project I no longer need to follow,
So that my identity and my commission assignments are removed from that project.

**Acceptance Criteria:**

**Given** I'm on the project settings screen, **When** I tap "Quitter ce projet", **Then** a confirmation dialog appears ("Tu vas quitter Conseil municipal de Saint-Machin. Ton historique d'arbitrage sera effacé. Continuer ?").

**Given** I confirm, **When** the request completes, **Then** my `member` row's `device_id` is set to NULL (so the row remains for historical attribution but no longer associated with this device), my `member_commission` links are removed, my `arbitration` rows are deleted (cascade), and I am routed back to the Welcome screen.

**Given** offline mode, **When** I attempt to leave, **Then** the action is disabled with the "Connexion requise" message.

### Story 2.9: Supprimer un projet

As the creator of a project (or any member, per flat model),
I want to delete the project entirely,
So that all associated data (commissions, members, meetings, arbitrations) is erased from the server.

**Acceptance Criteria:**

**Given** I'm on the project settings screen, **When** I tap "Supprimer le projet", **Then** a strong confirmation dialog appears ("Cette action supprime DÉFINITIVEMENT le projet et toutes ses données pour tous ses membres. Continuer ?") with a typed-confirmation requirement (the user must type "supprimer" to enable the destructive button).

**Given** I confirm, **When** the delete request completes, **Then** the project row is deleted and all dependent rows are removed via Postgres CASCADE, **And** all member devices currently subscribed will see the project disappear on their next foreground sync.

**Given** offline mode, **When** I attempt to delete, **Then** the action is disabled.

---

## Epic 3: Agenda

**Epic Goal:** Au terme de cet epic, Sophie peut consulter son agenda personnel agrégé sur toutes ses commissions (vue semaine + vue mois), ouvrir le détail d'une réunion, consulter la liste des réunions de n'importe quelle commission, et utiliser l'app en mode hors-ligne en lecture avec sync automatique au retour de connexion.

### Story 3.1: Vue calendrier personnelle agrégée (semaine + mois)

As Sophie,
I want to see my personal agenda — all meetings from all the commissions I belong to — in a weekly and a monthly view,
So that I have one place to check whether I have anything coming up.

**Acceptance Criteria:**

**Given** I'm a member of 2 commissions with meetings in them, **When** the Agenda screen loads, **Then** the default view (weekly) renders matching `docs/design/project/screens-agenda.jsx`, showing the current week with all my meetings (regardless of commission).

**Given** the weekly view, **When** I tap a "Mois" toggle, **Then** the view switches to a monthly grid with my meetings displayed as compact entries on each day.

**Given** I have no upcoming meetings, **When** either view loads, **Then** I see a sympathetic empty state ("Aucune réunion à venir — bravo !") rather than an empty grid.

**Given** the data, **When** I observe what's displayed, **Then** the data comes from Room (DAO `meetingDao.observeForMember(memberId): Flow<List<Meeting>>`) — never directly from supabase-kt.

**Given** I scroll the weekly view, **When** I tap on a meeting tile, **Then** I navigate to the meeting detail (story 3.2).

### Story 3.2: Détail d'une réunion

As Sophie,
I want to tap on a meeting and see its full details — title, date, time, duration, commissions involved, creator, conflicts and arbitration status,
So that I can decide quickly whether to attend or rearrange.

**Acceptance Criteria:**

**Given** I tap on a meeting in the agenda, **When** the detail screen loads, **Then** I see the meeting title (or commission name as fallback), date and time range, duration, list of commissions concerned, the creator's name, and any conflict/arbitration data attached.

**Given** the meeting has conflicts and I'm one of the conflicted members, **When** the screen loads, **Then** my arbitration status ("En attente", "Tu iras à...", or "Tu as choisi cette réunion") is highlighted near the top.

**Given** I'm the creator of the meeting, **When** the screen loads, **Then** I additionally see action buttons "Modifier" and "Supprimer" (Epic 4 stories).

**Given** the meeting has been deleted by another member while I was offline, **When** I navigate to its detail after sync, **Then** I see a "Cette réunion a été supprimée" state with a back button.

### Story 3.3: Liste des réunions par commission

As any member of a project,
I want to see the upcoming meetings of any commission in the project (even ones I don't belong to),
So that I have transparency on the project's overall activity.

**Acceptance Criteria:**

**Given** I navigate to a commission from the commission list (Epic 2 surfaces) or from the agenda, **When** the commission detail screen loads, **Then** I see the commission name, the list of its members, and a chronological list of its upcoming meetings.

**Given** the upcoming meeting list, **When** I tap a meeting, **Then** I navigate to the meeting detail (story 3.2).

**Given** I'm not a member of this commission, **When** the screen loads, **Then** I can still see all of the above (no restriction), but no action button to create a meeting in this commission unless I'm a member.

### Story 3.4: Mode hors-ligne lecture + sync au retour de connexion

As Sophie,
I want the app to remain usable in read mode when I lose connectivity, and to silently sync when connection returns,
So that my agenda is reliable even in the subway.

**Acceptance Criteria:**

**Given** my device is online, **When** I open the Agenda for the first time, **Then** the app fetches the project's current state from Supabase via `supabase.postgrest`, upserts all rows into Room, and renders from Room (FR37, FR40).

**Given** my device goes offline, **When** I navigate within the app, **Then** all read-only views (Agenda, Meeting Detail, Commission Detail, Member list) continue to render the last-known Room state with no crash.

**Given** I'm offline, **When** the Agenda renders, **Then** a discreet banner appears at the top of the screen ("Mode hors-ligne — dernière synchro à 14h32") (FR38).

**Given** connectivity returns, **When** the app detects it (via platform connectivity APIs wrapped in an `expect`/`actual` `ConnectivityObserver`), **Then** a fresh pull from Supabase is triggered, Room is upserted, the offline banner disappears (FR40).

**Given** the app returns to foreground after being backgrounded, **When** `onResume` fires, **Then** a fresh pull is also triggered regardless of connectivity state at the time of backgrounding.

### Story 3.5: Désactivation des actions d'écriture hors-ligne

As Sophie,
I want write actions (create/edit/delete meeting, member, commission, arbitrage) to be visibly disabled when I'm offline, with a clear message,
So that I don't waste time trying to do something that will fail silently.

**Acceptance Criteria:**

**Given** I'm offline, **When** I tap "+ Réunion" (Epic 4) or "+ Commission" (Epic 2) or any write action, **Then** the button appears visually disabled (opacity reduced) and tapping it produces a Snackbar with the message "Connexion requise pour cette action".

**Given** I'm offline and the connectivity returns, **When** `ConnectivityObserver` detects the change, **Then** write buttons re-enable automatically without user interaction.

**Given** the arbitration screen (Epic 5), **When** I'm offline, **Then** the 3 arbitrage choice buttons are disabled with the same message.

---

## Epic 4: Meeting Scheduling

**Epic Goal:** Au terme de cet epic, Marc peut créer une réunion avec une ou plusieurs commissions, voir en temps réel les conflits cross-commissions détectés, choisir de décaler ou de voir des suggestions de créneaux libres, ou créer malgré tout (déclenchant alors un push aux conflictés). Il peut modifier ou supprimer ses réunions (avec push aux concernés). Il consulte le statut consolidé des arbitrages sur ses réunions conflictuelles.

### Story 4.1: Créer une réunion sans détection (form + push standard)

As Marc (an organizer),
I want to create a meeting by specifying date, time, duration, one or more commissions, and an optional title,
So that the meeting appears on the agenda of everyone in those commissions, and they get a push notification.

**Acceptance Criteria:**

**Given** I tap "+ Réunion" from the Agenda or a Commission detail, **When** the creation form opens, **Then** I see fields matching `docs/design/project/screens-meeting.jsx` : date picker, start time, duration chips (30min / 1h / 1h30 / 2h / libre), multi-select of commissions, optional title input, "Vérifier les conflits" primary button.

**Given** I fill all required fields (date, time, duration, ≥ 1 commission), **When** I tap "Vérifier les conflits", **Then** if no conflicts exist (story 4.2 returns empty), I proceed to a final confirmation step and then to creation.

**Given** I confirm creation with no conflicts, **When** the request completes, **Then** the meeting is inserted via supabase-kt with associated `meeting_commission` rows. The meeting appears in my agenda and Room.

**Given** the meeting is created, **When** the Edge Function `dispatch_meeting_push` fires (triggered by Postgres webhook on `meeting` INSERT), **Then** all members of the selected commissions receive a push notification "Nouvelle réunion : {titre ou commission} — jeudi 21 mai 20h" (FR28).

**Given** offline mode, **When** I open the creation form, **Then** the "Vérifier les conflits" button is disabled with the "Connexion requise" message.

### Story 4.2: Détection de conflit en amont (Kotlin pré-check + Postgres function)

As Marc,
I want to see — *before* validating the meeting — which members of the selected commissions are already booked on the chosen time slot in another commission,
So that I don't create a conflict by accident.

**Acceptance Criteria:**

**Given** I'm filling the creation form, **When** all required fields are present (date, time, duration, commissions), **Then** a Kotlin pre-check (`DetectConflictsLocallyUseCase`) runs against the Room cache and displays a discreet badge ("⚠️ X conflits potentiels") that updates live as I edit fields.

**Given** I tap "Vérifier les conflits", **When** the request completes, **Then** the app calls Postgres function `detect_conflicts(p_project_id, p_commission_ids, p_start, p_end)` via supabase-kt. The response is the authoritative list of conflicted members.

**Given** the server returns ≥ 1 conflict, **When** the conflict screen displays, **Then** I see, for each conflicted member : name, concurrent commission, concurrent meeting time slot (FR21). The display matches the design.

**Given** the Kotlin pre-check and the server response differ (race condition), **When** the user sees the result, **Then** the server response wins — the displayed list is always the server's, never the local pre-check's.

**Given** the latency, **When** the conflict check runs in good network conditions, **Then** the round-trip is under 200 ms (cf. NFR-P2). Local pre-check is under 50 ms.

### Story 4.3: Suggestion de créneaux libres + 3 choix face aux conflits

As Marc faced with detected conflicts,
I want three explicit options — décaler / voir des suggestions de créneaux libres / créer malgré tout —,
So that I can decide quickly and intelligently rather than re-thinking from scratch.

**Acceptance Criteria:**

**Given** the conflicts are displayed, **When** the three action buttons appear, **Then** they are labeled "Décaler", "Voir des créneaux libres", "Créer quand même" — all visible at once, matching the design.

**Given** I tap "Décaler", **When** the action triggers, **Then** I return to the creation form with the previously entered values pre-filled, focused on the time field for editing.

**Given** I tap "Voir des créneaux libres", **When** the suggestion screen loads, **Then** the app calls a Postgres function (or reuses `detect_conflicts` cleverly) that proposes 3-5 alternative time slots within a configurable temporal window (default ± 7 days, same duration) where the conflict count is zero.

**Given** I tap a proposed slot, **When** I confirm, **Then** the meeting is created at that slot, triggering the standard push to commission members (FR28).

**Given** the latency, **When** the suggestion completes, **Then** it returns in under 500 ms (a slightly looser bar than `detect_conflicts` since it involves more computation).

### Story 4.4: Création malgré conflit + Edge Function dispatch_conflict_push

As Marc choosing to override the conflict,
I want the meeting to be created and the conflicted members to be notified immediately by push so they can arbitrate,
So that I don't bottleneck the calendar but the conflicted parties are aware and can react.

**Acceptance Criteria:**

**Given** the conflict screen, **When** I tap "Créer quand même" and confirm, **Then** the meeting is inserted via supabase-kt as in story 4.1, AND a row is inserted into a `pending_push_conflict` audit table (or equivalent mechanism that the Edge Function consumes).

**Given** the Postgres trigger on `meeting INSERT` runs, **When** it detects conflicts (via `detect_conflicts` re-computed server-side), **Then** the Edge Function `dispatch_conflict_push` is invoked with the meeting id and the list of conflicted member ids.

**Given** `dispatch_conflict_push` runs (TypeScript Deno), **When** it processes the input, **Then** for each conflicted member, it looks up their FCM/APNs token from the `device` table and sends a push payload (data-only) with type `conflict_detected`, the meeting id, and a deep-link to the arbitration screen (FR29, FR32).

**Given** an FCM/APNs error (5xx or 4xx), **When** dispatch fails, **Then** the Edge Function retries up to 3 times with exponential backoff (300ms, 900ms, 2700ms). After 3 failures, log to `push_failure_log` and report to Sentry via the Edge Function context.

**Given** the latency, **When** the full pipeline runs end-to-end, **Then** the conflict push reaches the conflicted member's device in under 5 seconds (NFR-P4).

### Story 4.5: Modifier ou supprimer une réunion + push aux concernés

As Marc (creator of a meeting),
I want to modify date/time/duration/commissions/title or delete the meeting altogether, with the concerned members automatically notified by push,
So that no one is left in the dark when plans change.

**Acceptance Criteria:**

**Given** I'm on the detail screen of a meeting I created, **When** I tap "Modifier", **Then** the same form as creation opens, pre-filled with the meeting's current values, allowing me to edit and re-submit. The re-submit re-triggers conflict detection (story 4.2-4.4) on the new time slot.

**Given** I modify the meeting, **When** the update completes, **Then** all members of the (new + old) commission set receive a push "Réunion modifiée : {titre} — nouvelle date jeudi 21 mai 20h" (FR31).

**Given** I tap "Supprimer", **When** a confirmation dialog appears ("Supprimer la réunion {titre} ?") and I confirm, **Then** the meeting is deleted via supabase-kt and a push is sent to all members of the affected commissions : "Réunion annulée : {titre}" (FR31).

**Given** I'm not the creator, **When** I open the detail of a meeting created by another member, **Then** the "Modifier" and "Supprimer" buttons are NOT shown (the flat model only restricts these to the creator at the UI level — at the data level, RLS allows any member to modify, but UX keeps it simple).

### Story 4.6: Vue créateur — statut d'arbitrage consolidé

As Marc (creator of a meeting that triggered conflicts),
I want to see on the meeting detail screen the consolidated status of each conflicted member's arbitration (who's coming, who's not, who hasn't decided yet),
So that I know whether I should keep the meeting, reschedule, or just go ahead.

**Acceptance Criteria:**

**Given** I'm the creator of a meeting that has conflicts, **When** I open its detail screen, **Then** I see a "Statut des conflictés" section listing each conflicted member with their arbitration status : "Vient" (green), "Vient pas — va à {autre commission}" (red), "En attente" (gray).

**Given** the arbitration statuses are pulled, **When** the screen renders, **Then** they come from the `arbitration` table joined to `member`, observed via Room. (Sync follows the pull-on-foreground rules from Epic 3 story 3.4.)

**Given** a member subsequently arbitrates while I'm on the screen, **When** I pull-to-refresh (or open the app from a push), **Then** the new arbitration status appears (FR27).

---

## Epic 5: Arbitration

**Epic Goal:** Au terme de cet epic, Sophie qui reçoit un push de conflit peut taper la notif et atterrir directement sur l'écran d'arbitrage via deep-link. Elle choisit entre les deux réunions concurrentes ou reporte sa décision. Son arbitrage est enregistré et les créateurs des deux réunions sont notifiés en retour.

### Story 5.1: Écran d'arbitrage + deep-link depuis push

As Sophie receiving a conflict push notification,
I want tapping the push to open the app directly on the arbitration screen for that conflict, with both meetings displayed side by side,
So that I can make a decision in seconds without navigating.

**Acceptance Criteria:**

**Given** I receive a push of type `conflict_detected` with a deep-link `colision://arbitration/{conflictMeetingId}`, **When** I tap the push, **Then** the app opens (or comes to foreground) and routes me directly to the Arbitration screen with the appropriate context loaded.

**Given** the Arbitration screen, **When** it renders, **Then** it matches `docs/design/project/screens-arbitrage.jsx` : the two conflicting meetings displayed side by side, each showing commission, date/time, duration, number of members already engaged.

**Given** the arbitration screen, **When** it loads its data, **Then** the data comes from Room (after a sync triggered by the push tap) — never directly from supabase-kt.

**Given** I came from a push but the arbitration is already resolved (e.g., the meeting was cancelled by the creator while the push was in flight), **When** the screen loads, **Then** I see a clear state ("Cette réunion a été annulée — pas de conflit à arbitrer") with a back button.

### Story 5.2: Choix d'arbitrage + persistance

As Sophie facing the arbitration screen,
I want three explicit choices — "Je vais à A" / "Je vais à B" / "Je trancherai plus tard" — with my choice immediately persisted,
So that I commit one tap and move on.

**Acceptance Criteria:**

**Given** the arbitration screen, **When** I see the action area, **Then** I see three large tappable buttons matching the design : "Je vais à {commission A}", "Je vais à {commission B}", "Je trancherai plus tard".

**Given** I tap "Je vais à {commission A}", **When** the request completes, **Then** an `arbitration` row is inserted via supabase-kt with `member_id = me`, `meeting_id = B (the one I'm skipping)`, `conflicting_meeting_id = A`. Room reflects the change.

**Given** I had previously chosen and then change my mind, **When** I tap a different option, **Then** the existing `arbitration` row is updated (the UNIQUE constraint on `(member_id, meeting_id, conflicting_meeting_id)` allows the update).

**Given** I tap "Je trancherai plus tard", **When** the action triggers, **Then** no `arbitration` row is created (or any existing one is deleted), and on the agenda the conflict appears as a pending arbitrage badge until I revisit and decide.

**Given** offline mode, **When** I tap any arbitration choice, **Then** the action is disabled with the "Connexion requise" message (FR39).

### Story 5.3: Edge Function dispatch_arbitration_push

As Marc (creator of one of the conflicting meetings),
I want to receive a push notification when a conflicted member arbitrates,
So that I see in real-time how many people are coming and can decide whether to keep, reschedule, or cancel my meeting.

**Acceptance Criteria:**

**Given** the Postgres trigger on `arbitration INSERT` runs, **When** the trigger fires, **Then** it invokes the Edge Function `dispatch_arbitration_push` with the arbitration id.

**Given** `dispatch_arbitration_push` runs, **When** it processes the input, **Then** it looks up the creators of both conflicting meetings, fetches their FCM/APNs tokens, and sends a push payload (data-only) to each : "{Sophie} ira à {commission gagnante} jeudi 21 mai" with deep-link `colision://meeting/{their meeting id}`.

**Given** the FCM/APNs delivery, **When** it fails (5xx), **Then** retry 3x with exponential backoff (same pattern as story 4.4). After 3 failures, log to `push_failure_log`.

**Given** the latency, **When** the pipeline runs, **Then** Marc receives the push in under 5 seconds (NFR-P4).

**Given** Marc taps the push, **When** the deep-link routes, **Then** he lands on the detail of his meeting, where the "Statut des conflictés" section (story 4.6) now reflects the new arbitration.

---

## Epic 6: Launch

**Epic Goal:** Au terme de cet epic, Colision est publié sur Google Play et l'App Store, avec sa privacy policy française hébergée, ses assets store complets, et les flavors `production` pointant vers le projet Supabase de prod. Sophie peut télécharger et utiliser l'app sans aide externe.

### Story 6.1: Privacy Policy française + hosting public

As the Colision team,
I want a concise French privacy policy publicly hosted on a free static service, linked from the app stores,
So that we meet RGPD requirements (NFR-S6) and unblock the App Store and Play Store submissions.

**Acceptance Criteria:**

**Given** the existing PRD and architecture privacy requirements, **When** the privacy policy is drafted, **Then** it is in French, clear and short (< 1 page), listing : (1) the data collected (first name + commission affiliations), (2) where it's stored (Supabase EU Frankfurt), (3) how long it's kept (until the member leaves or the project is deleted), (4) the user's rights (access, rectification, erasure), (5) contact info (Anthony's email).

**Given** the privacy policy file, **When** it is hosted, **Then** it lives on a free static service (GitHub Pages, Vercel, or Netlify) at a stable URL (e.g., `https://anthooop.github.io/colision-privacy/`), accessible publicly.

**Given** the URL, **When** the iOS Info.plist is updated, **Then** it contains the privacy policy URL in the appropriate key (`NSPrivacyPolicyURL`).

**Given** the URL, **When** the Play Console "Data safety" form is filled, **Then** the URL is provided in the privacy policy field.

### Story 6.2: Préparation des assets store (Android + iOS)

As the Colision team,
I want all required store assets (icons, screenshots, descriptions, privacy labels) prepared and uploaded,
So that the submission process is unblocked on both platforms.

**Acceptance Criteria:**

**Given** the design system, **When** the launcher icon is produced, **Then** it exists in all required sizes for Android (mipmap-xhdpi/xxhdpi/xxxhdpi + adaptive icon foreground + background) and iOS (multiple sizes per Apple's spec). The icon respects the design theme (forest green + off-white).

**Given** the 4 user journeys + 4 screens, **When** the screenshots are captured, **Then** there are at least 5 screenshots per platform showing the key flows : Welcome / Join / Create / Agenda / Arbitration. Captured on a Pixel 6a and an iPhone 15 simulator (or equivalent). Localized in French.

**Given** the App Store Connect form, **When** the privacy nutrition labels are filled, **Then** they declare : "Data Not Collected" for everything except "Personal Info — Name" (= the first name entered by users). Tracking : disabled.

**Given** the Play Console "Data safety" form, **When** it's filled, **Then** the only "data collected" entry is "Personal Info — Name", marked as not sold, not used for ads, encrypted in transit, deletable from the app.

**Given** the store descriptions (French), **When** they're written, **Then** they're under 4000 chars for Play Store and 4000 chars for App Store, structured with : short subtitle ("L'app qui détecte les conflits de réunions entre commissions"), key features bulleted, and a positioning paragraph.

### Story 6.3: Soumission TestFlight + Play Internal Track

As Sophie (the first real beta user),
I want to download Colision from a private link sent by Anthony,
So that I can start using the app with my conseil before public launch.

**Acceptance Criteria:**

**Given** the `production` flavor BuildConfig is updated with the Supabase prod project URL + anon key, **When** the production build runs locally (`./gradlew :composeApp:bundleProductionRelease` for Android, Xcode Archive for iOS), **Then** it produces a signed AAB and a signed IPA respectively.

**Given** the signed AAB, **When** it's uploaded to Play Console Internal Testing track via the Play Console UI, **Then** the build appears in Internal Testing and is downloadable via the provided opt-in URL.

**Given** the signed IPA, **When** it's uploaded to App Store Connect via Xcode (or `xcrun altool`), **Then** the build appears in TestFlight and Sophie can be added as an internal tester (no review required for internal testers).

**Given** Sophie receives the TestFlight invite or the Play Internal opt-in URL, **When** she follows it on her device, **Then** she can install Colision V1.0.0 (build 1) without going through the public store.

**Given** the version, **When** the app is installed and opened, **Then** Sophie completes onboarding (Epic 2), sees an empty agenda, and can start using the app exactly as documented in the user journeys.

**Given** the post-launch state, **When** the first feedback comes in, **Then** the iteration loop begins — V1.0.1 / V1.1 follow per the roadmap in `docs/architecture.md` and `docs/prd.md`.
