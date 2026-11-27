# Tech stack

Every tool and library below is free and open source. Under D-005 (zero investment), any new entry must list its licence and cost before it is added.

Versions were verified on 2026-11-28 from the Gradle files and Gradle's dependency reports.

## Toolchain

| Item | Version | Where it is set | Notes |
|---|---|---|---|
| Android Studio bundled JDK (JBR) | 25 | Android Studio / `gradle/gradle-daemon-jvm.properties` | Use the bundled JDK; do not install a separate one. |
| Gradle | 9.6.0 | `gradle/wrapper/gradle-wrapper.properties` | |
| Android Gradle Plugin (AGP) | 9.5.3 | `gradle/libs.versions.toml` (`agp`) | Uses **built-in Kotlin**: the app does not apply `org.jetbrains.kotlin.android`. |
| Kotlin Gradle plugin / compiler | 2.2.10 | AGP 9.5.3 default, aligned with `kotlin` in the catalog | This is the compiler that builds the app. Gradle's own embedded Kotlin (2.3.21, shown by `gradlew --version`) only compiles build scripts. |
| Compose compiler plugin | 2.2.10 | catalog `kotlin` | Must match the Kotlin Gradle plugin version. |
| Java source/target compatibility | 11 | `app/build.gradle.kts` | |
| compileSdk / targetSdk | 37 / 37 | `app/build.gradle.kts` | |
| minSdk | 24 | `app/build.gradle.kts` | Room 2.8 requires 23+. |

## Libraries

| Library | Version | Purpose | Licence | Cost |
|---|---|---|---|---|
| Jetpack Compose (BOM) | 2026.02.01 | UI | Apache 2.0 | ₹0 |
| AndroidX Core KTX / Activity Compose / Lifecycle | 1.10.1 / 1.8.0 / 2.6.1 | Android basics | Apache 2.0 | ₹0 |
| Room (`room-runtime`, `room-compiler`) | 2.8.5 | On-device SQLite persistence | Apache 2.0 | ₹0 |
| Room Gradle plugin (`androidx.room`) | 2.8.5 | Schema export to `app/schemas/` | Apache 2.0 | ₹0 |
| KSP (`com.google.devtools.ksp`) | 2.3.6 | Runs Room's annotation processor | Apache 2.0 | ₹0 |
| kotlinx-coroutines | 1.9.0 (resolved transitively) | `suspend` / `Flow` | Apache 2.0 | ₹0 |

Why Room 2.8.5 and KSP 2.3.6, and when to change them, is recorded in `DECISIONS.md` D-006. In short:
- KSP must be 2.3.1 or newer for AGP 9 built-in Kotlin.
- Newer KSP releases (2.3.7+) target Kotlin 2.3.20+, which this project does not use yet.

## Testing

| Library | Version | Used for | Licence |
|---|---|---|---|
| JUnit 4 | 4.13.2 | JVM and instrumented tests | EPL 1.0 |
| AndroidX Test ext JUnit | 1.1.5 | `AndroidJUnit4` runner | Apache 2.0 |
| Espresso core | 3.5.1 | Template UI test dependency | Apache 2.0 |
| Compose UI test | via BOM | Not used yet | Apache 2.0 |

Room database tests use `Room.inMemoryDatabaseBuilder` from `room-runtime`; no extra test library is needed yet. Add `androidx.room:room-testing` with the first schema migration.

## Services

None. There is no backend, cloud database, analytics, crash reporting, or paid API. Financial data does not leave the device by default; the database is excluded from cloud backup (D-007).

## Useful commands (Windows, project root)

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest   # build + JVM unit tests
.\gradlew.bat connectedDebugAndroidTest         # instrumented tests (needs a running emulator/device)
.\gradlew.bat lintDebug                         # Android Lint
.\gradlew.bat buildEnvironment                  # shows plugin versions, incl. kotlin-gradle-plugin
```
