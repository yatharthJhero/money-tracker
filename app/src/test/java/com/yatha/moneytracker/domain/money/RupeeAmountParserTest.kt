package com.yatha.moneytracker.domain.money

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RupeeAmountParserTest {

    private fun assertValid(text: String, expectedPaise: Long) {
        assertEquals("input \"$text\"", AmountParseResult.Valid(expectedPaise), RupeeAmountParser.parse(text))
    }

    private fun assertInvalid(text: String, expectedError: AmountError) {
        assertEquals("input \"$text\"", AmountParseResult.Invalid(expectedError), RupeeAmountParser.parse(text))
    }

    @Test
    fun requiredExamples_convertExactly() {
        assertValid("125.50", 12_550L)
        assertValid("100", 10_000L)
        assertValid("0.01", 1L)
        assertValid("10.5", 1_050L)
    }

    @Test
    fun wholeRupees_convertToPaise() {
        assertValid("1", 100L)
        assertValid("7", 700L)
        assertValid("2500", 250_000L)
    }

    @Test
    fun oneAndTwoDecimalPlaces_convertToPaise() {
        assertValid("0.1", 10L)
        assertValid("0.10", 10L)
        assertValid("0.99", 99L)
        assertValid("1.05", 105L)
        assertValid("99.99", 9_999L)
    }

    @Test
    fun valuesThatDoubleArithmeticGetsWrong_areExact() {
        // e.g. 0.29 * 100 == 28.999999999999996 and 0.57 * 100 == 56.99999999999999 as Double.
        assertValid("0.29", 29L)
        assertValid("0.57", 57L)
        assertValid("1.15", 115L)
        assertValid("4.35", 435L)
        assertValid("19.99", 1_999L)
    }

    @Test
    fun surroundingWhitespace_isIgnored() {
        assertValid(" 125.50 ", 12_550L)
        assertValid("\t100\n", 10_000L)
    }

    @Test
    fun leadingZeros_areAccepted() {
        assertValid("007", 700L)
        assertValid("00.50", 50L)
    }

    @Test
    fun missingWholeOrFractionDigits_areAccepted() {
        assertValid(".5", 50L)
        assertValid("5.", 500L)
    }

    @Test
    fun emptyOrBlank_isRejectedAsEmpty() {
        listOf("", " ", "   ", "\n\t").forEach { assertInvalid(it, AmountError.EMPTY) }
    }

    @Test
    fun zero_isRejected() {
        listOf("0", "0.0", "0.00", "000", ".0", "0.").forEach { assertInvalid(it, AmountError.ZERO) }
    }

    @Test
    fun negativeNumbers_areRejectedAsNegative() {
        listOf("-1", "-0.01", "-125.50", " -5 ").forEach { assertInvalid(it, AmountError.NEGATIVE) }
    }

    @Test
    fun negativeEdgeCases_areStillRejected() {
        // A minus sign is reported as NEGATIVE even when the rest would be zero or too precise,
        // because the sign is the most useful thing to tell the user about.
        listOf("-0", "-0.00", "-1.005", "-1000000000").forEach { assertInvalid(it, AmountError.NEGATIVE) }
        // A minus sign that is not directly followed by a number is malformed.
        listOf("- 5", "5-", "1-2", "−5" /* U+2212 minus sign */).forEach { assertInvalid(it, AmountError.MALFORMED) }
    }

    @Test
    fun whitespaceInsideTheNumber_isRejected() {
        listOf("12 .50", "12. 50", "1\t000", "1\n5").forEach { assertInvalid(it, AmountError.MALFORMED) }
    }

    @Test
    fun otherDigitAndSeparatorCharacters_areRejected() {
        listOf(
            "１００", // full-width digits
            "100/-", // common written form of a rupee amount
            "1_000",
            "1'000",
            "12,50", // decimal comma
            "100 INR",
        ).forEach { assertInvalid(it, AmountError.MALFORMED) }
    }

    @Test
    fun malformedText_isRejected() {
        listOf(
            "abc", "12abc", "1,000", "1.000,50", "1e3", "+5", ".", "-", "-abc", "--5", "1.2.3",
            "₹100", "Rs 100", "1 000", "0x10", "NaN", "Infinity",
            "١٢٣", // Arabic-Indic digits
            "१००", // Devanagari digits
        ).forEach { assertInvalid(it, AmountError.MALFORMED) }
    }

    @Test
    fun moreThanTwoDecimalPlaces_isRejected() {
        listOf("1.005", "0.001", "125.500", "10.123456").forEach {
            assertInvalid(it, AmountError.TOO_MANY_DECIMAL_PLACES)
        }
    }

    @Test
    fun maximumAmount_isAccepted() {
        assertValid("999999999.99", RupeeAmountParser.MAX_AMOUNT_PAISE)
        assertValid("999999999.99", 99_999_999_999L)
    }

    @Test
    fun aboveMaximum_isRejectedAsTooLarge() {
        listOf(
            "1000000000",
            "1000000000.00",
            "92233720368547758.07", // Long.MAX_VALUE paise
            "92233720368547758.08", // one paisa beyond Long.MAX_VALUE
            "99999999999999999999999999999999",
        ).forEach { assertInvalid(it, AmountError.TOO_LARGE) }
    }

    @Test
    fun oneHundredthOfAPaisaAboveTheMaximum_isReportedAsTooManyDecimals() {
        // Precision is checked before size, so no value with 3+ decimals is ever rounded into range.
        assertInvalid("999999999.991", AmountError.TOO_MANY_DECIMAL_PLACES)
        assertInvalid("999999999.999", AmountError.TOO_MANY_DECIMAL_PLACES)
    }

    @Test
    fun hugeInputs_areRejectedWithoutOverflowOrCrash() {
        // Far beyond Long.MAX_VALUE: must not wrap around to a small or negative number.
        assertInvalid("9".repeat(1_000), AmountError.TOO_LARGE)
        assertInvalid("1" + "0".repeat(19), AmountError.TOO_LARGE) // 10^19 rupees
        assertInvalid("18446744073709551616", AmountError.TOO_LARGE) // 2^64 rupees
        assertInvalid("9".repeat(1_000) + ".99", AmountError.TOO_LARGE)
        assertInvalid("1." + "0".repeat(1_000), AmountError.TOO_MANY_DECIMAL_PLACES)
    }

    @Test
    fun maximumAmount_leavesRoomForTotalsWithoutLongOverflow() {
        // Tens of millions of maximum-size expenses can be summed into a Long.
        assertTrue(Long.MAX_VALUE / RupeeAmountParser.MAX_AMOUNT_PAISE > 90_000_000L)
    }
}
