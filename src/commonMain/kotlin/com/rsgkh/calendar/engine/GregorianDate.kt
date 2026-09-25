@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package com.rsgkh.calendar.engine

import kotlin.js.JsExport

/** A proleptic Gregorian civil date. No device time zone or timestamp is involved. */
@JsExport
data class GregorianDate(val year: Int, val month: Int, val day: Int) {
    init {
        requireInteger(year)
        requireInteger(month)
        requireInteger(day)
        require(year in 1..9999) { "Gregorian year must be 1..9999" }
        require(month in 1..12) { "Gregorian month must be 1..12" }
        require(day in 1..daysInMonth(year, month)) { "Invalid Gregorian day" }
        freezeValue(this)
    }

    val iso: String get() = "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    val epochDay: Int get() {
        val y = year - 1
        val beforeYear = 365 * y + y / 4 - y / 100 + y / 400
        var beforeMonth = 0
        for (m in 1 until month) beforeMonth += daysInMonth(year, m)
        return beforeYear + beforeMonth + day - 1 - DAYS_BEFORE_1970
    }
    /** ISO weekday: Monday = 1, Sunday = 7. */
    val dayOfWeek: Int get() = floorMod(epochDay + 3, 7) + 1

    fun plusDays(days: Int): GregorianDate {
        requireInteger(days)
        val target = epochDay.toLong() + days
        require(target in MIN_EPOCH.toLong()..MAX_EPOCH.toLong()) { "Gregorian date outside 1..9999" }
        return fromEpochDay(target.toInt())
    }

    override fun toString(): String = iso
}

internal fun floorMod(value: Int, modulus: Int): Int = (value % modulus + modulus) % modulus
// Kotlin vals are immutable on the JVM; JavaScript also needs runtime protection for exported values.
internal expect fun freezeValue(value: Any)
internal expect fun requireInteger(value: Int)
internal fun isGregorianLeap(year: Int): Boolean = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
internal fun daysInMonth(year: Int, month: Int): Int = when (month) {
    2 -> if (isGregorianLeap(year)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}
private const val DAYS_BEFORE_1970 = 719162
private const val MIN_EPOCH = -719162
private const val MAX_EPOCH = 2932896

/** Inverts the day count using year and month lengths, including century leap rules. */
internal fun fromEpochDay(epoch: Int): GregorianDate {
    require(epoch in MIN_EPOCH..MAX_EPOCH)
    val absolute = epoch + DAYS_BEFORE_1970
    var low = 1
    var high = 9999
    while (low < high) {
        val mid = (low + high + 1) / 2
        val y = mid - 1
        if (365 * y + y / 4 - y / 100 + y / 400 <= absolute) low = mid else high = mid - 1
    }
    var remaining = absolute - GregorianDate(low, 1, 1).epochDay - DAYS_BEFORE_1970
    var month = 1
    while (remaining >= daysInMonth(low, month)) {
        remaining -= daysInMonth(low, month)
        month++
    }
    return GregorianDate(low, month, remaining + 1)
}
