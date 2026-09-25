package com.rsgkh.calendar.engine.western

import com.rsgkh.calendar.engine.requireFiniteDouble
import kotlin.math.*

internal object MeeusEngine {

    private const val PI_OVER_180 = PI / 180.0
    private const val ONE_EIGHTY_OVER_PI = 180.0 / PI

    fun radians(deg: Double): Double = deg * PI_OVER_180
    fun degrees(rad: Double): Double = rad * ONE_EIGHTY_OVER_PI
    fun normalizeDegrees(deg: Double): Double = (deg % 360.0 + 360.0) % 360.0

    /**
     * Complete 10-interval piecewise Delta-T (TT - UT1) in seconds based on
     * Espenak & Meeus (2004/2006) for the Five Millennium Canon of Solar Eclipses.
     * Evaluated on decimal year y.
     */
    fun deltaTSeconds(decimalYear: Double): Double {
        val y = decimalYear
        return when {
            y < 1700.0 -> {
                val u = (y - 1820.0) / 100.0
                -20.0 + 32.0 * u * u
            }
            y < 1800.0 -> {
                val t = y - 1700.0
                8.83 + 0.1603 * t - 0.0059285 * t * t + 0.00013336 * t * t * t - (t * t * t * t) / 1174000.0
            }
            y < 1860.0 -> {
                val t = y - 1800.0
                13.72 - 0.332447 * t + 0.0068612 * t * t + 0.0041116 * t * t * t -
                    0.00037436 * t * t * t * t + 0.0000121272 * t.pow(5) -
                    0.0000001699 * t.pow(6) + 0.000000000875 * t.pow(7)
            }
            y < 1900.0 -> {
                val t = y - 1860.0
                7.62 + 0.5737 * t - 0.251754 * t * t + 0.01680668 * t * t * t -
                    0.0004473624 * t * t * t * t + t.pow(5) / 233174.0
            }
            y < 1920.0 -> {
                val t = y - 1900.0
                -2.79 + 1.494119 * t - 0.0598939 * t * t + 0.0061966 * t * t * t - 0.000197 * t * t * t * t
            }
            y < 1941.0 -> {
                val t = y - 1920.0
                21.20 + 0.84493 * t - 0.076100 * t * t + 0.0020936 * t * t * t
            }
            y < 1961.0 -> {
                val t = y - 1950.0
                29.07 + 0.407 * t - (t * t) / 233.0 + (t * t * t) / 2547.0
            }
            y < 1986.0 -> {
                val t = y - 1975.0
                45.45 + 1.067 * t - (t * t) / 260.0 - (t * t * t) / 718.0
            }
            y < 2005.0 -> {
                val t = y - 2000.0
                63.86 + 0.3345 * t - 0.060374 * t * t + 0.0017275 * t * t * t +
                    0.000651814 * t * t * t * t + 0.00002373599 * t.pow(5)
            }
            y < 2050.0 -> {
                val t = y - 2000.0
                62.92 + 0.32217 * t + 0.005589 * t * t
            }
            y < 2150.0 -> {
                val u = (y - 1820.0) / 100.0
                -20.0 + 32.0 * u * u - 0.5628 * (2150.0 - y)
            }
            else -> {
                val u = (y - 1820.0) / 100.0
                -20.0 + 32.0 * u * u
            }
        }
    }

    fun julianDay(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Double): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + a / 4
        val jd0 = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
        val dayFraction = (hour + (minute + second / 60.0) / 60.0) / 24.0
        return jd0 + dayFraction
    }

    fun julianCenturies(jd: Double): Double = (jd - 2451545.0) / 36525.0

    /**
     * True obliquity and nutation in longitude (Meeus Ch. 22).
     */
    fun nutationAndObliquity(tTt: Double): Triple<Double, Double, Double> {
        val omega = 125.04452 - 1934.136261 * tTt + 0.0020708 * tTt * tTt + (tTt * tTt * tTt) / 450000.0
        val l0 = 280.46646 + 36000.76983 * tTt
        val omegaRad = radians(omega)
        val l0Rad = radians(l0)

        val deltaPsi = -0.0047778 * sin(omegaRad) - 0.0003667 * sin(2.0 * l0Rad)
        val deltaEps = 0.0025556 * cos(omegaRad) + 0.0001583 * cos(2.0 * l0Rad)

        val eps0 = 23.43929111 - 0.013004167 * tTt - 1.63889e-7 * tTt * tTt + 5.03611e-7 * tTt * tTt * tTt
        val trueEps = eps0 + deltaEps

        return Triple(trueEps, deltaPsi, omega)
    }

    /**
     * Greenwich Apparent Sidereal Time (GAST) in degrees.
     */
    fun apparentSiderealTime(jdUt: Double, tUt: Double, trueEpsDeg: Double, deltaPsiDeg: Double): Double {
        val d = jdUt - 2451545.0
        val gmst = 280.46061837 + 360.98564736629 * d + 0.000387933 * tUt * tUt - (tUt * tUt * tUt) / 38710000.0
        val equationOfEquinoxes = deltaPsiDeg * cos(radians(trueEpsDeg))
        return normalizeDegrees(gmst + equationOfEquinoxes)
    }

    /**
     * Tropical Sun longitude (Meeus Ch. 25).
     */
    fun sunLongitude(tTt: Double, deltaPsiDeg: Double): Double {
        val l0 = 280.46646 + 36000.76983 * tTt + 0.0003032 * tTt * tTt
        val m = 357.52911 + 35999.05029 * tTt - 0.0001537 * tTt * tTt
        val mRad = radians(m)
        val c = (1.914602 - 0.004817 * tTt - 0.000014 * tTt * tTt) * sin(mRad) +
                (0.019993 - 0.000101 * tTt) * sin(2.0 * mRad) +
                0.000289 * sin(3.0 * mRad)
        val trueLong = l0 + c
        val apparentLong = trueLong - 0.00569 + deltaPsiDeg
        return normalizeDegrees(apparentLong)
    }

    // 60 terms of Meeus Table 47.A packed into argument bytes (D, M, M', F) and amplitudes (10^-6 deg)
    private val LUNAR_ARGS = byteArrayOf(
        0, 0, 1, 0,   2, 0,-1, 0,   2, 0, 0, 0,   0, 0, 2, 0,   0, 1, 0, 0,
        0, 0, 0, 2,   2, 0,-2, 0,   2,-1,-1, 0,   2, 0, 1, 0,   2,-1, 0, 0,
        0, 1,-1, 0,   1, 0, 0, 0,   0, 1, 1, 0,   2, 0, 0,-2,   0, 0, 1, 2,
        0, 0, 1,-2,   4, 0,-1, 0,   0, 0, 3, 0,   4, 0,-2, 0,   2, 1,-1, 0,
        2, 1, 0, 0,   1, 0,-1, 0,   1, 1, 0, 0,   2,-1, 1, 0,   2, 0, 2, 0,
        4, 0, 0, 0,   2, 0,-3, 0,   0, 1,-2, 0,   2, 0,-1, 2,   2,-1,-2, 0,
        1, 0, 1, 0,   2,-2, 0, 0,   0, 1, 2, 0,   0, 2, 0, 0,   2,-2,-1, 0,
        2, 0, 1,-2,   2, 0, 0, 2,   4,-1,-1, 0,   0, 0, 2, 2,   3, 0,-1, 0,
        2, 1, 1, 0,   4,-1,-2, 0,   0, 2,-1, 0,   2, 2,-1, 0,   2, 1,-2, 0,
        2,-1, 0,-2,   4, 0, 1, 0,   0, 0, 4, 0,   4,-1, 0, 0,   1, 0,-2, 0,
        2, 1, 0,-2,   0, 0, 2,-2,   1, 1, 1, 0,   3, 0,-2, 0,   4, 0,-3, 0,
        2,-1, 2, 0,   0, 2, 1, 0,   1, 1,-1, 0,   2, 0, 3, 0,   2, 0,-1,-2
    )

    private val LUNAR_AMPS = intArrayOf(
         6288774,  1274027,   658314,   213618,  -185116,
        -114332,    58793,    57066,    53322,    45758,
         -40923,   -34720,   -30383,    15327,   -12528,
          10980,    10675,    10034,     8548,    -7888,
          -6766,    -5163,     4987,     4036,     3994,
           3861,     3665,    -2689,    -2602,     2390,
          -2348,     2236,    -2120,    -2069,     2048,
          -1773,    -1595,     1215,    -1110,     -892,
           -810,      759,     -713,     -700,      691,
            596,      549,      537,      520,     -487,
           -399,     -381,      351,     -340,      330,
            327,     -323,      299,      294,        0
    )

    /**
     * Tropical Moon longitude (Meeus Ch. 47).
     */
    fun moonLongitude(tTt: Double, deltaPsiDeg: Double): Double {
        val lp = 218.3164477 + 481267.88123421 * tTt - 0.0015786 * tTt * tTt + (tTt * tTt * tTt) / 538841.0 - (tTt * tTt * tTt * tTt) / 65194000.0
        val d  = 297.8501921 + 445267.1114034  * tTt - 0.0018819 * tTt * tTt + (tTt * tTt * tTt) / 545868.0 - (tTt * tTt * tTt * tTt) / 113065000.0
        val m  = 357.5291092 + 35999.0502909   * tTt - 0.0001536 * tTt * tTt + (tTt * tTt * tTt) / 24490000.0
        val mp = 134.9633964 + 477198.8675055  * tTt + 0.0087414 * tTt * tTt + (tTt * tTt * tTt) / 69699.0  - (tTt * tTt * tTt * tTt) / 14712000.0
        val f  = 93.2720950  + 483202.0175233  * tTt - 0.0036539 * tTt * tTt - (tTt * tTt * tTt) / 3526000.0 + (tTt * tTt * tTt * tTt) / 863310000.0

        val a1 = radians(119.75 + 131.849 * tTt)
        val a2 = radians(53.09 + 479264.290 * tTt)

        val e = 1.0 - 0.002516 * tTt - 0.0000074 * tTt * tTt
        val e2 = e * e

        val dr = radians(d)
        val mr = radians(m)
        val mpr = radians(mp)
        val fr = radians(f)

        var sigmal = 0.0
        for (i in 0 until 60) {
            val base = i * 4
            val a = LUNAR_ARGS[base].toInt()
            val b = LUNAR_ARGS[base + 1].toInt()
            val c = LUNAR_ARGS[base + 2].toInt()
            val dd = LUNAR_ARGS[base + 3].toInt()
            var coeff = LUNAR_AMPS[i].toDouble()

            if (abs(b) == 1) coeff *= e
            else if (abs(b) == 2) coeff *= e2

            val arg = a * dr + b * mr + c * mpr + dd * fr
            sigmal += coeff * sin(arg)
        }

        sigmal += 3958.0 * sin(a1) + 1962.0 * sin(radians(lp - f)) + 318.0 * sin(a2)
        val apparent = lp + sigmal / 1000000.0 + deltaPsiDeg
        return normalizeDegrees(apparent)
    }

    /**
     * Ascendant with Scaled Vector Formulation & Singularity Detection.
     * Avoids tan(latitude) division-by-zero at poles and detects coincident planes
     * in both Northern (RAMC=270, Lat=90-eps) and Southern (RAMC=90, Lat=-(90-eps)) hemispheres.
     */
    fun ascendant(ramcDeg: Double, trueEpsDeg: Double, latDeg: Double): Pair<Double?, AscendantStatus> {
        if (abs(latDeg) >= 89.99) {
            return Pair(null, AscendantStatus.DEGENERATE_POLE)
        }

        val r = radians(ramcDeg)
        val e = radians(trueEpsDeg)
        val p = radians(latDeg)

        val cosLat = cos(p)
        val sinLat = sin(p)
        val sinEps = sin(e)
        val cosEps = cos(e)
        val sinR = sin(r)
        val cosR = cos(r)

        val y = cosR * cosLat
        val x = -(sinEps * sinLat + cosEps * sinR * cosLat)
        val r2 = x * x + y * y

        if (r2 < 1e-10) {
            return Pair(null, AscendantStatus.COINCIDENT_PLANES)
        }

        var ascDeg = normalizeDegrees(degrees(atan2(y, x)))

        // Eastern hemisphere rising horizon vector check:
        // Ecliptic vector: (cos lambda, sin lambda * cos eps, sin lambda * sin eps)
        // East horizon vector: (-sin RAMC, cos RAMC, 0)
        val ascRad = radians(ascDeg)
        val eastDot = -cos(ascRad) * sinR + sin(ascRad) * cosEps * cosR
        if (eastDot < 0.0) {
            ascDeg = normalizeDegrees(ascDeg + 180.0)
        }

        val status = if (abs(latDeg) >= (90.0 - trueEpsDeg)) {
            AscendantStatus.POLAR_NON_RISING
        } else {
            AscendantStatus.CALCULATED
        }

        return Pair(ascDeg, status)
    }

    /**
     * Midheaven (MC).
     */
    fun midheaven(ramcDeg: Double, trueEpsDeg: Double): Double {
        val r = radians(ramcDeg)
        val e = radians(trueEpsDeg)
        val y = sin(r)
        val x = cos(r) * cos(e)
        return normalizeDegrees(degrees(atan2(y, x)))
    }

    fun toZodiacPosition(longitudeDeg: Double): ZodiacPosition {
        requireFiniteDouble(longitudeDeg, "longitudeDeg")
        val norm = normalizeDegrees(longitudeDeg)
        val signIdx = floor(norm / 30.0).toInt().coerceIn(0, 11)
        val degInSign = (norm - signIdx * 30.0).coerceIn(0.0, 29.999999999999)
        val wholeDeg = floor(degInSign).toInt().coerceIn(0, 29)
        val totalMinutes = (degInSign - wholeDeg) * 60.0
        val minute = floor(totalMinutes).toInt().coerceIn(0, 59)
        val second = ((totalMinutes - minute) * 60.0).coerceIn(0.0, 59.999999999999)

        return ZodiacPosition(
            sign = WesternZodiacSign.fromIndex(signIdx),
            degreeInSign = degInSign,
            wholeDegree = wholeDeg,
            minute = minute,
            second = second,
            totalLongitude = norm
        )
    }
}
