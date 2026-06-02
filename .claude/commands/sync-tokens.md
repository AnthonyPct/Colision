---
description: Sync Kotlin design tokens (ColorScheme, Typography, spacing) with docs/design/project/tokens.jsx
---

Synchronize the Kotlin design tokens in `core/design/` with `docs/design/project/tokens.jsx`.

1. Read `docs/design/project/tokens.jsx` end-to-end.
2. Read the current Kotlin token files in `composeApp/src/commonMain/kotlin/com/anthooop/colision/core/design/` (Color.kt, Theme.kt, Typography.kt, Shapes.kt — create them if missing).
3. Build a diff table: token name → current Kotlin value → JSX value → action (add / update / delete). Show this table to the user.
4. Wait for confirmation before editing. The design is the source of truth — if Kotlin diverges, Kotlin loses.
5. After confirmation, apply edits. Map color tokens to Material3 `ColorScheme` slots (primary, onPrimary, surface, etc.) following the M3-aligned naming already in the JSX. Light + dark schemes must both be covered. Typography uses DM Sans across all styles.
6. Compile-check with `./gradlew :composeApp:compileDevelopmentDebugKotlinAndroid` and report errors.