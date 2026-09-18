package com.rsgkh.calendar.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ChineseLunisolarEngineTest {

    @Test
    fun benchmarkDatesMatchArchiveProfile100Percent() {
        val engine = ChineseLunisolarEngine(FestivalProfile.ARCHIVE_V1)
        val expected = parseBenchmarkTsv()

        var matchedDates = 0
        for ((key, expectedDates) in expected) {
            val parts = key.split(":")
            val year = parts[0].toInt()
            val festivalId = parts[1]
            val actualDates = engine.getFestivalDates(year, festivalId).toList()
            assertEquals(expectedDates, actualDates, "Mismatch for $key")
            matchedDates += actualDates.size
        }
        assertEquals(334, matchedDates, "Should match exactly 334 benchmark dates")
    }

    @Test
    fun benchmarkDatesMatchCnReferenceProfileExceptThreeKnownAdjustments() {
        val engine = ChineseLunisolarEngine(FestivalProfile.CN_REFERENCE_UTC8)
        val expected = parseBenchmarkTsv()

        var matchedDates = 0
        var differentDates = 0

        for ((key, expectedDates) in expected) {
            val parts = key.split(":")
            val year = parts[0].toInt()
            val festivalId = parts[1]
            val actualDates = engine.getFestivalDates(year, festivalId).toList()

            when (key) {
                "2009:chinese_qingming_festival" -> {
                    assertEquals(listOf("2009-04-05"), expectedDates)
                    assertEquals(listOf("2009-04-04"), actualDates)
                    differentDates++
                }
                "2013:chinese_zongzi_festival" -> {
                    assertEquals(listOf("2013-06-13"), expectedDates)
                    assertEquals(listOf("2013-06-12"), actualDates)
                    differentDates++
                }
                "2029:chinese_qingming_festival" -> {
                    assertEquals(listOf("2029-04-05"), expectedDates)
                    assertEquals(listOf("2029-04-04"), actualDates)
                    differentDates++
                }
                else -> {
                    assertEquals(expectedDates, actualDates, "Mismatch on standard date for $key")
                    matchedDates += actualDates.size
                }
            }
        }
        assertEquals(3, differentDates, "Exactly 3 documented legacy archive anomalies should differ")
        assertEquals(331, matchedDates, "Exactly 331 dates should match standard astronomical reference")
    }

    @Test
    fun roundTripLunarAndGregorianConversions() {
        val engine = ChineseLunisolarEngine(FestivalProfile.CN_REFERENCE_UTC8)

        // Test known landmark dates
        val cny2024 = engine.lunarToGregorian(2024, 1, 1)
        assertEquals("2024-02-10", cny2024)
        assertEquals(ChineseLunarDate(2024, 1, 1, false), engine.gregorianToLunar("2024-02-10"))

        val cny2025 = engine.lunarToGregorian(2025, 1, 1)
        assertEquals("2025-01-29", cny2025)
        assertEquals(ChineseLunarDate(2025, 1, 1, false), engine.gregorianToLunar("2025-01-29"))

        val cny2026 = engine.lunarToGregorian(2026, 1, 1)
        assertEquals("2026-02-17", cny2026)
        assertEquals(ChineseLunarDate(2026, 1, 1, false), engine.gregorianToLunar("2026-02-17"))

        // Boundary fragment 1899/12
        assertEquals(ChineseLunarDate(1899, 12, 1, false), engine.gregorianToLunar("1900-01-01"))
        assertEquals(ChineseLunarDate(1899, 12, 30, false), engine.gregorianToLunar("1900-01-30"))
        assertEquals(ChineseLunarDate(1900, 1, 1, false), engine.gregorianToLunar("1900-01-31"))

        // Leap month conversions (2020 leap month 4)
        assertEquals(4, engine.getLeapMonth(2020))
        val normalMonth4 = engine.lunarToGregorian(2020, 4, 1, isLeap = false)
        val leapMonth4 = engine.lunarToGregorian(2020, 4, 1, isLeap = true)
        assertEquals("2020-04-23", normalMonth4)
        assertEquals("2020-05-23", leapMonth4)
        assertEquals(ChineseLunarDate(2020, 4, 1, false), engine.gregorianToLunar(normalMonth4))
        assertEquals(ChineseLunarDate(2020, 4, 1, true), engine.gregorianToLunar(leapMonth4))
    }

    @Test
    fun boundaryLimitsAndErrorHandling() {
        val engine = ChineseLunisolarEngine(FestivalProfile.ARCHIVE_V1)

        // Year range
        assertFailsWith<IllegalArgumentException> { engine.getFestivalDates(1899, "chinese_new_year_days") }
        assertFailsWith<IllegalArgumentException> { engine.getFestivalDates(2101, "chinese_new_year_days") }

        // Unknown festival
        assertFailsWith<IllegalArgumentException> { engine.getFestivalDates(2024, "unknown_festival") }

        // Precedes 1900-01-01 boundary
        assertFailsWith<IllegalArgumentException> { engine.gregorianToLunar("1899-12-31") }

        // Invalid leap month request
        assertFailsWith<IllegalArgumentException> { engine.getLunarMonthDays(2024, 1, isLeap = true) }
    }

    @Test
    fun festivalProfileEnumParsing() {
        assertEquals(FestivalProfile.ARCHIVE_V1, FestivalProfile.fromId("archive-v1"))
        assertEquals(FestivalProfile.CN_REFERENCE_UTC8, FestivalProfile.fromId("cn-reference-utc8"))
        assertEquals(FestivalProfile.CN_LUNAR_UTC7_SOLAR, FestivalProfile.fromId("cn-lunar-utc7-solar"))
        assertEquals(FestivalProfile.LOCAL_UTC7_MODEL, FestivalProfile.fromId("local-utc7-model"))
        assertFailsWith<IllegalArgumentException> { FestivalProfile.fromId("invalid") }
    }

    companion object {
        private fun parseBenchmarkTsv(): Map<String, List<String>> {
            val map = linkedMapOf<String, MutableList<String>>()
            for (line in BENCHMARK_TSV.trim().lines()) {
                if (line.isBlank()) continue
                val parts = line.split("\t")
                val key = parts[0] + ":" + parts[1]
                map.getOrPut(key) { mutableListOf() }.add(parts[2])
            }
            return map
        }

        private const val BENCHMARK_TSV = """2000	chinese_kitchen_god_festival	2000-01-30
2000	chinese_new_year_eve	2000-02-04
2000	chinese_new_year_days	2000-02-05
2000	chinese_new_year_days	2000-02-06
2000	chinese_new_year_days	2000-02-07
2000	chinese_spirit_parade	2000-02-19
2000	chinese_qingming_festival	2000-04-04
2000	chinese_zongzi_festival	2000-06-06
2000	chinese_ghost_festival	2000-08-14
2000	chinese_mid_autumn_festival	2000-09-12
2000	chinese_winter_solstice	2000-12-21
2001	chinese_kitchen_god_festival	2001-01-18
2001	chinese_new_year_eve	2001-01-23
2001	chinese_new_year_days	2001-01-24
2001	chinese_new_year_days	2001-01-25
2001	chinese_new_year_days	2001-01-26
2001	chinese_spirit_parade	2001-02-07
2001	chinese_qingming_festival	2001-04-05
2001	chinese_zongzi_festival	2001-06-25
2001	chinese_ghost_festival	2001-09-02
2001	chinese_mid_autumn_festival	2001-10-01
2001	chinese_winter_solstice	2001-12-22
2002	chinese_kitchen_god_festival	2002-02-05
2002	chinese_new_year_eve	2002-02-11
2002	chinese_new_year_days	2002-02-12
2002	chinese_new_year_days	2002-02-13
2002	chinese_new_year_days	2002-02-14
2002	chinese_spirit_parade	2002-02-26
2002	chinese_qingming_festival	2002-04-05
2002	chinese_zongzi_festival	2002-06-15
2002	chinese_ghost_festival	2002-08-23
2002	chinese_mid_autumn_festival	2002-09-21
2002	chinese_winter_solstice	2002-12-22
2003	chinese_kitchen_god_festival	2003-01-26
2003	chinese_new_year_eve	2003-01-31
2003	chinese_new_year_days	2003-02-01
2003	chinese_new_year_days	2003-02-02
2003	chinese_new_year_days	2003-02-03
2003	chinese_spirit_parade	2003-02-15
2003	chinese_qingming_festival	2003-04-05
2003	chinese_zongzi_festival	2003-06-04
2003	chinese_ghost_festival	2003-08-12
2003	chinese_mid_autumn_festival	2003-09-11
2003	chinese_winter_solstice	2003-12-22
2004	chinese_kitchen_god_festival	2004-01-15
2004	chinese_new_year_eve	2004-01-21
2004	chinese_new_year_days	2004-01-22
2004	chinese_new_year_days	2004-01-23
2004	chinese_new_year_days	2004-01-24
2004	chinese_spirit_parade	2004-02-05
2004	chinese_qingming_festival	2004-04-04
2004	chinese_zongzi_festival	2004-06-22
2004	chinese_ghost_festival	2004-08-30
2004	chinese_mid_autumn_festival	2004-09-28
2004	chinese_winter_solstice	2004-12-21
2005	chinese_kitchen_god_festival	2005-02-02
2005	chinese_new_year_eve	2005-02-08
2005	chinese_new_year_days	2005-02-09
2005	chinese_new_year_days	2005-02-10
2005	chinese_new_year_days	2005-02-11
2005	chinese_spirit_parade	2005-02-23
2005	chinese_qingming_festival	2005-04-05
2005	chinese_zongzi_festival	2005-06-11
2005	chinese_ghost_festival	2005-08-19
2005	chinese_mid_autumn_festival	2005-09-18
2005	chinese_winter_solstice	2005-12-22
2006	chinese_kitchen_god_festival	2006-01-23
2006	chinese_new_year_eve	2006-01-28
2006	chinese_new_year_days	2006-01-29
2006	chinese_new_year_days	2006-01-30
2006	chinese_new_year_days	2006-01-31
2006	chinese_spirit_parade	2006-02-12
2006	chinese_qingming_festival	2006-04-05
2006	chinese_zongzi_festival	2006-05-31
2006	chinese_ghost_festival	2006-08-08
2006	chinese_mid_autumn_festival	2006-10-06
2006	chinese_winter_solstice	2006-12-22
2007	chinese_kitchen_god_festival	2007-02-11
2007	chinese_new_year_eve	2007-02-17
2007	chinese_new_year_days	2007-02-18
2007	chinese_new_year_days	2007-02-19
2007	chinese_new_year_days	2007-02-20
2007	chinese_spirit_parade	2007-03-04
2007	chinese_qingming_festival	2007-04-05
2007	chinese_zongzi_festival	2007-06-19
2007	chinese_ghost_festival	2007-08-27
2007	chinese_mid_autumn_festival	2007-09-25
2007	chinese_winter_solstice	2007-12-22
2008	chinese_kitchen_god_festival	2008-01-31
2008	chinese_new_year_eve	2008-02-06
2008	chinese_new_year_days	2008-02-07
2008	chinese_new_year_days	2008-02-08
2008	chinese_new_year_days	2008-02-09
2008	chinese_spirit_parade	2008-02-21
2008	chinese_qingming_festival	2008-04-04
2008	chinese_zongzi_festival	2008-06-08
2008	chinese_ghost_festival	2008-08-15
2008	chinese_mid_autumn_festival	2008-09-14
2008	chinese_winter_solstice	2008-12-21
2009	chinese_kitchen_god_festival	2009-01-19
2009	chinese_new_year_eve	2009-01-25
2009	chinese_new_year_days	2009-01-26
2009	chinese_new_year_days	2009-01-27
2009	chinese_new_year_days	2009-01-28
2009	chinese_spirit_parade	2009-02-09
2009	chinese_qingming_festival	2009-04-05
2009	chinese_zongzi_festival	2009-05-28
2009	chinese_ghost_festival	2009-09-03
2009	chinese_mid_autumn_festival	2009-10-03
2009	chinese_winter_solstice	2009-12-22
2010	chinese_kitchen_god_festival	2010-02-07
2010	chinese_new_year_eve	2010-02-13
2010	chinese_new_year_days	2010-02-14
2010	chinese_new_year_days	2010-02-15
2010	chinese_new_year_days	2010-02-16
2010	chinese_spirit_parade	2010-02-28
2010	chinese_qingming_festival	2010-04-05
2010	chinese_zongzi_festival	2010-06-16
2010	chinese_ghost_festival	2010-08-24
2010	chinese_mid_autumn_festival	2010-09-22
2010	chinese_winter_solstice	2010-12-22
2011	chinese_kitchen_god_festival	2011-01-27
2011	chinese_new_year_eve	2011-02-02
2011	chinese_new_year_days	2011-02-03
2011	chinese_new_year_days	2011-02-04
2011	chinese_new_year_days	2011-02-05
2011	chinese_spirit_parade	2011-02-17
2011	chinese_qingming_festival	2011-04-05
2011	chinese_zongzi_festival	2011-06-06
2011	chinese_ghost_festival	2011-08-14
2011	chinese_mid_autumn_festival	2011-09-12
2011	chinese_winter_solstice	2011-12-22
2012	chinese_kitchen_god_festival	2012-01-17
2012	chinese_new_year_eve	2012-01-22
2012	chinese_new_year_days	2012-01-23
2012	chinese_new_year_days	2012-01-24
2012	chinese_new_year_days	2012-01-25
2012	chinese_spirit_parade	2012-02-06
2012	chinese_qingming_festival	2012-04-04
2012	chinese_zongzi_festival	2012-06-23
2012	chinese_ghost_festival	2012-08-31
2012	chinese_mid_autumn_festival	2012-09-30
2012	chinese_winter_solstice	2012-12-21
2013	chinese_kitchen_god_festival	2013-02-04
2013	chinese_new_year_eve	2013-02-09
2013	chinese_new_year_days	2013-02-10
2013	chinese_new_year_days	2013-02-11
2013	chinese_new_year_days	2013-02-12
2013	chinese_spirit_parade	2013-02-24
2013	chinese_qingming_festival	2013-04-04
2013	chinese_zongzi_festival	2013-06-13
2013	chinese_ghost_festival	2013-08-21
2013	chinese_mid_autumn_festival	2013-09-19
2013	chinese_winter_solstice	2013-12-22
2014	chinese_kitchen_god_festival	2014-01-24
2014	chinese_new_year_eve	2014-01-30
2014	chinese_new_year_days	2014-01-31
2014	chinese_new_year_days	2014-02-01
2014	chinese_new_year_days	2014-02-02
2014	chinese_spirit_parade	2014-02-14
2014	chinese_qingming_festival	2014-04-05
2014	chinese_zongzi_festival	2014-06-02
2014	chinese_ghost_festival	2014-08-10
2014	chinese_mid_autumn_festival	2014-09-08
2014	chinese_winter_solstice	2014-12-22
2015	chinese_kitchen_god_festival	2015-02-12
2015	chinese_new_year_eve	2015-02-18
2015	chinese_new_year_days	2015-02-19
2015	chinese_new_year_days	2015-02-20
2015	chinese_new_year_days	2015-02-21
2015	chinese_spirit_parade	2015-03-05
2015	chinese_qingming_festival	2015-04-05
2015	chinese_zongzi_festival	2015-06-20
2015	chinese_ghost_festival	2015-08-28
2015	chinese_mid_autumn_festival	2015-09-27
2015	chinese_winter_solstice	2015-12-22
2016	chinese_kitchen_god_festival	2016-02-02
2016	chinese_new_year_eve	2016-02-07
2016	chinese_new_year_days	2016-02-08
2016	chinese_new_year_days	2016-02-09
2016	chinese_new_year_days	2016-02-10
2016	chinese_spirit_parade	2016-02-22
2016	chinese_qingming_festival	2016-04-04
2016	chinese_zongzi_festival	2016-06-09
2016	chinese_ghost_festival	2016-08-17
2016	chinese_mid_autumn_festival	2016-09-15
2016	chinese_winter_solstice	2016-12-21
2017	chinese_kitchen_god_festival	2017-01-21
2017	chinese_new_year_eve	2017-01-27
2017	chinese_new_year_days	2017-01-28
2017	chinese_new_year_days	2017-01-29
2017	chinese_new_year_days	2017-01-30
2017	chinese_spirit_parade	2017-02-11
2017	chinese_qingming_festival	2017-04-04
2017	chinese_zongzi_festival	2017-05-30
2017	chinese_ghost_festival	2017-09-05
2017	chinese_mid_autumn_festival	2017-10-04
2017	chinese_winter_solstice	2017-12-22
2018	chinese_kitchen_god_festival	2018-02-09
2018	chinese_new_year_eve	2018-02-15
2018	chinese_new_year_days	2018-02-16
2018	chinese_new_year_days	2018-02-17
2018	chinese_new_year_days	2018-02-18
2018	chinese_spirit_parade	2018-03-02
2018	chinese_qingming_festival	2018-04-05
2018	chinese_zongzi_festival	2018-06-18
2018	chinese_ghost_festival	2018-08-25
2018	chinese_mid_autumn_festival	2018-09-24
2018	chinese_winter_solstice	2018-12-22
2019	chinese_kitchen_god_festival	2019-01-29
2019	chinese_new_year_eve	2019-02-04
2019	chinese_new_year_days	2019-02-05
2019	chinese_new_year_days	2019-02-06
2019	chinese_new_year_days	2019-02-07
2019	chinese_spirit_parade	2019-02-19
2019	chinese_qingming_festival	2019-04-05
2019	chinese_zongzi_festival	2019-06-07
2019	chinese_ghost_festival	2019-08-15
2019	chinese_mid_autumn_festival	2019-09-13
2019	chinese_winter_solstice	2019-12-22
2020	chinese_kitchen_god_festival	2020-01-18
2020	chinese_new_year_eve	2020-01-24
2020	chinese_new_year_days	2020-01-25
2020	chinese_new_year_days	2020-01-26
2020	chinese_new_year_days	2020-01-27
2020	chinese_spirit_parade	2020-02-08
2020	chinese_qingming_festival	2020-04-04
2020	chinese_zongzi_festival	2020-06-25
2020	chinese_ghost_festival	2020-09-02
2020	chinese_mid_autumn_festival	2020-10-01
2020	chinese_winter_solstice	2020-12-21
2021	chinese_kitchen_god_festival	2021-02-05
2021	chinese_new_year_eve	2021-02-11
2021	chinese_new_year_days	2021-02-12
2021	chinese_new_year_days	2021-02-13
2021	chinese_new_year_days	2021-02-14
2021	chinese_spirit_parade	2021-02-26
2021	chinese_qingming_festival	2021-04-04
2021	chinese_zongzi_festival	2021-06-14
2021	chinese_ghost_festival	2021-08-22
2021	chinese_mid_autumn_festival	2021-09-21
2021	chinese_winter_solstice	2021-12-21
2022	chinese_kitchen_god_festival	2022-01-26
2022	chinese_new_year_eve	2022-01-31
2022	chinese_new_year_days	2022-02-01
2022	chinese_new_year_days	2022-02-02
2022	chinese_new_year_days	2022-02-03
2022	chinese_spirit_parade	2022-02-15
2022	chinese_qingming_festival	2022-04-05
2022	chinese_zongzi_festival	2022-06-03
2022	chinese_ghost_festival	2022-08-12
2022	chinese_mid_autumn_festival	2022-09-10
2022	chinese_winter_solstice	2022-12-22
2023	chinese_kitchen_god_festival	2023-01-15
2023	chinese_new_year_eve	2023-01-21
2023	chinese_new_year_days	2023-01-22
2023	chinese_new_year_days	2023-01-23
2023	chinese_new_year_days	2023-01-24
2023	chinese_spirit_parade	2023-02-05
2023	chinese_qingming_festival	2023-04-05
2023	chinese_zongzi_festival	2023-06-22
2023	chinese_ghost_festival	2023-08-30
2023	chinese_mid_autumn_festival	2023-09-29
2023	chinese_winter_solstice	2023-12-22
2024	chinese_kitchen_god_festival	2024-02-03
2024	chinese_new_year_eve	2024-02-09
2024	chinese_new_year_days	2024-02-10
2024	chinese_new_year_days	2024-02-11
2024	chinese_new_year_days	2024-02-12
2024	chinese_qingming_festival	2024-04-04
2024	chinese_zongzi_festival	2024-06-10
2024	chinese_ghost_festival	2024-08-18
2024	chinese_mid_autumn_festival	2024-09-17
2024	chinese_winter_solstice	2024-12-21
2025	chinese_kitchen_god_festival	2025-01-23
2025	chinese_new_year_eve	2025-01-28
2025	chinese_new_year_days	2025-01-29
2025	chinese_new_year_days	2025-01-30
2025	chinese_new_year_days	2025-01-31
2025	chinese_qingming_festival	2025-04-04
2025	chinese_zongzi_festival	2025-05-31
2025	chinese_ghost_festival	2025-09-06
2025	chinese_mid_autumn_festival	2025-10-06
2025	chinese_winter_solstice	2025-12-21
2026	chinese_kitchen_god_festival	2026-02-11
2026	chinese_new_year_eve	2026-02-16
2026	chinese_new_year_days	2026-02-17
2026	chinese_new_year_days	2026-02-18
2026	chinese_new_year_days	2026-02-19
2026	chinese_qingming_festival	2026-04-05
2026	chinese_zongzi_festival	2026-06-19
2026	chinese_ghost_festival	2026-08-27
2026	chinese_mid_autumn_festival	2026-09-25
2026	chinese_winter_solstice	2026-12-22
2027	chinese_kitchen_god_festival	2027-01-31
2027	chinese_new_year_eve	2027-02-05
2027	chinese_new_year_days	2027-02-06
2027	chinese_new_year_days	2027-02-07
2027	chinese_new_year_days	2027-02-08
2027	chinese_qingming_festival	2027-04-05
2027	chinese_zongzi_festival	2027-06-09
2027	chinese_ghost_festival	2027-08-16
2027	chinese_mid_autumn_festival	2027-09-15
2027	chinese_winter_solstice	2027-12-22
2028	chinese_kitchen_god_festival	2028-01-20
2028	chinese_new_year_eve	2028-01-25
2028	chinese_new_year_days	2028-01-26
2028	chinese_new_year_days	2028-01-27
2028	chinese_new_year_days	2028-01-28
2028	chinese_qingming_festival	2028-04-04
2028	chinese_zongzi_festival	2028-05-28
2028	chinese_ghost_festival	2028-09-03
2028	chinese_mid_autumn_festival	2028-10-03
2028	chinese_winter_solstice	2028-12-21
2029	chinese_kitchen_god_festival	2029-02-07
2029	chinese_new_year_eve	2029-02-12
2029	chinese_new_year_days	2029-02-13
2029	chinese_new_year_days	2029-02-14
2029	chinese_new_year_days	2029-02-15
2029	chinese_qingming_festival	2029-04-05
2029	chinese_zongzi_festival	2029-06-16
2029	chinese_ghost_festival	2029-08-24
2029	chinese_mid_autumn_festival	2029-09-22
2029	chinese_winter_solstice	2029-12-21
2030	chinese_kitchen_god_festival	2030-01-27
2030	chinese_new_year_eve	2030-02-02
2030	chinese_new_year_days	2030-02-03
2030	chinese_new_year_days	2030-02-04
2030	chinese_new_year_days	2030-02-05
2030	chinese_qingming_festival	2030-04-05
2030	chinese_zongzi_festival	2030-06-05
2030	chinese_ghost_festival	2030-08-13
2030	chinese_mid_autumn_festival	2030-09-12
2030	chinese_winter_solstice	2030-12-22"""
    }
}
