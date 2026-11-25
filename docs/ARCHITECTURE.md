# Architecture (initial direction)

## Principles
- Android-native Kotlin application.
- Local-first data handling.
- Clear separation between UI, domain logic and persistence.
- Avoid premature backend and integrations.

## Initial conceptual flow

Compose UI → ViewModel → repository → Room database

Keep money calculations in testable domain functions rather than UI code.

## Transaction data rules
- Store amounts as integer paise (`Long`), not `Float` or `Double`.
- Store currency explicitly (initial default may be INR, but do not silently assume all future data is INR).
- Keep transaction date/time and creation/import metadata distinct where needed.
- Design duplicate handling before adding import sources.
- Preserve user corrections.

## Privacy
- Collect the minimum data needed.
- No bank credentials, UPI PINs, OTPs or CVVs.
- No network transmission of financial data by default.
- Explain permissions before requesting them.
