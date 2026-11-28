# Architecture

## Principles
- Android-native Kotlin application.
- Local-first data handling.
- Clear separation between UI, domain logic and persistence.
- Avoid premature backend and integrations.
- Zero-investment: free, open-source, on-device only (see `DECISIONS.md` D-005).

## Flow

```text
ExpenseScreen (Compose, stateless)
   ▲ state (StateFlow)          │ events (onAmountChange, saveExpense, …)
   │                            ▼
ExpenseViewModel ── validates with validateExpenseForm / RupeeAmountParser
   │ suspend + Flow
   ▼
ExpenseRepository ── maps Expense <-> ExpenseEntity
   ▼
ExpenseDao (Room) ── SQLite file money_tracker.db (app-private storage)
```

Money calculations, parsing, validation and date conversion are plain Kotlin functions that are unit-tested without Android.

**Implemented so far:** one screen that adds expenses and lists them, backed by Room. Editing, deleting and category totals are not built yet.

## Package layout

```text
com.yatha.moneytracker
├── MainActivity.kt     Manual wiring: database -> repository -> ViewModel; shows ExpenseRoute
├── domain/
│   ├── model/          Expense, ExpenseCategory
│   ├── money/          RupeeAmountParser (text -> paise), MoneyFormatter (paise -> "₹1,23,456.78")
│   └── time/           CalendarDate (date without time; API 24-safe)
├── data/
│   ├── local/          Room: ExpenseEntity, ExpenseDao, MoneyTrackerDatabase, ExpenseMapper
│   └── repository/     ExpenseRepository: the only data API the UI layer calls
└── ui/
    ├── expense/        ExpenseViewModel, ExpenseUiState, ExpenseFormValidation, ExpenseScreen
    └── theme/          Compose theme (template)
```

Rules:
- `domain` has no Android, Room or UI imports. Only `java.util` / `java.math` and Kotlin are used.
- Room annotations and `ExpenseEntity` stay inside `data/local`. Outside the data layer, code sees only domain `Expense` objects.
- There is no dependency-injection framework (D-010). `MainActivity` creates the ViewModel with `by viewModels { ExpenseViewModel.factory(repository) }`. The database is a process-wide singleton (`MoneyTrackerDatabase.getInstance`).

## UI layer (`ui/expense`)

- **`ExpenseViewModel`** exposes two `StateFlow`s:
  - `formState: StateFlow<ExpenseFormState>`: what the user typed, validation errors, `isSaving`, and a one-off `message` (SAVED / SAVE_FAILED). It is a plain `MutableStateFlow`, updated synchronously, which keeps text fields responsive.
  - `listState: StateFlow<ExpenseListState>`: `Loading`, `Loaded(expenses)` or `Error`, built from `repository.observeExpenses()` with `stateIn(viewModelScope, WhileSubscribed(5 s))`. It updates automatically after every database change.
- **Saving** (`saveExpense()`):
  1. If a save is already running, do nothing. This prevents duplicates, and the Save button is also disabled.
  2. Validate all fields at once (`validateExpenseForm`). If anything is invalid, show the errors and stop.
  3. Set `isSaving = true` synchronously, then save in `viewModelScope`. The expense is built with `id = 0` (Room generates the id), currency `INR`, the trimmed description, `transactionDate = date at 12:00 local time` (D-008) and `createdAt = now`.
  4. On success, reset the form (date = today) and report SAVED.
  5. On failure, keep every field as typed, set `isSaving = false`, and report SAVE_FAILED. Errors are deliberately not logged, to avoid writing financial details to logs.
- **`ExpenseScreen`** is stateless: it receives state and lambdas, which makes it easy to preview and test. `ExpenseRoute` connects it to the ViewModel with `collectAsStateWithLifecycle`. One `LazyColumn` holds the form and then the transactions (stable item keys: expense `id`). Feedback appears in a snackbar. The date picker only allows today or earlier.
- **Form rules:** amount required (see below); category required; description optional, trimmed, ≤ 200 characters; date ≤ today.
- **Not yet handled:** typed form input is kept across rotation (the ViewModel survives) but not across process death. There is no `SavedStateHandle` yet.

## Money and dates

- **Amount input** (`RupeeAmountParser`, D-009):
  - Accepts ASCII digits with an optional `.` and at most 2 decimals; surrounding whitespace is ignored.
  - Converted exactly with `BigDecimal`, never `Float` or `Double`.
  - Rejected with a specific reason: empty, malformed (commas, signs, symbols, exponents, non-ASCII digits), negative, zero, more than 2 decimals, or above ₹99,99,99,999.99.
- **Display** (`MoneyFormatter`): integer arithmetic only. INR is shown as `₹` with Indian grouping (`₹1,23,456.78`).
- **Dates** (`CalendarDate`, D-008):
  - The UI collects a calendar date, not a time.
  - It is stored as **12:00 local time** on that date, in epoch milliseconds, in `transaction_date`.
  - `created_at` is the real moment of saving.
  - "Today" and display formatting use the device's current time zone.
  - Everything uses `java.util.GregorianCalendar` and `java.text.DateFormat`, which work on API 24 without desugaring.

## Persistence (Room over SQLite)

Database file: `money_tracker.db` in app-private storage. Schema version: **1**.

Table `expenses`:

| Column | SQLite type | Kotlin (entity) | Notes |
|---|---|---|---|
| `id` | INTEGER PK AUTOINCREMENT | `Long` | Generated by SQLite. `0` in Kotlin means "not saved yet". Ids are never reused after deletion. |
| `amount_minor` | INTEGER NOT NULL | `Long` | Minor units (paise for INR). Never floating point. |
| `currency_code` | TEXT NOT NULL | `String` | ISO 4217 code, stored for every row. |
| `category` | TEXT NOT NULL | `String` | `ExpenseCategory` **name** (e.g. `FOOD`), never the ordinal. |
| `description` | TEXT NOT NULL | `String` | Empty string when absent. |
| `transaction_date` | INTEGER NOT NULL | `Long` | When the expense happened, epoch milliseconds (manual entries: 12:00 local time on the chosen date). |
| `created_at` | INTEGER NOT NULL | `Long` | When it was recorded in the app, epoch milliseconds. |

Behaviour:
- **Ordering:** `observeExpenses()` returns newest `transaction_date` first; ties are broken by `id` descending (most recently inserted first). Because `id` is unique, the order is deterministic.
- **Insert:** `ExpenseRepository.addExpense` requires `id == 0` and returns the generated id. Inserting an existing id fails rather than overwriting.
- **Update / delete:** these return `false` when no row has that id. Neither is used by the UI yet.
- **Validation:** `Expense` rejects non-positive amounts on both write and read. An unknown stored category makes `toDomain()` throw rather than silently re-categorising data; the UI then shows "Couldn't load your expenses."
- **Not validated yet:** currency codes are stored as given; they are not checked against ISO 4217.

### Schema changes and migrations
- The schema JSON for every version is exported to `app/schemas/` by the Room Gradle plugin. **Commit these files.**
- Destructive migration is **not** enabled. If the version number increases without a migration, Room throws at startup instead of deleting user data.
- To change the schema:
  1. Bump `version` in `MoneyTrackerDatabase`.
  2. Add an `AutoMigration` or a manual `Migration`.
  3. Add `androidx.room:room-testing` and a `MigrationTestHelper` test for that migration.
- Renaming or removing an `ExpenseCategory` constant requires a migration that rewrites stored `category` values. It also needs a new label in `ExpenseCategory.labelRes()`; the compiler enforces that.

## Transaction data rules
- Store amounts as integer paise (`Long`), not `Float` or `Double`.
- Store currency explicitly (initial default may be INR, but do not silently assume all future data is INR).
- Keep transaction date/time and creation/import metadata distinct where needed.
- Design duplicate handling before adding import sources.
- Preserve user corrections.

## Testing layout
- `app/src/test/` (JVM, no device):
  - The domain model, parser, formatter, `CalendarDate`, form validation and mapper.
  - `ExpenseViewModel` with a `FakeExpenseDao` behind the real `ExpenseRepository`. The fake can delay or fail writes.
  - `testing/MainDispatcherRule` swaps `Dispatchers.Main` for a test dispatcher (`kotlinx-coroutines-test`).
- `app/src/androidTest/` (emulator or device):
  - Room DAO and repository tests on `Room.inMemoryDatabaseBuilder`.
  - Compose UI tests (`ExpenseScreenTest`), both of the stateless screen and of the real ViewModel with an in-memory database. They never touch real user data.

## Privacy
- Collect the minimum data needed.
- No bank credentials, UPI PINs, OTPs or CVVs.
- No network transmission of financial data by default. The app declares no `INTERNET` permission.
- The expense database is excluded from Android cloud backup (D-007).
- Save failures are not logged, so amounts and descriptions never reach logcat.
- Explain permissions before requesting them.
