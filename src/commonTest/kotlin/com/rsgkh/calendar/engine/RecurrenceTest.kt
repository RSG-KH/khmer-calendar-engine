package com.rsgkh.calendar.engine

import kotlin.test.*

class RecurrenceTest {
    private val engine = KhmerCalendarEngine()
    private fun dates(year: Int, rule: RecurrenceRule) = engine.evaluateRule(year, rule).map { it.date.iso }

    @Test fun secondAsadhPolicyWorksInOrdinaryAndLeapMonthYears() {
        val lent = RecurrenceRule("lent", "khmer_lunar", 7, 1, waxing = false, monthPolicy = "ordinary_or_second_asadh")
        assertEquals(listOf("2025-07-11"), dates(2025, lent))
        assertEquals(listOf("2026-07-30"), dates(2026, lent))
        assertEquals(listOf("2031-08-04"), dates(2031, lent))
        assertEquals(listOf("2031-07-27"), dates(2031, RecurrenceRule("candles", "khmer_lunar", 7, 8, monthPolicy = "ordinary_or_second_asadh")))
        assertEquals(listOf("2031-08-02"), dates(2031, RecurrenceRule("dragon", "khmer_lunar", 7, 14, monthPolicy = "ordinary_or_second_asadh")))
        for (year in 1800..2200) assertEquals(1, dates(year, lent).size, "$year")
        assertEquals(emptyList(), dates(2026, RecurrenceRule("literal-asadh", "khmer_lunar", 7, 1, waxing = false)))
    }

    @Test fun lunarFestivalRulesMatchPublishedDateAnchors() {
        // G01/G04/G05: ECCC 2012, 2013 Prakas, MEF 2025, LRC 2026.
        val plough = RecurrenceRule("plough", "khmer_lunar", 5, 4, waxing = false)
        for ((year, expected) in mapOf(2012 to "2012-05-09", 2013 to "2013-05-28", 2025 to "2025-05-15", 2026 to "2026-05-05")) {
            assertEquals(listOf(expected), dates(year, plough))
        }
        val pchum = RecurrenceRule("pchum", "khmer_lunar", 9, 15, waxing = false, offset = -1, duration = 3)
        assertEquals(listOf("2012-10-14", "2012-10-15", "2012-10-16"), dates(2012, pchum))
        val water = RecurrenceRule("water", "khmer_lunar", 11, 14, duration = 3)
        assertEquals(listOf("2013-11-16", "2013-11-17", "2013-11-18"), dates(2013, water))
    }

    @Test fun solarLeapDaysAndNthWeekdaysDoNotSpillIntoAnotherMonth() {
        val leapDay = RecurrenceRule("leap-day", "solar", 2, 29)
        assertEquals(emptyList(), dates(1900, leapDay))
        assertEquals(listOf("2000-02-29"), dates(2000, leapDay))
        val fifthSunday = RecurrenceRule("fifth-sunday", "solar_nth_weekday", 2, 7, occurrence = 5)
        assertEquals(emptyList(), dates(2025, fifthSunday))
        assertEquals(listOf("2004-02-29"), dates(2004, fifthSunday))
        assertEquals(listOf("2026-05-10"), dates(2026, RecurrenceRule("second-sunday", "solar_nth_weekday", 5, 7, occurrence = 2)))
    }

    @Test fun durationsStagesAndEffectiveYears() {
        assertEquals(listOf("2023-12-31", "2024-01-01", "2024-01-02"), dates(2024, RecurrenceRule("span", "solar", 1, 1, offset = -1, duration = 3)))
        assertEquals(listOf("2024-04-14", "2024-04-15"), dates(2024, RecurrenceRule("middle", "new_year_middle")))
        assertEquals(listOf("2012-04-15"), dates(2012, RecurrenceRule("last", "new_year_last")))
        assertEquals(emptyList(), dates(2019, RecurrenceRule("modern", "solar", fromYear = 2020)))
        assertFailsWith<IllegalArgumentException> { dates(1800, RecurrenceRule("outside", "solar", offset = -1)) }
    }

    @Test fun explicitCorrectionsAndCancellationsKeepProvenance() {
        val rule = RecurrenceRule("event", "solar", 4, 14, fromYear = 2020)
        val supplied = arrayOf(GregorianDate(2012, 4, 13))
        val replacement = EventDateOverride("event", 2012, supplied, "review-2012", "Published date")
        supplied[0] = GregorianDate(2012, 4, 14)
        val result = engine.evaluateRule(2012, rule, replacement).single()
        assertEquals("2012-04-13", result.date.iso)
        assertEquals("source_override", result.basis)
        assertEquals("review-2012", result.sourceId)
        assertEquals(0, engine.evaluateRule(2024, rule, EventDateOverride("event", 2024, emptyArray(), "notice", "Cancelled")).size)
        assertFailsWith<IllegalArgumentException> { engine.evaluateRule(2013, rule, replacement) }
        assertFailsWith<IllegalArgumentException> { EventDateOverride("event", 2012, supplied, "", "No source") }
    }

    @Test fun invalidRulesAreRejectedBeforeEvaluation() {
        assertFailsWith<IllegalArgumentException> { RecurrenceRule("x", "unknown") }
        assertFailsWith<IllegalArgumentException> { RecurrenceRule("x", "solar", 2, 30) }
        assertFailsWith<IllegalArgumentException> { RecurrenceRule("x", "khmer_lunar", 7, 1, monthPolicy = "typo") }
        assertFailsWith<IllegalArgumentException> { RecurrenceRule("x", "solar", monthPolicy = "ordinary_or_second_asadh") }
        assertFailsWith<IllegalArgumentException> { RecurrenceRule("x", "solar_nth_weekday", occurrence = 0) }
        assertFailsWith<IllegalArgumentException> { RecurrenceRule("x", "solar", duration = 0) }
        assertFailsWith<IllegalArgumentException> { RecurrenceRule("x", "solar", fromYear = 2100, throughYear = 2000) }
    }
}
