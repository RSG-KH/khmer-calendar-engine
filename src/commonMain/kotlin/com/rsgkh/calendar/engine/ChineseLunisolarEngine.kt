@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package com.rsgkh.calendar.engine

import kotlin.js.JsExport

/** Named computation policies; none claims to certify Cambodian community practice. */
@JsExport
enum class FestivalProfile(val id: String) {
    ARCHIVE_V1("archive-v1"),
    CN_REFERENCE_UTC8("cn-reference-utc8"),
    CN_LUNAR_UTC7_SOLAR("cn-lunar-utc7-solar"),
    LOCAL_UTC7_MODEL("local-utc7-model");

    init {
        freezeValue(this)
    }

    companion object {
        fun fromId(id: String): FestivalProfile = entries.firstOrNull { it.id == id }
            ?: throw IllegalArgumentException("Unknown profile: $id")
    }
}

@JsExport
data class ChineseLunarDate(val year: Int, val month: Int, val day: Int, val isLeap: Boolean) {
    init { freezeValue(this) }
}

/**
 * commonMain-only, zero external runtime dependencies. No system timezone,
 * java.time, kotlinx.datetime, I/O, or astronomical floating-point calculations.
 * Public festival years: 1900..2100. Two packed tables total 1206 data bytes.
 * Pass a profile explicitly in new integrations; no-argument mode is legacy.
 */
@JsExport
class ChineseLunisolarEngine(val profile: FestivalProfile = FestivalProfile.ARCHIVE_V1) {
    private val lunar = if (profile == FestivalProfile.LOCAL_UTC7_MODEL) LOCAL_TABLE else CN_TABLE
    private val solar = if (profile == FestivalProfile.LOCAL_UTC7_MODEL || profile == FestivalProfile.CN_LUNAR_UTC7_SOLAR) LOCAL_TABLE else CN_TABLE

    init { freezeValue(this) }

    private fun cnySerial(year: Int): Int {
        var out = beforeYear(1900) + 30 // 1900-01-31 in both frozen profiles.
        for (y in 1900 until year) out += yearDays(word(lunar, y))
        return out
    }

    fun getLeapMonth(year: Int): Int {
        yearCheck(year)
        return word(lunar, year) and 15
    }

    fun getLunarYearDays(year: Int): Int {
        yearCheck(year)
        return yearDays(word(lunar, year))
    }

    fun getLunarMonthDays(year: Int, month: Int, isLeap: Boolean = false): Int {
        yearCheck(year)
        requireInteger(month)
        require(month in 1..12) { "lunar month must be 1..12" }
        val w = word(lunar, year)
        require(!isLeap || (w and 15) == month) { "No leap month $month in $year" }
        return if (isLeap) leapDays(w) else regularDays(w, month)
    }

    fun lunarToGregorian(year: Int, month: Int, day: Int, isLeap: Boolean = false): String {
        requireInteger(day)
        val days = getLunarMonthDays(year, month, isLeap)
        require(day in 1..days) { "lunar day must be 1..$days" }
        return fromSerial(cnySerial(year) + offset(word(lunar, year), month, isLeap) + day - 1)
    }

    /** Also supports the 1899/12 boundary fragment, Gregorian 1900-01-01..30. */
    fun gregorianToLunar(date: String): ChineseLunarDate {
        val target = toSerial(date)
        val base = cnySerial(1900)
        val guardDays = 29 + ((word(lunar, 1900) ushr 21) and 1)
        if (target < base) {
            require(target >= base - guardDays) { "date precedes supported boundary" }
            return ChineseLunarDate(1899, 12, target - (base - guardDays) + 1, false)
        }
        var y = 1900
        var left = target - base
        while (y <= 2100 && left >= yearDays(word(lunar, y))) {
            left -= yearDays(word(lunar, y))
            y++
        }
        require(y <= 2100) { "date exceeds supported lunar table" }
        val w = word(lunar, y)
        for (m in 1..12) {
            val normal = regularDays(w, m)
            if (left < normal) return ChineseLunarDate(y, m, left + 1, false)
            left -= normal
            if (m == (w and 15)) {
                if (left < leapDays(w)) return ChineseLunarDate(y, m, left + 1, true)
                left -= leapDays(w)
            }
        }
        error("Calendar invariant violated")
    }

    fun getFestivalDates(year: Int, festivalId: String): Array<String> {
        yearCheck(year)
        if (festivalId == "chinese_qingming_festival") {
            val adjustment = if (profile == FestivalProfile.ARCHIVE_V1 && year in listOf(2009, 2029)) 1 else 0
            return arrayOf(iso(year, 4, 4 + ((word(solar, year) ushr 17) and 3) + adjustment))
        }
        if (festivalId == "chinese_winter_solstice") {
            return arrayOf(iso(year, 12, 21 + ((word(solar, year) ushr 19) and 3)))
        }
        val cny = cnySerial(year)
        val w = word(lunar, year)
        return when (festivalId) {
            "chinese_new_year_days" -> Array(3) { fromSerial(cny + it) }
            "chinese_new_year_eve" -> arrayOf(fromSerial(cny - 1))
            "chinese_kitchen_god_festival" -> if (year == 1900) {
                arrayOf(fromSerial(cny - (29 + ((w ushr 21) and 1)) + 23))
            } else {
                arrayOf(lunarToGregorian(year - 1, 12, 24))
            }
            "chinese_spirit_parade" -> arrayOf(fromSerial(cny + 14))
            "chinese_zongzi_festival" -> arrayOf(
                fromSerial(cny + offset(w, 5, false) + 4 + if (profile == FestivalProfile.ARCHIVE_V1 && year == 2013) 1 else 0)
            )
            "chinese_ghost_festival" -> arrayOf(fromSerial(cny + offset(w, 7, false) + 14))
            "chinese_mid_autumn_festival" -> arrayOf(fromSerial(cny + offset(w, 8, false) + 14))
            else -> throw IllegalArgumentException("Unknown Chinese festival: $festivalId")
        }
    }

    companion object {
        const val MIN_YEAR = 1900
        const val MAX_YEAR = 2100
        const val TABLE_BYTES = 603
        const val ALL_TABLE_BYTES = 1206

        private val INTERNAL_FESTIVAL_IDS: Array<String> = arrayOf(
            "chinese_new_year_days", "chinese_new_year_eve", "chinese_kitchen_god_festival",
            "chinese_spirit_parade", "chinese_zongzi_festival", "chinese_ghost_festival",
            "chinese_mid_autumn_festival", "chinese_qingming_festival", "chinese_winter_solstice"
        )

        val FESTIVAL_IDS: Array<String>
            get() {
                val copy = INTERNAL_FESTIVAL_IDS.copyOf()
                freezeValue(copy)
                return copy
            }

        internal fun isKnownFestivalId(id: String): Boolean = id in INTERNAL_FESTIVAL_IDS

        private fun decodeHex(hex: String): ByteArray {
            require(hex.length == TABLE_BYTES * 2 && hex.all { it in '0'..'9' || it in 'a'..'f' }) { "Invalid calendar table" }
            return ByteArray(TABLE_BYTES) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
        }

        // Little-endian; lunarInfo in low 17 bits; solar days and guard in spare bits.
        private val CN_TABLE = decodeHex(
            "d84b2ae04a0a70a514d5541460d20a50d90a546515a05614d09a0ad2550ae04a14b6a514d0a40a50d20a55d21340b514" +
            "a0d60aa2ad0ab0950a77491570490ab0a40ab5b40a506a14406d0a54ab0b602b0a709514f2520a70490a66650aa0d414" +
            "50ea0a956a0bd05a0a602b14e3860be0920ad7c80b50c914a0d40aa6d80b50b50aa05614b4a50bd0250ad0920ab2d212" +
            "50a90a57b50aa06c0a50b51255530ba04d0ab0a50a73450bb0520aa8a90a50e90aa06a0aa6ae0a50ab0a604b0ae4aa0a" +
            "70a50a60520a63f20a50d90a575b0aa0560ad0960ad54d0ad04a0ad0a40ad4d40a50d20a58d50840b50aa0b60aa6950b" +
            "b09508b0490a74a90ab0a40a7ab208506a0a406d0a46af0a60ab0070950af54a0a70490ab06400a3740a50ea0a586b0a" +
            "c05a0060ab0ad5960ae0920a60c90054d90aa0d40a50da0a527500a0560ab7ab0ad0250ad09200b5ca0850a90aa0b40a" +
            "a4ba0050ad08d9550aa04b0ab0a500765109b0520a30a90a547900a06a0050ad0a525b0a604b00e6a600e0a40a60d20a" +
            "65ea0030d500a05a0aa3760ad09600fb4a00d04a0ad0a40ab6d00150d20020d50a45dd0aa0b500d05600b25508b0490a" +
            "77a500b0a40050aa0855b20b206d00a0ad00634b0970930af84900704900b06408a6680b50ea00206b00c4a601e0aa0a" +
            "e09200e3d20060c90057d50aa0d40050da00555d00a0560ad0a600d45500d05200b8a90a50a900a0b400a6b60050ad08" +
            "a05500a4ab00b0a500b0520873b200306900377300a06a0850ad00554b01604b0070a508e4540060d10068e90020d500" +
            "a0da00a66a01d05600e04a00d4a900d0a20050d10052f20020d50a"
        )

        private val LOCAL_TABLE = decodeHex(
            "d84b2ae04a0a60a514d5d41460d20a30d90a545515a05614d0960ad2550ad04a12b6a514d0a40a50d20a55d20b40b514" +
            "a0b60aa38d0bb0950a77491570490ab0a40ab6b00b506a14406d0a54ab0b602b0a709514f2520a70490a66650aa0d414" +
            "50ea0a556e0ac05a0a60ab14d3860be0920ad8c90a50a912a0d40aa6d80b50b50aa05612b4ad0ad0250ad0920ab2920b" +
            "50a90a57b50aa06a0a50ad0a55530ba04b0ab0a50a73450b70520a68690a50e90aa06a0aa6ae0a509b0a604b0ae4aa0a" +
            "e0a40a60d20a63f20a20d90a47db0aa0d60ad0960ad54d0ad04a08d0a40ab4d40a50b20a58d50840b50aa0b50aa6550b" +
            "b09500b0490a74a90ab0a40a50aa0052aa0b206d0a47ad0b60ab0070930af54a0a70490ab06400a3740a50ea0a586a0b" +
            "a05600d0aa0ad5960ae0920a60c90054d90aa0d40a50da0a527500a05608a7a70ad0a50ab09200b5aa0850a90aa0b40a" +
            "a4ba0050ad08d9550aa04b0ab0a50076510170520a30690a347900a06a0050ad0a525b0a604b00e6a600e0a40a60d20a" +
            "65ea0020d500a0da0aa3560bd05600fb4a00d0490ad0a40ab6d00150b20020b50825dd0aa0b500d05500b25508b0490a" +
            "77a500b0a40050aa0855b20b206d0060ad00634b0170530ae8490070c900b06400a6680b50da00a05a00a4a601d0aa0a" +
            "e05200e3d20050c90057d50aa0d40050d900555d00a05608d0a600d45500b05200b8a90830a90090b400a6b60050ad08" +
            "a05500a4ab0070a500b0520873b100306900376b00a06a0050ad00552b01602b0070a500e4520060d10058e90020d500" +
            "90da00a65a01d05600e02a00d4a900d0a20050d10052e90020b508"
        )

        private fun yearCheck(year: Int) {
            requireInteger(year)
            require(year in MIN_YEAR..MAX_YEAR) { "year must be $MIN_YEAR..$MAX_YEAR" }
        }

        private fun word(table: ByteArray, year: Int): Int {
            val i = (year - MIN_YEAR) * 3
            return (table[i].toInt() and 255) or ((table[i + 1].toInt() and 255) shl 8) or ((table[i + 2].toInt() and 255) shl 16)
        }

        private fun regularDays(w: Int, month: Int) = 29 + ((w ushr (16 - month)) and 1)
        private fun leapDays(w: Int) = if ((w and 15) == 0) 0 else 29 + ((w ushr 16) and 1)

        private fun yearDays(w: Int): Int {
            var n = (w ushr 4) and 0xfff
            var days = 348 + leapDays(w)
            while (n != 0) {
                n = n and (n - 1)
                days++
            }
            return days
        }

        private fun offset(w: Int, month: Int, isLeap: Boolean): Int {
            var total = 0
            for (m in 1 until month) {
                total += regularDays(w, m)
                if (m == (w and 15)) total += leapDays(w)
            }
            return total + if (isLeap) regularDays(w, month) else 0
        }

        private fun beforeYear(year: Int): Int {
            val y = year - 1
            return 365 * y + y / 4 - y / 100 + y / 400
        }

        private fun solarMonthDays(year: Int, month: Int): Int = when (month) {
            2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }

        private fun iso(year: Int, month: Int, day: Int) =
            year.toString().padStart(4, '0') + "-" + month.toString().padStart(2, '0') + "-" + day.toString().padStart(2, '0')

        private fun fromSerial(serial: Int): String {
            var year = serial / 366 + 1
            while (beforeYear(year + 1) <= serial) year++
            var left = serial - beforeYear(year)
            var month = 1
            while (left >= solarMonthDays(year, month)) {
                left -= solarMonthDays(year, month)
                month++
            }
            return iso(year, month, left + 1)
        }

        private val ISO_PATTERN = Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$")

        private fun toSerial(text: String): Int {
            require(ISO_PATTERN.matches(text)) { "date must be YYYY-MM-DD" }
            val (y, m, d) = text.split('-').map { it.toInt() }
            require(y in 1900..2101 && m in 1..12) { "Gregorian date out of range" }
            require(d in 1..solarMonthDays(y, m)) { "Invalid Gregorian day" }
            var out = beforeYear(y) + d - 1
            for (i in 1 until m) out += solarMonthDays(y, i)
            return out
        }
    }
}
