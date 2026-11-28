package com.yatha.moneytracker.domain.money

import java.math.BigDecimal

/** Why a piece of amount text could not be turned into paise. */
enum class AmountError {
    /** Nothing was entered (or only whitespace). */
    EMPTY,

    /** Not a plain decimal number, e.g. `abc`, `1,000`, `1e3`, `+5`, `.`. */
    MALFORMED,

    /** Starts with a minus sign. */
    NEGATIVE,

    /** Parses to exactly zero, e.g. `0` or `0.00`. */
    ZERO,

    /** More than two digits after the decimal point, e.g. `1.005`. */
    TOO_MANY_DECIMAL_PLACES,

    /** Larger than [RupeeAmountParser.MAX_AMOUNT_PAISE]. */
    TOO_LARGE,
}

/** Result of [RupeeAmountParser.parse]. */
sealed interface AmountParseResult {
    data class Valid(val paise: Long) : AmountParseResult
    data class Invalid(val error: AmountError) : AmountParseResult
}

/**
 * Converts rupee text typed by the user (e.g. `"125.50"`) into an exact number of paise
 * (e.g. `12550`).
 *
 * Floating-point types are never used. The text is validated with a strict pattern and
 * converted with [BigDecimal], which is exact for decimal input.
 *
 * Accepted: digits with an optional decimal point and at most two decimal digits.
 * Surrounding whitespace is ignored, and `5.` and `.5` are accepted as ₹5.00 and ₹0.50.
 * Rejected: grouping commas, signs, exponents, currency symbols and non-ASCII digits.
 */
object RupeeAmountParser {

    /**
     * The largest single amount accepted: ₹99,99,99,999.99 (just under ₹100 crore).
     *
     * This stays far below `Long.MAX_VALUE`, so adding up many expenses (category totals)
     * cannot overflow a `Long`. See docs/DECISIONS.md (D-009).
     */
    const val MAX_AMOUNT_PAISE: Long = 99_999_999_999L

    private const val PAISE_PER_RUPEE_EXPONENT = 2
    private const val MAX_DECIMAL_PLACES = 2

    /** Whole part, then an optional `.` with an optional fraction. Only ASCII digits. */
    private val AMOUNT_PATTERN = Regex("""([0-9]*)(?:\.([0-9]*))?""")

    private val MAX_AMOUNT = BigDecimal.valueOf(MAX_AMOUNT_PAISE)

    fun parse(text: String): AmountParseResult {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return AmountParseResult.Invalid(AmountError.EMPTY)

        if (trimmed.startsWith('-')) {
            val error = if (isPlainNumber(trimmed.substring(1))) AmountError.NEGATIVE else AmountError.MALFORMED
            return AmountParseResult.Invalid(error)
        }

        val match = AMOUNT_PATTERN.matchEntire(trimmed)
            ?: return AmountParseResult.Invalid(AmountError.MALFORMED)
        val wholePart = match.groupValues[1]
        val fractionPart = match.groupValues[2]
        if (wholePart.isEmpty() && fractionPart.isEmpty()) {
            return AmountParseResult.Invalid(AmountError.MALFORMED)
        }
        if (fractionPart.length > MAX_DECIMAL_PLACES) {
            return AmountParseResult.Invalid(AmountError.TOO_MANY_DECIMAL_PLACES)
        }

        // Exact: e.g. "10.5" -> 10.5 -> 1050 (scale 0). Never goes through Double.
        val paise = BigDecimal(trimmed).movePointRight(PAISE_PER_RUPEE_EXPONENT)
        return when {
            paise.signum() == 0 -> AmountParseResult.Invalid(AmountError.ZERO)
            paise > MAX_AMOUNT -> AmountParseResult.Invalid(AmountError.TOO_LARGE)
            else -> AmountParseResult.Valid(paise.longValueExact())
        }
    }

    private fun isPlainNumber(text: String): Boolean {
        val match = AMOUNT_PATTERN.matchEntire(text) ?: return false
        return match.groupValues[1].isNotEmpty() || match.groupValues[2].isNotEmpty()
    }
}
