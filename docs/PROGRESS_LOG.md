# Progress log

## 2026-11-26
- [x] Developer profile recorded: intermediate, Windows, Kotlin, students and working professionals, 15–25 hours/week.
- [x] Decision made to start coding while running lightweight validation in parallel.
- [x] Starter documentation prepared.
- [ ] GitHub repository created.
- [ ] Android Studio project created and run.
- [ ] First feature implemented.

## 2026-11-27
- [x] GitHub repository `yatharthJhero/money-tracker` exists; history preserved (`0eaffe5`, `4396ffc`).
- [x] Android Studio project (Empty Activity, Compose) created: package `com.yatha.moneytracker`, minSdk 24, targetSdk 37.
- [x] Android project and documentation consolidated into this single repository, stored outside OneDrive.
- [x] Pre-repository planning notes archived verbatim in `docs/planning/`.
- [x] Verified in the consolidated location: `gradlew.bat assembleDebug testDebugUnitTest` — BUILD SUCCESSFUL; 1 unit test (template `ExampleUnitTest`) passed, 0 failed.
- Toolchain at this point: AGP 9.5.3, Gradle 9.6.0, Kotlin 2.2.10, Compose BOM 2026.02.01, Android Studio bundled JDK (JBR) 25.
- [ ] App launched on an emulator or device from the consolidated location (manual check pending).

## 2026-11-28 — Room persistence (branch `feature/room-persistence`)
- [x] Baseline re-verified before changes: the domain model from commit `2c12d98` (`Expense`, `ExpenseCategory`) passes. `gradlew.bat :app:testDebugUnitTest --rerun` gave 7/7 passing (`ExampleUnitTest` 1, `ExpenseTest` 6).
- [x] Zero-investment constraint recorded as D-005.
- [x] Room **2.8.5** and KSP **2.3.6** added through the version catalog, plus the `androidx.room` Gradle plugin for schema export (D-006).
  - The Kotlin Gradle plugin and compiler remain **2.2.10**, and the runtime `kotlin-stdlib` remains 2.2.10. Checked with `buildEnvironment` and the `kotlinCompilerClasspath` and `debugRuntimeClasspath` dependency reports.
  - No existing dependency version was changed.
- [x] Room data layer implemented in `data/local/` (`ExpenseEntity`, `ExpenseDao`, `MoneyTrackerDatabase`, `ExpenseMapper`) and `data/repository/` (`ExpenseRepository`).
  - Covers insert, read, update and delete, plus a `Flow` list ordered by `transaction_date DESC, id DESC`.
  - No destructive migration fallback.
- [x] Schema version 1 exported to `app/schemas/com.yatha.moneytracker.data.local.MoneyTrackerDatabase/1.json`.
- [x] Database excluded from Android cloud backup (D-007, awaiting developer confirmation).
- [x] Verified with `gradlew.bat assembleDebug :app:testDebugUnitTest :app:connectedDebugAndroidTest :app:lintDebug`: BUILD SUCCESSFUL.
  - JVM unit tests: **18/18 passed** (`ExampleUnitTest` 1, `ExpenseTest` 6, `ExpenseMapperTest` 11).
  - Instrumented tests on the `Pixel_7` AVD (API 37): **24/24 passed** (`ExampleInstrumentedTest` 1, `ExpenseDaoTest` 13, `ExpenseRepositoryTest` 10).
  - Lint: 0 errors, 16 warnings. All 16 are on pre-existing template content: newer versions available for existing dependencies, unused template colours, a redundant manifest label, and a lifecycle lint-jar notice.
- [x] Mutation check: three deliberate bugs, each caught by the tests, then reverted.
  - Swapping `transactionDate`/`createdAt` in the mapper: caught.
  - Storing the category ordinal: caught.
  - Reversing the sort order: caught.
- [ ] Instrumented tests have only been run on API 37, not on a minSdk 24 device or emulator.
- [ ] Cloud-backup exclusion not exercised on a device (only validated by lint).
- [ ] Persistence is not yet used by any screen or ViewModel.

No user-facing feature is implemented yet: the UI is still the template greeting. Update this log only after work is actually completed and verified.
