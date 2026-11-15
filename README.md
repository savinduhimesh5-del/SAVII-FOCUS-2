# SAVII FOCUS

An offline-first Android study and focus organiser built with Kotlin and Jetpack Compose.

## Included in this build

- Dashboard with live task and focus totals
- Local subject list (add and remove)
- Task planner (add, complete, filter and delete)
- Focus timer presets: 15, 25, 45 and 60 minutes
- Active timer recovery using the device clock when the app is reopened
- Completed focus sessions saved locally
- Basic insights for today, the last seven local calendar dates and all-time totals
- Dark purple visual theme and a simple launcher icon
- Room database with compile-time query processing
- Unit tests for timer time calculation
- GitHub Actions workflow for tests, Android lint and debug APK build

## Technology baseline

- Android Gradle Plugin 8.13.2
- Gradle 8.13
- Kotlin 2.3.21 with the Compose compiler Gradle plugin
- Compose BOM 2026.09.00
- Room 2.8.5 with KSP 2.3.12
- Activity Compose 1.13.0 (stable)
- Lifecycle 2.12.0
- Java 17, compile/target SDK 36, minimum SDK 26

Versions are pinned rather than using dynamic `+` versions. The Gradle/Kotlin/KSP combination follows the published compatibility guidance and the Compose/Room versions are pinned to stable releases.

## Build

The GitHub Actions workflow installs Gradle 8.13 and Android SDK tooling on its runner, then runs the commands below. This ZIP does not include the Gradle Wrapper JAR, so a local build requires Gradle 8.13 to be installed/configured first. In a local Gradle 8.13 terminal you can generate the standard wrapper with `gradle wrapper --gradle-version 8.13`. Android SDK 36 is also required for a local Android build.

```bash
gradle testDebugUnitTest
gradle lintDebug
gradle assembleDebug
```

The debug APK is produced at:

`app/build/outputs/apk/debug/app-debug.apk`

## Important limitations

This is a functional offline foundation, not a finished commercial release. AI responses, sign-in, cloud sync, multi-device sync, background completion notifications, due dates and reminders are not connected. The timer measures elapsed time from the device clock and saves the session once the app returns to the foreground; it does not send a notification when time ends in the background.

Backup is disabled in the manifest to keep app data local. Uninstalling the app or clearing its storage deletes subjects, tasks and sessions. Export/restore should be added before relying on the app as the only copy of important study records.

No software can honestly be guaranteed bug-free. The unit tests in this project cover timer calculations only. Full Android build, lint and tests must pass in GitHub Actions, and the app must then be exercised on a real Redmi Note 14 before being treated as release-ready.
