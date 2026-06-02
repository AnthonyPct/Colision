---
description: Run lint + tests for both Android and iOS targets, report only failures
---

Run the full check suite and report only what's broken.

1. Run `./gradlew :composeApp:check` (covers Android lint + unit tests + commonTest).
2. Run `./gradlew :composeApp:iosSimulatorArm64Test`.
3. Parse the output. For each failure, report: test class, test name, failure reason, file:line if available. Do not dump full stack traces — link to the report path instead.
4. If everything passes, reply with a single line: `✅ check + iosSimulatorArm64Test green`.

If a build fails before tests run (compilation error), report only the first error and stop — fixing compilation comes before fixing tests.