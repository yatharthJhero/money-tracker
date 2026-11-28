# Roadmap

## Now — MVP foundation
- [x] Android project and GitHub repository
- [x] Expense domain model (`Expense`, `ExpenseCategory`, unit tests)
- [x] Local persistence: Room data layer (entity, DAO, database, mapper, repository) with JVM and instrumented tests
- [x] Persistence wired into the app (`ExpenseViewModel` + manual wiring in `MainActivity`)
- [x] Add expense form (amount in INR, category, optional description, date ≤ today; validation, saving and feedback states)
- [x] Transaction list (newest first, reactive, empty state; survives app restart)
- [ ] Category totals
- [x] Unit tests and basic UI tests (JVM unit tests, Room instrumented tests, basic Compose UI tests)
- [ ] Edit and delete an expense from the UI (repository support exists; no UI yet)

## Next — Usability and validation
- [ ] Test with a few students and working professionals
- [ ] Observe where people struggle
- [ ] Improve empty states, editing and deletion
- [ ] CSV export / import only if user need supports it

## Later — Evidence-dependent
- [ ] Statement import
- [ ] Android notification capture (opt-in, best-effort, privacy-reviewed)
- [ ] Merchant normalization and categorization assistance
- [ ] Recurring-payment indicators
- [ ] Plain-language insights

Do not treat later items as committed scope. Reprioritize based on evidence.
