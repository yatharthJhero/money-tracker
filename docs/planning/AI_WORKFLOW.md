# Working with ChatGPT and Claude

## Source of truth
The Markdown files in this folder are the project memory. Update them after meaningful decisions or implementation milestones. When starting a fresh AI chat, attach this folder or paste the relevant files.

## Roles
- ChatGPT: product decomposition, architecture reasoning, step-by-step implementation plans, test design, documentation and review.
- Claude Pro: independent code review, alternative designs, debugging hypotheses and reviewing larger code changes.

These are suggested roles, not separate sources of truth. Either assistant may be used for any task.

## Rules for every coding task
1. State the current phase and exact task.
2. Share the relevant files and current code, not just a description.
3. Ask for the smallest coherent change.
4. Require a list of files to create / modify.
5. Require build and test commands.
6. Review the diff before accepting it.
7. Run the app and tests locally.
8. Record the result in `PROGRESS_LOG.md`.
9. Update `ARCHITECTURE.md` or `DECISIONS.md` when a design changes.

## Suggested prompt for a new conversation
"You are helping me build Money Tracker. Read the attached README.md, PROJECT_CONTEXT.md, ROADMAP.md, ARCHITECTURE.md, DECISIONS.md and PROGRESS_LOG.md. Treat them as the source of truth. Do not assume unchecked tasks are complete. We are currently at [phase/stage]. Our exact next task is [task]. First explain the approach and acceptance criteria; then help implement the smallest testable change. Do not introduce new dependencies or change architecture without explaining why."

## Suggested Claude review prompt
"Review this change against the Money Tracker project docs. Look for correctness, edge cases, privacy/security issues, Android lifecycle issues, data migration risks and missing tests. Do not rewrite the whole project. Return prioritized findings with file and line references."

## Avoid context drift
- Never ask an AI to “finish the whole app” in one prompt.
- Do not accept invented API names or unverified policy claims.
- Ask for official documentation links when platform / regulatory rules matter.
- Keep one active branch and one canonical schema.
- If assistants disagree, compare evidence and run a small test; do not merge both approaches blindly.
