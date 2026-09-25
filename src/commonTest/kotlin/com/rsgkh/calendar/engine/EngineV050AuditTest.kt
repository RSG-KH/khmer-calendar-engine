package com.rsgkh.calendar.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Copy to src/commonTest/kotlin/com/rsgkh/calendar/engine/EngineV050AuditTest.kt.
 * Run with the repository's pinned toolchain. These are regression tests, not fixes.
 * The first two test methods are expected to fail against the reviewed source.
 */
class EngineV050AuditTest {
    private fun assertSamePillar(expected: GanzhiPillar, actual: GanzhiPillar, context: String) {
        assertEquals(expected.stem.index, actual.stem.index, "$context: stem")
        assertEquals(expected.branch.index, actual.branch.index, "$context: branch")
    }

    @Test
    fun fourPillarsRollsDayAndHourAt23ButNotYearAndMonth() {
        val z = ChineseZodiacCalculator
        // Include a solar-year boundary eve and the last supported Four Pillars date.
        val lichunEve = GregorianDate(2026, 2, z.getSectionalTermDay(2026, 2)).plusDays(-1)
        val dates = listOf(
            GregorianDate(1900, 1, 1), GregorianDate(2026, 1, 1),
            lichunEve, GregorianDate(2026, 12, 31), GregorianDate(2100, 12, 31)
        )
        for (g in dates) {
            val tomorrow = g.plusDays(1)
            val todayDay = z.getDayPillar(g.year, g.month, g.day)
            val nextDay = z.getDayPillar(tomorrow.year, tomorrow.month, tomorrow.day)
            val before = z.getFourPillars(g.year, g.month, g.day, 22)
            val late = z.getFourPillars(g.year, g.month, g.day, 23)
            assertSamePillar(todayDay, before.day, "${g.iso} 22:00 day")
            assertSamePillar(nextDay, late.day, "${g.iso} 23:00 day")
            assertSamePillar(z.getHourPillar(nextDay.stem, 23), late.hour, "${g.iso} 23:00 hour")
            assertSamePillar(z.getYearPillar(g.year, g.month, g.day), late.year, "civil year retained")
            assertSamePillar(z.getMonthPillar(g.year, g.month, g.day), late.month, "civil month retained")
            // Do not change the date-only getDayPillar contract as part of this fix.
            assertSamePillar(todayDay, z.getDayPillar(g.year, g.month, g.day), "date-only day retained")
        }
    }

    @Test
    fun publicFestivalIdsCannotChangeRuleValidation() {
        val exposed = ChineseLunisolarEngine.FESTIVAL_IDS
        val genuine = exposed[0]
        val fake = "audit_nonexistent_festival"
        try {
            // Both a defensive copy and an immutable exported array are acceptable.
            runCatching { exposed[0] = fake }
            assertTrue(genuine in ChineseLunisolarEngine.FESTIVAL_IDS)
            assertTrue(fake !in ChineseLunisolarEngine.FESTIVAL_IDS)
            RecurrenceRule(genuine, "chinese_festival")
            assertFailsWith<IllegalArgumentException> { RecurrenceRule(fake, "chinese_festival") }
        } finally {
            // Restore reviewed releases' shared state so other tests stay independent.
            runCatching { exposed[0] = genuine }
        }
    }

    @Test
    fun explicitFestivalProfilesAgreeAcrossStandaloneAndRecurrence() {
        // This is a passing guardrail. It does NOT decide a new default profile.
        val e = KhmerCalendarEngine()
        val id = "chinese_zongzi_festival"
        for (profile in listOf(FestivalProfile.ARCHIVE_V1, FestivalProfile.CN_REFERENCE_UTC8)) {
            val expected = ChineseLunisolarEngine(profile).getFestivalDates(2013, id).toList()
            val actual = e.evaluateRule(2013, RecurrenceRule(id, "chinese_festival", monthPolicy = profile.id))
                .map { it.date.iso }
            assertEquals(expected, actual, profile.id)
        }
    }
}
