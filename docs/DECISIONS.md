# Decision log

## D-001 — Android-native Kotlin direction
**Status:** Proposed  
**Reason:** User prefers Kotlin and the first target is Android.  
**Revisit if:** Research or technical constraints show a compelling reason.

## D-002 — Local-first MVP
**Status:** Proposed  
**Reason:** Reduce cost, complexity and exposure of sensitive financial data.  
**Revisit if:** A validated multi-device or backup requirement emerges.

## D-003 — Manual entry before automatic ingestion
**Status:** Proposed  
**Reason:** Establish a working product and validate the core transaction experience before adding unreliable integrations.  
**Revisit if:** User research demonstrates manual entry makes the product unusable and a safe ingestion path is feasible.

## D-004 — Build and validate in parallel
**Status:** Accepted  
**Reason:** User has 15–25 hours/week and placement preparation; a long discovery-only period is not practical.  
**Guardrail:** Keep early scope small and conduct lightweight interviews / feedback sessions alongside implementation.

## D-005 — Zero-investment constraint (₹0 budget)
**Status:** Accepted (mandatory project constraint, 2026-11-28)  
**Decision:** The project is developed and operated with a budget of ₹0.
- Use only free and open-source development dependencies.
- No paid APIs, cloud databases, hosting services or subscriptions.
- No services that require credit-card registration.
- No reliance on temporary free trials.
- All financial data stays on the user's device by default.
- Any potential monetary cost must be communicated to the developer **before** it is implemented.

**Reason:** Solo student project with no budget; on-device storage also reduces privacy exposure (see D-002).  
**How to apply:** Every new dependency or tool is checked for licence and cost before it is added, and its cost is recorded in `docs/TECH_STACK.md`.  
**Revisit if:** The developer explicitly approves a specific, costed exception.

## D-006 — Room 2.8.5 with KSP 2.3.6 for local persistence
**Status:** Accepted (verified by build and tests on 2026-11-28)  
**Decision:**
- Use `androidx.room` **2.8.5** (`room-runtime`, `room-compiler` via KSP, and the `androidx.room` Gradle plugin for schema export).
- Use KSP **2.3.6**.
- Keep the Kotlin Gradle plugin at the AGP 9.5.3 default, **2.2.10**.

**Reasons:**
- Room 2.8.5 is the latest stable 2.x release (2026-09-09). Its requirements are all met: Kotlin 2.0+ (since 2.7.0), minSdk 23+, and AGP 8.4+ for the Room Gradle plugin. The standard Android Room guide applies to it directly.
- Room 3.0 (`androidx.room3`, latest stable 3.0.3) was **not** chosen. It is a new major version focused on Kotlin Multiplatform: new packages, a mandatory `SQLiteDriver`, no SupportSQLite APIs, and coroutine-only APIs. Its 3.1 alphas require kotlinc 2.2. That is more change than this project needs now.
- KSP 2.3.1 was the first release to support AGP 9 built-in Kotlin. The Kotlin-prefixed line (`2.2.10-2.0.2`) predates that support.
- The Android Developers KSP migration page states that AGP 9.0–9.5 (default KGP 2.2.10) works with "KSP 2.3.0 and higher". Gradle's dependency report shows AGP's own KSP constraint (`2.2.10-2.0.2 -> 2.3.6`) being satisfied.
- KSP 2.3.7 raised KSP's Kotlin baseline to 2.3.20, and KSP's current compatibility table (written for 2.3.12) lists KGP 2.3.20–2.4.x. KSP 2.3.6 is therefore the newest release that stays close to this project's Kotlin version without requiring a Kotlin upgrade. It already includes the AGP 9 built-in-Kotlin fixes from 2.3.1, 2.3.2, 2.3.5 and 2.3.6.
- `room-ktx` is not added. In 2.8.5 it is an almost empty artifact (~6 KB): coroutine and `Flow` support live in `room-runtime`, which exposes `kotlinx-coroutines` as an API dependency.

**Caveat:** KSP does not publish an explicit statement that 2.3.6 supports KGP 2.2.10. Compatibility here rests on two things: the Android documentation above, and verification in this project. Both `assembleDebug` and the JVM and instrumented test suites pass (see `docs/PROGRESS_LOG.md`, 2026-11-28).  
**Revisit if:**
- AGP is upgraded to 9.6+. Per the Android docs, its default KGP becomes 2.4.20 and AGP stops constraining KSP, so choose a KSP version whose table covers that KGP.
- Kotlin is upgraded to 2.3.20+. Move to the latest KSP 2.3.x, but avoid 2.3.13, which shipped with a known defect.
- Room 3 becomes the documented default.

## D-007 — Keep the expense database out of cloud backup
**Status:** Accepted (implemented 2026-11-28; approved by the developer 2026-11-28)  
**Decision:** `money_tracker.db` (and its `-wal`, `-shm` and `-journal` files) is excluded from Android Auto Backup to the cloud.
- Android 12+ uses `res/xml/data_extraction_rules.xml`.
- Android 6–11 uses `res/xml/backup_rules.xml`.
- Direct device-to-device transfer on Android 12+ stays at the platform default (allowed).

**Reason:** With `android:allowBackup="true"` and the template rules, the database would be uploaded to the user's Google Drive backup. That contradicts D-005 and CLAUDE.md ("Do not transmit raw financial data off-device by default").  
**Trade-off:** If the user loses or resets the phone, expenses cannot be restored from cloud backup. The planned CSV export is the intended user-controlled backup path.  
**Alternatives considered:**
- Allow cloud backup (rejected by default: off-device).
- `android:allowBackup="false"` (rejected: also blocks device-to-device transfer on older Android versions).

**Verification:** Lint reports no errors for these files. The actual backup behaviour has not been exercised on a device.  
**Revisit if:** Users ask for cloud backup and explicitly opt in.

## D-008 — Manual entries store a calendar date as 12:00 local time
**Status:** Accepted (implemented 2026-11-28)  
**Decision:**
- The add-expense form asks for a date, not a time; it defaults to today and allows today or earlier.
- The date is stored in the existing `transaction_date` column (epoch milliseconds) as **12:00 noon in the device's time zone** on that date.
- `created_at` stores the real moment of saving.
- Dates are handled with a small `CalendarDate` class built on `java.util.GregorianCalendar`, not `java.time`.

**Reasons:**
- Noon is never skipped or repeated by daylight-saving changes.
- Noon keeps the same calendar date when viewed from any time zone within ±11 hours. Midnight would show as the previous day as soon as the phone moved one hour west.
- `java.time` needs API 26 (minSdk is 24) or core-library desugaring, which would be an extra dependency and build configuration. Material 3's DatePicker works on API 24 without it.
- No schema change was needed.

**Trade-offs:**
- Manual entries on the same day are ordered by insertion (`id`), not by a real time of day.
- Future imported transactions with real times will sort around noon for that day.

**Revisit if:** users need a time of day, or the project adopts desugaring / raises minSdk to 26.

## D-009 — Amount input: strict format, exact conversion, ₹99,99,99,999.99 maximum
**Status:** Accepted (implemented 2026-11-28)  
**Decision:**
- Amount text must be ASCII digits with an optional `.` and at most two decimals. `5.` and `.5` are allowed; surrounding whitespace is ignored.
- It is converted to paise with `BigDecimal`.
- Commas, signs, currency symbols, exponents and non-ASCII digits are rejected, each with a clear message.
- The maximum single expense is **₹99,99,99,999.99** (`99_999_999_999` paise).

**Reasons:**
- Float and Double cannot represent most decimal amounts exactly. For example, `0.29 * 100` is `28.999999999999996` as a Double.
- Accepting `1,000` or `10,5` risks silently misreading the amount, because a comma is a thousands separator in India but a decimal separator elsewhere.
- The cap is far below `Long.MAX_VALUE`, so summing tens of millions of maximum-size expenses for totals cannot overflow a `Long`. SQLite's `SUM` raises an error on integer overflow.

**Revisit if:** users need larger amounts or grouping commas in input.

## D-010 — One screen, ViewModel with StateFlow, manual wiring
**Status:** Accepted (implemented 2026-11-28)  
**Decision:**
- The add form and the transaction list share one Compose screen.
- `ExpenseViewModel` exposes `formState` and `listState` as `StateFlow`s and uses `viewModelScope`.
- Composables are stateless (state in, events out).
- `MainActivity` wires database → repository → ViewModel by hand.
- No Navigation library, no Hilt/Koin/Dagger, no `lifecycle-viewmodel-compose`.

**Reasons:**
- This is the smallest structure that meets the milestone, and it is easy for a solo intermediate developer to follow.
- Every library used was already on the classpath through Compose and Activity. The only new explicit dependencies are `lifecycle-viewmodel-ktx` and `lifecycle-runtime-compose`, which already resolved to 2.9.4 transitively, plus test-only `kotlinx-coroutines-test` (see `TECH_STACK.md`).

**Revisit if:** a second screen (e.g. edit expense or totals) makes navigation worthwhile, or manual wiring becomes repetitive.

## D-011 — Upgrade AndroidX Test to Espresso 3.7.0 / ext.junit 1.3.0 (test-only)
**Status:** Accepted (implemented 2026-11-28)  
**Decision:** The test dependencies `espresso-core` move from 3.5.1 to **3.7.0**, and `androidx.test.ext:junit` from 1.1.5 to **1.3.0**. These are the same coordinated androidx.test release (core 1.7.0, runner 1.7.0, monitor 1.8.0).  
**Reason:**
- Every Compose UI test failed on the API 37 emulator with `NoSuchMethodException: android.hardware.input.InputManager.getInstance`. Espresso 3.5.1 reaches that Android internal by reflection.
- The official AndroidX Test release notes list the fix in Espresso 3.7.0 (stable, 30 July 2025): "Use getSystemService instead of reflective InputManager.getInstance".
- 3.7.0 requires minSdk 21 and Kotlin 1.9+, both satisfied.

**Impact:**
- Test APK only. App runtime dependencies are unchanged.
- Gradle still resolves coroutines 1.9.0 for the tests, matching the app.

**Cost:** ₹0 (Apache 2.0).
