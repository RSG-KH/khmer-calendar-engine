@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package com.rsgkh.calendar.engine.western

import com.rsgkh.calendar.engine.floorMod
import com.rsgkh.calendar.engine.freezeValue
import com.rsgkh.calendar.engine.requireFiniteDouble
import com.rsgkh.calendar.engine.requireInteger
import kotlin.js.JsExport
import kotlin.math.abs
import kotlin.math.floor

@JsExport
enum class AscendantStatus(val code: String, val description: String) {
    CALCULATED("CALCULATED", "Normal unique ascending horizon intersection"),
    COINCIDENT_PLANES("COINCIDENT_PLANES", "Horizon and ecliptic planes coincide; no unique intersection"),
    POLAR_NON_RISING("POLAR_NON_RISING", "Circumpolar ecliptic condition; Ascendant represents geometric intersection"),
    DEGENERATE_POLE("DEGENERATE_POLE", "Geographic pole latitude; diurnal rotation is parallel to horizon");

    init {
        freezeValue(this)
    }
}

@JsExport
enum class WesternZodiacSign(
    val index: Int,
    val symbol: String,
    val englishName: String,
    val khmerName: String,
    val element: String,
    val modality: String
) {
    ARIES(0, "♈", "Aries", "មេស", "Fire", "Cardinal"),
    TAURUS(1, "♉", "Taurus", "ឧសភ", "Earth", "Fixed"),
    GEMINI(2, "♊", "Gemini", "មេថុន", "Air", "Mutable"),
    CANCER(3, "♋", "Cancer", "កក្កដា", "Water", "Cardinal"),
    LEO(4, "♌", "Leo", "សីហ", "Fire", "Fixed"),
    VIRGO(5, "♍", "Virgo", "កញ្ញា", "Earth", "Mutable"),
    LIBRA(6, "♎", "Libra", "តុលា", "Air", "Cardinal"),
    SCORPIO(7, "♏", "Scorpio", "វិច្ឆិកា", "Water", "Fixed"),
    SAGITTARIUS(8, "♐", "Sagittarius", "ធ្នូ", "Fire", "Mutable"),
    CAPRICORN(9, "♑", "Capricorn", "មករ", "Earth", "Cardinal"),
    AQUARIUS(10, "♒", "Aquarius", "កុម្ភៈ", "Air", "Fixed"),
    PISCES(11, "♓", "Pisces", "មីន", "Water", "Mutable");

    init {
        freezeValue(this)
    }

    companion object {
        fun fromIndex(index: Int): WesternZodiacSign {
            requireInteger(index)
            return entries[floorMod(index, 12)]
        }
    }
}

@JsExport
data class ZodiacPosition(
    val sign: WesternZodiacSign,
    val degreeInSign: Double,    // 0.0 <= degreeInSign < 30.0
    val wholeDegree: Int,        // 0 .. 29
    val minute: Int,             // 0 .. 59
    val second: Double,          // 0.0 <= second < 60.0
    val totalLongitude: Double   // 0.0 <= totalLongitude < 360.0
) {
    init {
        requireFiniteDouble(totalLongitude, "totalLongitude")
        requireFiniteDouble(degreeInSign, "degreeInSign")
        requireFiniteDouble(second, "second")
        requireInteger(wholeDegree)
        requireInteger(minute)

        // Strict half-open range constraints before tolerance checks (R3-003)
        require(totalLongitude in 0.0..<360.0) { "totalLongitude must be in [0, 360), received $totalLongitude" }
        require(degreeInSign >= 0.0 && degreeInSign < 30.0) { "degreeInSign must be in [0, 30), received $degreeInSign" }
        require(wholeDegree in 0..29) { "wholeDegree must be in 0..29, received $wholeDegree" }
        require(minute in 0..59) { "minute must be in 0..59, received $minute" }
        require(second >= 0.0 && second < 60.0) { "second must be in [0, 60), received $second" }

        val expSignIdx = floor(totalLongitude / 30.0).toInt().coerceIn(0, 11)
        val expSign = WesternZodiacSign.fromIndex(expSignIdx)
        // Verify genuine enum referential identity (rejects fake JS objects)
        require(sign === expSign) {
            "Sign mismatch for longitude $totalLongitude: expected $expSign, got $sign"
        }

        val expDegInSign = totalLongitude - expSignIdx * 30.0
        require(abs(degreeInSign - expDegInSign) < 1e-5) {
            "degreeInSign mismatch: expected $expDegInSign, got $degreeInSign"
        }
        val expWholeDeg = floor(expDegInSign).toInt()
        require(wholeDegree == expWholeDeg) {
            "wholeDegree mismatch: expected $expWholeDeg, got $wholeDegree"
        }
        val remMin = (expDegInSign - expWholeDeg) * 60.0
        val expMin = floor(remMin).toInt()
        require(minute == expMin) {
            "minute mismatch: expected $expMin, got $minute"
        }
        val expSec = (remMin - expMin) * 60.0
        require(abs(second - expSec) < 1e-4) {
            "second mismatch: expected $expSec, got $second"
        }
        freezeValue(this)
    }

    companion object {
        fun fromLongitude(totalLongitude: Double): ZodiacPosition {
            // Validate primitive finite number before any arithmetic or normalization (R3-002)
            requireFiniteDouble(totalLongitude, "totalLongitude")
            return MeeusEngine.toZodiacPosition(totalLongitude)
        }
    }

    val formatted: String
        get() = "${sign.englishName} ${wholeDegree}° ${minute}' ${second.toInt()}\""
}

@JsExport
data class WesternHoroscope(
    val sun: ZodiacPosition,
    val moon: ZodiacPosition,
    val ascendant: ZodiacPosition?,
    val midheaven: ZodiacPosition,
    val isPolarLatitude: Boolean,
    val ascendantStatus: AscendantStatus
) {
    init {
        // Enforce genuine AscendantStatus enum membership (rejects null, fake duck-typed objects, or unknown values from JS)
        require(AscendantStatus.entries.any { it === ascendantStatus }) {
            "ascendantStatus must be a genuine AscendantStatus instance, received $ascendantStatus"
        }
        // Enforce Ascendant nullability and status invariant consistency (R3-003)
        when (ascendantStatus) {
            AscendantStatus.CALCULATED, AscendantStatus.POLAR_NON_RISING -> {
                require(ascendant != null) { "ascendant must not be null when ascendantStatus is $ascendantStatus" }
            }
            AscendantStatus.COINCIDENT_PLANES, AscendantStatus.DEGENERATE_POLE -> {
                require(ascendant == null) { "ascendant must be null when ascendantStatus is $ascendantStatus" }
            }
        }
        freezeValue(this)
    }
}
