@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package com.rsgkh.calendar.engine

import kotlin.js.JsExport

@JsExport
object ChineseZodiacCalculator {

    init {
        freezeValue(this)
    }

    /**
     * Converts a civil Gregorian date to an astronomical Julian Day Number (JDN)
     * using the Fliegel & van Flandern integer algorithm.
     * Output is a 32-bit signed Int (valid for years 1..9999; max intermediate
     * term is ~5.4M, far below Int overflow).
     */
    fun gregorianToJdn(year: Int, month: Int, day: Int): Int {
        listOf(year, month, day).forEach(::requireInteger)
        require(year in 1..9999) { "Gregorian year must be 1..9999" }
        require(month in 1..12) { "Gregorian month must be 1..12" }
        require(day in 1..daysInMonth(year, month)) { "Invalid Gregorian day" }

        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    }

    /** JDN calculation directly from the engine's GregorianDate model. */
    fun gregorianDateToJdn(date: GregorianDate): Int =
        gregorianToJdn(date.year, date.month, date.day)

    /**
     * Computes the 60-day pillar (Ganzhi) for a Gregorian date.
     * The sexagenary day cycle is an unbroken modulo-60 counter with anchor offset +49.
     */
    fun getDayPillar(year: Int, month: Int, day: Int): GanzhiPillar {
        val jdn = gregorianToJdn(year, month, day)
        val cycle = floorMod(jdn + 49, 60)
        val stem = HeavenlyStem.fromIndex(cycle % 10)
        val branch = EarthlyBranch.fromIndex(cycle % 12)
        return GanzhiPillar(stem, branch)
    }

    fun getDayPillarForGregorianDate(date: GregorianDate): GanzhiPillar =
        getDayPillar(date.year, date.month, date.day)

    /**
     * Returns the Earthly Branch (Zodiac animal) for an hour in civil time (0..23).
     * Windows start with the Rat (Zi) at 23:00.
     */
    fun getHourBranch(hourOfDay: Int): EarthlyBranch {
        requireInteger(hourOfDay)
        require(hourOfDay in 0..23) { "Hour must be 0..23, received $hourOfDay" }
        val branchIndex = ((hourOfDay + 1) / 2) % 12
        return EarthlyBranch.fromIndex(branchIndex)
    }

    /**
     * Computes the Hour Pillar from a Day Stem using the classical
     * "Five Rats Seeking Day" rule (五鼠遁日起时法).
     *
     * Note: 23:00 belongs to the upcoming day's Rat (Zi) branch. Callers using
     * this raw primitive for 23:00 must supply tomorrow's stem if adhering to
     * the midnight boundary. Use [getHourPillarForDate] for automated date handling.
     */
    fun getHourPillar(dayStem: HeavenlyStem, hourOfDay: Int): GanzhiPillar {
        val branch = getHourBranch(hourOfDay)
        val baseStemIndex = (dayStem.index % 5) * 2
        val hourStem = HeavenlyStem.fromIndex(baseStemIndex + branch.index)
        return GanzhiPillar(hourStem, branch)
    }

    /**
     * Computes the complete Hour Pillar for a Gregorian date and hour.
     * Automatically rolls the day stem forward at 23:00 (late Rat hour).
     */
    fun getHourPillarForDate(year: Int, month: Int, day: Int, hourOfDay: Int): GanzhiPillar {
        requireInteger(hourOfDay)
        require(hourOfDay in 0..23) { "Hour must be 0..23, received $hourOfDay" }
        val jdn = gregorianToJdn(year, month, day)
        val effectiveJdn = if (hourOfDay == 23) jdn + 1 else jdn
        val dayCycle = floorMod(effectiveJdn + 49, 60)
        val effectiveDayStem = HeavenlyStem.fromIndex(dayCycle % 10)
        return getHourPillar(effectiveDayStem, hourOfDay)
    }

    fun getHourPillarForGregorianDate(date: GregorianDate, hourOfDay: Int): GanzhiPillar =
        getHourPillarForDate(date.year, date.month, date.day, hourOfDay)
}
