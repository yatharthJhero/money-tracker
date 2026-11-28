package com.yatha.moneytracker.testing

import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

val IST: TimeZone = TimeZone.getTimeZone("Asia/Kolkata")
val UTC: TimeZone = TimeZone.getTimeZone("UTC")

/** Epoch milliseconds of the given wall-clock time in [zone]. [month] is 1-based. */
fun millisAt(
    zone: TimeZone,
    year: Int,
    month: Int,
    day: Int,
    hour: Int = 0,
    minute: Int = 0,
    second: Int = 0,
): Long = GregorianCalendar(zone, Locale.ROOT).apply {
    clear()
    set(year, month - 1, day, hour, minute, second)
}.timeInMillis
