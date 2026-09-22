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
}
