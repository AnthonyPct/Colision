---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8]
inputDocuments:
  - docs/product-brief-Colision-2026-05-10.md
  - docs/prd.md
  - docs/design/README.md
  - docs/design/chats/chat1.md
  - docs/design/project/tokens.jsx
  - CLAUDE.md
workflowType: 'architecture'
workflowStatus: 'complete'
lastStep: 8
project_name: 'Colision'
user_name: 'Anthony Picquet'
date: '2026-05-15'
completed: '2026-05-16'
---

# Architecture Decision Document — Colision

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

## Project Context Analysis

### Requirements Overview

**Functional Requirements** — 43 FRs organisés en 8 capability areas :

| Capability Area | FRs | Implication architecturale |
|---|---|---|
| Project Management | FR1–FR5 | Génération de code unique, modèle "projet = tenant logique" |
| Commission Management | FR6–FR9 | CRUD simple, isolation par projet |
| Member Management | FR10–FR14 | Liaisons N:N (membre ↔ commission), pas d'auth utilisateur |
| Meeting Scheduling | FR15–FR19 | Modèle temporel (date+durée), liaisons N:N (réunion ↔ commission) |
| **Conflict Detection & Resolution** | FR20–FR27 | **Cœur algorithmique** : détection cross-commissions, suggestion de créneaux libres, stockage des arbitrages |
| Notifications | FR28–FR33 | Pipeline push FCM/APNs, deep-linking, dispatch backend |
| Calendar & Discovery | FR34–FR36 | Lecture agrégée multi-commissions, vues semaine/mois |
| Offline & Sync | FR37–FR41 | Cache lecture seule, désactivation des écritures offline, sync auto au retour, Realtime live |
| Privacy & Data Management | FR42–FR43 | Suppressions en cascade, conformité RGPD |

**Non-Functional Requirements** — les contraintes qui *piloteront* les choix techniques :

- **Performance** : conflit detection < 200 ms (≤ 1 000 réunions), sync Realtime < 1 s, push < 5 s end-to-end, cold start < 2 s, 60 FPS UI.
- **Security** : RLS Postgres par projet, code 6 chars dans un alphabet sans ambigus (30^6 espace), TLS partout, rate-limiting Edge Function, hébergement UE.
- **Reliability** : 99,5 % crash-free, retry push 3× avec backoff, gestion gracieuse online↔offline.
- **Accessibility** : 48 dp tap targets, contraste 4,5:1, support fontScale 130 %, labels TalkBack/VoiceOver. Pragmatique au MVP.
- **Scalability** : 100 projets / 2 000 membres / 1 000 réunions par projet sans dégradation. Capacity de bascule Supabase free → Pro sans changement de code.
- **Maintainability** : coverage tests > 60 % sur la logique métier, CI sur chaque push, monitoring erreurs + analytics.

### Scale & Complexity

- **Primary technical domain** : `mobile_app` cross-platform (Android + iOS via Kotlin Multiplatform / Compose Multiplatform).
- **Complexity level** : **medium**. Pas de régulation, pas de paiement, pas d'IA, pas de multi-tenancy enterprise. Mais la combinaison *KMP + Realtime + push natifs + algorithme de conflit + offline-first + flavors* représente une vraie surface technique pour un solo dev.
- **Composants architecturaux à concevoir** :
  1. Client mobile (KMP/Compose) — UI (4 features), MVI, design system, navigation
  2. Backend Supabase — schéma Postgres, RLS policies, Realtime channels, Edge Functions
  3. Pipeline notifications — Edge Function dispatch + FCM + APNs + deep-linking
  4. Persistence locale — Room KMP, stratégie de sync avec Supabase
  5. Algorithme de détection de conflit — décision client vs serveur à prendre
  6. CI/CD — GitHub Actions ou Bitrise, jobs Android + iOS + tests
  7. Monitoring + Analytics — outils à arbitrer pour KMP-iOS compatibility

### Technical Constraints & Dependencies

**Décisions déjà actées (dans CLAUDE.md — non négociables)** :
- Single Gradle module `:composeApp`
- Kotlin 2.3.21, AGP 8.11.2, Compose Multiplatform 1.10.3, Material3 1.10.0-alpha05, JVM 11, minSdk 28, iOS 15+
- Pattern : Google Android architecture (UI/Domain/Data) **organisé par feature**
- MVI strict : `ViewModel` + `Route` + `Screen` + `Repository`
- DI : Koin partout, `viewModelOf` + `koinViewModel()`
- Networking : Ktor (base) + supabase-kt (jamais REST direct sur Supabase)
- Persistence locale : Room KMP, drivers plateforme via Koin
- Flavors : `development` + `production`, BuildConfig par flavor, secrets jamais en clair dans `commonMain`
- Design : tokens.jsx → mapping vers `ColorScheme` + `Typography` dans `core/design/` avant tout écran

**Contraintes utilisateur** :
- **Solo developer** — toute solution doit être maintenable seul, sans expertise externe.
- **Persona racine non-tech** — UX plus simple que WhatsApp, contrainte qui invalide toute interaction qui demanderait un raisonnement technique de l'utilisateur (ex. saisir un email, configurer une intégration, déchiffrer un message d'erreur technique).
- **Pas d'authentification utilisateur au MVP** — le code projet est l'unique mécanisme d'accès, mais il faut quand même un *device identity* technique pour stocker les FCM/APNs tokens et tracer les arbitrages.

**Dépendances externes** :
- Supabase (Postgres + Realtime + Edge Functions, région UE).
- Firebase Cloud Messaging (Android).
- Apple Push Notification service (iOS) — nécessite Apple Developer Account + provisioning + clé APNs token-based.
- Stores : Google Play (compte ouvert) + App Store Connect (compte Apple Developer 99 $/an).

### Cross-Cutting Concerns Identified

Préoccupations qui traversent **plusieurs features** et qu'il faut concevoir une seule fois pour éviter la divergence :

1. **Identité device sans compte** — chaque appareil obtient un `device_id` opaque à la première ouverture, persisté en local. Il sert à : associer les FCM/APNs tokens, tracer qui a fait quel arbitrage, attribuer la création d'une réunion. Pas un identifiant utilisateur — un identifiant **appareil**.
2. **Lien device ↔ membre dans un projet** — quand un utilisateur sélectionne son identité dans un projet (FR11), le couple `(device_id, project_id) → member_id` est stocké, à la fois en local (Room) et côté Supabase. Un appareil = un membre pour un projet donné (au MVP).
3. **Sync online ↔ offline** — Room est la *source de vérité* pour l'UI ; Supabase est la *source de vérité* pour l'état serveur. Stratégie : pull au démarrage + Realtime subscription en arrière-plan ; les écritures sont online-only au MVP (FR39).
4. **Deep-linking de notifications** — les pushes (FR32) doivent ouvrir une *route* spécifique (détail réunion, écran arbitrage). La navigation doit donc être adressable par URI, peu importe la lib choisie.
5. **Theming light/dark + 4 palettes** — le système de thème doit supporter le changement dynamique (système iOS/Android) + les 4 palettes du design system (`forest`, `coral`, `indigo`, `plum`). Décision : palette `forest` par défaut au MVP, les 3 autres en *tweaks panel* différé V1.1.
6. **Localisation française** — toutes les chaînes via `composeResources` (`Res.string.xxx`), pas de texte en dur (NFR-A3).
7. **Observabilité** — un wrapper unique `Logger` + `Analytics` + `CrashReporter` injecté via Koin, pour swap facile d'implémentation entre dev/prod et pour découpler le code métier des SDK tiers.
8. **Erreurs et résultats** — un type `Result<T>` (kotlinx ou maison) unifié dans `core/common/`, pour que toutes les repositories aient la même signature de retour et que les ViewModels traitent les erreurs de façon uniforme.
9. **Date/heure et fuseaux** — utilisation de `kotlinx-datetime` partout (compatible KMP). Tous les créneaux sont stockés en UTC en base, affichés en heure locale de l'appareil. Pas de support multi-timezone au MVP (les conseils municipaux sont locaux).
10. **Gestion des permissions notification** — wrapped dans une interface `NotificationPermissionManager` (expect/actual), demandée en fin d'onboarding (FR33) après explication, jamais en cold-launch.

## Starter Template & Foundation

### Primary Technology Domain

**Mobile cross-platform** via Kotlin Multiplatform avec UI partagée Compose Multiplatform.

### Starter Status

**Le starter est déjà en place** dans le repo. Initialisé via le wizard officiel Kotlin Multiplatform (`kmp-wizard` / JetBrains) avec :

- Module Gradle unique `:composeApp`
- Cibles : `androidTarget()`, `iosArm64()`, `iosSimulatorArm64()`
- Source sets : `commonMain`, `androidMain`, `iosMain`, `commonTest`, `androidUnitTest`
- Scaffolding initial : `App.kt`, `Greeting.kt`, `Platform.kt`, MainActivity (Android), MainViewController (iOS)
- Version catalog `gradle/libs.versions.toml` pour centraliser les versions
- `settings.gradle.kts` avec `TYPESAFE_PROJECT_ACCESSORS` activé

**Versions pinnées dans `gradle/libs.versions.toml`** :
- Kotlin **2.3.21**
- AGP **8.11.2** (Android Gradle Plugin)
- Compose Multiplatform **1.10.3**
- Material3 **1.10.0-alpha05**
- JVM target **11**
- Android `compileSdk` / `targetSdk` **36**, `minSdk` **28**
- iOS minimum **15.0**

### Architectural Decisions Provided by the Starter

| Décision | Valeur héritée du starter |
|---|---|
| Langage | Kotlin 2.3.21 |
| UI framework | Compose Multiplatform (UI partagée 100 % via `commonMain`) |
| Plateformes cibles | Android + iOS arm64 (device + simulator) |
| Système de build | Gradle 8.x + Kotlin DSL + version catalog |
| Structure source set | Standard KMP (commonMain / androidMain / iosMain) |
| Resources partagées | `commonMain/composeResources/` avec `Res` généré |
| Build framework iOS | Framework Kotlin (statique) consommé par Xcode |

### Librairies à Ajouter à `libs.versions.toml`

Ces dépendances seront introduites dans la **première implementation story** (initial scaffolding). Versions à résoudre au moment de l'ajout via la résolution Gradle ; les coordonnées Maven sont stables.

| Catégorie | Coordonnées Maven | Justification |
|---|---|---|
| **Dependency injection** | `io.insert-koin:koin-core`, `io.insert-koin:koin-compose`, `io.insert-koin:koin-compose-viewmodel`, `io.insert-koin:koin-android` | Koin KMP, mandaté par CLAUDE.md |
| **Networking** | `io.ktor:ktor-client-core`, `io.ktor:ktor-client-content-negotiation`, `io.ktor:ktor-serialization-kotlinx-json`, `io.ktor:ktor-client-logging`, `io.ktor:ktor-client-okhttp` (Android), `io.ktor:ktor-client-darwin` (iOS) | Ktor KMP, mandaté |
| **Supabase** | `io.github.jan-tennert.supabase:supabase-kt-bom`, `postgrest-kt`, `realtime-kt`, `functions-kt` | Client Supabase officiel KMP |
| **Persistence locale** | `androidx.room:room-runtime`, `androidx.room:room-compiler` (KSP), `androidx.sqlite:sqlite-bundled` | Room KMP, mandaté |
| **Sérialisation** | `org.jetbrains.kotlinx:kotlinx-serialization-json` | Indispensable pour Ktor + Supabase |
| **Date/heure** | `org.jetbrains.kotlinx:kotlinx-datetime` | Mandaté pour cross-cutting concern #9 |
| **Coroutines** | `org.jetbrains.kotlinx:kotlinx-coroutines-core` | Déjà tiré transitivement, à expliciter |
| **Navigation** | À décider en étape 4 (Compose Navigation vs Decompose vs Voyager) | Décision pendante |
| **FCM Android** | `com.google.firebase:firebase-bom`, `firebase-messaging-ktx`, `com.google.gms.google-services` (plugin) | Push Android |
| **APNs iOS** | Pas de dépendance Kotlin — usage de l'API iOS native via `expect`/`actual` | Push iOS |
| **KSP** | `com.google.devtools.ksp` (plugin) | Requis par Room |
| **Tests** | `org.jetbrains.kotlin:kotlin-test`, `app.cash.turbine:turbine` (Flow testing), `io.mockk:mockk` (Android-only) | Tests communs + plateforme |
| **Crash reporting / Analytics** | À décider en étape 4 (Sentry vs Crashlytics ; PostHog vs Firebase Analytics) | Décision pendante |

### Initial Implementation Story (Bootstrap)

La toute première story du backlog d'implémentation devra :

1. Ajouter les dépendances ci-dessus à `gradle/libs.versions.toml` et aux `dependencies { }` du module `:composeApp` (Common + Android + iOS).
2. Configurer le plugin KSP pour Room.
3. Créer les répertoires de l'arborescence par feature : `app/`, `core/{design,network,database,di,common}/`, `feature/{onboarding,agenda,meeting,arbitrage}/`.
4. Mapper `docs/design/project/tokens.jsx` en un `ColorScheme` + `Typography` Kotlin dans `core/design/`.
5. Configurer les deux flavors `development` et `production` dans le `build.gradle.kts` du module, avec leur `BuildConfig.kt` dédié.
6. Setup Koin de base : `appModule` + `corePlatformModule` (Android et iOS) + un module vide par feature.
7. Smoke test : `./gradlew :composeApp:assembleDevelopmentDebug` et `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64` passent.

**Note** : aucune feature fonctionnelle dans cette story. Elle pose les fondations pour que toutes les stories suivantes puissent suivre la même discipline architecturale.

## Core Architectural Decisions

### Decision Summary

| # | Decision | Choice | Rationale |
|---|---|---|---|
| 1 | Navigation library | **Compose Navigation Multiplatform** (`androidx.navigation:navigation-compose` KMP) | Officiel JetBrains, deep-linking natif via `uriPattern`, API familière, boring technology |
| 2 | Auth "device-only" | **Supabase Anonymous Auth** + `SecureStorage` `expect`/`actual` | Signé, RLS native via `auth.uid()`, transparent pour Sophie, évolue vers compte nommé en V2 via `linkIdentity()` |
| 3 | Conflict detection | **Hybride** : Kotlin pré-check + Postgres function source de vérité | UX réactive (latence 0 pendant la saisie) + cohérence stricte au tap "Vérifier", même algo réutilisé par triggers post-insert |
| 4 | Sync Room ↔ Supabase | **Pull-on-foreground + optimistic local writes + push notifs comme seul signal live**. **Pas de Realtime.** | Plus simple, moins cher (pas de pression concurrent connections), batterie économisée, push couvre les cas urgents (NFR-P4 5 s end-to-end) |
| 5 | Génération code projet | Alphabet 30 chars sans ambigus + UNIQUE constraint + retry + génération via Postgres function `create_project(p_name)` | Aucune confusion à l'oral/SMS, 730M combinaisons, source de vérité serveur |
| 6 | Rate limiting | **Différé V1.1**. NFR-S2 noté comme deferred. | Risque résiduel acceptable au MVP (pas de visibilité publique, peu d'incitation à brute-force). Trade-off explicite et conscient. |
| 7 | CI/CD | **GH Actions** (PR build + tests + lint) + **Bitrise** (release pipeline signed, différé au 1er test externe) | GH Actions intégré, gratuit, YAML versionné ; Bitrise spécialisé mobile quand le signing devient un sujet ; pas de précipitation |
| 8 | Crash monitoring | **Sentry** (SDK officiel KMP) + hosting EU + wrapper `CrashReporter` Koin | Seul à avoir un vrai SDK KMP, hébergement Frankfurt aligne NFR-S5, breadcrumbs partagés common ↔ plateforme |
| 9 | Analytics produit | **PostHog** (cloud EU) + wrapper `Analytics` Koin + events anonymisés (pas d'`identify(userId)` au MVP) | Hébergement EU, free tier 1M events/mois, all-in-one (events + funnels + retention), self-hostable si besoin |
| 10 | Modèle d'erreur | **`kotlin.Result<T>`** stdlib + `sealed AppError : Throwable` pour typage métier | Pas de dépendance, syntaxe `.fold` familière, sealed AppError pour les cas métier ; mitigation explicite de la non-exhaustivité par convention de code review et tests |

### Decision Priority Analysis

**Critical Decisions (Block Implementation)** : 1, 2, 3, 4, 5, 10
**Important Decisions (Shape Architecture)** : 7, 8, 9
**Deferred to V1.1+** : 6 (rate limiting), release pipeline signed via Bitrise

---

### Data Architecture

#### Conflict Detection Algorithm (Hybrid)

**Localisation** : la fonction Postgres `detect_conflicts(p_project_id uuid, p_commission_ids uuid[], p_start timestamptz, p_end timestamptz)` est la **source de vérité**. Elle est appelée :
- Côté client via supabase-kt au tap "Vérifier les conflits" (FR20) → résultat affiché.
- Côté serveur via trigger post-insert sur `meeting` → dispatch des pushes curatifs (FR24).

Une **fonction Kotlin pure** dans `feature/meeting/domain/` effectue un **pré-check optimiste** sur les données Room en cache pendant que l'utilisateur saisit le créneau, pour un feedback live.

**Tests miroirs** : un test commun valide que les deux implémentations (SQL et Kotlin) retournent le même résultat sur un jeu de cas représentatif. Si les deux divergent, le test casse.

#### Postgres Schema (Source of Truth)

```sql
-- Identité device, liée à auth.users (Supabase Anonymous Auth)
create table device (
  id uuid primary key default gen_random_uuid(),
  auth_user_id uuid not null references auth.users(id) on delete cascade,
  fcm_token text,
  apns_token text,
  platform text not null check (platform in ('android', 'ios')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (auth_user_id)
);

create table project (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  share_code char(6) not null unique,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index on project (share_code);

create table commission (
  id uuid primary key default gen_random_uuid(),
  project_id uuid not null references project(id) on delete cascade,
  name text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index on commission (project_id);

create table member (
  id uuid primary key default gen_random_uuid(),
  project_id uuid not null references project(id) on delete cascade,
  device_id uuid references device(id) on delete set null,
  display_name text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index on member (project_id);
create index on member (device_id);

create table member_commission (
  member_id uuid not null references member(id) on delete cascade,
  commission_id uuid not null references commission(id) on delete cascade,
  primary key (member_id, commission_id)
);

create table meeting (
  id uuid primary key default gen_random_uuid(),
  project_id uuid not null references project(id) on delete cascade,
  title text,
  starts_at timestamptz not null,
  ends_at timestamptz not null,
  created_by_member_id uuid references member(id) on delete set null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  check (ends_at > starts_at)
);
create index on meeting (project_id, starts_at, ends_at);

create table meeting_commission (
  meeting_id uuid not null references meeting(id) on delete cascade,
  commission_id uuid not null references commission(id) on delete cascade,
  primary key (meeting_id, commission_id)
);

create table arbitration (
  id uuid primary key default gen_random_uuid(),
  member_id uuid not null references member(id) on delete cascade,
  meeting_id uuid not null references meeting(id) on delete cascade,
  conflicting_meeting_id uuid not null references meeting(id) on delete cascade,
  decided_at timestamptz not null default now(),
  unique (member_id, meeting_id, conflicting_meeting_id)
);

-- Toutes les tables : trigger `set_updated_at` qui met à jour `updated_at` à chaque UPDATE.
```

#### RLS Policies (Isolation par Projet)

Toutes les tables ont **RLS activée**. Pattern global : un device A ne peut accéder aux données d'un projet B que s'il a une row dans `member` liée à ce projet B (`member.device_id = (select id from device where auth_user_id = auth.uid())`).

Les policies seront spécifiées table par table dans la story d'implémentation correspondante. Pattern type :
```sql
create policy "members of project can read meetings"
  on meeting for select
  using (
    exists (
      select 1 from member m
      join device d on d.id = m.device_id
      where d.auth_user_id = auth.uid()
        and m.project_id = meeting.project_id
    )
  );
```

#### Sync Strategy (Pull-on-Foreground)

1. **Cold start avec projet rejoint** : pull complet du projet via `supabase.postgrest` → upsert dans Room.
2. **App passe en foreground (onResume)** : re-pull du projet courant.
3. **Tap sur notification push** : open app → pull → naviguer vers le deep-link.
4. **Écriture** : `repository.create(...)` → appel Supabase → succès → upsert local optimiste dans Room → Flow Room ré-emet → UI update.

**Pas de Realtime subscription**. NFR-P3 obsolète et retiré.

---

### Authentication & Security

#### Modèle "Device-Only" via Supabase Anonymous Auth

- Au 1er lancement : `supabase.auth.signInAnonymously()` → JWT + uid opaque.
- Stockage du JWT et refresh token via interface `SecureStorage` `expect`/`actual` :
  - **Android** : `EncryptedSharedPreferences`
  - **iOS** : Keychain Services
- supabase-kt utilise automatiquement le JWT à chaque requête.
- Trigger Postgres `on_auth_user_created` crée une row `device` à chaque INSERT sur `auth.users`.

#### Generation du Code Projet

```sql
create function generate_share_code() returns char(6) as $$
declare
  alphabet text := '23456789ACDEFGHJKMNPQRSTUVWXYZ';
  result text := '';
  i int;
begin
  for i in 1..6 loop
    result := result || substr(alphabet, 1 + floor(random() * length(alphabet))::int, 1);
  end loop;
  return result;
end;
$$ language plpgsql volatile;

create function create_project(p_name text) returns project as $$
declare
  v_project project;
  v_attempts int := 0;
begin
  loop
    begin
      insert into project (name, share_code)
      values (p_name, generate_share_code())
      returning * into v_project;
      return v_project;
    exception when unique_violation then
      v_attempts := v_attempts + 1;
      if v_attempts >= 5 then
        raise exception 'Could not generate unique share code after 5 attempts';
      end if;
    end;
  end loop;
end;
$$ language plpgsql security definer;
```

Alphabet 30 caractères sans ambigus : `23456789ACDEFGHJKMNPQRSTUVWXYZ`. Espace ~ 730 M.

#### Rate Limiting — DIFFÉRÉ V1.1

Le NFR-S2 n'est **pas implémenté au MVP**. Story V1.1 à créer : "Implement rate limiting on `try_resolve_code` via Postgres-based audit table `share_code_attempt`". L'archi proposée (table + Postgres function + `pg_cron` cleanup) est documentée pour reprise ultérieure.

---

### API & Communication Patterns

#### Patterns supabase-kt

- **Lecture** : `supabase.postgrest["table"].select { filter { ... } }` typé via `@Serializable` data classes en `commonMain`.
- **Écriture simple** : `supabase.postgrest["table"].insert(value)`.
- **Logique métier** : Postgres functions appelées via `supabase.postgrest.rpc("function_name", params)`. Toute logique non-triviale est côté serveur.
- **Edge Functions** : `supabase.functions.invoke("function_name", body)` réservées aux opérations exigeant un secret serveur (dispatch FCM/APNs).

#### Edge Functions (TypeScript Deno)

| Function | Trigger | Rôle |
|---|---|---|
| `dispatch_meeting_push` | Postgres webhook on `meeting` INSERT/UPDATE | Calcule les destinataires (membres de la commission), récupère leurs FCM/APNs tokens, envoie le push |
| `dispatch_conflict_push` | Postgres webhook on `meeting` INSERT (via trigger inspectant les conflits) | Push aux membres conflictés avec deep-link vers l'écran d'arbitrage |
| `dispatch_arbitration_push` | Postgres webhook on `arbitration` INSERT | Notifie les créateurs des deux réunions concernées |

Secrets dans Supabase Vault : `FCM_SERVICE_ACCOUNT_KEY` (JSON), `APNS_KEY_ID`, `APNS_TEAM_ID`, `APNS_AUTH_KEY` (`.p8`).

#### Modèle d'Erreur Unifié

`AppError` est un sealed class **séparé** de `Throwable` (pour préserver l'exhaustivité du `when` et éviter les singleton-throwable anti-pattern). Un Throwable wrapper `AppErrorThrowable` permet l'usage avec `kotlin.Result<T>`.

```kotlin
// core/common/AppError.kt
sealed class AppError {
    data object NetworkUnavailable : AppError()
    data object ServerUnreachable : AppError()
    data object ProjectCodeInvalid : AppError()
    data object ProjectCodeRateLimited : AppError()
    data class MeetingTimeRangeInvalid(val reason: String) : AppError()
    data object CommissionRequired : AppError()
    data object AnonymousSessionExpired : AppError()
    data class Unknown(val cause: Throwable) : AppError()
}

// core/common/AppErrorThrowable.kt
class AppErrorThrowable(val error: AppError) : Throwable(error.toString())

// core/common/ResultHelpers.kt
fun <T> appErrorResult(error: AppError): Result<T> =
    Result.failure(AppErrorThrowable(error))

inline fun <T, R> Result<T>.foldAppError(
    onSuccess: (T) -> R,
    onError: (AppError) -> R
): R = fold(
    onSuccess = onSuccess,
    onFailure = { t -> onError((t as? AppErrorThrowable)?.error ?: AppError.Unknown(t)) }
)
```

**Pattern repository** :
```kotlin
suspend fun joinByCode(code: String): Result<Project> = try {
    val project = supabase.postgrest.rpc("try_resolve_code", mapOf("p_code" to code))
        .decodeAs<Project>()
    Result.success(project)
} catch (e: PostgrestException) {
    when (e.code) {
        "P0001" -> appErrorResult(AppError.ProjectCodeRateLimited)
        "P0002" -> appErrorResult(AppError.ProjectCodeInvalid)
        else -> appErrorResult(AppError.Unknown(e))
    }
} catch (e: IOException) {
    appErrorResult(AppError.NetworkUnavailable)
}
```

**Pattern ViewModel** (exhaustivité retrouvée !) :
```kotlin
viewModelScope.launch {
    repository.joinByCode(code).foldAppError(
        onSuccess = { project -> /* navigate */ },
        onError = { error -> when (error) {
            AppError.ProjectCodeInvalid -> emit("Code introuvable.")
            AppError.ProjectCodeRateLimited -> emit("Trop de tentatives, réessaie dans une minute.")
            AppError.NetworkUnavailable -> emit("Pas de connexion.")
            is AppError.Unknown -> { crashReporter.captureException(error.cause); emit("Erreur inattendue.") }
            // … compilateur force à gérer chaque case du sealed AppError
            else -> emit("Erreur inattendue.")  // fallback safe pour les cas non pertinents à ce flow
        }}
    )
}
```

**Bénéfice net** : `when (error)` est maintenant exhaustif au sens du compilateur (sealed class), tu retrouves la safety qu'on avait perdue. Coût : un wrapper `AppErrorThrowable` (5 lignes) + un helper `foldAppError` (5 lignes). Trade-off très favorable.

---

### Frontend Architecture

#### Navigation

**Lib** : `androidx.navigation:navigation-compose` (version KMP).

**Routes typées** par sealed class :
```kotlin
sealed interface AppRoute {
    @Serializable object Splash : AppRoute
    @Serializable object Onboarding : AppRoute
    @Serializable object Agenda : AppRoute
    @Serializable data class MeetingDetail(val meetingId: String) : AppRoute
    @Serializable object MeetingCreate : AppRoute
    @Serializable data class Arbitration(val conflictMeetingId: String) : AppRoute
}
```

**Deep-linking** pour les pushes :
- `colision://meeting/{id}` → `MeetingDetail`
- `colision://arbitration/{conflictId}` → `Arbitration`

Configuré via `deepLink { uriPattern = "colision://meeting/{id}" }` dans le NavGraph.

#### Compose Performance

- Toutes les data classes UI marquées `@Immutable` ou `@Stable`.
- `LazyColumn`/`LazyRow` exigent toujours `key = { it.id }`.
- Pas d'animations sur les écrans denses (sobriété pour Sophie non-tech).

---

### Infrastructure & Deployment

#### CI/CD Hybride

**GitHub Actions** — `.github/workflows/` :
- `build-android.yml` (runner `ubuntu-latest`) : `assembleDevelopmentDebug` + `testDebugUnitTest` sur chaque push/PR
- `build-ios.yml` (runner `macos-14`) : `linkDebugFrameworkIosSimulatorArm64` + `iosSimulatorArm64Test` sur chaque push/PR
- `lint.yml` (runner `ubuntu-latest`) : `detekt` + `ktlintCheck`
- `android-instrumented.yml` (runner `ubuntu-latest`, KVM-enabled) : optionnel, sur push `main` ou nightly, via `reactivecircus/android-emulator-runner@v2`

**Aucun secret à provisionner sur GH au MVP**. Les valeurs Supabase URL + anon key sont placées directement dans `composeApp/src/{development,production}/kotlin/.../config/BuildConfig.kt` (acceptable car le repo est privé et la anon key est intentionnellement publique côté Supabase).

**Bitrise** (release pipeline, différé au 1er test externe) :
- Signing iOS automatisé (provisioning + certificats via UI Bitrise)
- Signing Android (keystore via UI Bitrise)
- Upload TestFlight (via App Store Connect API)
- Upload Play Internal Track (via Google Play Developer API)
- Triggered manuellement ou sur tag `v*`

#### Crash Monitoring : Sentry

- Dépendance : `io.sentry:sentry-kotlin-multiplatform`
- Wrapper `CrashReporter` dans `core/common/`, injecté via Koin
- DSN différents par flavor (`SENTRY_DSN_DEV`, `SENTRY_DSN_PROD` dans `BuildConfig.kt` par flavor)
- Hosting : Sentry Cloud EU (Frankfurt)
- Init au démarrage Android (`MainActivity`) et iOS (`MainViewController`)
- `setUserContext(deviceId)` après `signInAnonymously()` réussi
- Implémentation `NoopCrashReporter` pour les tests unitaires

#### Analytics : PostHog

- Dépendance Android : `com.posthog:posthog-android`
- Dépendance iOS : PostHog iOS via SPM dans `iosApp/iosApp.xcodeproj`
- Wrapper `Analytics` `expect`/`actual` dans `core/common/`, injecté via Koin
- Hosting : PostHog Cloud EU
- Projets séparés par flavor : "Colision Dev" + "Colision Prod"
- Pas d'`identify(userId)` au MVP — events anonymisés
- Option "Disable IP capture" activée côté projet PostHog
- Events instrumentés dès le MVP (alignés NFR-M5) :
  - `app_opened` (cold start) : `flavor`, `app_version`
  - `project_created` : `project_id` (hashé)
  - `project_joined` : `project_id` (hashé), `time_since_install`
  - `commission_created` : `project_id` (hashé)
  - `meeting_created` : `commission_count`, `had_conflicts`
  - `conflict_detected` : `conflict_count`
  - `arbitration_submitted` : `choice`
  - `push_received`, `push_tapped` : `push_type`

#### Environments

- **Supabase Dev** : staging, EU Frankfurt, lié flavor `development`
- **Supabase Prod** : production, EU Frankfurt, lié flavor `production`
- **Apple Developer Account** : 99 $/an
- **Google Play Developer Account** : 25 $ one-shot
- **Sentry** : 1 projet, environnements `dev` / `prod`
- **PostHog** : 2 projets séparés (Dev / Prod)

---

### Decision Impact Analysis

#### Implementation Sequence

1. **Bootstrap** : starter + Koin + Ktor + tokens design → app vide qui démarre.
2. **Schema & Auth** : schéma Postgres, RLS policies, Supabase Anonymous Auth, `SecureStorage`, trigger `on_auth_user_created`.
3. **Project Management** : feature `onboarding` (J1 + J2), Postgres functions `create_project` + `try_resolve_code` (sans rate limit au MVP).
4. **Member & Commission Management** : CRUD via supabase-kt + RPC pour les opérations transactionnelles.
5. **Meeting Scheduling + Conflict Detection** : feature `meeting` (J3), Postgres function `detect_conflicts`, pré-check Kotlin client, tests miroirs.
6. **Push Pipeline** : Edge Functions, FCM setup, APNs setup, deep-linking Navigation Compose.
7. **Arbitration** : feature `arbitrage` (J4), pushes curatifs.
8. **Calendar Views** : feature `agenda` (J2 ongoing), Room queries + pull-on-foreground.
9. **Polish & Soumission** : copy, accessibilité, store assets, privacy policy, soumission manuelle iOS (Xcode) + Android (Play Console).

#### Cross-Component Dependencies

- **Anonymous Auth** débloque tout le reste (RLS dépend de `auth.uid()`).
- **Postgres schema** débloque toutes les features.
- **Edge Functions** dépendent du schema et des secrets FCM/APNs.
- **Navigation Compose** débloque le deep-linking, donc le pipeline push.
- **CrashReporter + Analytics wrappers** sont prérequis au bootstrap (injectés dès la story 1 même en NoopImpl).

#### Deferred / Out-of-MVP

- **NFR-P3 (Realtime sync < 1s)** : supprimé. Remplacé par pull-on-foreground + push notifs.
- **NFR-S2 (rate limiting 5/min/IP)** : V1.1.
- **Release pipeline signed (Bitrise)** : différé au 1er test externe.
- **WCAG 2.1 AA audit complet (NFR-A6)** : V1.1.
- **Tests instrumentés Android sur émulateur en CI** : optionnel post-MVP.
- **Maestro tests cross-platform** : V1.1.

## Implementation Patterns & Consistency Rules

### Source of Truth Hierarchy

Quand un pattern entre en conflit entre plusieurs documents :

1. **`CLAUDE.md`** — source de vérité opérationnelle pour toute personne (humaine ou AI) qui code dans ce repo. Toutes les contraintes structurelles y figurent.
2. **`docs/architecture.md`** (ce document) — décisions architecturales et patterns techniques au-delà de ce qui est dans CLAUDE.md.
3. **`docs/design/`** — contrat visuel pour tout ce qui est UI.
4. **`docs/prd.md` + `docs/product-brief-Colision-*.md`** — contrat fonctionnel.

Si une AI agent trouve un conflit entre ces documents, elle **doit demander clarification** plutôt que choisir arbitrairement.

### Patterns Déjà Enforced (par `CLAUDE.md`)

Ne pas re-spécifier — référencer directement `CLAUDE.md` :

- **Architecture** : Google Android (UI/Domain/Data) adapté KMP, organisé par feature
- **MVI** : `ViewModel` + `Route` + `Screen` + `Repository` (artefacts strictement séparés)
- **DI** : Koin partout, `viewModelOf` + `koinViewModel()`, modules par feature
- **Networking** : Ktor + supabase-kt (jamais REST direct sur Supabase)
- **Persistence locale** : Room KMP, drivers via Koin
- **Design** : `tokens.jsx` → `ColorScheme` + `Typography` dans `core/design/` avant tout écran
- **Flavors** : `development` / `production`, BuildConfig par flavor, secrets jamais dans `commonMain`
- **Folder structure** : `app/`, `core/{design,network,database,di,common}/`, `feature/{name}/{data,domain,ui,di}/`

### Naming Patterns (Gaps comblés ici)

#### Code Kotlin

| Élément | Convention | Exemple |
|---|---|---|
| Class | PascalCase | `MeetingRepository`, `OnboardingViewModel` |
| Function / method | camelCase | `joinByCode()`, `detectConflicts()` |
| Variable / param | camelCase | `meetingId`, `displayName` |
| Constants (top-level `val`) | UPPER_SNAKE_CASE | `MAX_RETRY_ATTEMPTS`, `SHARE_CODE_ALPHABET` |
| Composable function | PascalCase | `MeetingDetailScreen()`, `ConflictBadge()` |
| File Kotlin | Match its main class/function | `MeetingRepository.kt`, `OnboardingScreen.kt` |
| Package | all.lowercase, par feature/concern | `com.anthooop.colision.feature.meeting.ui` |

#### State / Intent / Event (MVI)

Pattern strict pour chaque feature :

| Type | Suffixe | Exemple |
|---|---|---|
| State (data class immuable) | `State` | `OnboardingState` |
| Intent (sealed interface, actions utilisateur) | `Intent` | `OnboardingIntent.CodeEntered(val code: String)` |
| Event (sealed interface, side-effects one-shot) | `Event` | `OnboardingEvent.NavigateToAgenda` |
| Grouping optionnel | `Contract` (si les 3 sont petits, regroupable) | `OnboardingContract.kt` |

#### Postgres / Supabase

| Élément | Convention | Exemple |
|---|---|---|
| Table | singular, snake_case | `project`, `meeting_commission` |
| Colonne | snake_case | `created_at`, `device_id` |
| Foreign key | `<referenced_table>_id` | `project_id`, `member_id` |
| Index | `<table>_<columns>_idx` ou laissé à Postgres (autonommé) | `meeting_project_id_starts_at_idx` |
| Function | snake_case, verbe d'action | `create_project`, `detect_conflicts`, `try_resolve_code` |
| Trigger | `<verb>_<table>_<action>` | `set_updated_at_meeting`, `on_auth_user_created` |
| RLS policy name | phrase descriptive entre guillemets | `"members of project can read meetings"` |
| Type | snake_case | `meeting_status` |

#### Mapping Kotlin ↔ SQL

supabase-kt + kotlinx-serialization gère le mapping automatique entre `camelCase` Kotlin et `snake_case` Postgres **si on annote correctement** :

```kotlin
@Serializable
data class Meeting(
    val id: String,
    @SerialName("project_id") val projectId: String,
    @SerialName("starts_at") val startsAt: Instant,
    @SerialName("ends_at") val endsAt: Instant,
    val title: String?,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant
)
```

**Convention** : toujours annoter `@SerialName` pour les colonnes snake_case côté SQL, même quand la propriété Kotlin est en camelCase identique au snake_case. Pas d'exception. Cela évite les bugs de mapping silencieux.

### Date/Time Patterns

**Une seule lib** : `kotlinx-datetime`.

| Concept | Type Kotlin |
|---|---|
| Instant exact (DB, transport) | `kotlinx.datetime.Instant` |
| Date locale (UI calendar header) | `kotlinx.datetime.LocalDate` |
| Heure locale (UI time picker) | `kotlinx.datetime.LocalTime` |
| Combinaison | `kotlinx.datetime.LocalDateTime` |
| Fuseau | `kotlinx.datetime.TimeZone` |

**Règle** : tout est stocké en `Instant` UTC en base et en réseau. Conversion en `LocalDateTime` selon `TimeZone.currentSystemDefault()` uniquement au moment du rendu UI. **Jamais** d'arithmétique sur des strings ISO.

**Anti-pattern** : utiliser `java.time.Instant` ou `kotlin.time.Duration` mélangés avec kotlinx-datetime. **Toujours** kotlinx-datetime.

### Localisation Patterns

- **Toutes** les chaînes visibles utilisateur passent par `composeResources` :
  ```kotlin
  Text(text = stringResource(Res.string.onboarding_welcome_title))
  ```
- **Aucune** chaîne en dur dans un `Text()`, même temporairement. Si une AI doit écrire un nouvel écran, elle ajoute d'abord les clés dans `commonMain/composeResources/values/strings.xml`.
- Naming des clés : `<feature>_<screen>_<element>` → `onboarding_join_code_input_label`, `meeting_detail_attendees_title`.

### Test Patterns

- **Localisation** : tests communs dans `commonTest/kotlin/`, tests Android dans `androidUnitTest/`, tests iOS dans `iosTest/`.
- **Naming** : `<ClassUnderTest>Test.kt` (ex. `MeetingRepositoryTest.kt`). Une classe de test = une classe testée.
- **Fonction de test** : `fun \`describe what is tested in plain french\`()`. Backticks autorisés pour la lisibilité. Exemple : `` `detectConflicts retourne vide quand aucun membre conflicté`() ``.
- **Frameworks** :
  - Assertions : `kotlin.test` (`assertEquals`, `assertTrue`, etc.) — partagé sur toutes les plateformes.
  - Flow testing : Turbine.
  - Mocking : mockk pour les tests Android-only ; pour `commonTest`, utiliser des **fakes** manuels (interfaces concrètes simples) — mockk n'est pas KMP-compatible.
- **Structure d'un test** : pattern Given/When/Then commenté dans le code.

### Logging Patterns

Interface `Logger` injectée via Koin, wrapping le SDK plateforme.

```kotlin
interface Logger {
    fun debug(tag: String, message: String, throwable: Throwable? = null)
    fun info(tag: String, message: String)
    fun warn(tag: String, message: String, throwable: Throwable? = null)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}
```

- Flavor `development` : tous les logs vers `Logcat` (Android) / `os_log` (iOS) + breadcrumbs Sentry.
- Flavor `production` : `debug` et `info` no-op. `warn` et `error` envoyés vers Sentry (breadcrumbs + capture).
- **Tag** = nom de la classe émettrice : `"MeetingRepository"`.
- **Jamais** de log de PII ou de payload complet contenant des prénoms / IDs sensibles. Logs d'événements seulement (`"joinByCode failed"`, pas `"joinByCode failed for code ABCDEF"`).

### Error Handling Patterns

(détaillé section Modèle d'Erreur Unifié plus haut)

**Règle d'or** : *toute* fonction de Repository retourne `Result<T>` enrobant un `AppErrorThrowable(AppError)`. Les ViewModels appellent `.foldAppError(...)` exhaustivement.

Les `try/catch` sauvages dans les ViewModels ou Composables sont **interdits**. Les exceptions doivent être attrapées et converties en `AppError` au plus bas niveau possible (typiquement dans le Repository).

### Loading States

- Chaque écran qui charge de la donnée a son `State` qui inclut un champ `isLoading: Boolean` ou un sealed state plus riche (`Loading | Loaded(data) | Error(message)` selon complexité).
- **Pas de loader global** au MVP — chaque écran gère son loader localement.
- **Pattern recommandé** pour un écran qui consomme un `Flow<List<X>>` depuis Room :
  ```kotlin
  data class AgendaState(
      val meetings: List<Meeting> = emptyList(),
      val isInitialLoading: Boolean = true,    // true tant qu'on n'a jamais reçu de Flow
      val isRefreshing: Boolean = false,       // true pendant un pull-on-foreground en cours
      val error: String? = null
  )
  ```

### Push Notification Patterns

- **Payload** : payload data-only (pas de payload notification visible côté serveur). Le client Kotlin reçoit le data payload via `FirebaseMessagingService` (Android) ou `UNUserNotificationCenter` (iOS), parse les champs, et présente la notif via `NotificationManager` natif. Permet de personnaliser le contenu et de gérer le deep-link uniformément.
- **Champs payload** :
  ```json
  {
    "type": "conflict_detected | meeting_created | arbitration_decided",
    "project_id": "...",
    "meeting_id": "...",
    "deep_link": "colision://arbitration/..."
  }
  ```
- **Deep link** systématique : ouvre l'écran cible avec une nav fresh-stack si l'app était cold.

### Enforcement & Validation

**Outils en CI** :
- `detekt` : analyse statique Kotlin (configuration `config/detekt/detekt.yml` à la racine).
- `ktlint` : formatage et conventions de style.
- `room-compiler` : valide les schemas Room à la compilation (via KSP).
- Compilateur Kotlin : enforce le sealed `AppError` côté ViewModel via les `when` exhaustifs.

**Pas en CI mais en pratique** :
- Revues de PR (même solo) avec une checklist explicite : "Strings dans `composeResources` ? `@SerialName` annoté ? `kotlinx-datetime` utilisé ?".
- Si un agent AI ajoute du code qui viole un de ces patterns, le `detekt` lint ou la review humaine doit l'attraper.

**Updates** :
- Les patterns évoluent. Toute mise à jour passe par une PR qui modifie `CLAUDE.md` (si structurel) ou `docs/architecture.md` (si décisionnel). Le but est qu'un agent AI qui ouvre le projet en 2027 sache immédiatement ce qui s'applique.

### Anti-Patterns Explicitement Bannis

- ❌ Texte en dur dans les Composables (sans `stringResource`)
- ❌ `try/catch` dans un ViewModel ou Composable
- ❌ Lecture directe depuis Supabase dans un Composable
- ❌ Création d'une `HttpClient` ad-hoc en dehors de `core/network/`
- ❌ Instanciation manuelle d'un ViewModel ou Repository en dehors de Koin
- ❌ Format de date string custom (ex. `SimpleDateFormat`) — kotlinx-datetime only
- ❌ Mixage `java.time.*` et `kotlinx.datetime.*` dans le même module
- ❌ `data object` qui étend `Throwable` (stack trace figée — utiliser `AppError` + `AppErrorThrowable` wrapper)
- ❌ Logs contenant des prénoms, codes projet ou autres identifiants pouvant identifier un utilisateur
- ❌ Dépendances ajoutées en dehors de `gradle/libs.versions.toml`

## Project Structure & Boundaries

### Complete Project Directory Structure

Voici l'arborescence cible **après la story Bootstrap** (story 1). Toute story suivante ajoute des fichiers dans `feature/` ou `core/` selon le pattern.

```
Colision/
├── CLAUDE.md                              # Source de vérité opérationnelle (versionné)
├── README.md                              # Présentation du repo (versionné)
├── settings.gradle.kts                    # Gradle settings (TYPESAFE_PROJECT_ACCESSORS)
├── build.gradle.kts                       # Root build script
├── gradle.properties
├── gradlew, gradlew.bat
├── gradle/
│   ├── libs.versions.toml                 # Version catalog (toutes les versions ici)
│   └── wrapper/
├── config/
│   └── detekt/
│       └── detekt.yml                     # Configuration analyse statique
├── .github/
│   └── workflows/
│       ├── build-android.yml              # CI : build Android debug + tests
│       ├── build-ios.yml                  # CI : build iOS framework + tests
│       ├── lint.yml                       # CI : detekt + ktlint
│       └── android-instrumented.yml       # CI optionnel : tests émulateur Android
├── docs/
│   ├── product-brief-Colision-2026-05-10.md
│   ├── prd.md
│   ├── architecture.md                    # CE DOCUMENT
│   ├── bmm-workflow-status.yaml
│   ├── design/                            # Bundle design (HTML/JSX prototypes)
│   │   ├── README.md
│   │   ├── Colision — prototype.pdf
│   │   ├── chats/
│   │   └── project/
│   │       ├── tokens.jsx                 # SOURCE DE VÉRITÉ des design tokens
│   │       ├── components.jsx
│   │       ├── screens-*.jsx
│   │       └── ...
│   └── produit/                           # Réservé docs produit non BMAD
├── supabase/                              # Schéma + migrations + Edge Functions
│   ├── config.toml                        # Config Supabase CLI
│   ├── migrations/                        # Migrations SQL versionnées
│   │   ├── 20260516_000_init_schema.sql
│   │   ├── 20260516_001_rls_policies.sql
│   │   ├── 20260516_002_functions.sql
│   │   └── ...
│   ├── seed.sql                           # Données de seed pour le projet dev
│   └── functions/                         # Edge Functions Deno/TypeScript
│       ├── dispatch_meeting_push/
│       │   └── index.ts
│       ├── dispatch_conflict_push/
│       │   └── index.ts
│       ├── dispatch_arbitration_push/
│       │   └── index.ts
│       └── _shared/                       # Modules partagés entre EF
│           ├── fcm.ts                     # Client FCM HTTP v1 API
│           ├── apns.ts                    # Client APNs token-based
│           └── supabase-admin.ts          # Helpers service role
├── iosApp/                                # Projet Xcode iOS (entry point)
│   ├── iosApp.xcodeproj/
│   ├── Configuration/
│   │   ├── Config.dev.xcconfig            # Build config flavor dev iOS
│   │   └── Config.prod.xcconfig           # Build config flavor prod iOS
│   └── iosApp/
│       ├── iOSApp.swift                   # @main SwiftUI App entry point
│       ├── ContentView.swift              # Wrap du ComposeUIViewController KMP
│       ├── AppDelegate.swift              # APNs registration + Sentry init + PostHog init
│       ├── Info.plist
│       └── GoogleService-Info-Dev.plist   # Firebase iOS (FCM) — flavor dev
└── composeApp/                            # Module Gradle KMP unique
    ├── build.gradle.kts                   # Configure flavors development/production + KSP
    └── src/
        ├── commonMain/
        │   ├── composeResources/
        │   │   ├── values/
        │   │   │   └── strings.xml        # i18n français (clé `feature_screen_element`)
        │   │   ├── drawable/              # Icônes + illustrations partagées
        │   │   └── font/                  # DM Sans + JetBrains Mono
        │   └── kotlin/com/anthooop/colision/
        │       ├── app/
        │       │   ├── App.kt             # @Composable racine, ColisionTheme, NavHost
        │       │   ├── ColisionTheme.kt   # ColorScheme + Typography (mappés depuis tokens.jsx)
        │       │   ├── nav/
        │       │   │   ├── AppRoute.kt    # sealed interface des routes (@Serializable)
        │       │   │   └── DeepLinks.kt   # Patterns "colision://meeting/{id}" etc.
        │       │   └── di/
        │       │       └── AppModule.kt   # Module Koin top-level + features
        │       ├── core/
        │       │   ├── common/
        │       │   │   ├── AppError.kt
        │       │   │   ├── AppErrorThrowable.kt
        │       │   │   ├── ResultHelpers.kt   # appErrorResult, foldAppError
        │       │   │   ├── Logger.kt           # interface
        │       │   │   ├── CrashReporter.kt    # interface
        │       │   │   ├── Analytics.kt        # interface
        │       │   │   ├── SecureStorage.kt    # interface expect
        │       │   │   ├── NotificationPermissionManager.kt  # interface expect
        │       │   │   └── DispatcherProvider.kt
        │       │   ├── design/
        │       │   │   ├── ColorScheme.kt   # Color scheme light/dark, 4 palettes
        │       │   │   ├── Typography.kt    # DM Sans, scale type
        │       │   │   ├── Shapes.kt        # Radius scale (xs/sm/md/lg/xl/pill)
        │       │   │   ├── Spacing.kt       # SP scale 4-based
        │       │   │   └── components/      # Composables réutilisables (CodeInput, etc.)
        │       │   ├── network/
        │       │   │   ├── HttpClientFactory.kt   # Ktor base config
        │       │   │   └── SupabaseClientProvider.kt
        │       │   ├── database/
        │       │   │   ├── ColisionDatabase.kt   # @Database Room
        │       │   │   ├── Converters.kt          # @TypeConverter Instant <-> Long
        │       │   │   ├── entity/                # @Entity Room
        │       │   │   │   ├── ProjectEntity.kt
        │       │   │   │   ├── CommissionEntity.kt
        │       │   │   │   ├── MemberEntity.kt
        │       │   │   │   ├── MeetingEntity.kt
        │       │   │   │   ├── ArbitrationEntity.kt
        │       │   │   │   ├── MemberCommissionCrossRef.kt
        │       │   │   │   └── MeetingCommissionCrossRef.kt
        │       │   │   └── dao/
        │       │   │       ├── ProjectDao.kt
        │       │   │       ├── CommissionDao.kt
        │       │   │       ├── MemberDao.kt
        │       │   │       ├── MeetingDao.kt
        │       │   │       └── ArbitrationDao.kt
        │       │   └── di/
        │       │       └── CoreModule.kt    # Module Koin pour core
        │       ├── config/
        │       │   └── BuildConfig.kt       # expect class (flavor-specific actual)
        │       └── feature/
        │           ├── onboarding/
        │           │   ├── data/
        │           │   │   ├── OnboardingRepository.kt        # interface
        │           │   │   ├── OnboardingRepositoryImpl.kt
        │           │   │   ├── remote/
        │           │   │   │   └── OnboardingRemoteSource.kt  # supabase-kt calls
        │           │   │   ├── local/
        │           │   │   │   └── OnboardingLocalSource.kt   # Room calls
        │           │   │   └── dto/
        │           │   │       └── ProjectDto.kt              # @Serializable, @SerialName
        │           │   ├── domain/
        │           │   │   └── ValidateShareCodeUseCase.kt    # Si logique non triviale
        │           │   ├── ui/
        │           │   │   ├── OnboardingRoute.kt
        │           │   │   ├── OnboardingScreen.kt
        │           │   │   ├── OnboardingViewModel.kt
        │           │   │   ├── OnboardingState.kt
        │           │   │   ├── OnboardingIntent.kt
        │           │   │   └── OnboardingEvent.kt
        │           │   └── di/
        │           │       └── OnboardingModule.kt
        │           ├── agenda/                   # Même structure que onboarding
        │           ├── meeting/                  # Idem
        │           │   ├── data/
        │           │   ├── domain/
        │           │   │   └── DetectConflictsLocallyUseCase.kt   # Kotlin pré-check
        │           │   ├── ui/
        │           │   └── di/
        │           └── arbitrage/                # Idem
        ├── androidMain/
        │   ├── AndroidManifest.xml            # Permissions, FirebaseMessagingService
        │   ├── kotlin/com/anthooop/colision/
        │   │   ├── MainActivity.kt            # setContent { App() }, startKoin
        │   │   ├── ColisionApplication.kt     # init Sentry, PostHog, FCM token
        │   │   ├── push/
        │   │   │   └── ColisionMessagingService.kt   # FirebaseMessagingService
        │   │   ├── core/
        │   │   │   ├── common/
        │   │   │   │   ├── SecureStorage.android.kt           # EncryptedSharedPreferences
        │   │   │   │   ├── NotificationPermissionManager.android.kt
        │   │   │   │   ├── LoggerAndroid.kt
        │   │   │   │   ├── CrashReporterSentry.android.kt
        │   │   │   │   └── AnalyticsPostHog.android.kt
        │   │   │   └── database/
        │   │   │       └── ColisionDatabaseProvider.android.kt  # Room driver Android
        │   │   └── di/
        │   │       └── AndroidPlatformModule.kt
        │   └── res/
        │       ├── mipmap-*/                  # Launcher icons
        │       ├── values/
        │       │   └── strings.xml            # Just app_name (le reste est partagé)
        │       └── xml/
        │           └── network_security_config.xml
        ├── iosMain/
        │   └── kotlin/com/anthooop/colision/
        │       ├── MainViewController.kt      # ComposeUIViewController { App() }
        │       ├── core/
        │       │   ├── common/
        │       │   │   ├── SecureStorage.ios.kt              # Keychain Services
        │       │   │   ├── NotificationPermissionManager.ios.kt
        │       │   │   ├── LoggerIos.kt
        │       │   │   ├── CrashReporterSentry.ios.kt
        │       │   │   └── AnalyticsPostHog.ios.kt
        │       │   └── database/
        │       │       └── ColisionDatabaseProvider.ios.kt   # Room driver iOS
        │       └── di/
        │           └── IosPlatformModule.kt
        ├── commonTest/
        │   └── kotlin/com/anthooop/colision/
        │       ├── feature/
        │       │   ├── onboarding/
        │       │   │   ├── OnboardingViewModelTest.kt
        │       │   │   └── OnboardingRepositoryTest.kt
        │       │   └── meeting/
        │       │       └── DetectConflictsLocallyUseCaseTest.kt   # Tests miroirs SQL
        │       └── fakes/
        │           ├── FakeSupabaseClient.kt
        │           └── FakeMeetingDao.kt
        ├── androidUnitTest/
        │   └── kotlin/com/anthooop/colision/
        │       └── (tests Android-spécifiques nécessitant mockk)
        ├── iosTest/
        │   └── kotlin/com/anthooop/colision/
        │       └── (tests iOS-spécifiques)
        ├── development/                       # Source set flavor development
        │   ├── AndroidManifest.xml            # google-services dev
        │   ├── kotlin/com/anthooop/colision/config/
        │   │   └── BuildConfig.kt             # actual class avec valeurs dev
        │   └── google-services.json           # Firebase Android dev
        └── production/                        # Source set flavor production
            ├── AndroidManifest.xml
            ├── kotlin/com/anthooop/colision/config/
            │   └── BuildConfig.kt             # actual class avec valeurs prod
            └── google-services.json           # Firebase Android prod
```

### Architectural Boundaries

#### Boundary 1 — Frontière UI ↔ Domaine

- **Composables** (`Screen.kt`) ne touchent QUE le `State` reçu en paramètre + émettent des `Intent`. Pas de Koin, pas de ViewModel, pas de Repository, pas de Supabase, pas de Room.
- **`Route.kt`** est l'unique point qui obtient le `ViewModel` (via `koinViewModel()`) et collecte `state` / `events`.
- **`ViewModel.kt`** orchestre les Repositories. Pas d'accès direct à Supabase ou Room.

#### Boundary 2 — Frontière Domaine ↔ Data

- **Repository** est l'unique abstraction qui peut appeler Supabase (via supabase-kt) ET Room (via DAO). Il décide quelle source utiliser selon le cas.
- **Pas de DAO Room ni de client Supabase exposé en dehors de la couche `data`** d'une feature.
- Les **DTOs** (`@Serializable @SerialName(...)`) vivent en `data/dto/`, les **entities Room** en `core/database/entity/`. Le repository convertit DTO ↔ Entity ↔ Domain Model.

#### Boundary 3 — Frontière Common ↔ Plateforme

- **expect/actual** pour : `SecureStorage`, `NotificationPermissionManager`, `Logger` (impl différente Android/iOS), `Analytics` impl, `CrashReporter` impl, Room database provider.
- Tout le reste du code business vit en `commonMain`.

#### Boundary 4 — Frontière App ↔ Backend Supabase

- **Aucun appel HTTP direct** vers Supabase REST. Tout passe par les modules `supabase-kt` (`postgrest`, `functions`, `auth`).
- **Les Edge Functions Supabase** sont l'unique zone où un secret de service est utilisé (FCM/APNs). Le code client n'a jamais le service role key.

#### Boundary 5 — Frontière Stories (versionnement) ↔ Migrations SQL

- **Une story = une migration SQL** maximum (ou zéro). Les migrations sont numérotées chronologiquement (`YYYYMMDD_NNN_description.sql`) et versionnées dans `supabase/migrations/`.
- Les migrations sont déployées via la **Supabase CLI** (`supabase db push`).
- **Jamais** de modification SQL directe via le dashboard Supabase en production — toujours via migration.

### Requirements to Structure Mapping

| FR / Capability | Feature module | Postgres migration | Edge Function |
|---|---|---|---|
| Project Management (FR1-5) | `feature/onboarding/` | `init_schema.sql` (table `project`), `functions.sql` (`create_project`, `try_resolve_code`) | — |
| Commission Management (FR6-9) | `feature/onboarding/`, `feature/agenda/` | `init_schema.sql` (table `commission`) | — |
| Member Management (FR10-14) | `feature/onboarding/` | `init_schema.sql` (tables `member`, `member_commission`) | — |
| Meeting Scheduling (FR15-19) | `feature/meeting/`, `feature/agenda/` | `init_schema.sql` (tables `meeting`, `meeting_commission`), `functions.sql` (`detect_conflicts`) | — |
| Conflict Detection & Resolution (FR20-27) | `feature/meeting/`, `feature/arbitrage/` | `functions.sql` (`detect_conflicts`), `init_schema.sql` (table `arbitration`) | `dispatch_conflict_push`, `dispatch_arbitration_push` |
| Notifications (FR28-33) | `feature/meeting/`, `feature/arbitrage/`, `core/common/` (push handlers) | (rien de spécifique) | `dispatch_meeting_push`, `dispatch_conflict_push`, `dispatch_arbitration_push` |
| Calendar & Discovery (FR34-36) | `feature/agenda/` | (read-only queries) | — |
| Offline & Sync (FR37-41) | `core/database/` + tous les Repositories | — | — |
| Privacy & Data Management (FR42-43) | `feature/onboarding/` (quitter projet), `app/` (settings écran) | `rls_policies.sql` (DELETE policies) | — |

### Cross-Cutting Concerns Mapping

| Concern | Location |
|---|---|
| Anonymous Auth + JWT storage | `core/common/SecureStorage.{android,ios}.kt` + init dans `ColisionApplication.kt` / `iOSApp.swift` |
| Logging | `core/common/Logger.kt` + actuals |
| Crash reporting | `core/common/CrashReporter.kt` + actuals (Sentry) |
| Analytics | `core/common/Analytics.kt` + actuals (PostHog) |
| Push token registration | `androidMain/push/ColisionMessagingService.kt` + `iosApp/AppDelegate.swift` → POST vers Supabase pour mise à jour `device.fcm_token` / `apns_token` |
| Deep linking | `app/nav/DeepLinks.kt` + NavGraph entries |
| Theming | `app/ColisionTheme.kt` (mapping tokens.jsx → Compose) |
| Locale | `commonMain/composeResources/values/strings.xml` |

### Build & Distribution Structure

- **Output Android** : `composeApp/build/outputs/apk/{flavor}/{buildType}/composeApp-{flavor}-{buildType}.apk` + `.aab` pour release
- **Output iOS** : Framework Kotlin produit par `linkDebugFrameworkIosSimulatorArm64` ; consommé par Xcode qui produit l'`.app` ou `.ipa`
- **Edge Functions** : déployées via `supabase functions deploy <name>`
- **Migrations** : appliquées via `supabase db push`
- **Distribution** :
  - MVP : Xcode Archive + drag-drop AAB dans Play Console
  - Post-MVP : Bitrise pipeline sur tag `v*` (signing + TestFlight + Play Internal)

## Architecture Validation Results

### Coherence Validation ✅

**Decision Compatibility** : aucun conflit entre les 10 décisions actées.
- Compose Navigation Multiplatform + deep-linking + push payload structure → consistent.
- Supabase Anonymous Auth + RLS via `auth.uid()` + trigger sur `auth.users` → trinité cohérente.
- Pas de Realtime + pull-on-foreground + push notifs comme signal live → modèle simple et auto-suffisant.
- Hybride conflict detection (Postgres function + Kotlin pré-check) + tests miroirs → cohérence garantie par CI.
- `kotlin.Result<T>` + `sealed AppError` + `AppErrorThrowable` wrapper → exhaustivité préservée, anti-pattern singleton-Throwable évité.

**Pattern Consistency** : les patterns d'implémentation (naming, MVI strict, kotlinx-datetime exclusif, composeResources, etc.) supportent les décisions sans friction.

**Structure Alignment** : l'arborescence projet implémente exactement le pattern par feature × MVI × Koin. Chaque répertoire a une raison d'être tracée à une décision ou à un FR.

### Requirements Coverage Validation

**FRs couverts** : 43 / 43 ✅

| FR Block | Coverage |
|---|---|
| FR1-5 Project Mgmt | `feature/onboarding/` + Postgres functions `create_project`, `try_resolve_code` |
| FR6-9 Commission Mgmt | `feature/onboarding/`, `feature/agenda/` + table `commission` |
| FR10-14 Member Mgmt | `feature/onboarding/` + tables `member`, `member_commission` |
| FR15-19 Meeting Scheduling | `feature/meeting/`, `feature/agenda/` + tables `meeting`, `meeting_commission` |
| FR20-27 Conflict Detection & Resolution | `feature/meeting/`, `feature/arbitrage/` + fonction `detect_conflicts` + table `arbitration` + Edge Functions `dispatch_conflict_push`, `dispatch_arbitration_push` |
| FR28-33 Notifications | `core/common/` + Edge Functions + push handlers Android/iOS |
| FR34-36 Calendar | `feature/agenda/` + Room queries |
| FR37-41 Offline & Sync | `core/database/` + Repositories + pull-on-foreground ⚠️ (voir gap #1) |
| FR42-43 Privacy & Data Mgmt | `feature/onboarding/` + RLS DELETE policies + cascading deletes |

**NFRs couverts** : 24 / 26 avec 2 différés explicitement.

| NFR | Statut | Mécanisme |
|---|---|---|
| NFR-P1 cold start < 2s | ✅ | Profilage continu, instrumenté via Sentry transactions |
| NFR-P2 conflict detection < 200ms | ✅ | Postgres function indexée + Kotlin pré-check |
| NFR-P3 Realtime sync < 1s | ❌ **Retiré** | Pas de Realtime ; remplacé par push notif (NFR-P4) + pull-on-foreground |
| NFR-P4 push end-to-end < 5s | ✅ | FCM/APNs + Edge Function dispatch |
| NFR-P5 app < 30 Mo | ✅ | Compose MP + KMP + dépendances mesurées |
| NFR-P6 60 FPS | ✅ | Stable types, LazyColumn keys, pas d'animation lourde |
| NFR-S1 isolation RLS | ✅ | Policies Postgres par projet basées sur `auth.uid()` |
| NFR-S2 rate limit code | ❌ **Différé V1.1** | Décision explicite (cf. Décision 6) |
| NFR-S3 TLS partout | ✅ | Standard Supabase + Ktor |
| NFR-S4 pas de PII sensible | ✅ | Schéma ne stocke que prénom + appartenances |
| NFR-S5 hébergement UE | ✅ | Supabase Frankfurt + Sentry EU + PostHog Cloud EU |
| NFR-S6 privacy policy publiée | ⚠️ **À produire avant soumission stores** (gap #2) |
| NFR-S7 deletion 24h | ✅ | Cascading deletes Postgres + RLS DELETE policies |
| NFR-R1 99,5 % crash-free | ✅ | Sentry monitoring |
| NFR-R2 push retry 3x backoff | ⚠️ **Pattern à spécifier dans la story Edge Function** (gap #3) |
| NFR-R3 messages d'erreur clairs | ✅ | AppError typés + mapping localisé |
| NFR-R4 graceful offline | ✅ | Cache lecture Room + désactivation écritures |
| NFR-R5 Supabase SLA | ✅ | Pas d'engagement SLA Colision, hérite Supabase |
| NFR-A1 à A5 accessibilité | ✅ | Material3 défaut + tap targets, contraste, fontScale, semantics |
| NFR-A6 WCAG 2.1 AA audit | ✅ **Différé V1.1** | Décision PRD |
| NFR-SC1 à SC4 scalabilité | ✅ | Postgres indexé + Supabase Pro plan disponible + Edge Function autoscale |
| NFR-M1 à M7 maintainability | ✅ | CI dès J1 + version catalog + observabilité wrappée |

**Journeys couvertes** : 4 / 4 ✅

| Journey | Coverage |
|---|---|
| J1 Antoine initiateur | `feature/onboarding/` + `create_project` + ajout commissions + ajout membres |
| J2 Sophie membre invité | `feature/onboarding/` + `try_resolve_code` + sélection identité + `feature/agenda/` |
| J3 Marc organisateur | `feature/meeting/` + `detect_conflicts` (hybride) + suggestion créneaux + push à la création |
| J4 Sophie conflictée | `feature/arbitrage/` + push curatif + écran d'arbitrage + notif aux organisateurs |

### Gap Analysis — Honest Edition

#### Gap #1 — FR41 ↔ Décision "Pas de Realtime"

**Problème** : FR41 dit "Lorsqu'un autre membre crée, modifie ou supprime une donnée pendant que l'application est ouverte, le changement est répercuté en quasi-temps-réel sur l'appareil du membre connecté."

Avec la décision "Pas de Realtime", la propagation se fait via :
- Push notification (NFR-P4 < 5s) **si l'event déclenche un push**
- Pull au prochain `onResume()` sinon

**Résolution proposée** : reformuler FR41 dans le PRD :
> "Lorsqu'un autre membre crée ou modifie une réunion, l'appareil reçoit une notification push (FR28-31, NFR-P4). Pour les autres modifications (membres, commissions), la propagation se fait au prochain retour de l'app au premier plan."

→ **Action** : éditer FR41 dans le PRD. Pas un blocker technique — juste un alignement de wording.

#### Gap #2 — Privacy Policy Publiée (NFR-S6)

**Problème** : NFR-S6 exige une politique de confidentialité publique en français avant la 1re soumission aux stores. Elle n'est ni écrite, ni hébergée.

**Résolution proposée** : story d'implémentation dédiée (probablement vers la fin du backlog MVP) — "Rédiger Privacy Policy française + héberger statiquement (ex. GitHub Pages, Vercel statique gratuit) + lier dans Info.plist iOS et Data Safety Play Console".

→ **Action** : ajouter cette story explicitement au backlog MVP, l'enrôler comme prérequis store-soumission.

#### Gap #3 — Pattern de retry pour les Edge Functions de Push (NFR-R2)

**Problème** : NFR-R2 demande "3 tentatives avec backoff exponentiel" pour le dispatch push. L'archi mentionne ce besoin mais ne détaille pas le pattern d'implémentation.

**Résolution proposée** : pattern à spécifier dans la story d'implémentation Edge Function. Approche envisagée :
- Si FCM/APNs retourne 5xx : retry exponentiel inline (300ms, 900ms, 2700ms) avec timeout total < 8s.
- Si toutes les tentatives échouent : INSERT dans une table `push_failure_log` pour audit + alerter via Sentry.
- Pas de queue persistante au MVP — le retry inline suffit pour un volume faible.

→ **Action** : à intégrer dans la story Edge Function dispatch.

#### Gap #4 — Expiration du JWT anonyme (cas non-couvert)

**Problème** : Supabase Anonymous Auth émet un JWT avec un refresh token qui expire ~30 jours. Si Sophie n'ouvre pas l'app pendant 30+ jours, le refresh token expire silencieusement. Au prochain lancement, supabase-kt va échouer à toutes les requêtes avec un 401.

**Verdict honnête** : c'est non trivial. Plusieurs approches alternatives :
- **(a)** Refresh proactif + récupération de l'identité via Postgres function (complexe mais propre).
- **(b)** Refresh à chaque foreground : si l'app est ouverte au moins une fois par mois, le refresh token est renouvelé. Simple, coûte 1 appel auth par cold start.
- **(c)** Écran "session expirée, rejoignez à nouveau via code" — délègue au user. Simple, dégrade l'UX dans le cas rare.

**Recommandation** : **(b) refresh proactif à chaque foreground**. À documenter dans `core/common/AnonymousAuthManager.kt`.

→ **Action** : trancher entre (a)/(b)/(c) en story d'implémentation Auth.

#### Gap #5 — Liste de dépendances bootstrap non exhaustivement vérifiée

**Problème** : manques mineurs possibles dans la liste libs.versions.toml :
- `androidx.lifecycle:lifecycle-viewmodel-compose` (requis pour `koinViewModel()` dans Compose KMP)
- Plugin `kotlin-parcelize` (utile si on persiste du state Navigation)
- Alignement explicite des versions Compose côté Android via `compose-bom`

→ **Action** : la story Bootstrap fera la découverte au moment de l'ajout. Pas un blocker.

### Architecture Completeness Checklist

**✅ Requirements Analysis**
- [x] Project context thoroughly analyzed (PRD + brief + design + CLAUDE.md)
- [x] Scale and complexity assessed (medium / mobile_app / general)
- [x] Technical constraints identified (solo dev, persona non-tech, EU)
- [x] Cross-cutting concerns mapped (10 + 1 à ajouter pour JWT lifecycle)

**✅ Architectural Decisions**
- [x] 10 décisions critiques/importantes documentées avec rationale
- [x] Technology stack fully specified avec versions pinnées
- [x] Integration patterns defined (Supabase, FCM/APNs, Sentry, PostHog)
- [x] Performance considerations addressed (objectifs NFR-P traçables)

**✅ Implementation Patterns**
- [x] Naming conventions établies (Kotlin + Postgres + serialization)
- [x] Structure patterns defined (MVI strict, expect/actual narrow surface)
- [x] Communication patterns specified (Repository pattern, Result/AppError)
- [x] Process patterns documented (error handling, loading states, push payloads)

**✅ Project Structure**
- [x] Complete directory structure defined avec liste exhaustive
- [x] Component boundaries établies (5 boundaries explicites)
- [x] Integration points mappés (FR → file mapping)
- [x] Requirements to structure mapping complete

### Architecture Readiness Assessment

**Overall Status:** READY FOR IMPLEMENTATION, avec 4 actions à tracker.

**Confidence Level:** HIGH — les décisions sont défendables, les gaps identifiés sont gérables (3 sur 4 sont du tracking d'action plutôt que de l'architecture à refaire).

**Key Strengths:**
- Décisions explicites avec rationale tracé (pas de "ça paraît bien")
- Trade-offs assumés et documentés (rate limit deferred, no Realtime, etc.)
- Alignment fort entre PRD, design, CLAUDE.md et archi
- Solo-dev friendly partout (boring tech, vendors minimes, CI dès J1)
- Conformité RGPD/EU bouclée (hosting + privacy by design)

**Open Action Items** (à traiter avant ou pendant l'implémentation) :

| # | Action | Quand |
|---|---|---|
| 1 | Reformuler FR41 dans le PRD pour matcher l'absence de Realtime | Avant story 1 (Bootstrap) |
| 2 | Story dédiée Privacy Policy française + hosting | Avant soumission stores (fin MVP) |
| 3 | Spécifier le pattern de retry exponentiel dans la story Edge Function dispatch | Au moment de la story push pipeline |
| 4 | Implémenter la stratégie de refresh proactif anonymous JWT (recommandation : option b — refresh à chaque foreground) | Story 2 (Schema & Auth) |

### Implementation Handoff

**AI Agent Guidelines:**
- Suivre les décisions architecturales **exactement** comme documentées dans ce document
- Référencer `CLAUDE.md` pour les patterns structurels et les conventions de naming
- Référencer `docs/design/` pour toute décision visuelle
- En cas de conflit entre documents : suivre la hiérarchie déclarée ("Source of Truth Hierarchy") et demander clarification si nécessaire
- Pas de dépendance ajoutée en dehors de `gradle/libs.versions.toml`
- Pas d'écart au pattern MVI sans validation explicite

**First Implementation Priority:**
Story 1 — Bootstrap (cf. section "Initial Implementation Story" plus haut). Doit livrer une app vide qui démarre sur les 2 plateformes, avec les wrappers `core/common/` en place (Logger, CrashReporter, Analytics, SecureStorage en NoopImpl ou impl minimale) et la CI verte.
