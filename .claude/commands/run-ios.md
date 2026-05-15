---
description: Relink the iOS framework so Xcode picks up the latest Kotlin changes
---

Relink the iOS framework so an already-open Xcode picks up the latest Kotlin code.

1. Run `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`.
2. If it fails, report the first compilation error (file:line + message) and stop.
3. On success, remind the user to **rebuild in Xcode** (⌘B) — Xcode doesn't auto-detect framework changes. Mention that the SwiftUI shell in `iosApp/` should stay thin; any logic change belongs in `commonMain`.