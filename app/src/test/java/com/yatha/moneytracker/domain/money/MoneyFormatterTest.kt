package com.yatha.moneytracker.domain.money

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatterTest {

    private fun inr(paise: Long) = MoneyFormatter.format(paise, "INR")

    @Test
    fun smallInrAmounts_alwaysShowTwoDecimals() {
        assertEquals("₹0.00", inr(0L))
        assertEquals("₹0.01", inr(1L))
        assertEquals("₹0.50", inr(50L))
        assertEquals("₹100.00", inr(10_000L))
        assertEquals("₹125.50", inr(12_550L))
    }

    @Test
    fun inrAmounts_useIndianDigitGrouping() {
        assertEquals("₹999.99", inr(99_999L))
        assertEquals("₹1,000.00", inr(100_000L))
        assertEquals("₹12,345.67", inr(1_234_567L))
        assertEquals("₹1,23,456.78", inr(12_345_678L))
        assertEquals("₹1,00,00,000.00", inr(1_000_000_000L)) // one crore
        assertEquals("₹99,99,99,999.99", inr(RupeeAmountParser.MAX_AMOUNT_PAISE))
    }

    @Test
    fun extremeValues_doNotOverflow() {
        assertEquals("₹92,23,37,20,36,85,47,758.07", inr(Long.MAX_VALUE))
        assertEquals("-₹92,23,37,20,36,85,47,758.08", inr(Long.MIN_VALUE))
    }

    @Test
    fun negativeAmounts_keepTheSignInFront() {
        assertEquals("-₹125.50", inr(-12_550L))
        assertEquals("-₹0.01", inr(-1L))
    }

    @Test
    fun otherCurrencies_useTheCodeAndInternationalGrouping() {
        assertEquals("USD 1,234.56", MoneyFormatter.format(123_456L, "USD"))
        assertEquals("EUR 1,000,000.00", MoneyFormatter.format(100_000_000L, "EUR"))
        assertEquals("USD 0.05", MoneyFormatter.format(5L, "USD"))
    }
}
