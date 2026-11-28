package com.yatha.moneytracker.domain.time

import com.yatha.moneytracker.testing.IST
import com.yatha.moneytracker.testing.UTC
import com.yatha.moneytracker.testing.millisAt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

class CalendarDateTest {

    private val newYork = TimeZone.getTimeZone("America/New_York")
    private val tokyo = TimeZone.getTimeZone("Asia/Tokyo")
    private val london = TimeZone.getTimeZone("Europe/London")

    @Test
    fun of_usesTheLocalDate_eitherSideOfLocalMidnight() {
        // 23:59:59 IST on 27 Nov is 18:29:59 UTC; 00:00 IST on 28 Nov is 18:30 UTC.
        assertEquals(CalendarDate(2026, 11, 27), CalendarDate.of(millisAt(UTC, 2026, 11, 27, 18, 29, 59), IST))
        assertEquals(CalendarDate(2026, 11, 28), CalendarDate.of(millisAt(UTC, 2026, 11, 27, 18, 30, 0), IST))
    }

    @Test
    fun sameInstant_canBeDifferentDates_inDifferentZones() {
        val instant = millisAt(UTC, 2026, 11, 27, 20, 0)

        assertEquals(CalendarDate(2026, 11, 28), CalendarDate.of(instant, IST))
        assertEquals(CalendarDate(2026, 11, 27), CalendarDate.of(instant, newYork))
    }

    @Test
    fun utcMidnightMillis_matchesDatePickerFormat_andRoundTrips() {
        val date = CalendarDate(2026, 11, 28)

        assertEquals(millisAt(UTC, 2026, 11, 28), date.toUtcMidnightMillis())
        listOf(
            date,
            CalendarDate(1970, 1, 1),
            CalendarDate(2026, 12, 31),
            CalendarDate(2027, 1, 1),
            CalendarDate(2028, 2, 29), // leap day
        ).forEach { assertEquals(it, CalendarDate.fromUtcMidnightMillis(it.toUtcMidnightMillis())) }
    }

    @Test
    fun localNoonMillis_isTwelveOClockInThatZone() {
        // 12:00 IST (UTC+5:30) is 06:30 UTC.
        assertEquals(millisAt(UTC, 2026, 11, 28, 6, 30), CalendarDate(2026, 11, 28).toLocalNoonMillis(IST))
    }

    @Test
    fun localNoon_keepsTheSameDate_whenViewedFromOtherZones() {
        val date = CalendarDate(2026, 11, 28)
        val storedInIndia = date.toLocalNoonMillis(IST)

        listOf(IST, UTC, london, newYork, tokyo).forEach { zone ->
            assertEquals("viewed in ${zone.id}", date, CalendarDate.of(storedInIndia, zone))
        }
    }

    @Test
    fun localNoon_isWellDefined_onDaylightSavingChangeDays() {
        // New York: clocks go forward on 8 Mar 2026 and back on 1 Nov 2026.
        listOf(CalendarDate(2026, 3, 8), CalendarDate(2026, 11, 1)).forEach { date ->
            assertEquals(date, CalendarDate.of(date.toLocalNoonMillis(newYork), newYork))
        }
        assertEquals(millisAt(UTC, 2026, 3, 8, 16, 0), CalendarDate(2026, 3, 8).toLocalNoonMillis(newYork))
        assertEquals(millisAt(UTC, 2026, 11, 1, 17, 0), CalendarDate(2026, 11, 1).toLocalNoonMillis(newYork))
    }

    @Test
    fun dates_compareChronologically() {
        val ordered = listOf(
            CalendarDate(2025, 12, 31),
            CalendarDate(2026, 1, 1),
            CalendarDate(2026, 11, 28),
            CalendarDate(2026, 11, 29),
            CalendarDate(2026, 12, 1),
        )

        assertEquals(ordered, ordered.shuffled(java.util.Random(42)).sorted())
        assertTrue(CalendarDate(2026, 11, 28) < CalendarDate(2026, 11, 29))
    }

    @Test
    fun invalidMonthOrDay_isRejected() {
        listOf(0 to 1, 13 to 1, 1 to 0, 1 to 32).forEach { (month, day) ->
            assertThrows(IllegalArgumentException::class.java) { CalendarDate(2026, month, day) }
        }
    }

    @Test
    fun deviceLocaleWithNonGregorianCalendar_stillGivesGregorianDates() {
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("th-TH-u-ca-buddhist"))

            assertEquals(CalendarDate(2026, 11, 28), CalendarDate.of(millisAt(UTC, 2026, 11, 28, 6, 30), IST))
        } finally {
            Locale.setDefault(original)
        }
    }
}
