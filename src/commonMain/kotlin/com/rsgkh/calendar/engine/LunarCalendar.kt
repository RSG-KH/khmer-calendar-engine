// Lunar arithmetic derived from MetheaX and MomentKH (MIT). See NOTICE.
package com.rsgkh.calendar.engine

internal object LunarCalendar {
    private data class MonthStart(val epoch: Int, val month: Int, val length: Int)
    private val minEpoch = GregorianDate(MIN_YEAR, 1, 1).epochDay
    private val maxEpoch = GregorianDate(MAX_YEAR, 12, 31).epochDay
    private fun approximateYear(date: GregorianDate) = date.year + if (date.month <= 4) 543 else 544
    private fun aharkun(year: Int) = (year * 292207L + 499) / 800 + 4
    private fun avoman(year: Int) = ((11 * aharkun(year) + 25) % 692).toInt()
    private fun bodithey(year: Int): Int {
        val ah = aharkun(year)
        return ((11 * ah + 25) / 692 + ah + 29).rem(30).toInt()
    }
    private fun rawLeap(year: Int): Int {
        val b = bodithey(year)
        val a = avoman(year)
        val leapMonth = if (b == 25 && bodithey(year + 1) == 5) false
            else (b == 24 && bodithey(year + 1) == 6) || b >= 25 || b <= 5
        val solarLeap = 800 - (year * 292207L + 499) % 800 <= 207
        val leapDay = if (solarLeap) a <= 126 else a <= 137 && !(a == 137 && avoman(year + 1) == 0)
        return (if (leapMonth) 1 else 0) + (if (leapDay) 2 else 0)
    }
    private fun leapType(year: Int): Int {
        val type = rawLeap(year)
        if (type and 1 != 0) return 1
        if (type and 2 != 0) return 2
        // A leap day deferred by consecutive leap-month years carries forward.
        var previous = year - 1
        while (rawLeap(previous) and 1 != 0) {
            if (rawLeap(previous) and 2 != 0) return 2
            previous--
        }
        return 0
    }
    private fun monthLength(month: Int, year: Int) = when {
        month == 6 && leapType(year) == 2 -> 30
        month >= 12 -> 30
        month % 2 == 0 -> 29
        else -> 30
    }
    private fun nextMonth(month: Int, year: Int) = when (month) {
        6 -> if (leapType(year) == 1) 12 else 7
        11 -> 0
        12 -> 13
        13 -> 8
        else -> month + 1
    }
    private val months by lazy {
        buildList {
            // Arithmetic epoch: 1 waxing Boss. This is an algorithm anchor, not a reviewed almanac date.
            var date = GregorianDate(1799, 12, 27)
            var month = 1
            while (date.epochDay <= maxEpoch + 31) {
                val length = monthLength(month, approximateYear(date))
                add(MonthStart(date.epochDay, month, length))
                date = date.plusDays(length)
                month = nextMonth(month, approximateYear(date))
            }
        }
    }
    private val buddhistNewYears by lazy {
        months.filter { it.month == 5 }.associate { fromEpochDay(it.epoch).year to it.epoch + 15 }
    }
    private val inverseMonths by lazy { months.groupBy { fromEpochDay(it.epoch).year * 16 + it.month } }

    fun fromGregorian(date: GregorianDate): LunarDate {
        requireCalendarYear(date.year)
        val epoch = date.epochDay
        val search = months.binarySearchBy(epoch) { it.epoch }
        val start = months[if (search >= 0) search else -search - 2]
        val offset = epoch - start.epoch
        val beYear = date.year + if (epoch < buddhistNewYears.getValue(date.year)) 543 else 544
        return LunarDate(offset % 15 + 1, offset < 15, start.month, beYear, start.length)
    }

    fun toGregorian(buddhistYear: Int, month: Int, day: Int, waxing: Boolean): GregorianDate {
        listOf(buddhistYear, month, day).forEach(::requireInteger)
        require(buddhistYear in MIN_YEAR + 543..MAX_YEAR + 544) { "Buddhist year outside supported range" }
        require(month in 0..13 && day in 1..15) { "Invalid lunar month or day" }
        val ordinal = day - 1 + if (waxing) 0 else 15
        val matches = buildList {
            for (year in buddhistYear - 544..buddhistYear - 543) {
                for (start in inverseMonths[year * 16 + month].orEmpty()) {
                    val epoch = start.epoch + ordinal
                    if (ordinal < start.length && epoch in minEpoch..maxEpoch) {
                        val date = fromEpochDay(epoch)
                        if (fromGregorian(date).buddhistYear == buddhistYear) add(date)
                    }
                }
            }
        }
        require(matches.size == 1) { "Lunar date does not exist within the supported range" }
        return matches.single()
    }
}
