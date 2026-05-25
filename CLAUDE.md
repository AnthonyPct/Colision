# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Kotlin Multiplatform / Compose Multiplatform project targeting **Android** and **iOS** (arm64 device + arm64 simulator). Application id / namespace: `com.anthooop.colision`. Single Gradle module: `:composeApp`.

Toolchain: Kotlin 2.3.21, AGP 8.11.2, Compose Multiplatform 1.10.3, Material3 1.10.0-alpha05, JVM target 11, Android `compileSdk`/`targetSdk` 36, `minSdk` 28. Versions are centralized in `gradle/libs.versions.toml` (version catalog) — change versions there, not in module build files. `settings.gradle.kts` enables `TYPESAFE_PROJECT_ACCESSORS`.

## Product context

Colision is a scheduling app that detects cross-commission meeting conflicts. Primary persona (Sophie) has **near-zero tech literacy** — the UX bar is "simpler than WhatsApp".

When implementing, optimize for: minimal taps, no jargon, generous touch targets, immediate feedback. Reject any flow that would confuse a non-technical user, even if it's "the Android way".

## Source-of-truth documentation — read before implementing

**Every feature, sub-screen, or non-trivial change must be grounded in these documents.** Read them in this order before writing code or proposing an approach:

1. **`docs/product-brief-Colision-2026-05-10.md`** — product vision, persona (Sophie), problem framing. Read once per session to anchor decisions.
2. **`docs/prd.md`** — current PRD: scope, requirements, user journeys (J1–J4), acceptance criteria, edge cases. **Authoritative source for what the feature should do.** If the request contradicts the PRD, flag it before implementing.
3. **User stories / epics** — currently embedded in `docs/prd.md`. When BMAD splits stories into separate files (under `docs/` per BMAD output convention), read the relevant story file end-to-end before touching code.
4. **`docs/architecture.md`** — current architectural decisions and constraints. Anything you build must comply with the patterns documented there; if you need to deviate, surface it explicitly.
5. **`docs/design/`** — visual + interaction contract. Specifically:
   - `docs/design/chats/chat1.md` — design intent and rationale (read once per session).
   - `docs/design/project/tokens.jsx` — colors, typography, spacing.
   - `docs/design/project/components.jsx` — shared components.
   - `docs/design/project/screens-<feature>.jsx` — the screens of the feature you're working on.

If any of these documents are missing context the task needs (a sub-screen not in the design, an unclear acceptance criterion, conflicting copy between PRD and design), **stop and ask the user** rather than guessing. The cost of one clarifying question is far less than building the wrong thing.

When you write or update a feature, the resulting code must be traceable back to: a PRD requirement / story (the *what* and *why*), the architecture doc (the *how it fits*), and the design (the *how it looks and behaves*). If you can't point to all three, you're missing context.

## Common commands

```bash
./gradlew :composeApp:assembleDevelopmentDebug    # Android debug APK (development flavor)
./gradlew :composeApp:assembleProductionDebug     # Android debug APK (production flavor)
./gradlew :composeApp:installDevelopmentDebug     # Install dev flavor on device
./gradlew :composeApp:check                       # Lint + tests for all targets
./gradlew :composeApp:testDebugUnitTest           # Android unit tests
./gradlew :composeApp:iosSimulatorArm64Test       # commonTest on iOS simulator
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64  # iOS framework for Xcode
```

Run a single Kotlin test: `./gradlew :composeApp:testDebugUnitTest --tests "com.anthooop.colision.FooTest.someTest"`.

iOS app: open `iosApp/iosApp.xcodeproj` in Xcode and run. Xcode's build phase invokes Gradle to produce the `ComposeApp` static framework — Kotlin code changes require an Xcode rebuild (or the explicit `linkDebugFramework…` task above) before they're picked up.

## Flavors

Two product flavors defined in `composeApp/build.gradle.kts`:

- **development** — points at the Supabase staging project, verbose logging, debug overlays allowed, suffix `.dev` on the applicationId.
- **production** — production Supabase project, logs stripped, no debug UI.

Flavor-specific config (Supabase URL/anon key, FCM sender, feature flags) lives in `composeApp/src/{development,production}/kotlin/com/anthooop/colision/config/BuildConfig.kt` — never hardcode environment values in `commonMain`. iOS reads the same values via an `expect`/`actual` `BuildConfig` exposed through the Kotlin framework.

## Architecture

Single Gradle module `:composeApp`. Standard KMP layout under `composeApp/src/`:

- `commonMain/kotlin/com/anthooop/colision/` — shared Kotlin + Compose UI. `App.kt` is the root `@Composable`; `Platform.kt` declares the `expect` platform abstraction.
- `androidMain/` — `MainActivity.kt` hosts `App()`; Android `actual` impls + Room Android driver + Koin Android module.
- `iosMain/` — `MainViewController.kt` exposes a `ComposeUIViewController { App() }`; iOS `actual` impls + Room iOS driver + Koin iOS module.
- `commonMain/composeResources/` — Compose Multiplatform resources, accessed via `colision.composeapp.generated.resources.Res`. Add all shared assets here, not under platform-specific resource folders.

Follow **Google's Android app architecture** (UI / Domain / Data) adapted to KMP. Code is organized **by feature**, and each feature is split into **sub-screens**, not into a single screen per feature:

```
commonMain/kotlin/com/anthooop/colision/
├── app/                    # App entry, root NavHost, theme
├── core/
│   ├── design/             # ColorScheme, Typography, Shapes, reusable composables
│   ├── network/            # Ktor HttpClient setup, Supabase client
│   ├── database/           # Room database, DAOs base, type converters
│   ├── navigation/         # Destination sealed classes, nav extensions
│   ├── di/                 # Top-level Koin modules wiring
│   └── common/             # Result, dispatchers, utils
└── feature/
    ├── onboarding/
    │   ├── data/                       # OnboardingRepository, remote/local sources (shared across sub-screens)
    │   ├── domain/                     # Use cases (only when logic warrants — don't over-abstract)
    │   ├── navigation/
    │   │   └── OnboardingGraph.kt      # NavGraphBuilder.onboardingGraph(...) extension
    │   ├── welcome/                    # one sub-screen = one folder
    │   │   ├── WelcomeRoute.kt
    │   │   ├── WelcomeScreen.kt
    │   │   ├── WelcomeViewModel.kt
    │   │   └── WelcomeContract.kt
    │   ├── projectcode/                # WelcomeRoute → ProjectCodeRoute → ...
    │   ├── projectconfirm/
    │   ├── identity/
    │   ├── commission/
    │   ├── notifications/
    │   └── di/
    │       └── OnboardingModule.kt     # one Koin module per feature, declares every sub-screen ViewModel
    ├── agenda/                         # same structure, sub-screens TBD
    ├── meeting/
    └── arbitrage/
```

The four `screens-*.jsx` files in `docs/design/` are **categories / user journeys**, not single screens. Each one contains several sub-screens:

- **onboarding** (J1+J2 setup): welcome → projectcode → projectconfirm → identity → commission → notifications.
- **agenda** (J2 ongoing), **meeting** (J3), **arbitrage** (J4) similarly decompose into multiple sub-screens — read the relevant `screens-<category>.jsx` to enumerate them before scaffolding.

Each sub-screen gets its **own** `Route` + `Screen` + `ViewModel` + `Contract`. Do not merge several sub-screens into one ViewModel even if they share data — share via the feature `Repository` instead.

Platform-specific behavior follows the `expect`/`actual` pattern declared in `commonMain`, with implementations in `androidMain` and `iosMain`. Keep the platform surface narrow — most code lives in `commonMain`.

## Navigation — Jetpack Compose Navigation, multi-graph

Use Jetpack Compose Navigation (the KMP-compatible artifact). The root `NavHost` lives in `app/` and switches between **top-level nested graphs**, one per feature:

- `onboardingGraph` — first-run flow (welcome → projectcode → projectconfirm → identity → commission → notifications). Self-contained; on completion it pops itself and navigates to the home graph.
- `homeGraph` — main app once onboarded (`agenda`, `meeting`, `arbitrage` and their sub-screens).

Each feature declares its graph in `feature/<name>/navigation/<Name>Graph.kt` as a `NavGraphBuilder` extension:

```kotlin
fun NavGraphBuilder.onboardingGraph(navController: NavController) {
    navigation<OnboardingGraph>(startDestination = OnboardingDestination.Welcome) {
        composable<OnboardingDestination.Welcome> { WelcomeRoute(...) }
        composable<OnboardingDestination.ProjectCode> { ProjectCodeRoute(...) }
        // ...
    }
}
```

Conventions:

- Destinations are **type-safe** (`@Serializable` data objects / classes), grouped in `feature/<name>/navigation/<Name>Destination.kt`.
- The root `NavHost` decides the start graph based on auth/onboarding state read from a top-level `AppViewModel` — never branch inside a graph.
- Cross-graph navigation goes through the root `NavController` passed into each graph builder. A graph never knows about another graph's internal destinations.
- Inter sub-screen navigation **inside a graph** is exposed by the `Route` via lambdas (`onNavigateToProjectConfirm: (code: String) -> Unit`) — `Screen` composables stay navigation-agnostic.

## MVI pattern (mandatory for every sub-screen)

Each **sub-screen** has exactly four UI artifacts (not one set per feature category):

- **`*ViewModel.kt`** — holds state, coordinates use cases / repositories, exposes:
  - `val state: StateFlow<FooState>` — UI state (immutable data class, single source of truth for the screen)
  - `val events: SharedFlow<FooEvent>` — one-shot side effects (navigation, snackbars, errors)
  - `fun onIntent(intent: FooIntent)` — single entry point for user intents
- **`*Route.kt`** — stateful entry point used by the nav graph. Collects `state` via `collectAsStateWithLifecycle()`, observes `events` in a `LaunchedEffect`, forwards intents to the ViewModel. **No UI logic here** beyond plumbing.
- **`*Screen.kt`** — pure stateless Composable. Receives `state: FooState` and `onIntent: (FooIntent) -> Unit`. Material3 only. Previewable in isolation. No ViewModel, no Koin, no side effects.
- **`*Repository.kt`** — abstracts data sources (Ktor / Supabase client + Room DAO). Returns `Flow<T>` or `Result<T>`. Owns the merge strategy between remote and local.

State/intent/event types live next to the ViewModel: `FooState.kt`, `FooIntent.kt`, `FooEvent.kt` (or grouped in `FooContract.kt` if small).

Rule of thumb: if a Composable needs to call the ViewModel, it should be in the `Route`, not the `Screen`.

### Section block comments inside ViewModels

Every ViewModel body is split into labelled section blocks so the file structure is scannable. Use this exact comment format (three lines of 75 forward slashes wrapping an upper-case label):

```kotlin
///////////////////////////////////////////////////////////////////////////
// UI STATE
///////////////////////////////////////////////////////////////////////////
```

Canonical sections, in this order (omit a section only if it has no content):

1. `UI STATE` — `_state` / `state` declarations.
2. `EVENT` — `_events` / `events` declarations (one-shot side effects).
3. `PUBLIC API` — `onIntent` and any other public entry points.
4. `INIT` — `init { ... }` block wiring observers / kicking off sync.
5. `HELPER` — private functions (state builders, flow plumbing, formatters).

Apply the same convention to other coordinator classes (e.g. `AppViewModel`) — keep the same labels and order, drop sections that don't apply. Don't invent new section names; if something doesn't fit, it usually belongs in `HELPER` or in a separate class.

## Dependency injection — Koin (mandatory)

**Every** non-trivial class is injected via Koin. No manual instantiation of ViewModels, repositories, or clients in production code.

- Module wiring lives in `core/di/AppModule.kt` (top-level) and per-feature `feature/<name>/di/<Name>Module.kt`.
- ViewModels are bound via `viewModelOf(::FooViewModel)` and retrieved in the Route with `koinViewModel()`.
- Platform-specific bindings (Android `Context`, iOS `NSObject` helpers, Room driver) go in `androidMain` / `iosMain` modules registered alongside the common module.
- Koin is started in `MainActivity` (Android) and `MainViewController` (iOS) with `startKoin { modules(appModule + platformModule + featureModules) }`.

When adding a new feature, register its module in the feature module list — don't load Koin modules ad-hoc.

## Networking — Ktor

All HTTP goes through a single `HttpClient` configured in `core/network/`. Use `ContentNegotiation` + `kotlinx.serialization`, install `Logging` only on the `development` flavor, and let Koin inject the client into repositories.

For Supabase, use the `supabase-kt` client (auth, postgrest, realtime, storage) injected via Koin. Do not call Supabase REST endpoints directly with raw Ktor — always go through the supabase-kt modules so RLS and auth headers are handled.

## Local persistence — Room (KMP)

Use Room with KMP support. The database, DAOs, and entities live in `core/database/` in `commonMain`. Platform drivers (`BundledSQLiteDriver` on Android, the iOS Room driver) are provided in `androidMain` / `iosMain` Koin modules.

Room is the offline cache and source of truth for what the UI displays. Repositories expose `Flow<T>` from the DAO and refresh from Supabase in the background.

## Design contract — non-negotiable

The visual reference is in `docs/design/` (HTML/JSX prototypes — pixel-accurate spec, not to be rendered, read the source):

- `docs/design/project/tokens.jsx` — single source of truth for colors, typography (DM Sans), spacing (4-based scale), shapes. Map this into a `ColorScheme` + `Typography` in `core/design/` before implementing any screen.
- `docs/design/project/components.jsx` — shared components used across screens.
- `docs/design/project/screens-{onboarding,agenda,meeting,arbitrage}.jsx` — four screens, one per user journey.
- `docs/design/chats/chat1.md` — design intent and rationale; read before opening individual screen files.

**Before implementing any UI**: read the relevant `screens-*.jsx` and check the tokens. Every feature implementation must match the design — colors, spacing, typography, copy, layout. Theme: warm + civic, off-white `#FAF7F2`, forest green `#0E7C66` primary, coral `#C8553D` for conflicts. Light + dark modes.

Don't render the HTML in a browser — everything (dimensions, layout, copy) is spelled out in the source.

## Internationalization (i18n) — mandatory

Every user-visible string must be declared in `composeApp/src/commonMain/composeResources/values/strings.xml` (plurals in `plurals.xml`) and consumed via Compose Multiplatform Resources — `stringResource(Res.string.xxx)` — never as a raw Kotlin literal in a `Screen`, `Route`, `ViewModel`, or any composable that ends up on screen. The `Res` symbol is `colision.composeapp.generated.resources.Res`.

Conventions:

- Group keys by screen with an XML comment header (e.g. `<!-- WelcomeScreen -->`) and prefix keys with the screen slug: `welcome_title`, `create_project_name_label`, `agenda_empty_state`, etc. Shared/cross-screen wording goes under `<!-- Common -->` with an `action_` / `dialog_` / etc. prefix.
- Escape apostrophes (`\'`) and use `\n` for explicit line breaks — see existing entries.
- Use `pluralStringResource` + `plurals.xml` for any count-dependent copy; don't fake plurals with string concatenation.
- Errors, snackbars, and content descriptions count as user-visible — they must be in `strings.xml` too. `ViewModel`s should emit a string **key/id** (or a typed event), and the `Screen` resolves it with `stringResource`. Don't hardcode French copy in the ViewModel.

**Exception — `@Preview` composables only**: hardcoded sample strings used purely to seed a `*Preview` function are allowed (they never ship to users). Anything outside a `@Preview` block must go through `strings.xml`.

## BMAD planning artifacts

All BMAD outputs live under `docs/` (not `_bmad-output/`). PRD, product brief, and any workflow status files are kept there.
