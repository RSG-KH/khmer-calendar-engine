package com.rsgkh.calendar.engine.western

import kotlin.math.abs
import kotlin.test.*

class WesternZodiacCalculatorTest {

    private fun angularDiffDeg(a: Double, b: Double): Double {
        val diff = (a - b + 540.0) % 360.0 - 180.0
        return abs(diff)
    }

    private fun arcseconds(deg: Double): Double = deg * 3600.0

    @Test
    fun benchmarkB1PhnomPenhReferenceChart() {
        val h = WesternZodiacCalculator.calculateHoroscope(
            year = 2026,
            month = 4,
            day = 14,
            hour = 10,
            minute = 30,
            second = 0.0,
            utcOffsetHours = 7.0,
            latitudeDeg = 11.5564,
            longitudeDeg = 104.9282
        )

        // Sun expected ~ 24.203663 deg (Aries), tolerance < 35 arcseconds
        val sunErrorArcsec = arcseconds(angularDiffDeg(h.sun.totalLongitude, 24.203663))
        assertTrue(sunErrorArcsec < 35.0, "B1 Sun error was $sunErrorArcsec arcsec")
        assertEquals(WesternZodiacSign.ARIES, h.sun.sign)

        // Moon expected ~ 340.240971 deg (Pisces), tolerance < 10 arcseconds
        val moonErrorArcsec = arcseconds(angularDiffDeg(h.moon.totalLongitude, 340.240971))
        assertTrue(moonErrorArcsec < 10.0, "B1 Moon error was $moonErrorArcsec arcsec")
        assertEquals(WesternZodiacSign.PISCES, h.moon.sign)

        // Ascendant expected ~ 94.428577 deg (Cancer), tolerance < 10 arcseconds
        assertNotNull(h.ascendant)
        val ascErrorArcsec = arcseconds(angularDiffDeg(h.ascendant.totalLongitude, 94.428577))
        assertTrue(ascErrorArcsec < 10.0, "B1 Ascendant error was $ascErrorArcsec arcsec")
        assertEquals(WesternZodiacSign.CANCER, h.ascendant.sign)
        assertEquals(AscendantStatus.CALCULATED, h.ascendantStatus)

        // Midheaven expected ~ 359.735466 deg (Pisces), tolerance < 10 arcseconds
        val mcErrorArcsec = arcseconds(angularDiffDeg(h.midheaven.totalLongitude, 359.735466))
        assertTrue(mcErrorArcsec < 10.0, "B1 Midheaven error was $mcErrorArcsec arcsec")
        assertEquals(WesternZodiacSign.PISCES, h.midheaven.sign)
    }

    @Test
    fun benchmarkB2SpringEquinoxCusp() {
        // Equinox near 2024-03-20 03:06:21 UTC
        val h = WesternZodiacCalculator.calculateHoroscopeUtc(
            yearUtc = 2024,
            monthUtc = 3,
            dayUtc = 20,
            hourUtc = 3,
            minuteUtc = 6,
            secondUtc = 21.0,
            latitudeDeg = 0.0,
            longitudeDeg = 0.0
        )
        // Angular error compared to 359.999964 deg must be < 35 arcseconds
        val errorArcsec = arcseconds(angularDiffDeg(h.sun.totalLongitude, 359.999964))
        assertTrue(errorArcsec < 35.0, "B2 Equinox error was $errorArcsec arcsec")

        // Ingress direction test: 2 hours prior should be in Pisces, 2 hours after in Aries
        val before = WesternZodiacCalculator.calculateHoroscopeUtc(
            2024, 3, 20, 1, 0, 0.0, 0.0, 0.0
        )
        assertEquals(WesternZodiacSign.PISCES, before.sun.sign)

        val after = WesternZodiacCalculator.calculateHoroscopeUtc(
            2024, 3, 20, 5, 0, 0.0, 0.0, 0.0
        )
        assertEquals(WesternZodiacSign.ARIES, after.sun.sign)
    }

    @Test
    fun benchmarkB3MoonSignTransitionGemini() {
        val h = WesternZodiacCalculator.calculateHoroscopeUtc(
            yearUtc = 2021,
            monthUtc = 3,
            dayUtc = 19,
            hourUtc = 0,
            minuteUtc = 0,
            secondUtc = 0.0,
            latitudeDeg = 0.0,
            longitudeDeg = 0.0
        )
        val moonErrorArcsec = arcseconds(angularDiffDeg(h.moon.totalLongitude, 60.105715))
        assertTrue(moonErrorArcsec < 5.0, "B3 Moon error was $moonErrorArcsec arcsec")
        assertEquals(WesternZodiacSign.GEMINI, h.moon.sign)
    }

    @Test
    fun benchmarkB4PolarAscendant() {
        val (ascLong, status) = MeeusEngine.ascendant(
            ramcDeg = 270.0,
            trueEpsDeg = 23.439291,
            latDeg = 70.0
        )
        assertNotNull(ascLong)
        val errorDeg = angularDiffDeg(ascLong, 0.0)
        assertTrue(errorDeg < 0.001, "B4 Polar Ascendant error was $errorDeg deg")
        assertEquals(AscendantStatus.POLAR_NON_RISING, status)
    }

    @Test
    fun benchmarkB5EquatorialSymmetry() {
        val (ascLong, status) = MeeusEngine.ascendant(
            ramcDeg = 0.0,
            trueEpsDeg = 23.44,
            latDeg = 0.0
        )
        assertNotNull(ascLong)
        val errorDeg = angularDiffDeg(ascLong, 90.0)
        assertTrue(errorDeg < 0.001, "B5 Equatorial symmetry error was $errorDeg deg")
        assertEquals(AscendantStatus.CALCULATED, status)
    }

    @Test
    fun benchmarkB6SouthernHemisphereSydney() {
        val h = WesternZodiacCalculator.calculateHoroscopeUtc(
            yearUtc = 2026,
            monthUtc = 6,
            dayUtc = 21,
            hourUtc = 12,
            minuteUtc = 0,
            secondUtc = 0.0,
            latitudeDeg = -33.8688,
            longitudeDeg = 151.2093
        )
        val sunErr = arcseconds(angularDiffDeg(h.sun.totalLongitude, 90.142823))
        assertTrue(sunErr < 35.0, "B6 Sun error: $sunErr arcsec")
        assertEquals(WesternZodiacSign.CANCER, h.sun.sign)

        val moonErr = arcseconds(angularDiffDeg(h.moon.totalLongitude, 175.217286))
        assertTrue(moonErr < 10.0, "B6 Moon error: $moonErr arcsec")
        assertEquals(WesternZodiacSign.VIRGO, h.moon.sign)

        assertNotNull(h.ascendant)
        val ascErr = arcseconds(angularDiffDeg(h.ascendant.totalLongitude, 335.540012))
        assertTrue(ascErr < 10.0, "B6 Ascendant error: $ascErr arcsec")
        assertEquals(WesternZodiacSign.PISCES, h.ascendant.sign)

        val mcErr = arcseconds(angularDiffDeg(h.midheaven.totalLongitude, 242.959507))
        assertTrue(mcErr < 10.0, "B6 MC error: $mcErr arcsec")
        assertEquals(WesternZodiacSign.SAGITTARIUS, h.midheaven.sign)
    }

    @Test
    fun benchmarkB7HistoricalEpoch1900() {
        val h = WesternZodiacCalculator.calculateHoroscopeUtc(
            yearUtc = 1900,
            monthUtc = 1,
            dayUtc = 1,
            hourUtc = 0,
            minuteUtc = 0,
            secondUtc = 0.0,
            latitudeDeg = 0.0,
            longitudeDeg = 0.0
        )
        val moonErr = arcseconds(angularDiffDeg(h.moon.totalLongitude, 272.416373))
        assertTrue(moonErr < 5.0, "B7 Moon error: $moonErr arcsec")
        assertEquals(WesternZodiacSign.CAPRICORN, h.moon.sign)
    }

    @Test
    fun benchmarkB8Epoch2000NoonUtc() {
        val h = WesternZodiacCalculator.calculateHoroscopeUtc(
            yearUtc = 2000,
            monthUtc = 1,
            dayUtc = 1,
            hourUtc = 12,
            minuteUtc = 0,
            secondUtc = 0.0,
            latitudeDeg = 0.0,
            longitudeDeg = 0.0
        )
        val moonErr = arcseconds(angularDiffDeg(h.moon.totalLongitude, 223.323825))
        assertTrue(moonErr < 10.0, "B8 Moon error: $moonErr arcsec")
        assertEquals(WesternZodiacSign.SCORPIO, h.moon.sign)
        assertEquals(WesternZodiacSign.CAPRICORN, h.sun.sign)
    }

    @Test
    fun exceptionalPolarCoincidentAndPoles() {
        val eps = 23.43929111
        // Northern coincident plane
        val (nAsc, nStatus) = MeeusEngine.ascendant(270.0, eps, 90.0 - eps)
        assertNull(nAsc)
        assertEquals(AscendantStatus.COINCIDENT_PLANES, nStatus)

        // Southern coincident plane
        val (sAsc, sStatus) = MeeusEngine.ascendant(90.0, eps, -(90.0 - eps))
        assertNull(sAsc)
        assertEquals(AscendantStatus.COINCIDENT_PLANES, sStatus)

        // North pole
        val (npAsc, npStatus) = MeeusEngine.ascendant(0.0, eps, 90.0)
        assertNull(npAsc)
        assertEquals(AscendantStatus.DEGENERATE_POLE, npStatus)

        // South pole
        val (spAsc, spStatus) = MeeusEngine.ascendant(0.0, eps, -90.0)
        assertNull(spAsc)
        assertEquals(AscendantStatus.DEGENERATE_POLE, spStatus)
    }

    @Test
    fun zodiacSignFormattingAndProperties() {
        val pos = ZodiacPosition.fromLongitude(250.505)
        assertEquals(WesternZodiacSign.SAGITTARIUS, pos.sign)
        assertEquals(10, pos.wholeDegree)
        assertEquals(30, pos.minute)
        assertTrue(pos.second >= 17.0 && pos.second <= 19.0)
        assertEquals("Sagittarius", pos.sign.englishName)
        assertEquals("ធ្នូ", pos.sign.khmerName)
        assertEquals("Fire", pos.sign.element)
        assertEquals("Mutable", pos.sign.modality)
        assertEquals("♐", pos.sign.symbol)
        assertTrue(pos.formatted.startsWith("Sagittarius 10° 30'"))
    }

    @Test
    fun boundaryValidation() {
        assertFailsWith<IllegalArgumentException> {
            WesternZodiacCalculator.calculateHoroscopeUtc(1799, 1, 1, 0, 0, 0.0, 0.0, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            WesternZodiacCalculator.calculateHoroscopeUtc(2201, 1, 1, 0, 0, 0.0, 0.0, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            WesternZodiacCalculator.calculateHoroscopeUtc(2026, 2, 29, 0, 0, 0.0, 0.0, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            WesternZodiacCalculator.calculateHoroscope(2026, 1, 1, 0, 0, 0.0, 14.1, 0.0, 0.0)
        }
    }

    @Test
    fun nearMidnightCarryAndBoundarySnapRegression() {
        val lat = 11.5564
        val lon = 104.9282

        // 1. Zero offset near midnight carries to next day 00:00:00 UTC
        val hZero = WesternZodiacCalculator.calculateHoroscope(
            2026, 1, 2, 23, 59, 59.999999999, 0.0, lat, lon
        )
        val expZero = WesternZodiacCalculator.calculateHoroscopeUtc(
            2026, 1, 3, 0, 0, 0.0, lat, lon
        )
        assertEquals(expZero.sun.totalLongitude, hZero.sun.totalLongitude, 1e-12)
        assertEquals(expZero.moon.totalLongitude, hZero.moon.totalLongitude, 1e-12)
        assertNotNull(expZero.ascendant)
        assertNotNull(hZero.ascendant)
        assertEquals(expZero.ascendant.totalLongitude, hZero.ascendant.totalLongitude, 1e-12)
        assertEquals(expZero.midheaven.totalLongitude, hZero.midheaven.totalLongitude, 1e-12)
        assertEquals(expZero.ascendantStatus, hZero.ascendantStatus)

        // 2. Minus 1 offset: local 22:59:59.999999999 with UTC-1.0 normalizes to 23:59:59.999999999 UTC -> next day 00:00:00
        val hMinus1 = WesternZodiacCalculator.calculateHoroscope(
            2026, 1, 2, 22, 59, 59.999999999, -1.0, lat, lon
        )
        val expMinus1 = WesternZodiacCalculator.calculateHoroscopeUtc(
            2026, 1, 3, 0, 0, 0.0, lat, lon
        )
        assertEquals(expMinus1.sun.totalLongitude, hMinus1.sun.totalLongitude, 1e-12)

        // 3. Plus 1 offset: local 00:59:59.999999999 with UTC+1.0 normalizes to 23:59:59.999999999 of prev day -> 00:00:00 of same day
        val hPlus1 = WesternZodiacCalculator.calculateHoroscope(
            2026, 1, 2, 0, 59, 59.999999999, 1.0, lat, lon
        )
        val expPlus1 = WesternZodiacCalculator.calculateHoroscopeUtc(
            2026, 1, 2, 0, 0, 0.0, lat, lon
        )
        assertEquals(expPlus1.sun.totalLongitude, hPlus1.sun.totalLongitude, 1e-12)

        // 4. Month carry: Jan 31 -> Feb 1
        val hMonth = WesternZodiacCalculator.calculateHoroscope(
            2026, 1, 31, 23, 59, 59.999999999, 0.0, lat, lon
        )
        val expMonth = WesternZodiacCalculator.calculateHoroscopeUtc(
            2026, 2, 1, 0, 0, 0.0, lat, lon
        )
        assertEquals(expMonth.sun.totalLongitude, hMonth.sun.totalLongitude, 1e-12)

        // 5. Year carry: Dec 31 -> Jan 1 of next year
        val hYear = WesternZodiacCalculator.calculateHoroscope(
            2026, 12, 31, 23, 59, 59.999999999, 0.0, lat, lon
        )
        val expYear = WesternZodiacCalculator.calculateHoroscopeUtc(
            2027, 1, 1, 0, 0, 0.0, lat, lon
        )
        assertEquals(expYear.sun.totalLongitude, hYear.sun.totalLongitude, 1e-12)

        // 6. Supported year endpoint behavior: 2200-12-31 near-midnight snaps into 2201, which is outside 1800..2200
        assertFailsWith<IllegalArgumentException> {
            WesternZodiacCalculator.calculateHoroscope(
                2200, 12, 31, 23, 59, 59.999999999, 0.0, lat, lon
            )
        }

        // 7. Backward carry out of 1800: 1800-01-01 00:00:00 with offset +1.0 carries back into 1799
        assertFailsWith<IllegalArgumentException> {
            WesternZodiacCalculator.calculateHoroscope(
                1800, 1, 1, 0, 0, 0.0, 1.0, lat, lon
            )
        }
    }

    @Test
    fun horoscopeAscendantStatusContract() {
        val sun = ZodiacPosition.fromLongitude(0.0)
        val moon = ZodiacPosition.fromLongitude(60.0)
        val asc = ZodiacPosition.fromLongitude(90.0)
        val mc = ZodiacPosition.fromLongitude(270.0)

        // CALCULATED and POLAR_NON_RISING require non-null ascendant
        WesternHoroscope(sun, moon, asc, mc, false, AscendantStatus.CALCULATED)
        WesternHoroscope(sun, moon, asc, mc, true, AscendantStatus.POLAR_NON_RISING)
        assertFailsWith<IllegalArgumentException> {
            WesternHoroscope(sun, moon, null, mc, false, AscendantStatus.CALCULATED)
        }
        assertFailsWith<IllegalArgumentException> {
            WesternHoroscope(sun, moon, null, mc, true, AscendantStatus.POLAR_NON_RISING)
        }

        // COINCIDENT_PLANES and DEGENERATE_POLE require null ascendant
        WesternHoroscope(sun, moon, null, mc, true, AscendantStatus.COINCIDENT_PLANES)
        WesternHoroscope(sun, moon, null, mc, true, AscendantStatus.DEGENERATE_POLE)
        assertFailsWith<IllegalArgumentException> {
            WesternHoroscope(sun, moon, asc, mc, true, AscendantStatus.COINCIDENT_PLANES)
        }
        assertFailsWith<IllegalArgumentException> {
            WesternHoroscope(sun, moon, asc, mc, true, AscendantStatus.DEGENERATE_POLE)
        }
    }
}
