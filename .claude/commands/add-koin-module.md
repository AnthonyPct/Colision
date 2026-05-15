---
description: Register a new Koin module in the app DI graph and platform-specific Koin start sites
argument-hint: <ModuleName>
---

Register `$ARGUMENTS` (a Koin `Module` declaration, e.g. `agendaModule`) in the app DI graph.

1. Locate the central feature-module list (typically `core/di/AppModule.kt` or `core/di/FeatureModules.kt`). If it doesn't exist, create `core/di/FeatureModules.kt` exposing `val featureModules = listOf(...)` and wire it into the existing `startKoin { modules(...) }` calls.
2. Add `$ARGUMENTS` to the list. Keep it alphabetically sorted.
3. Verify `MainActivity.kt` (Android) and `MainViewController.kt` (iOS) both consume the updated list — if either calls `startKoin` with hardcoded modules, fix it to use the central list.
4. Run `./gradlew :composeApp:compileDevelopmentDebugKotlinAndroid` to verify it compiles.

Report what was changed and any duplicate / conflicting bindings detected.