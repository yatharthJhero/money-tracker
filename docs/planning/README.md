# Money Tracker

A privacy-conscious personal finance companion that helps people understand where their money goes by consolidating available transaction records and translating them into plain language.

## Current status
**Stage:** 0 — Discovery and project definition  
**Implementation status:** Not started  
**Working name:** Money Tracker  
**Primary initial platform:** Android  
**Initial target users:** Student / young adult users, starting with the developer and a small opt-in campus beta  
**Budget constraint:** Aim for ₹0 using local-first development and free tiers; never assume third-party free tiers are permanent.

## Product promise
"Understand your spending without manually maintaining a spreadsheet."

The product should:
1. Collect transaction information from sources the user explicitly enables or uploads.
2. Convert inconsistent records into a consistent transaction format.
3. Categorize transactions and let users correct mistakes.
4. Explain spending in plain language.
5. Identify likely recurring payments, with confidence and evidence.
6. Make privacy, transparency, export and deletion core product features.

## Read this first in a new AI conversation
Read `PROJECT_CONTEXT.md`, then `ROADMAP.md`, `ARCHITECTURE.md`, and `DECISIONS.md`. Treat these files as the current source of truth. Do not claim a feature is implemented unless `PROGRESS_LOG.md` records evidence.

## Project documents
- `PROJECT_CONTEXT.md` — problem, target user, constraints and product boundaries.
- `ROADMAP.md` — phases, stages, deliverables and acceptance criteria.
- `ARCHITECTURE.md` — proposed system design and transaction schema.
- `DECISIONS.md` — decisions and assumptions, including items that need verification.
- `PROGRESS_LOG.md` — implementation record, tests, blockers and evidence.
- `AI_WORKFLOW.md` — how to use ChatGPT and Claude without creating conflicting code.

## Ground rules
- Build and test one vertical slice at a time.
- Prefer deterministic parsing before adding an LLM.
- Never send raw financial data to an AI provider by default.
- Do not request bank credentials, UPI PINs, OTPs, card CVVs or net-banking passwords.
- Treat notification access as sensitive, optional and revocable.
- Do not describe notification capture as complete bank synchronization.
- Measure parsing accuracy and user retention; do not invent success metrics.
- Keep a synthetic test dataset separate from real financial data.
