package com.yatha.moneytracker.domain.time

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

/**
 * A calendar date with no time of day and no time zone, such as 28 November 2026.
 *
 * This is used instead of `java.time.LocalDate`, which needs API 26 (the app's minSdk is 24)
 * or core-library desugaring. It is built on `java.util.GregorianCalendar`, which is
 * available on every Android version.
 *
 * Conversions:
 * - [toUtcMidnightMillis] / [fromUtcMidnightMillis]: the format Material 3's DatePicker uses.
 * - [toLocalNoonMillis]: the instant stored as an expense's `transactionDate`. See
 *   docs/DECISIONS.md (D-008).
 * - [of]: the date that contains an instant in a given time zone ("today").
 *
 * @property month 1 = January ... 12 = December (unlike `java.util.Calendar`, which starts at 0).
 */
data class CalendarDate(val year: Int, val month: Int, val dayOfMonth: Int) : Comparable<CalendarDate> {

    init {
        require(month in 1..12) { "month must be 1..12, was $month" }
        require(dayOfMonth in 1..31) { "dayOfMonth must be 1..31, was $dayOfMonth" }
    }

    override fun compareTo(other: CalendarDate): Int =
        compareValuesBy(this, other, { it.year }, { it.month }, { it.dayOfMonth })

    /** Midnight UTC at the start of this date, in epoch milliseconds. */
    fun toUtcMidnightMillis(): Long = startOfDay(UTC).timeInMillis

    /**
     * 12:00 local time on this date in [timeZone], in epoch milliseconds.
     *
     * Noon is used because it is never skipped or repeated by daylight-saving changes. It
     * also keeps the same calendar date when viewed from any time zone within ±11 hours.
     */
    fun toLocalNoonMillis(timeZone: TimeZone): Long =
        startOfDay(timeZone).apply { set(Calendar.HOUR_OF_DAY, 12) }.timeInMillis

    private fun startOfDay(timeZone: TimeZone): Calendar =
        gregorianCalendar(timeZone).apply {
            clear()
            set(year, month - 1, dayOfMonth, 0, 0, 0)
        }

    companion object {
        private val UTC: TimeZone = TimeZone.getTimeZone("UTC")

        /** The date whose UTC midnight is [utcMidnightMillis] (as returned by DatePicker). */
        fun fromUtcMidnightMillis(utcMidnightMillis: Long): CalendarDate = of(utcMidnightMillis, UTC)

        /** The date on which [instantMillis] falls in [timeZone]. */
        fun of(instantMillis: Long, timeZone: TimeZone): CalendarDate {
            val calendar = gregorianCalendar(timeZone).apply { timeInMillis = instantMillis }
            return CalendarDate(
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH) + 1,
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH),
            )
        }

        // Always Gregorian, whatever the device locale (some locales default to other calendars).
        private fun gregorianCalendar(timeZone: TimeZone): Calendar = GregorianCalendar(timeZone, Locale.ROOT)
    }
}
