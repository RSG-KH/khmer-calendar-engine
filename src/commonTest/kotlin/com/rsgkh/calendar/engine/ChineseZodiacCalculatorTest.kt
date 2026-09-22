package com.rsgkh.calendar.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ChineseZodiacCalculatorTest {

    @Test
    fun testJulianDayNumberCalculation() {
        assertEquals(2433191, ChineseZodiacCalculator.gregorianToJdn(1949, 10, 1))
        assertEquals(2451545, ChineseZodiacCalculator.gregorianToJdn(2000, 1, 1))
        assertEquals(2454687, ChineseZodiacCalculator.gregorianToJdn(2008, 8, 8))
        assertEquals(2460351, ChineseZodiacCalculator.gregorianToJdn(2024, 2, 10))
        assertEquals(2461042, ChineseZodiacCalculator.gregorianToJdn(2026, 1, 1))

        val gDate = GregorianDate(2026, 1, 1)
        assertEquals(2461042, ChineseZodiacCalculator.gregorianDateToJdn(gDate))
    }

    @Test
    fun testHistoricalDayPillarAnchors() {
        data class Anchor(val y: Int, val m: Int, val d: Int, val nameZh: String, val animal: String)

        val anchors = listOf(
            Anchor(1949, 10, 1, "甲子", "Rat"),
            Anchor(2000, 1, 1, "戊午", "Horse"),
            Anchor(2008, 8, 8, "庚辰", "Dragon"),
            Anchor(2024, 2, 10, "甲辰", "Dragon"),
            Anchor(2026, 1, 1, "乙亥", "Pig")
        )

        for (a in anchors) {
            val pillar = ChineseZodiacCalculator.getDayPillar(a.y, a.m, a.d)
            assertEquals(a.nameZh, pillar.nameZh, "Day name mismatch for ${a.y}-${a.m}-${a.d}")
            assertEquals(a.animal, pillar.animal, "Animal mismatch for ${a.y}-${a.m}-${a.d}")
            assertEquals(pillar, ChineseZodiacCalculator.getDayPillarForGregorianDate(GregorianDate(a.y, a.m, a.d)))
        }
    }

    @Test
    fun testHourBranchMappingAcrossAllHours() {
        val expected = listOf(
            0 to EarthlyBranch.ZI,
            1 to EarthlyBranch.CHOU,
            2 to EarthlyBranch.CHOU,
            3 to EarthlyBranch.YIN,
            4 to EarthlyBranch.YIN,
            5 to EarthlyBranch.MAO,
            6 to EarthlyBranch.MAO,
            7 to EarthlyBranch.CHEN,
            8 to EarthlyBranch.CHEN,
            9 to EarthlyBranch.SI,
            10 to EarthlyBranch.SI,
            11 to EarthlyBranch.WU,
            12 to EarthlyBranch.WU,
            13 to EarthlyBranch.WEI,
            14 to EarthlyBranch.WEI,
            15 to EarthlyBranch.SHEN,
            16 to EarthlyBranch.SHEN,
            17 to EarthlyBranch.YOU,
            18 to EarthlyBranch.YOU,
            19 to EarthlyBranch.XU,
            20 to EarthlyBranch.XU,
            21 to EarthlyBranch.HAI,
            22 to EarthlyBranch.HAI,
            23 to EarthlyBranch.ZI
        )

        for ((hour, branch) in expected) {
            assertEquals(branch, ChineseZodiacCalculator.getHourBranch(hour), "Hour $hour mismatch")
        }
    }

    @Test
    fun testFiveRatsRuleAndHourRolloverAt23() {
        // On 2026-01-01 (Yi-Hai day):
        // 12:00 (Horse hour) on Yi day: baseStem = (Yi.index % 5) * 2 = 1 * 2 = 2 (Bing).
        // Hour stem = Bing(2) + Wu(6) = 8 (Ren) -> Ren-Wu (壬午).
        val noonPillar = ChineseZodiacCalculator.getHourPillarForDate(2026, 1, 1, 12)
        assertEquals("壬午", noonPillar.nameZh)
        assertEquals("Horse", noonPillar.animal)

        // At 23:00 on 2026-01-01, day rolls to 2026-01-02 (Bing-Zi day).
        // On Bing day: baseStem = (Bing.index % 5) * 2 = 2 * 2 = 4 (Wu).
        // Hour stem = Wu(4) + Zi(0) = 4 (Wu) -> Wu-Zi (戊子).
        val lateRatPillar = ChineseZodiacCalculator.getHourPillarForDate(2026, 1, 1, 23)
        assertEquals("戊子", lateRatPillar.nameZh)
        assertEquals("Rat", lateRatPillar.animal)
    }

    @Test
    fun testInputValidation() {
        assertFailsWith<IllegalArgumentException> { ChineseZodiacCalculator.gregorianToJdn(0, 1, 1) }
        assertFailsWith<IllegalArgumentException> { ChineseZodiacCalculator.gregorianToJdn(2024, 2, 30) }
        assertFailsWith<IllegalArgumentException> { ChineseZodiacCalculator.getHourBranch(-1) }
        assertFailsWith<IllegalArgumentException> { ChineseZodiacCalculator.getHourBranch(24) }
    }

    // --- 0.5.0: astrological solar calendar (year/month pillars, Four Pillars) ---

    @Test
    fun testSectionalTermAnchors() {
        assertEquals(4, ChineseZodiacCalculator.getSectionalTermDay(2024, 2)) // Lichun
        assertEquals(3, ChineseZodiacCalculator.getSectionalTermDay(2025, 2)) // not the constant Feb 4
        assertEquals(4, ChineseZodiacCalculator.getSectionalTermDay(2026, 2))
        assertEquals(7, ChineseZodiacCalculator.getSectionalTermDay(2026, 9)) // Bailu
        assertEquals(5, ChineseZodiacCalculator.getSectionalTermDay(2025, 1)) // Xiaohan
        // Published almanac days for near-midnight terms (HKO-verified).
        assertEquals(5, ChineseZodiacCalculator.getSectionalTermDay(1980, 2))
        assertEquals(6, ChineseZodiacCalculator.getSectionalTermDay(1943, 4)) // equals CN_TABLE
        assertEquals(7, ChineseZodiacCalculator.getSectionalTermDay(1917, 12))
        assertEquals(5, ChineseZodiacCalculator.getSectionalTermDay(2084, 6))
        assertEquals(7, ChineseZodiacCalculator.getSectionalTermDay(1911, 5))
    }

    @Test
    fun testLichunYearBoundaryTransitions() {
        // 2024 Lichun fell on Feb 4; Feb 3 is still Gui-Mao (Water Rabbit).
        assertEquals("癸卯", ChineseZodiacCalculator.getYearPillar(2024, 2, 3).nameZh)
        assertEquals("Rabbit", ChineseZodiacCalculator.getYearPillar(2024, 2, 3).animal)
        // On Lichun day itself the astrological year has changed.
        assertEquals("甲辰", ChineseZodiacCalculator.getYearPillar(2024, 2, 4).nameZh)
        assertEquals("Dragon", ChineseZodiacCalculator.getYearPillar(2024, 2, 5).animal)

        // 2025 Lichun fell on Feb 3; the day before is still the prior year.
        assertEquals("甲辰", ChineseZodiacCalculator.getYearPillar(2025, 2, 2).nameZh)
        assertEquals("乙巳", ChineseZodiacCalculator.getYearPillar(2025, 2, 3).nameZh)

        assertEquals("乙巳", ChineseZodiacCalculator.getYearPillar(2026, 1, 15).nameZh)
        assertEquals("Snake", ChineseZodiacCalculator.getYearPillar(2026, 1, 15).animal)
        assertEquals("丙午", ChineseZodiacCalculator.getYearPillar(2026, 2, 10).nameZh)
        assertEquals("Horse", ChineseZodiacCalculator.getYearPillar(2026, 2, 10).animal)
    }

    @Test
    fun testMonthPillarAndFiveTigersRule() {
        // 2024-02-10: Jia-Chen year past Lichun; Tiger month stem on a Jia year is Bing.
        val m2024 = ChineseZodiacCalculator.getMonthPillar(2024, 2, 10)
        assertEquals("丙寅", m2024.nameZh)
        assertEquals("Tiger", m2024.animal)

        // 2026-09-22 is past Bailu (Sep 7); Rooster month on a Bing year: Geng base + 7 = Ding.
        assertEquals("丁酉", ChineseZodiacCalculator.getMonthPillar(2026, 9, 22).nameZh)
        // The day before Bailu is still the Monkey month.
        assertEquals("丙申", ChineseZodiacCalculator.getMonthPillar(2026, 9, 6).nameZh)
        assertEquals("丁酉", ChineseZodiacCalculator.getMonthPillar(2026, 9, 7).nameZh)

        // 2025-02-03 is Lichun day: Tiger month of the new Yi-Si year (Wu stem base).
        assertEquals("戊寅", ChineseZodiacCalculator.getMonthPillar(2025, 2, 3).nameZh)
        // 2025-02-02 is still the Ox month of the Jia-Chen year (Bing base + 11 = Ding).
        assertEquals("丁丑", ChineseZodiacCalculator.getMonthPillar(2025, 2, 2).nameZh)
    }

    @Test
    fun testFullFourPillarsAndClashes() {
        val pillars = ChineseZodiacCalculator.getFourPillars(2026, 9, 22, 12)

        assertEquals("丙午", pillars.year.nameZh)
        assertEquals(EarthlyBranch.WU, pillars.year.branch)
        assertEquals(EarthlyBranch.ZI, pillars.yearClash) // Horse clashes with Rat
        assertEquals("ជូត (Chuot)", pillars.year.clashKhmerAnimal)

        assertEquals("丁酉", pillars.month.nameZh)
        assertEquals(EarthlyBranch.YOU, pillars.month.branch)
        assertEquals(EarthlyBranch.MAO, pillars.monthClash) // Rooster clashes with Rabbit

        assertEquals("己亥", pillars.day.nameZh)
        assertEquals(EarthlyBranch.HAI, pillars.day.branch)
        assertEquals(EarthlyBranch.SI, pillars.dayClash) // Pig clashes with Snake

        assertEquals("庚午", pillars.hour.nameZh)
        assertEquals(EarthlyBranch.WU, pillars.hour.branch)
        assertEquals(EarthlyBranch.ZI, pillars.hourClash) // Horse hour clashes with Rat
    }

    @Test
    fun testQingmingDaysMatchChineseEngineTable() {
        val engine = ChineseLunisolarEngine(FestivalProfile.CN_REFERENCE_UTC8)
        for (year in 1900..2100) {
            val iso = engine.getFestivalDates(year, "chinese_qingming_festival")[0]
            val day = iso.substring(8, 10).toInt()
            assertEquals(day, ChineseZodiacCalculator.getSectionalTermDay(year, 4), "Qingming $year")
        }
    }

    @Test
    fun testSolarRangeAndDateValidation() {
        for (bad in listOf(1899, 2101)) {
            assertFailsWith<IllegalArgumentException> { ChineseZodiacCalculator.getYearPillar(bad, 6, 1) }
            assertFailsWith<IllegalArgumentException> { ChineseZodiacCalculator.getMonthPillar(bad, 6, 1) }
            assertFailsWith<IllegalArgumentException> { ChineseZodiacCalculator.getFourPillars(bad, 6, 1, 12) }
            assertFailsWith<IllegalArgumentException> { ChineseZodiacCalculator.getSectionalTermDay(bad, 6) }
        }
        assertFailsWith<IllegalArgumentException> { ChineseZodiacCalculator.getYearPillar(2025, 2, 30) }
        assertFailsWith<IllegalArgumentException> { ChineseZodiacCalculator.getMonthPillar(2025, 13, 1) }
        assertFailsWith<IllegalArgumentException> { ChineseZodiacCalculator.getSectionalTermDay(2024, 13) }
    }
}
