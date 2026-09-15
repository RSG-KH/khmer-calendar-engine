package com.rsgkh.calendar.engine

import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.time.LocalDate
import kotlin.test.*

class CompatibilityTest {
    private val engine = KhmerCalendarEngine()

    @Test fun pinnedUpstreamLunarHashes1900Through2100() {
        val rows = checkNotNull(javaClass.getResourceAsStream("/momentkh-compatibility.txt"))
            .bufferedReader().use { it.readLines() }.filter { it.isNotBlank() && !it.startsWith('#') }
        assertEquals(201, rows.size)
        for (row in rows) {
            val (yearText, expectedHash, upstreamStart) = row.split('|')
            val year = yearText.toInt()
            val hash = MessageDigest.getInstance("SHA-256")
            var date = LocalDate.of(year, 1, 1)
            while (date.year == year) {
                val lunar = engine.fromGregorian(date.year, date.monthValue, date.dayOfMonth).lunar
                hash.update("$date,${lunar.day},${if (lunar.waxing) 0 else 1},${lunar.month},${lunar.buddhistYear}\n".toByteArray(Charsets.UTF_8))
                date = date.plusDays(1)
            }
            assertEquals(expectedHash, hash.digest().joinToString("") { "%02x".format(it) }, "$year lunar compatibility")
            if (year == 2012) {
                // Keep the original fixture visible; independently reviewed dates correct its known error.
                assertEquals("2012-04-14", upstreamStart)
                assertEquals("2012-04-13", engine.newYear(year).start.iso)
            } else assertEquals(upstreamStart, engine.newYear(year).start.iso, "$year New Year compatibility")
        }
    }

    @Test fun javaTimeOracleAndCrossPlatformBaseline() {
        val path = Path.of(System.getProperty("engine.baselineFile", "build/reports/engine-baseline.tsv"))
        Files.createDirectories(path.parent)
        Files.newBufferedWriter(path, Charsets.UTF_8).use { out ->
            var date = LocalDate.of(1800, 1, 1)
            while (date.year <= 2200) {
                val value = engine.fromGregorian(date.year, date.monthValue, date.dayOfMonth)
                val lunar = value.lunar
                assertEquals(date.toEpochDay(), value.date.epochDay.toLong(), date.toString())
                assertEquals(date.dayOfWeek.value, value.date.dayOfWeek, date.toString())
                out.appendLine(listOf("D", value.date.iso, lunar.day, lunar.waxing, lunar.month,
                    lunar.buddhistYear, lunar.monthLength, lunar.isHolyDay, lunar.isShavingDay,
                    value.animalYear, value.sak, value.animalYearChangesToday, value.sakChangesToday).joinToString("\t"))
                date = date.plusDays(1)
            }
            for (year in 1800..2200) {
                val value = engine.newYear(year)
                out.appendLine("N\t$year\t${value.start.iso}\t${value.days}")
                // Exercise each recurrence family in both compiled targets.
                val rules = arrayOf(
                    RecurrenceRule("fixed", "solar", 5, 14),
                    RecurrenceRule("weekday", "solar_nth_weekday", 5, 7, occurrence = 2),
                    RecurrenceRule("lent", "khmer_lunar", 7, 1, waxing = false, monthPolicy = "ordinary_or_second_asadh"),
                    RecurrenceRule("first", "new_year_first"), RecurrenceRule("middle", "new_year_middle"),
                    RecurrenceRule("last", "new_year_last"),
                )
                for (rule in rules) for (occurrence in engine.evaluateRule(year, rule)) {
                    out.appendLine("R\t$year\t${rule.id}\t${occurrence.date.iso}")
                }
            }
        }
    }
}
