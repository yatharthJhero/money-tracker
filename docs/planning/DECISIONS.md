# Architecture Decision Record

This file records decisions and unresolved assumptions. Update it when the project direction changes.

## D-001 — Start with Android, not cross-platform
**Status:** Proposed  
**Decision:** Use native Kotlin + Jetpack Compose for the first implementation.  
**Reason:** The notification listener is Android-specific; native implementation avoids a platform-channel dependency and supports Android lifecycle / permission behavior directly.  
**Revisit if:** iOS support becomes a validated requirement.

## D-002 — Local-first MVP
**Status:** Proposed  
**Decision:** Use Room / SQLite and avoid a cloud backend initially.  
**Reason:** Lower cost, smaller attack surface and simpler privacy story for a solo developer.  
**Revisit if:** Multi-device sync, remote backup or collaboration becomes necessary.

## D-003 — CSV before broad PDF support
**Status:** Proposed  
**Decision:** Implement CSV import first, then support selected PDF formats incrementally.  
**Reason:** PDF statements vary significantly; a preview-and-review workflow is essential.

## D-004 — Notification listener is best-effort
**Status:** Accepted principle  
**Decision:** Treat Android notifications as an optional source, not a complete or authoritative bank feed.  
**Reason:** Notifications may be absent, delayed, disabled, hidden or incomplete.

## D-005 — Rules before generative AI
**Status:** Proposed  
**Decision:** Build deterministic extraction and categorization first. Consider an LLM only for uncertain suggestions after privacy and cost review.  
**Reason:** Reproducibility, explainability, offline behavior and predictable costs.

## D-006 — No AA integration in the MVP
**Status:** Accepted for MVP  
**Decision:** Do not depend on Account Aggregator access for the student prototype.  
**Reason:** Eligibility, partnership, consent, provider pricing and compliance require current verification.

## Assumptions requiring verification
- Current Android API behavior and target SDK requirements for notification listener services.
- Current Google Play policy treatment of notification access and financial-data handling.
- Current Google OAuth / Gmail restricted-scope verification requirements if email integration is pursued.
- Current RBI / Sahamati AA framework, FIU eligibility and partner requirements.
- Current terms, quotas and pricing of any cloud / AI free tier.

Never copy claims from an AI-generated answer into the project report as verified facts. Use current official documentation and record the source and date.

## Change log
- 2026-11-26: Initial project direction drafted. No implementation decision has yet been validated by a working prototype.
