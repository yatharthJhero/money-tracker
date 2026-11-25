# Claude working agreement — Money Tracker

You are assisting an intermediate solo developer building Money Tracker, an Android application in Kotlin on Windows.

## Before changing code
1. Read `README.md` and the relevant files in `docs/`.
2. Inspect the existing project and its Gradle / Android / Kotlin versions. Do not blindly replace working configuration.
3. State the smallest implementation plan before making changes.
4. Ask before adding a dependency, changing architecture, deleting files, or introducing a cloud service.

## Engineering rules
- Prefer Kotlin and Jetpack Compose for the Android UI unless the repository already establishes a different choice.
- Keep architecture understandable for a solo intermediate developer.
- Prefer small, cohesive changes over large rewrites.
- Use Room/SQLite for local persistence when the project reaches the persistence milestone.
- Use integer paise for monetary values; do not use floating-point arithmetic for money.
- Include tests for money calculations, parsing, validation and other business logic.
- Handle empty, invalid and boundary states.
- Do not add Firebase, Supabase, analytics, LLM APIs, bank integrations or notification access without a demonstrated requirement and explicit approval.
- Never request or store UPI PINs, OTPs, passwords, CVVs or bank login credentials.
- Do not transmit raw financial data off-device by default.
- Do not claim notification capture is complete or authoritative.
- Explain trade-offs and any assumptions.

## Workflow
1. Inspect.
2. Plan.
3. Implement one milestone.
4. Run relevant tests/build and report exact results.
5. Summarize changed files and any manual steps.
6. Update `docs/PROGRESS_LOG.md` and `docs/DECISIONS.md` when appropriate.

Never claim a build or test passed unless you actually ran it and observed success. If you cannot run commands in the current environment, provide the exact commands for the developer to run and label them unverified.

## Current first milestone
Create a runnable Android app foundation, then implement:
- Add expense
- Local persistence
- Transaction history
- Basic category totals

Do not start notification ingestion or AI features until the basic app works and user research supports the need.
