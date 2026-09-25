@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package com.rsgkh.calendar.engine

import kotlin.js.JsExport

/**
 * Standalone sexagenary (Ganzhi) calculations: day and hour pillars, the
 * astrological solar calendar (year and month pillars), the Four Pillars
 * (BaZi) and branch clashes.
 *
 * Day and hour pillars are pure arithmetic over the proleptic Gregorian
 * calendar (1..9999). Year and month pillars depend on the 12 sectional solar
 * terms (Jie) tabulated for 1900..2100 at the China Standard reference
 * meridian (UTC+8); a transition takes effect at 00:00 of the term's civil
 * date. The April bits are kept identical to ChineseLunisolarEngine's
 * CN_TABLE Qingming days; tools/generate_solar_terms.py regenerates and
 * validates this table, including the published almanac days for the
 * near-midnight terms.
 */
@JsExport
object ChineseZodiacCalculator {

    const val MIN_SOLAR_YEAR = 1900
    const val MAX_SOLAR_YEAR = 2100

    // Base civil day per month for the 12 sectional terms (UTC+8):
    // Jan Xiaohan, Feb Lichun, Mar Jingzhe, Apr Qingming, May Lixia, Jun Mangzhong,
    // Jul Xiaoshu, Aug Liqiu, Sep Bailu, Oct Hanlu, Nov Lidong, Dec Daxue.
    private val BASE_JIE_DAYS = intArrayOf(4, 3, 4, 4, 4, 4, 6, 6, 6, 7, 6, 6)

    // Little-endian; 2 bits per month hold (day - base), 3 bytes per year,
    // 603 bytes for 1900..2100. Verified against the published almanac record
    // (Hong Kong Observatory tables) and the engine's CN_TABLE Qingming days.
    private val SOLAR_JIE_TABLE: ByteArray by lazy {
        decodeJieHex(
            "669a6a66aaaaaaaeaabaefab6b9a6a66aaaaaaaaaabaefab6b9a6a66aaaaaaaaaa" +
            "baefab6b9a6a66aaaa66aaaaaaaeab6a9a6666aa6a66aaaaaaaeab6a9a6666aa6a" +
            "66aaaaaaaeab6a9a6666aa6a66aaaaaaaeaa6a9a56669a6a66aaaaaaaeaa6a9a56" +
            "669a6a66aaaaaaaaaa6a9a56669a6a66aaaaaaaaaa6a9a56669a6a66aaaaaaaaaa" +
            "6a9956669a6666aaaa66aaaa5a5956659a6666aaaa66aaaa5a5956659a6666aa6a" +
            "66aaaa5a5956659a66669a6a66aaaa5a5955659a56669a6a66aaaa5a5955659a56" +
            "669a6a66aaaa5a5555659a56669a6a66aaaa5a5555659956669a6a66aaaa1a5555" +
            "655956669a6666aaaa1a5555655956669a6666aaaa165555555956659a66669a6a" +
            "165555555955659a66669a6a165555555955659a56669a6a165555555555659a56" +
            "669a6a165555555555659a56669a6a165555555555655956669a6a165555155555" +
            "655956669a66165555155555655956669a66165555115555555956659a66164555" +
            "115555555955659a66164515115555555555659a56164515115555555555659a56" +
            "164515115555555555655956164515115555555555655956164515115555155555" +
            "655956164511115555155555655956164511114555115555555955154511114555" +
            "115555555555154511114515115555555555154501114515115555555555154501" +
            "114515115555555555150401114515115555555555150401114511115555155555" +
            "150401114511114555155555150401114511114555115555050400104511114555" +
            "115555050000104511114515115555050000104501114515115555050000100501" +
            "114515115555555555"
        )
    }

    private fun decodeJieHex(hex: String): ByteArray {
        require(hex.length == 603 * 2 && hex.all { it in '0'..'9' || it in 'a'..'f' }) { "Invalid solar terms table" }
        return ByteArray(603) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }

    /**
     * Converts a civil Gregorian date to an astronomical Julian Day Number (JDN)
     * using the Fliegel & van Flandern integer algorithm.
     * Output is a 32-bit signed Int (valid for years 1..9999; max intermediate
     * term is ~5.4M, far below Int overflow).
     */
    fun gregorianToJdn(year: Int, month: Int, day: Int): Int {
        requireInteger(year)
        requireInteger(month)
        requireInteger(day)
        require(year in 1..9999) { "Gregorian year must be 1..9999" }
        require(month in 1..12) { "Gregorian month must be 1..12" }
        require(day in 1..daysInMonth(year, month)) { "Invalid Gregorian day" }

        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    }

    /** JDN calculation directly from the engine's GregorianDate model. */
    fun gregorianDateToJdn(date: GregorianDate): Int =
        gregorianToJdn(date.year, date.month, date.day)

    /** Civil day of the month (UTC+8) on which a sectional solar term (Jie) begins. */
    fun getSectionalTermDay(year: Int, month: Int): Int {
        requireInteger(year)
        requireInteger(month)
        require(year in MIN_SOLAR_YEAR..MAX_SOLAR_YEAR) { "Year must be $MIN_SOLAR_YEAR..$MAX_SOLAR_YEAR" }
        require(month in 1..12) { "Month must be 1..12" }
        val i = (year - MIN_SOLAR_YEAR) * 3
        val word = (SOLAR_JIE_TABLE[i].toInt() and 255) or
            ((SOLAR_JIE_TABLE[i + 1].toInt() and 255) shl 8) or
            ((SOLAR_JIE_TABLE[i + 2].toInt() and 255) shl 16)
        return BASE_JIE_DAYS[month - 1] + ((word ushr ((month - 1) * 2)) and 3)
    }

    private fun requireSolarDate(year: Int, month: Int, day: Int) {
        requireInteger(year)
        requireInteger(month)
        requireInteger(day)
        require(year in MIN_SOLAR_YEAR..MAX_SOLAR_YEAR) { "Year must be $MIN_SOLAR_YEAR..$MAX_SOLAR_YEAR" }
        require(month in 1..12) { "Month must be 1..12" }
        require(day in 1..daysInMonth(year, month)) { "Invalid Gregorian day" }
    }

    /**
     * Calculates the Astrological Year Pillar. The astrological year changes
     * at Lichun (early February), not on January 1 or the lunar new year.
     */
    fun getYearPillar(year: Int, month: Int, day: Int): GanzhiPillar {
        requireSolarDate(year, month, day)
        val lichunDay = getSectionalTermDay(year, 2)
        val beforeLichun = month < 2 || (month == 2 && day < lichunDay)
        val solarYear = if (beforeLichun) year - 1 else year
        val stem = HeavenlyStem.fromIndex(floorMod(solarYear - 4, 10))
        val branch = EarthlyBranch.fromIndex(floorMod(solarYear - 4, 12))
        return GanzhiPillar(stem, branch)
    }

    fun getYearPillarForGregorianDate(date: GregorianDate): GanzhiPillar =
        getYearPillar(date.year, date.month, date.day)

    /**
     * Calculates the Astrological Month Pillar from the 12 sectional solar
     * terms and the classical "Five Tigers Seeking Month" rule (五虎遁月法).
     */
    fun getMonthPillar(year: Int, month: Int, day: Int): GanzhiPillar {
        requireSolarDate(year, month, day)
        val isPastTerm = day >= getSectionalTermDay(year, month)

        // Astrological month offset (0 = Tiger month at Lichun ... 11 = Ox month at Xiaohan).
        val monthOffset: Int
        val solarYearForStem: Int
        when {
            month == 1 -> {
                // January: before Xiaohan is the Rat month; from Xiaohan the Ox month, both of the prior solar year.
                monthOffset = if (isPastTerm) 11 else 10
                solarYearForStem = year - 1
            }
            month == 2 -> {
                // February: from Lichun the Tiger month of the current solar year; before it the prior year's Ox month.
                monthOffset = if (isPastTerm) 0 else 11
                solarYearForStem = if (isPastTerm) year else year - 1
            }
            else -> {
                monthOffset = if (isPastTerm) month - 2 else month - 3
                solarYearForStem = year
            }
        }

        val yearStemIndex = floorMod(solarYearForStem - 4, 10)
        val baseMonthStem = floorMod((yearStemIndex % 5) * 2 + 2, 10)
        val stem = HeavenlyStem.fromIndex(baseMonthStem + monthOffset)
        val branch = EarthlyBranch.fromIndex(2 + monthOffset) // Month 0 is Yin (Tiger).
        return GanzhiPillar(stem, branch)
    }

    fun getMonthPillarForGregorianDate(date: GregorianDate): GanzhiPillar =
        getMonthPillar(date.year, date.month, date.day)

    /**
     * Computes the 60-day pillar (Ganzhi) for a Gregorian date.
     * The sexagenary day cycle is an unbroken modulo-60 counter with anchor offset +49.
     */
    fun getDayPillar(year: Int, month: Int, day: Int): GanzhiPillar {
        val jdn = gregorianToJdn(year, month, day)
        return dayPillarFromJdn(jdn)
    }

    private fun dayPillarFromJdn(jdn: Int): GanzhiPillar {
        val cycle = floorMod(jdn + 49, 60)
        val stem = HeavenlyStem.fromIndex(cycle % 10)
        val branch = EarthlyBranch.fromIndex(cycle % 12)
        return GanzhiPillar(stem, branch)
    }

    fun getDayPillarForGregorianDate(date: GregorianDate): GanzhiPillar =
        getDayPillar(date.year, date.month, date.day)

    /**
     * Returns the Earthly Branch (Zodiac animal) for an hour in civil time (0..23).
     * Windows start with the Rat (Zi) at 23:00.
     */
    fun getHourBranch(hourOfDay: Int): EarthlyBranch {
        requireInteger(hourOfDay)
        require(hourOfDay in 0..23) { "Hour must be 0..23, received $hourOfDay" }
        val branchIndex = ((hourOfDay + 1) / 2) % 12
        return EarthlyBranch.fromIndex(branchIndex)
    }

    /**
     * Computes the Hour Pillar from a Day Stem using the classical
     * "Five Rats Seeking Day" rule (五鼠遁日起时法).
     *
     * Note: 23:00 belongs to the upcoming day's Rat (Zi) branch. Callers using
     * this raw primitive for 23:00 must supply tomorrow's stem if adhering to
     * the midnight boundary. Use [getHourPillarForDate] for automated date handling.
     */
    fun getHourPillar(dayStem: HeavenlyStem, hourOfDay: Int): GanzhiPillar {
        val branch = getHourBranch(hourOfDay)
        val baseStemIndex = (dayStem.index % 5) * 2
        val hourStem = HeavenlyStem.fromIndex(baseStemIndex + branch.index)
        return GanzhiPillar(hourStem, branch)
    }

    /**
     * Computes the complete Hour Pillar for a Gregorian date and hour.
     * Automatically rolls the day stem forward at 23:00 (late Rat hour);
     * the month and year pillars keep the calendar date.
     */
    fun getHourPillarForDate(year: Int, month: Int, day: Int, hourOfDay: Int): GanzhiPillar {
        requireInteger(hourOfDay)
        require(hourOfDay in 0..23) { "Hour must be 0..23, received $hourOfDay" }
        val jdn = gregorianToJdn(year, month, day)
        val effectiveJdn = if (hourOfDay == 23) jdn + 1 else jdn
        val effectiveDayStem = dayPillarFromJdn(effectiveJdn).stem
        return getHourPillar(effectiveDayStem, hourOfDay)
    }

    fun getHourPillarForGregorianDate(date: GregorianDate, hourOfDay: Int): GanzhiPillar =
        getHourPillarForDate(date.year, date.month, date.day, hourOfDay)

    /**
     * Constructs the complete Four Pillars of Destiny (BaZi) and the four
     * clash branches. Requires 1900..2100 (the solar term table's range).
     * Both day and hour roll forward at 23:00 (late Rat hour);
     * year and month retain the civil calendar date.
     */
    fun getFourPillars(year: Int, month: Int, day: Int, hourOfDay: Int): FourPillars {
        requireInteger(hourOfDay)
        require(hourOfDay in 0..23) { "Hour must be 0..23, received $hourOfDay" }
        val jdn = gregorianToJdn(year, month, day)
        val effectiveJdn = if (hourOfDay == 23) jdn + 1 else jdn
        val effectiveDayPillar = dayPillarFromJdn(effectiveJdn)
        return FourPillars(
            getYearPillar(year, month, day),
            getMonthPillar(year, month, day),
            effectiveDayPillar,
            getHourPillar(effectiveDayPillar.stem, hourOfDay)
        )
    }

    fun getFourPillarsForGregorianDate(date: GregorianDate, hourOfDay: Int): FourPillars =
        getFourPillars(date.year, date.month, date.day, hourOfDay)

    init {
        // Freeze after all fields exist; freezing earlier breaks Kotlin/JS field assignment.
        freezeValue(this)
    }
}
