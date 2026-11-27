# Implementation Roadmap

A solo developer should expect this to be a multi-stage project. Timings are estimates, not promises; finish each stage's acceptance criteria before moving on.

## Phase 0 — Discovery and validation
**Goal:** Confirm the problem before committing to a large build.

Tasks:
- Interview 8–12 people in the initial target segment.
- Ask how they currently track expenses, which sources they use, what they forget, and what they distrust.
- Ask participants to demonstrate their current workflow; do not collect credentials or unnecessary financial records.
- Create 3–5 user journeys and a simple clickable wireframe.
- Record competing products and what users dislike / still cannot do.

Deliverables:
- Interview notes with consent and anonymized quotes.
- Problem statement and initial personas.
- Prioritized MVP backlog.

Acceptance:
- At least three recurring pain points are supported by actual interviews.
- One narrow first user segment is selected.
- The first release can be described in one sentence.

## Phase 1 — Foundation and engineering setup
**Goal:** Create a reproducible Android project and establish safe development practices.

Tasks:
- Choose native Kotlin + Jetpack Compose for the first Android app.
- Set up Git repository, README, issue list and branching conventions.
- Create app modules / packages for UI, domain, data and ingestion.
- Add local database (Room / SQLite).
- Define transaction domain model and migrations.
- Create synthetic sample data and unit-test structure.
- Add a privacy explanation and data deletion plan.

Deliverables:
- App builds on a physical Android device.
- Empty transaction list screen.
- Local database can save, read, edit and delete a transaction.
- Automated tests run locally.

Acceptance:
- A clean clone can be built by following the README.
- No secrets or real financial records are committed to Git.

## Phase 2 — Manual expense MVP
**Goal:** Deliver a useful app before automating ingestion.

Tasks:
- Add manual expense and income entry.
- Add transaction list, date filters and category totals.
- Add edit / delete and payment mode.
- Add weekly and monthly summaries.
- Add CSV export.

Acceptance:
- User can record an expense in a few seconds.
- Totals reconcile with the underlying transaction list.
- Editing or deleting a transaction updates all summaries correctly.

## Phase 3 — Statement import
**Goal:** Import historical transactions with user review.

Tasks:
- Start with CSV and document supported columns.
- Add a parser interface so each bank format is an adapter.
- Add one text-based PDF format only after CSV works.
- Extract date, narration, amount, debit/credit and optional balance.
- Normalize dates, decimal formats and signs.
- Show preview, warnings and row-level errors before import.
- Add duplicate detection and import batch tracking.

Acceptance:
- A known test statement imports with documented field-level accuracy.
- Invalid rows are not silently discarded.
- Re-importing the same file does not create duplicate transactions.
- User can inspect and undo an import batch.

## Phase 4 — Android notification capture
**Goal:** Demonstrate best-effort transaction capture from supported notifications.

Tasks:
- Build a separate proof of concept for `NotificationListenerService`.
- Explain notification access before linking to Android settings.
- Allow users to select / review supported app packages.
- Parse only notifications relevant to transaction capture.
- Store the minimum necessary fields.
- Add duplicate detection across notification and statement imports.
- Add source label, capture time and confidence.
- Test notification disabled, hidden content, duplicate, delayed and malformed cases.

Acceptance:
- User explicitly grants and can revoke access.
- Service does not claim complete coverage.
- Unsupported / ambiguous notifications are safely ignored or queued for review.
- Raw notification content is not retained longer than necessary.
- Tests cover at least the supported notification formats.

## Phase 5 — Normalization, categorization and user correction
**Goal:** Turn noisy text into trustworthy, editable records.

Pipeline:
1. Source-specific extraction.
2. Validation and normalization.
3. Merchant alias lookup.
4. Rule-based category assignment.
5. Confidence / reason generation.
6. User correction.
7. Per-user correction memory.

Example:
- Raw narration: `UPI/P2M/12345/RAJESH K/OKICICI`
- Extracted counterparty: `RAJESH K`
- Category: `Uncategorized`
- Prompt: “What was this payment for?”
- User selects `Food & Dining`.
- Future similar records can reuse the user's label, subject to matching confidence.

Acceptance:
- Every inferred field records its source / method.
- Low-confidence results remain visibly uncertain.
- User corrections override generic rules.
- One person's private labels never leak to another user.

## Phase 6 — Recurring-payment detection and plain-language insights
**Goal:** Explain spending patterns without overstating predictions.

Tasks:
- Detect candidate recurring transactions using counterparty / merchant, amount tolerance and date interval.
- Distinguish confirmed recurring payments from candidates.
- Generate weekly and monthly summaries from deterministic aggregates.
- Add questions such as “How much did I spend on food last month?”
- If using an LLM later, send only minimized, user-approved data; validate outputs against computed totals.

Acceptance:
- Recurring alerts say “possible” until confirmed.
- Summary amounts reconcile exactly with transaction data.
- The system never invents a reason for a spending increase.

## Phase 7 — Closed beta and evaluation
**Goal:** Test whether it helps real users.

Tasks:
- Start with 5 consenting testers, then expand cautiously.
- Provide a clear consent / privacy notice and a support contact.
- Collect structured feedback and crash / parsing diagnostics without collecting raw financial content unnecessarily.
- Track accuracy, correction rate, retention and onboarding completion.
- Fix the most frequent failure modes before adding features.

Acceptance:
- Testers can onboard without developer intervention.
- Each tester can revoke notification access and delete their data.
- A written evaluation includes failures, not just successful demos.

## Phase 8 — Security, deployment and future integrations
**Goal:** Prepare a responsible public release or polished academic demonstration.

Tasks:
- Threat model and privacy review.
- Encryption and secure key handling appropriate to chosen storage.
- Dependency and secret scanning.
- Backup / restore and account deletion design if cloud sync is introduced.
- Review Android distribution and Google Play policies if publishing.
- Research current AA eligibility, FIU partnership routes and provider terms from official sources before proposing integration.

Acceptance:
- Security and privacy checklist completed.
- Known limitations disclosed.
- Demo and project report match the actual implementation.

## Suggested first milestone
Do not start with AI, cloud sync or all notification formats. First make a Kotlin app that can manually save a transaction, list it, calculate a monthly total and pass unit tests. That is the first end-to-end vertical slice.
