---
description: Build + install the development flavor on the connected Android device/emulator
---

Build and install the development debug APK on the connected Android device.

1. Run `adb devices` to confirm a device is connected. If none, tell the user to plug a device or start an emulator and stop.
2. Run `./gradlew :composeApp:installDevelopmentDebug`.
3. If the build fails, report the first compilation error with file:line and the error message — don't dump the whole log.
4. On success, launch the app with `adb shell monkey -p com.anthooop.colision.dev -c android.intent.category.LAUNCHER 1` and report the device + flavor it was installed on.