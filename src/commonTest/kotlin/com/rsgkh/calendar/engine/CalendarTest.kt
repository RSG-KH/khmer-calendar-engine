package com.rsgkh.calendar.engine

import kotlin.test.*

class CalendarTest {
    private val engine = KhmerCalendarEngine()

    @Test fun gregorianCenturyBoundariesAndEpochs() {
        assertEquals(0, GregorianDate(1970, 1, 1).epochDay)
        assertEquals(4, GregorianDate(1970, 1, 1).dayOfWeek)
        assertEquals("1900-03-01", GregorianDate(1900, 2, 28).plusDays(1).iso)
        assertEquals("2000-02-29", GregorianDate(2000, 2, 28).plusDays(1).iso)
        assertEquals("2100-03-01", GregorianDate(2100, 2, 28).plusDays(1).iso)
        assertEquals("1969-12-31", GregorianDate(1970, 1, 1).plusDays(-1).iso)
        assertEquals("0001-01-01", fromEpochDay(-719162).iso)
        assertEquals("9999-12-31", fromEpochDay(2932896).iso)
        assertFailsWith<IllegalArgumentException> { GregorianDate(1900, 2, 29) }
        assertFailsWith<IllegalArgumentException> { GregorianDate(2024, 13, 1) }
        assertFailsWith<IllegalArgumentException> { GregorianDate(2024, 1, 0) }
        assertFailsWith<IllegalArgumentException> { GregorianDate(1, 1, 1).plusDays(-1) }
    }

    @Test fun everySupportedDayRoundTripsWithoutLosingLunarIdentity() {
        val first = GregorianDate(1800, 1, 1).epochDay
        val last = GregorianDate(2200, 12, 31).epochDay
        assertEquals(146462, last - first + 1)
        var leapMonths = 0
        var leapDays = 0
        var shortMonthEnds = 0
        var previous: LunarDate? = null
        for (epoch in first..last) {
            val date = fromEpochDay(epoch)
            val actual = engine.fromGregorian(date.year, date.month, date.day)
            val lunar = actual.lunar
            assertEquals(epoch, date.epochDay)
            assertEquals(date, engine.toGregorian(lunar.buddhistYear, lunar.month, lunar.day, lunar.waxing), date.iso)
            assertTrue(lunar.monthLength in 29..30)
            assertTrue(lunar.day in 1..if (lunar.waxing) 15 else lunar.monthLength - 15)
            val prev = previous
            if (prev != null) {
                val ordinal = lunar.day - 1 + if (lunar.waxing) 0 else 15
                val previousOrdinal = prev.day - 1 + if (prev.waxing) 0 else 15
                assertEquals((previousOrdinal + 1) % prev.monthLength, ordinal, date.iso)
                if (ordinal > 0) assertEquals(prev.month, lunar.month)
            }
            if (lunar.month == 13 && lunar.waxing && lunar.day == 1) leapMonths++
            if (lunar.month == 6 && !lunar.waxing && lunar.day == 15) leapDays++
            if (!lunar.waxing && lunar.day == 14 && lunar.monthLength == 29) {
                assertTrue(lunar.isHolyDay)
                shortMonthEnds++
            }
            previous = lunar
        }
        assertTrue(leapMonths > 0 && leapDays > 0 && shortMonthEnds > 0)
    }

    @Test fun reviewedFestivalDateCases() {
        // Direct sources and review limits: docs/references.md, T06/N01..N10/G01/G04/G05.
        // Published arrival minutes are tested as dataset evidence in the manager, never
        // as expected output of the estimate calculation below.
        val starts = mapOf(2011 to 14, 2012 to 13, 2013 to 14, 2014 to 14, 2015 to 14,
            2024 to 13, 2025 to 14, 2026 to 14)
        for ((year, day) in starts) {
            val result = engine.newYear(year)
            assertEquals(GregorianDate(year, 4, day), result.start, "$year")
            assertEquals(if (year == 2024) 4 else 3, result.days, "$year")
        }
        assertEquals(listOf("2012-04-13", "2012-04-14", "2012-04-15"), engine.newYear(2012).dates.map { it.iso })
        assertEquals(listOf("2024-04-13", "2024-04-14", "2024-04-15", "2024-04-16"), engine.newYear(2024).dates.map { it.iso })
    }

    @Test fun arrivalEstimateReproducesTheTraditionalTimePath() {
        // Diagnostic reconstruction of the pinned MomentKH arithmetic (research time path,
        // docs/references.md). These are computed minutes, not published arrival clocks:
        // 2011–2015 and 2024 disagree with the reviewed publications by 1–24 minutes.
        val expected = mapOf(
            2011 to 816, 2012 to 1152, 2013 to 144, 2014 to 504, 2015 to 864,
            2024 to 1344, 2025 to 288, 2026 to 648)
        for ((year, minuteOfDay) in expected) {
            assertEquals(minuteOfDay, engine.newYear(year).arrivalEstimate.minuteOfDay, "$year")
        }
        val estimate = engine.newYear(2012).arrivalEstimate
        assertEquals(19, estimate.hour)
        assertEquals(12, estimate.minute)
    }

    @Test fun arrivalEstimateStaysOnThe24MinuteLatticeForEverySupportedYear() {
        // Structural limitation of the traditional conversion: minuteOfDay is always a
        // multiple of 24, so published clocks such as 19:11 or 08:07 are unreachable.
        for (year in 1800..2200) {
            val estimate = engine.newYear(year).arrivalEstimate
            assertEquals(0, estimate.minuteOfDay % 24, "$year")
            assertTrue(estimate.minuteOfDay in 0..1439, "$year")
            assertEquals(estimate.minuteOfDay / 60, estimate.hour, "$year")
            assertEquals(estimate.minuteOfDay % 60, estimate.minute, "$year")
        }
    }

    @Test fun corrected2012DateLevelTransitions() {
        val before = engine.fromGregorian(2012, 4, 12)
        val start = engine.fromGregorian(2012, 4, 13)
        val middle = engine.fromGregorian(2012, 4, 14)
        val last = engine.fromGregorian(2012, 4, 15)
        assertEquals((before.animalYear + 1) % 12, start.animalYear)
        assertTrue(start.animalYearChangesToday)
        assertFalse(middle.animalYearChangesToday)
        assertEquals(before.sak, middle.sak)
        assertEquals((middle.sak + 1) % 10, last.sak)
        assertTrue(last.sakChangesToday)
    }

    @Test fun invalidOrUnsupportedLunarDatesAreRejected() {
        assertFailsWith<IllegalArgumentException> { engine.fromGregorian(1799, 12, 31) }
        assertFailsWith<IllegalArgumentException> { engine.newYear(2201) }
        assertFailsWith<IllegalArgumentException> { engine.toGregorian(2568, 14, 1, true) }
        assertFailsWith<IllegalArgumentException> { engine.toGregorian(2568, 5, 16, true) }
        val normal = engine.fromGregorian(2025, 7, 1).lunar.buddhistYear
        assertFailsWith<IllegalArgumentException> { engine.toGregorian(normal, 13, 1, true) }
    }

    @Test fun buddhistEraChangesAfterVisakAndIndependentlyOfNewYear() {
        // Published Visak dates: ECCC 2012 circular and Prakas 223 for 2013.
        for (date in listOf(GregorianDate(2012, 5, 5), GregorianDate(2013, 5, 24))) {
            val full = engine.fromGregorian(date.year, date.month, date.day)
            val next = date.plusDays(1)
            val waning = engine.fromGregorian(next.year, next.month, next.day)
            assertEquals(5, full.lunar.month)
            assertEquals(15, full.lunar.day)
            assertTrue(full.lunar.waxing && full.lunar.isHolyDay)
            assertEquals(full.lunar.buddhistYear + 1, waning.lunar.buddhistYear)
            assertEquals(1, waning.lunar.day)
            assertFalse(waning.lunar.waxing)
            assertEquals(full.animalYear, waning.animalYear)
            assertEquals(full.sak, waning.sak)
        }
    }
}
