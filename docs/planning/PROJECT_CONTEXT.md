# Project Context — Money Tracker

## 1. Problem statement
Individuals lose control over spending because transactions are scattered across UPI apps, cards, wallets and subscriptions, with no single, simple view of everyday expenses in plain language.

## 2. Problem symptoms to validate
These are hypotheses, not yet proven findings:
- Small digital purchases are easy to forget and accumulate.
- Records are fragmented across payment apps, banks, cards and wallets.
- Bank narrations can be cryptic or inconsistent.
- Personal UPI transfers do not reveal the purpose of the payment.
- Recurring payments and subscriptions can go unnoticed.
- Cash spending is missing from digital transaction records.

## 3. Initial target segment
Start narrow: the developer and 5–10 consenting college students / young adults who use UPI regularly. Expand only after observing actual usage and data quality.

## 4. Product outcome
A user should be able to answer:
- How much did I spend this week/month?
- What were my biggest spending categories?
- Where did my spending increase?
- Which transactions are uncertain or need my label?
- Which payments appear recurring?

The app must communicate uncertainty. A guessed merchant or category must not be presented as fact.

## 5. Proposed product principles
1. **Local-first:** process and store data on-device wherever practical in the first Android prototype.
2. **Consent-first:** each data source is opt-in, explained and independently revocable.
3. **User control:** edit, export and delete transactions and source data.
4. **Explainable:** show why a transaction received a category.
5. **Honest coverage:** show which sources and date ranges are connected or missing.
6. **No credential collection:** never ask for bank login passwords, UPI PINs, OTPs or CVVs.
7. **No dark patterns:** no loan/credit cross-selling in the MVP.

## 6. Feasible ingestion methods

### A. Android notification listener
Use Android `NotificationListenerService` after the user explicitly grants notification access. It can process notifications posted by selected apps. It cannot guarantee every payment is captured: notifications may be disabled, delayed, hidden, absent, altered or incomplete. Treat it as best-effort capture, not authoritative bank synchronization.

### B. Statement import
Allow PDF and CSV uploads. Start with CSV and one known PDF format; add formats incrementally. PDFs may be text-based or scanned and may require different extraction paths. Always provide a review screen before importing.

### C. Manual entry
Provide a quick add flow for cash and missed transactions. This is essential because no digital source captures every expense.

### D. Email integration (later)
Consider manual email forwarding first. Gmail OAuth requires careful scope minimization, secure token handling and review of Google's current verification / restricted-scope requirements. Do not assume it is free or approval-free.

### E. Account Aggregator (future research only)
Do not build the MVP around direct AA access. FIU eligibility, regulated-entity requirements, partnerships, consent flows and commercial terms must be verified with current official sources and providers. Do not repeat unverified claims about licensing or per-user prices as facts.

## 7. Scope boundaries for the first release
IN:
- Android app
- Manual transaction entry
- CSV statement import
- Notification listener proof of concept
- Transaction normalization and deduplication
- Rule-based categorization and correction memory
- Monthly / weekly summaries
- Local data storage and export / delete

OUT initially:
- Direct bank login or screen scraping
- AA integration
- WhatsApp Business messaging
- Gmail read access
- Investment advice, credit scoring or lending
- Automatic cancellation of subscriptions
- Claims of guaranteed complete transaction capture

## 8. Success criteria to measure
Do not set arbitrary claims such as “95% accurate” before testing. Measure:
- Import success rate by file format
- Field extraction accuracy (date, amount, debit/credit, narration)
- Merchant and category accuracy on a manually labelled test set
- Duplicate rate
- Percentage of transactions requiring user correction
- Time required to onboard and import a statement
- Weekly active usage and whether summaries answer user questions
- User-reported trust, usefulness and reasons for disengagement

## 9. Interview narrative
A credible story is not “I built an AI finance app.” It is:
1. I identified a specific user problem.
2. I interviewed users and recorded evidence.
3. I compared feasible data-ingestion methods and their limitations.
4. I built a working pipeline and tested it on synthetic and consented data.
5. I measured extraction / categorization errors.
6. I improved the system based on observed failures.
7. I documented privacy, security and regulatory boundaries.
Only claim steps that are backed by the progress log, demo and test evidence.
