# Proposed Architecture and Data Model

## 1. Initial architecture
For the first Android MVP, prefer a local-first design:

- UI: Kotlin + Jetpack Compose
- Application architecture: MVVM / unidirectional state flow
- Local persistence: Room over SQLite
- Background work: WorkManager only for work that genuinely needs scheduling
- Notification capture: Android `NotificationListenerService`
- Statement import: Android document picker + format-specific parsers
- Tests: JUnit, coroutine test utilities and Android instrumented tests

A backend is not required for the first useful version. Add one only when a real requirement appears, such as multi-device sync or shared service infrastructure.

## 2. Logical pipeline

```text
Notification / CSV / PDF / Manual Entry
                 |
          Source Adapter
                 |
       Extracted Transaction
                 |
       Normalize + Validate
                 |
          Deduplicate
                 |
       Merchant Resolution
                 |
       Category Assignment
                 |
        User Review / Rules
                 |
          Local Database
                 |
     Aggregates + Plain-language UI
```

Every adapter should produce the same domain-level transaction candidate. This keeps source-specific quirks out of the UI.

## 3. Canonical transaction fields

| Field | Type | Notes |
|---|---|---|
| id | UUID | Internal primary key |
| occurred_at | ISO-8601 / epoch | Transaction time; retain timezone |
| amount_minor | integer | Amount in paise; avoid floating point |
| currency | string | Initially INR |
| direction | enum | DEBIT, CREDIT, REFUND, UNKNOWN |
| merchant_display | string nullable | Human-readable counterparty |
| raw_description | string nullable | Minimize retention; protect as sensitive |
| category_id | string nullable | User-editable category |
| payment_method | enum | UPI, CARD, BANK_TRANSFER, WALLET, CASH, OTHER, UNKNOWN |
| source_type | enum | MANUAL, CSV, PDF, NOTIFICATION, EMAIL |
| source_account_label | string nullable | User-defined label; do not store credentials |
| external_reference_hash | string nullable | Optional deduplication aid; avoid exposing full identifiers |
| confidence | decimal nullable | Confidence for inferred fields, not a truth score |
| review_status | enum | CONFIRMED, NEEDS_REVIEW, IGNORED |
| created_at | timestamp | Record creation |
| updated_at | timestamp | Last edit |

Store amounts as integer paise (e.g. ₹12.50 = 1250). Define refund and reversal semantics carefully so totals do not double-count.

## 4. Related entities

### Category
- id
- name
- parent_id nullable
- is_system
- owner_user_id nullable

### MerchantAlias
- id
- normalized_alias
- merchant_display
- default_category_id nullable
- owner_user_id nullable
- match_type (exact / contains / regex)
- priority

### ImportBatch
- id
- source_type
- file_name_safe
- imported_at
- row_count
- success_count
- error_count
- status

Do not retain uploaded statement files by default after successful import unless the user explicitly chooses to keep them.

### ParseEvidence
- id
- transaction_id
- field_name
- method (source_field / regex / dictionary / user)
- evidence_summary
- confidence
- created_at

Avoid storing complete notification payloads as evidence.

### RecurrenceCandidate
- id
- merchant_key
- typical_amount_minor
- interval_days_estimate
- last_seen_at
- confidence
- status (CANDIDATE / USER_CONFIRMED / DISMISSED)

## 5. Deduplication
The same payment may appear in a notification and a statement. Do not rely on amount alone.

Use a layered match:
- exact bank / UPI reference when available;
- otherwise compare direction, amount, normalized counterparty and a bounded time window;
- retain source provenance;
- if ambiguous, ask the user rather than silently merging.

Keep an import/source link table if one transaction can be supported by multiple source records.

## 6. Categorization order
1. User-specific exact correction.
2. Exact merchant / UPI alias dictionary.
3. Source-specific deterministic rules.
4. Conservative keyword / regex rules.
5. Optional model-assisted suggestion in a later phase.
6. Otherwise `Uncategorized` and ask the user.

Do not use an LLM to calculate totals. Compute totals in application code / SQL.

## 7. Privacy and security baseline
- Request only permissions required for an enabled feature.
- Explain notification access in plain language.
- Do not upload notification content or statements by default.
- Do not log raw financial descriptions in production logs.
- Use Android Keystore-backed encryption where appropriate for sensitive local data or encryption keys.
- Provide export and deletion.
- Keep test fixtures synthetic.
- Never store banking passwords, UPI PINs, OTPs or CVVs.
- Document retention and backup behavior.
