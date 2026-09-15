// Traditional solar New Year arithmetic derived from MomentKH (MIT). See NOTICE.
package com.rsgkh.calendar.engine

internal object SolarNewYear {
    private fun aharkun(year: Int) = (year * 292207L + 373) / 800 + 1
    private fun kromthupul(year: Int) = (800 - (year * 292207L + 373) % 800).toInt()
    private fun avoman(year: Int) = ((aharkun(year) * 11 + 650) % 692).toInt()
    private fun bodithey(year: Int): Int {
        val ah = aharkun(year)
        return ((ah + (11 * ah + 650) / 692) % 30).toInt()
    }
    private fun leapMonth(year: Int): Boolean {
        val b = bodithey(year)
        val next = bodithey(year + 1)
        if (b == 24 && next == 6) return true
        if (b == 25 && next == 5) return false
        return b > 24 || b < 6
    }
    private fun leapDay(year: Int): Boolean {
        val a = avoman(year)
        if (a == 0 && avoman(year - 1) == 137) return true
        if (kromthupul(year) <= 207) return a < 127
        if (a == 137 && avoman(year + 1) == 0) return false
        return a < 138
    }
    private fun solarDegree(year: Int, sotin: Int): Int {
        val r2 = 800 * sotin + kromthupul(year - 1)
        val average = 1800 * (r2 / 24350) + 60 * (r2 % 24350 / 811) + r2 % 24350 % 811 / 14 - 3
        val left = if (average < 4800) average - 4800 + 21600 else average - 4800
        val quadrant = left / 1800
        val remainder = when (quadrant) {
            in 0..2 -> quadrant
            in 3..5 -> 10800 - left
            in 6..8 -> left - 10800
            else -> 21600 - left
        }
        val angle = remainder % 1800 / 60
        val minute = remainder % 60
        val segment = 2 * (remainder / 1800) + if (angle >= 15) 1 else 0
        val portion = 60 * (if (angle >= 15) angle - 15 else angle) + minute
        val multipliers = intArrayOf(35, 32, 27, 22, 13, 5)
        val corrections = intArrayOf(0, 35, 67, 94, 116, 129)
        val correction = if (segment <= 5) portion * multipliers[segment] / 900 + corrections[segment] else 134
        val inauguration = if (quadrant <= 5) average - correction else average + correction
        return inauguration % 1800 / 60
    }
    private val celebrations by lazy { (MIN_YEAR..MAX_YEAR).map(::calculate) }
    fun forYear(year: Int): NewYearCelebration {
        requireCalendarYear(year)
        return celebrations[year - MIN_YEAR]
    }
    private fun calculate(year: Int): NewYearCelebration {
        val jsYear = year - 638
        val firstSotin = if (kromthupul(jsYear - 1) <= 207) 363 else 362
        val days = if (solarDegree(jsYear, firstSotin) == 0) 4 else 3
        var b = bodithey(jsYear)
        if (leapMonth(jsYear - 1) && leapDay(jsYear - 1)) b = (b + 1) % 30
        val lerngSakMonth = if (b >= 6) 4 else 5
        val lerngSakDay = if (b >= 6) b - 1 else b
        val epoch = GregorianDate(year, 4, 17)
        val lunar = LunarCalendar.fromGregorian(epoch)
        val ordinal = lunar.day - 1 + if (lunar.waxing) 0 else 15
        val difference = (lunar.month - 4) * 29 + ordinal - ((lerngSakMonth - 4) * 29 + lerngSakDay)
        // references.md, reviewed 2012 case: the former Apr 14 override contradicted the sources.
        // Other modern upstream date overrides equal the formula, so no date override is applied.
        val start = epoch.plusDays(-(difference + days - 1))
        return NewYearCelebration(start, days)
    }
}
