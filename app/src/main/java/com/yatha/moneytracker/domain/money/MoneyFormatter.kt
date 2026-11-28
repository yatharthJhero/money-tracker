package com.yatha.moneytracker.domain.money

/**
 * Formats an amount in minor units (paise) for display, using integer arithmetic only.
 *
 * - INR uses the ₹ symbol and Indian digit grouping: `12345678` -> `₹1,23,456.78`.
 * - Other currencies use the code and international grouping: `123456` (USD) -> `USD 1,234.56`.
 * - Two decimal places are always shown.
 *
 * Grouping is implemented here instead of with `java.text.NumberFormat`, because the JVM
 * (used by unit tests) and Android format Indian grouping differently.
 */
object MoneyFormatter {

    private const val INR = "INR"
    private const val RUPEE_SYMBOL = "₹"

    fun format(amountMinor: Long, currencyCode: String): String {
        // Split before taking absolute values so Long.MIN_VALUE cannot overflow.
        val major = (amountMinor / 100).let { if (it < 0) -it else it }
        val minor = (amountMinor % 100).let { if (it < 0) -it else it }
        val sign = if (amountMinor < 0) "-" else ""

        val isInr = currencyCode == INR
        val groupedMajor = if (isInr) groupIndian(major.toString()) else groupThousands(major.toString())
        val prefix = if (isInr) RUPEE_SYMBOL else "$currencyCode "
        return "$sign$prefix$groupedMajor.${minor.toString().padStart(2, '0')}"
    }

    /** `12345678` -> `1,23,45,678`: last three digits, then groups of two. */
    private fun groupIndian(digits: String): String {
        if (digits.length <= 3) return digits
        val lastThree = digits.takeLast(3)
        val rest = digits.dropLast(3)
        val restGroups = rest.reversed().chunked(2).joinToString(",").reversed()
        return "$restGroups,$lastThree"
    }

    /** `1234567` -> `1,234,567`. */
    private fun groupThousands(digits: String): String =
        digits.reversed().chunked(3).joinToString(",").reversed()
}
