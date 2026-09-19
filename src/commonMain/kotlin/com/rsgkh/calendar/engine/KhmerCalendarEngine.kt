@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package com.rsgkh.calendar.engine

import kotlin.js.JsExport

/** Month indices: Migasir=0, Boss=1, Meak=2, Phalkun=3, Chet=4, Pisakh=5,
 * Jestha=6, Asadh=7, Srapon=8, Phutrobot=9, Assoch=10, Kattik=11,
 * first Asadh=12, second Asadh=13. */
@JsExport
class LunarDate internal constructor(
    val day: Int, val waxing: Boolean, val month: Int,
    val buddhistYear: Int, val monthLength: Int,
) {
    init { freezeValue(this) }
    val isHolyDay: Boolean get() = day == 8 || (waxing && day == 15) || (!waxing && day == monthLength - 15)
    val isShavingDay: Boolean get() = day == 7 || (waxing && day == 14) || (!waxing && day == monthLength - 16)
}

@JsExport
class CalendarDate internal constructor(
    val date: GregorianDate, val lunar: LunarDate, val animalYear: Int, val sak: Int,
    val animalYearChangesToday: Boolean, val sakChangesToday: Boolean,
) {
    init { freezeValue(this) }
}

/** Traditional-arithmetic estimate of the Moha Sangkran arrival minute-of-day on the
 * festival's first date. This is **not** an official or certified time: the arithmetic
 * can only produce minutes on a 24-minute lattice and disagrees with reviewed
 * publications by 1–24 minutes. Published arrival clocks are maintained per year as
 * source-tagged data outside this engine. See docs/references.md. */
@JsExport
class ArrivalEstimate internal constructor(val minuteOfDay: Int) {
    init { freezeValue(this) }
    val hour: Int get() = minuteOfDay / 60
    val minute: Int get() = minuteOfDay % 60
}

@JsExport
class NewYearCelebration internal constructor(
    val start: GregorianDate, val days: Int, val arrivalEstimate: ArrivalEstimate,
) {
    init { freezeValue(this) }
    val end: GregorianDate get() = start.plusDays(days - 1)
    val dates: Array<GregorianDate> get() = Array(days) { start.plusDays(it) }
}

/** Pure calendar API. Dates are civil dates; year labels do not express arrival instants. */
@JsExport
class KhmerCalendarEngine {
    val version: String get() = "0.3.0"
    val minYear: Int get() = MIN_YEAR
    val maxYear: Int get() = MAX_YEAR

    fun fromGregorian(year: Int, month: Int, day: Int): CalendarDate {
        val date = GregorianDate(year, month, day)
        requireCalendarYear(year)
        val lunar = LunarCalendar.fromGregorian(date)
        val newYear = SolarNewYear.forYear(year)
        return CalendarDate(date, lunar,
            floorMod(year - 4 - if (date.epochDay < newYear.start.epochDay) 1 else 0, 12),
            floorMod(year - 638 - if (date.epochDay < newYear.end.epochDay) 1 else 0, 10),
            date == newYear.start, date == newYear.end)
    }

    /** Buddhist Era year and distinct leap-month indices make a lunar date unambiguous. */
    fun toGregorian(buddhistYear: Int, month: Int, day: Int, waxing: Boolean): GregorianDate =
        LunarCalendar.toGregorian(buddhistYear, month, day, waxing)

    fun newYear(year: Int): NewYearCelebration = SolarNewYear.forYear(year)

    /** Evaluate anchors in [year]; offsets and durations can extend into adjacent years. */
    fun evaluateRule(year: Int, rule: RecurrenceRule, dateOverride: EventDateOverride? = null): Array<EventOccurrence> =
        evaluateRecurrence(year, rule, dateOverride)
}

internal const val MIN_YEAR = 1800
internal const val MAX_YEAR = 2200
internal fun requireInteger(value: Int) {
    // JS callers can pass fractional numbers despite a TypeScript `number` declaration.
    require(value.toDouble().isFinite() && value.toDouble() % 1.0 == 0.0) { "Expected an integer" }
}
internal fun requireCalendarYear(year: Int) {
    requireInteger(year)
    require(year in MIN_YEAR..MAX_YEAR) { "Supported calendar years: 1800..2200" }
}
