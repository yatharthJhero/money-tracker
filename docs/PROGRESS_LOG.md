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

## 2026-11-28 — Room milestone committed; expense entry (branch `feature/expense-entry`)
- [x] D-007 accepted by the developer.
- [x] Room milestone re-verified, then committed as `4e7ff03` ("feat: implement local expense persistence with Room") and pushed to `origin/feature/room-persistence`.
  - Re-verification: build OK, JVM 18/18, instrumented 24/24, lint 0 errors.
  - Not merged into `main`.
- [x] Branch `feature/expense-entry` created from `4e7ff03`.
- [x] Money input parsing: `RupeeAmountParser` (text → paise, `BigDecimal`, no floating point, ₹99,99,99,999.99 cap; D-009) and `MoneyFormatter` (paise → `₹1,23,456.78`).
- [x] Dates: `CalendarDate` (API 24-safe). The chosen date is stored as 12:00 local time (D-008).
- [x] `ExpenseViewModel` with `StateFlow` form and list state, `viewModelScope`, validation, duplicate-save guard, keep-input-on-failure, and manual wiring in `MainActivity` (D-010).
- [x] Compose screen replaces the template greeting:
  - Amount (₹, INR) and category chips.
  - Optional description with a counter.
  - Date picker (today or earlier).
  - Save button with a progress state, and snackbar feedback.
  - Transaction list (`LazyColumn`, keys = expense id) with an empty state.
- [x] Dependencies:
  - Explicit `lifecycle-viewmodel-ktx` and `lifecycle-runtime-compose`, which were already resolved to 2.9.4.
  - Test-only `kotlinx-coroutines-test` 1.9.0.
  - Test-only upgrade to Espresso 3.7.0 / ext.junit 1.3.0, needed for Compose UI tests on API 34+ (D-011).
  - App runtime versions unchanged (checked with dependency reports).
- [x] Verified with `gradlew.bat assembleDebug :app:testDebugUnitTest --rerun :app:connectedDebugAndroidTest :app:lintDebug --rerun`: BUILD SUCCESSFUL.
  - JVM unit tests: **72/72 passed**: `ExampleUnitTest` 1, `ExpenseTest` 6, `ExpenseMapperTest` 11, `RupeeAmountParserTest` 15, `MoneyFormatterTest` 5, `CalendarDateTest` 9, `ExpenseFormValidationTest` 9, `ExpenseViewModelTest` 16.
  - Instrumented tests on the `Pixel_7` AVD (API 37): **30/30 passed**: `ExampleInstrumentedTest` 1, `ExpenseDaoTest` 13, `ExpenseRepositoryTest` 10, `ExpenseScreenTest` 6.
  - Lint: 0 errors, 17 warnings (dependency-version notices, unused template colours, a redundant manifest label, a lifecycle lint-jar notice).
- [x] `ExpenseScreenTest` stability:
  - One run failed: the Save tap missed while the soft keyboard was open.
  - The test now scrolls each control into view before interacting.
  - It then passed 3 consecutive isolated runs, plus the final full run.
- [x] Mutation check: six deliberate bugs, all caught by JVM tests (21 failures), then reverted.
  - Allowing 3 decimal places; removing the duplicate-save guard; wiping input on a failed save.
  - Storing midnight instead of noon; using Western instead of Indian digit grouping; treating today as a future date.
- [x] Emulator check by Claude, driving the installed debug app through `adb` (synthetic test data; app data cleared afterwards):
  - Empty-form validation messages.
  - Saved ₹125.50 / Food / description: form cleared, "Expense saved." shown, row displayed.
  - Date picker disables future days; saved ₹40 / Transport dated 25 Nov; list ordered newest transaction first.
  - After `am force-stop` and a cold relaunch, all expenses were still listed (persistence verified).
  - A rapid double tap on Save created exactly one expense.
- [ ] Not yet checked by the developer by hand. Not checked: screen rotation, a minSdk 24 device or emulator, dark theme, large font sizes, TalkBack.
- [ ] Expense-entry work is not committed yet (awaiting developer approval).

## 2026-11-28 — Expense entry: developer manual testing and final verification
- [x] Manual checklist completed by the developer by hand. All 10 items PASS:
  1. Application launch
  2. Empty-form validation
  3. Invalid-amount validation: **zero** and **three decimal places** only. Other invalid inputs (negative, malformed, too large) were **not** tested by hand; they are covered by automated tests below.
  4. First expense saved
  5. Second expense saved
  6. Transaction ordering (newest first)
  7. Data persistence (after closing and reopening the app)
  8. Screen rotation
  9. Dark mode
  10. Overall user experience
- [x] More automated tests for invalid amounts, and for invalid submissions not creating database records:
  - `RupeeAmountParserTest` (+5 tests):
    - negative edge cases (`-0`, `-1.005`, `- 5`, U+2212 minus);
    - whitespace inside the number;
    - other digit and separator characters (full-width digits, `100/-`, `12,50`);
    - 3 decimal places just above the maximum;
    - inputs of up to 1,000 digits that would overflow a `Long`.
  - `ExpenseViewModelTest` (+1): 18 invalid inputs covering negative, malformed, zero, blank, too many decimals and too large values. Each is rejected with the right error, the typed text is kept, and the DAO receives **0** insert calls.
  - `ExpenseScreenTest` (+1, instrumented): real UI → real ViewModel → in-memory Room database. Negative, malformed, 3-decimal, zero and `92233720368547758.08` inputs each show the right error, and the database holds **0** rows afterwards.
- [x] Verified with `gradlew.bat assembleDebug :app:testDebugUnitTest --rerun :app:connectedDebugAndroidTest :app:lintDebug --rerun`: BUILD SUCCESSFUL.
  - JVM unit tests: **78/78 passed**: `ExampleUnitTest` 1, `ExpenseTest` 6, `ExpenseMapperTest` 11, `RupeeAmountParserTest` 20, `MoneyFormatterTest` 5, `CalendarDateTest` 9, `ExpenseFormValidationTest` 9, `ExpenseViewModelTest` 17.
  - Instrumented tests on the `Pixel_7` AVD (API 37): **31/31 passed**: `ExampleInstrumentedTest` 1, `ExpenseDaoTest` 13, `ExpenseRepositoryTest` 10, `ExpenseScreenTest` 7.
  - Lint: 0 errors, 17 warnings. These are the same 17 as before: 8 dependency-version notices, 7 unused template colours, a redundant manifest label, and a lifecycle lint-jar notice.
- [ ] Still not checked: a minSdk 24 device or emulator, large font sizes, TalkBack.

The app now has its first user-facing feature: add an expense and see it in the list. Update this log only after work is actually completed and verified.
