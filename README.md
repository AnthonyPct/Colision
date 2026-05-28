# Colision

A scheduling app that detects cross-commission meeting conflicts. Kotlin
Multiplatform / Compose Multiplatform targeting **Android** and **iOS**.

The primary persona has near-zero tech literacy — the UX bar is "simpler than
WhatsApp". Build with that in mind.

## Project layout

```
composeApp/
├── src/commonMain/        # Shared Kotlin + Compose UI (~90% of the code)
├── src/androidMain/       # MainActivity, Android-specific actuals, Room driver
├── src/iosMain/           # MainViewController, iOS actuals, Room driver
├── src/{development,production}/
│                          # Flavor-specific BuildConfig.kt (Supabase URLs, etc.)
└── src/commonMain/composeResources/
                           # Shared assets, strings.xml, plurals.xml

iosApp/                    # Xcode project hosting the Kotlin framework
docs/                      # Product, architecture, design, CI docs
```

Single Gradle module: `:composeApp`. iOS code is built into a `ComposeApp` static
framework consumed by the Xcode project.

## Build & run

### Android

```shell
./gradlew :composeApp:installDevelopmentDebug   # install dev flavor on connected device
./gradlew :composeApp:assembleProductionDebug   # production-flavor debug APK
./gradlew :composeApp:check                     # lint + tests, all targets
```

Two product flavors:
- **`development`** — staging Supabase, verbose logging, `.dev` appId suffix
- **`production`** — production Supabase, logs stripped

### iOS

Open `iosApp/iosApp.xcodeproj` in Xcode and run. Xcode's build phase invokes
Gradle to produce the `ComposeApp` static framework — Kotlin changes require an
Xcode rebuild (or `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`)
before they're picked up.

Targets: arm64 device + arm64 simulator. Sentry Cocoa pinned to **8.58.2** via
SPM in `iosApp/iosApp.xcodeproj` (do not bump to 9.x — breaks the
sentry-kotlin-multiplatform plugin's linker integration).

## CI

Two-stage pipeline:

- **GitHub Actions** (`.github/workflows/`) — build verification + tests on
  every push/PR. Free, fast.
- **Bitrise** — release artifacts (signed AAB/APK, IPA), TestFlight/Play
  uploads. Paid macOS minutes, on-demand.

Full operational details — workflows, signing, manual setup of Apple/Google
service connections, Sentry xcframework workaround on CI — in
[`docs/ci.md`](docs/ci.md).

## Documentation map

| Doc | What it covers |
|---|---|
| [`docs/product-brief-Colision-2026-05-10.md`](docs/product-brief-Colision-2026-05-10.md) | Product vision, persona, problem framing |
| [`docs/prd.md`](docs/prd.md) | PRD: scope, requirements, user journeys, acceptance criteria |
| [`docs/epics.md`](docs/epics.md) | Epics & stories breakdown |
| [`docs/architecture.md`](docs/architecture.md) | Architectural decisions, schema, RLS, networking, CI overview |
| [`docs/ci.md`](docs/ci.md) | Bitrise + GitHub Actions operational guide |
| [`docs/design/`](docs/design) | Tokens, components, screens (JSX prototypes — pixel-accurate spec, read the source) |
| [`docs/legal/`](docs/legal) | Privacy policy & legal docs |
| [`CLAUDE.md`](CLAUDE.md) | Project conventions for AI-assisted development |

Read `CLAUDE.md` first if you're contributing — it enumerates the non-negotiable
conventions (MVI per sub-screen, Koin DI, i18n via `strings.xml`, design tokens
mapping, navigation graphs).

## Stack quick reference

- Kotlin 2.3.21, AGP 8.11.2, Compose Multiplatform 1.10.3, Material3 1.10.0-alpha05
- JVM target 11, Android `compileSdk` 36, `minSdk` 28
- DI: Koin (`viewModelOf` + `koinViewModel()`)
- Networking: Ktor + supabase-kt (auth, postgrest, realtime, storage)
- Local persistence: Room (KMP), `BundledSQLiteDriver` Android, iOS Room driver
- Observability: Sentry (crash + product analytics unified, EU region)
- Version catalog: `gradle/libs.versions.toml` — change versions there, not in
  module build files
