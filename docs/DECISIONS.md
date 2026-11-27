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
