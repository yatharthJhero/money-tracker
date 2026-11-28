# Tech stack

Every tool and library below is free and open source. Under D-005 (zero investment), any new entry must list its licence and cost before it is added.

Versions were verified on 2026-11-28 from the Gradle files and Gradle's dependency reports. "Resolved" is the version Gradle actually uses when a transitive dependency asks for a newer one than the catalog declares.

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
| minSdk | 24 | `app/build.gradle.kts` | Room 2.8 requires 23+. No `java.time` (API 26) and no desugaring; dates use `java.util` (D-008). |

## App libraries

| Library | Declared | Resolved | Purpose | Licence | Cost |
|---|---|---|---|---|---|
| Jetpack Compose (BOM) | 2026.02.01 | Compose UI/foundation 1.10.4, Material 3 1.4.0 | UI, DatePicker, chips | Apache 2.0 | ₹0 |
| AndroidX Core KTX | 1.10.1 | 1.10.1 | Android basics | Apache 2.0 | ₹0 |
| Activity Compose | 1.8.0 | 1.8.2 | `setContent`, `by viewModels` | Apache 2.0 | ₹0 |
| Lifecycle: `runtime-ktx`, `viewmodel-ktx`, `runtime-compose` | 2.6.1 (`lifecycleRuntimeKtx`) | **2.9.4** (required by Compose 1.10.4) | `ViewModel`, `viewModelScope`, `collectAsStateWithLifecycle` | Apache 2.0 | ₹0 |
| Room (`room-runtime`, `room-compiler`) | 2.8.5 | 2.8.5 | On-device SQLite persistence | Apache 2.0 | ₹0 |
| Room Gradle plugin (`androidx.room`) | 2.8.5 | 2.8.5 | Schema export to `app/schemas/` | Apache 2.0 | ₹0 |
| KSP (`com.google.devtools.ksp`) | 2.3.6 | 2.3.6 | Runs Room's annotation processor | Apache 2.0 | ₹0 |
| kotlinx-coroutines | (transitive) | 1.9.0 | `suspend` / `Flow` / `StateFlow` | Apache 2.0 | ₹0 |

Notes:
- `lifecycle-viewmodel-ktx` and `lifecycle-runtime-compose` were added explicitly on 2026-11-28, because the app now imports them directly. They were already on the classpath at 2.9.4 through Compose, so no resolved version changed (D-010).
- Why Room 2.8.5 and KSP 2.3.6, and when to change them, is in D-006. In short, KSP must be 2.3.1+ for AGP 9 built-in Kotlin, and KSP 2.3.7+ targets Kotlin 2.3.20+, which this project does not use yet.

## Testing

| Library | Version | Used for | Licence |
|---|---|---|---|
| JUnit 4 | 4.13.2 | JVM and instrumented tests | EPL 1.0 |
| kotlinx-coroutines-test | 1.9.0 (test only) | `runTest`, `Dispatchers.setMain` for ViewModel tests | Apache 2.0 |
| AndroidX Test ext JUnit | 1.3.0 | `AndroidJUnit4` runner | Apache 2.0 |
| Espresso core | 3.7.0 | Required by Compose UI tests; 3.7.0+ works on Android 14+ (D-011) | Apache 2.0 |
| Compose UI test (`ui-test-junit4`, `ui-test-manifest`) | via BOM (1.10.4) | Compose UI tests | Apache 2.0 |

- Keep `kotlinxCoroutines` in the catalog equal to the app's resolved coroutines version.
- Room database tests use `Room.inMemoryDatabaseBuilder` from `room-runtime`. Add `androidx.room:room-testing` with the first schema migration.

## Services

None. There is no backend, cloud database, analytics, crash reporting, or paid API. Financial data does not leave the device by default; the database is excluded from cloud backup (D-007).

## Useful commands (Windows, project root)

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest   # build + JVM unit tests
.\gradlew.bat connectedDebugAndroidTest         # instrumented + Compose UI tests (needs a running emulator/device)
.\gradlew.bat lintDebug                         # Android Lint
.\gradlew.bat installDebug                      # install the debug app on the running emulator/device
.\gradlew.bat buildEnvironment                  # shows plugin versions, incl. kotlin-gradle-plugin

# Run one instrumented test class:
.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.yatha.moneytracker.ui.expense.ExpenseScreenTest"
```
