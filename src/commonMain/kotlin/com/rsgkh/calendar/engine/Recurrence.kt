@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package com.rsgkh.calendar.engine

import kotlin.js.JsExport

/** Data-driven rule; contains no event labels or implied public-holiday classification.
 * For lunar rules [monthPolicy] is "exact" or "ordinary_or_second_asadh".
 * For Chinese festival rules [monthPolicy] can be "cn-reference-utc8" or "archive-v1".
 * For weekday rules [day] is ISO weekday and [occurrence] is 1..5. */
@JsExport
class RecurrenceRule(
    val id: String,
    val type: String,
    val month: Int = 1,
    val day: Int = 1,
    val waxing: Boolean = true,
    val offset: Int = 0,
    val duration: Int = 1,
    val fromYear: Int = MIN_YEAR,
    val throughYear: Int = MAX_YEAR,
    val monthPolicy: String = "exact",
    val occurrence: Int = 1,
) {
    init {
        require(id.isNotBlank() && id.length <= 128) { "A rule needs a stable non-blank ID (max 128 characters)" }
        listOf(month, day, offset, duration, fromYear, throughYear, occurrence).forEach(::requireInteger)
        requireCalendarYear(fromYear)
        requireCalendarYear(throughYear)
        require(fromYear <= throughYear) { "Invalid effective-year range" }
        require(offset in -366..366 && duration in 1..366) { "Invalid offset or duration" }
        require(monthPolicy in setOf("exact", "ordinary_or_second_asadh", "cn-reference-utc8", "archive-v1")) { "Unknown lunar month or festival policy" }
        require(monthPolicy != "ordinary_or_second_asadh" || (type == "khmer_lunar" && month == 7)) { "Second-Asadh policy requires an Asadh lunar rule" }
        when (type) {
            "solar" -> {
                require(month in 1..12 && day in 1..daysInMonth(2000, month)) { "Invalid fixed Gregorian date" }
                require(occurrence == 1 && waxing) { "Unexpected fixed-date rule parameters" }
            }
            "khmer_lunar" -> {
                require(month in 0..13 && day in 1..15) { "Invalid lunar date" }
                require(occurrence == 1) { "Unexpected lunar rule occurrence" }
            }
            "solar_nth_weekday" -> {
                require(month in 1..12 && day in 1..7 && occurrence in 1..5 && waxing) { "Invalid weekday rule" }
            }
            "new_year_first", "new_year_middle", "new_year_last" ->
                require(month == 1 && day == 1 && waxing && occurrence == 1) { "New Year stages do not take month/day parameters" }
            "chinese_festival" -> {
                require(id in ChineseLunisolarEngine.FESTIVAL_IDS) { "Unknown Chinese festival ID: $id" }
                require(month == 1 && day == 1 && waxing && occurrence == 1) { "Chinese festival rules do not take month/day parameters" }
            }
            else -> throw IllegalArgumentException("Unknown recurrence type: $type")
        }
        freezeValue(this)
    }
}

/** Explicit replacement of a rule's dates for an anchor year. Empty dates cancel that year.
 * An override can also supply dates outside the rule's normal effective-year range. */
@JsExport
class EventDateOverride(
    val ruleId: String, val year: Int, dates: Array<GregorianDate>,
    val sourceId: String, val reason: String,
) {
    private val storedDates = dates.copyOf()
    val dates: Array<GregorianDate> get() = storedDates.copyOf()
    init {
        requireCalendarYear(year)
        require(ruleId.isNotBlank() && sourceId.isNotBlank() && reason.isNotBlank()) { "An override needs a rule ID, source and reason" }
        storedDates.forEach { requireCalendarYear(it.year) }
        require(storedDates.map { it.epochDay }.distinct().size == storedDates.size) { "Duplicate override dates" }
        freezeValue(storedDates)
        freezeValue(this)
    }
}

@JsExport
class EventOccurrence internal constructor(
    val ruleId: String, val date: GregorianDate, val basis: String, val sourceId: String?,
) {
    init { freezeValue(this) }
}

internal fun evaluateRecurrence(year: Int, rule: RecurrenceRule, replacement: EventDateOverride?): Array<EventOccurrence> {
    requireCalendarYear(year)
    if (replacement != null) {
        require(replacement.ruleId == rule.id && replacement.year == year) { "Override does not match the rule and anchor year" }
        return replacement.dates.sortedBy { it.epochDay }
            .map { EventOccurrence(rule.id, it, "source_override", replacement.sourceId) }.toTypedArray()
    }
    if (year !in rule.fromYear..rule.throughYear) return emptyArray()
    val anchors = when (rule.type) {
        "solar" -> if (rule.day <= daysInMonth(year, rule.month)) listOf(GregorianDate(year, rule.month, rule.day)) else emptyList()
        "solar_nth_weekday" -> {
            val first = GregorianDate(year, rule.month, 1)
            val day = 1 + floorMod(rule.day - first.dayOfWeek, 7) + (rule.occurrence - 1) * 7
            if (day <= daysInMonth(year, rule.month)) listOf(GregorianDate(year, rule.month, day)) else emptyList()
        }
        "khmer_lunar" -> buildList {
            var date = GregorianDate(year, 1, 1)
            val last = GregorianDate(year, 12, 31).epochDay
            while (date.epochDay <= last) {
                val lunar = LunarCalendar.fromGregorian(date)
                val monthMatches = lunar.month == rule.month ||
                    (rule.monthPolicy == "ordinary_or_second_asadh" && lunar.month == 13)
                if (monthMatches && lunar.day == rule.day && lunar.waxing == rule.waxing) add(date)
                date = date.plusDays(1)
            }
        }
        "new_year_first" -> listOf(SolarNewYear.forYear(year).start)
        "new_year_middle" -> SolarNewYear.forYear(year).dates.drop(1).dropLast(1)
        "new_year_last" -> listOf(SolarNewYear.forYear(year).end)
        "chinese_festival" -> {
            if (year !in ChineseLunisolarEngine.MIN_YEAR..ChineseLunisolarEngine.MAX_YEAR) {
                emptyList()
            } else {
                val profile = if (rule.monthPolicy == "archive-v1") FestivalProfile.ARCHIVE_V1 else FestivalProfile.CN_REFERENCE_UTC8
                val chineseEngine = ChineseLunisolarEngine(profile)
                chineseEngine.getFestivalDates(year, rule.id).map { iso ->
                    val parts = iso.split("-").map { it.toInt() }
                    GregorianDate(parts[0], parts[1], parts[2])
                }.toList()
            }
        }
        else -> error("Validated rule type is unsupported")
    }
    return anchors.flatMap { anchor -> (0 until rule.duration).map { anchor.plusDays(rule.offset + it) } }
        .distinctBy { it.epochDay }.sortedBy { it.epochDay }.map { date ->
            requireCalendarYear(date.year)
            EventOccurrence(rule.id, date, "calculated", null)
        }.toTypedArray()
}
