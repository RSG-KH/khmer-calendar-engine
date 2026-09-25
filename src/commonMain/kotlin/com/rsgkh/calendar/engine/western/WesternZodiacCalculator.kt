@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package com.rsgkh.calendar.engine.western

import com.rsgkh.calendar.engine.GregorianDate
import com.rsgkh.calendar.engine.daysInMonth
import com.rsgkh.calendar.engine.freezeValue
import com.rsgkh.calendar.engine.requireFiniteDouble
import com.rsgkh.calendar.engine.requireInteger
import kotlin.js.JsExport
import kotlin.math.abs
import kotlin.math.floor

@JsExport
object WesternZodiacCalculator {

    init {
        freezeValue(this)
    }

    /**
     * Primary calculation taking UTC components.
     * All Double parameters are guarded by requireFiniteDouble to reject malformed JS inputs.
     */
    fun calculateHoroscopeUtc(
        yearUtc: Int,
        monthUtc: Int,
        dayUtc: Int,
        hourUtc: Int,
        minuteUtc: Int,
        secondUtc: Double = 0.0,
        latitudeDeg: Double,
        longitudeDeg: Double
    ): WesternHoroscope {
        requireInteger(yearUtc)
        requireInteger(monthUtc)
        requireInteger(dayUtc)
        requireInteger(hourUtc)
        requireInteger(minuteUtc)
        requireFiniteDouble(secondUtc, "secondUtc")
        requireFiniteDouble(latitudeDeg, "latitudeDeg")
        requireFiniteDouble(longitudeDeg, "longitudeDeg")

        require(yearUtc in 1800..2200) { "Year must be 1800..2200, received $yearUtc" }
        require(monthUtc in 1..12) { "Month must be 1..12, received $monthUtc" }
        require(dayUtc in 1..daysInMonth(yearUtc, monthUtc)) {
            "Invalid Gregorian day $dayUtc for month $monthUtc in year $yearUtc"
        }
        require(hourUtc in 0..23) { "Hour must be 0..23, received $hourUtc" }
        require(minuteUtc in 0..59) { "Minute must be 0..59, received $minuteUtc" }
        require(secondUtc >= 0.0 && secondUtc < 60.0) {
            "Second must be in [0.0, 60.0), received $secondUtc"
        }
        require(latitudeDeg in -90.0..90.0) {
            "Latitude must be -90..+90, received $latitudeDeg"
        }
        require(longitudeDeg in -180.0..180.0) {
            "Longitude must be -180..+180, received $longitudeDeg"
        }

        val jdUt = MeeusEngine.julianDay(yearUtc, monthUtc, dayUtc, hourUtc, minuteUtc, secondUtc)
        val decimalYear = yearUtc + (monthUtc - 1.0 + (dayUtc - 1.0 + (hourUtc + (minuteUtc + secondUtc / 60.0) / 60.0) / 24.0) / daysInMonth(yearUtc, monthUtc)) / 12.0
        val dtSec = MeeusEngine.deltaTSeconds(decimalYear)
        val jdTt = jdUt + dtSec / 86400.0

        val tUt = MeeusEngine.julianCenturies(jdUt)
        val tTt = MeeusEngine.julianCenturies(jdTt)

        val (trueEps, deltaPsi, _) = MeeusEngine.nutationAndObliquity(tTt)
        val gast = MeeusEngine.apparentSiderealTime(jdUt, tUt, trueEps, deltaPsi)
        val ramc = MeeusEngine.normalizeDegrees(gast + longitudeDeg)

        val sunLong = MeeusEngine.sunLongitude(tTt, deltaPsi)
        val moonLong = MeeusEngine.moonLongitude(tTt, deltaPsi)
        val (ascLong, ascStatus) = MeeusEngine.ascendant(ramc, trueEps, latitudeDeg)
        val mcLong = MeeusEngine.midheaven(ramc, trueEps)

        val isPolar = abs(latitudeDeg) >= (90.0 - trueEps)

        return WesternHoroscope(
            sun = MeeusEngine.toZodiacPosition(sunLong),
            moon = MeeusEngine.toZodiacPosition(moonLong),
            ascendant = ascLong?.let { MeeusEngine.toZodiacPosition(it) },
            midheaven = MeeusEngine.toZodiacPosition(mcLong),
            isPolarLatitude = isPolar,
            ascendantStatus = ascStatus
        )
    }

    /**
     * Convenience overload for local civil time and decimal UTC offset.
     * Original civil components are validated prior to bounded UTC normalization with carry reconciliation.
     */
    fun calculateHoroscope(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Double = 0.0,
        utcOffsetHours: Double,
        latitudeDeg: Double,
        longitudeDeg: Double
    ): WesternHoroscope {
        requireInteger(year)
        requireInteger(month)
        requireInteger(day)
        requireInteger(hour)
        requireInteger(minute)
        requireFiniteDouble(second, "second")
        requireFiniteDouble(utcOffsetHours, "utcOffsetHours")
        requireFiniteDouble(latitudeDeg, "latitudeDeg")
        requireFiniteDouble(longitudeDeg, "longitudeDeg")

        require(year in 1800..2200) { "Year must be 1800..2200, received $year" }
        require(month in 1..12) { "Month must be 1..12, received $month" }
        require(day in 1..daysInMonth(year, month)) { "Invalid Gregorian day $day for month $month in year $year" }
        require(hour in 0..23) { "Hour must be 0..23, received $hour" }
        require(minute in 0..59) { "Minute must be 0..59, received $minute" }
        require(second >= 0.0 && second < 60.0) { "Second must be in [0.0, 60.0), received $second" }
        require(utcOffsetHours in -14.0..14.0) {
            "UTC offset hours must be between -14 and +14, received $utcOffsetHours"
        }

        // Bounded UTC normalization with floating-point carry reconciliation (R3-004)
        val localDate = GregorianDate(year, month, day)
        val totalSeconds = hour * 3600.0 + minute * 60.0 + second - (utcOffsetHours * 3600.0)
        var dayOffset = floor(totalSeconds / 86400.0).toInt()
        var utcSecondsInDay = totalSeconds - dayOffset * 86400.0

        // Reconcile floating-point rounding near day boundaries (e.g. UTC+1.1 at 01:06:00)
        if (utcSecondsInDay >= 86400.0 - 1e-9) {
            utcSecondsInDay -= 86400.0
            dayOffset += 1
        } else if (utcSecondsInDay < 0.0) {
            utcSecondsInDay += 86400.0
            dayOffset -= 1
        }

        var hUtc = floor(utcSecondsInDay / 3600.0).toInt()
        val remAfterHour = utcSecondsInDay - hUtc * 3600.0
        var mUtc = floor(remAfterHour / 60.0).toInt()
        var sUtc = remAfterHour - mUtc * 60.0

        // Second carry reconciliation
        if (sUtc >= 60.0 - 1e-9) {
            sUtc = 0.0
            mUtc += 1
            if (mUtc >= 60) {
                mUtc = 0
                hUtc += 1
                if (hUtc >= 24) {
                    hUtc = 0
                    dayOffset += 1
                }
            }
        } else if (sUtc < 0.0) {
            sUtc = 0.0
        }

        val utcDate = localDate.plusDays(dayOffset)
        require(utcDate.year in 1800..2200) {
            "Normalized UTC year must be in 1800..2200, received ${utcDate.year}"
        }

        return calculateHoroscopeUtc(
            utcDate.year,
            utcDate.month,
            utcDate.day,
            hUtc,
            mUtc,
            sUtc,
            latitudeDeg,
            longitudeDeg
        )
    }
}
