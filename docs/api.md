# API and calendar conventions

Version **0.1.0** is the initial API. Kotlin and Java use package `com.rsgkh.calendar.engine`; JavaScript uses named exports from `khmer-calendar-engine`. Results contain facts and indices; applications supply translations and display formatting.

## Dates

Create one `KhmerCalendarEngine` and reuse it. Its immutable calculation tables are initialized lazily and shared between instances.

| Operation | Result |
| --- | --- |
| `fromGregorian(year, month, day)` | `CalendarDate`: Gregorian date, lunar date, animal year, Sak and date-level transition flags |
| `toGregorian(buddhistYear, month, day, waxing)` | The corresponding `GregorianDate`; rejects nonexistent lunar dates |
| `newYear(year)` | First date, number of days, last date and all festival dates |
| `evaluateRule(year, rule, dateOverride?)` | Sorted, unique occurrences for the rule's anchor year |

Engine calculations support Gregorian **1800-01-01 through 2200-12-31**, inclusive. Inputs outside this range throw an argument error. The standalone `GregorianDate` helper supports years 1–9999, validates leap days, and provides `iso`, `epochDay`, ISO `dayOfWeek` (Monday 1, Sunday 7) and `plusDays`.

All inputs are **civil dates** in the proleptic Gregorian calendar. There are no device time zones, timestamps or network requests in the calculation. Callers converting a timestamp must choose the intended civil date first, normally the date in Cambodia for a Cambodian calendar.

### Lunar month indices

| Index | Month | Index | Month |
| --- | --- | --- | --- |
| 0 | Migasir | 7 | Asadh, ordinary year |
| 1 | Boss | 8 | Srapon |
| 2 | Meak | 9 | Phutrobot |
| 3 | Phalkun | 10 | Assoch |
| 4 | Chet | 11 | Kattik |
| 5 | Pisakh | 12 | First Asadh, leap-month year |
| 6 | Jestha | 13 | Second Asadh, leap-month year |

Lunar `day` is 1–15; `waxing` distinguishes phases. Waning day 15 does not exist in a 29-day month. `monthLength` is 29 or 30. Holy days are phase day 8, waxing day 15 and the final waning day; shaving days precede them.

The implemented date-label conventions are:

- Buddhist Era increments on **1 waning Pisakh**, following Visak full moon.
- Animal year is indexed 0 (Rat) through 11 (Pig), and changes on the first New Year date.
- Sak is indexed 0 through 9, and changes on the last New Year date.

These are calendar-date labels, not claims about the precise transition instant. Historical validation limits and direct evidence are in [references.md](references.md). No arrival-time API is exposed.

## Recurrence rules

Kotlin supports named constructor arguments. JavaScript also provides `createRule(input)`, which takes a plain object, rejects unknown fields and constructs the same validated `RecurrenceRule` model. The generated constructor remains available for direct interoperability.

Every rule has a stable `id` and `type`. Shared options are `offset` (−366…366 days, default 0), `duration` (1…366 days, default 1), `fromYear` and `throughYear` (inclusive anchor years, defaults 1800 and 2200).

| Type | Parameters and behavior |
| --- | --- |
| `solar` | Gregorian `month` 1–12 and `day`; February 29 produces no occurrence in a non-leap year |
| `solar_nth_weekday` | Gregorian `month`, ISO weekday in `day`, and `occurrence` 1–5; a missing fifth weekday produces no occurrence |
| `khmer_lunar` | Lunar `month`, `day`, `waxing`; matches dates in the requested Gregorian year |
| `new_year_first` | First New Year day |
| `new_year_middle` | Every middle New Year day; two in a four-day festival |
| `new_year_last` | Final New Year day |

Lunar `monthPolicy` defaults to `exact`. With `month: 7`, `ordinary_or_second_asadh` selects ordinary Asadh in ordinary years and second Asadh in leap-month years. First Asadh is never substituted for second Asadh. Unknown policies are rejected.

Offsets and durations apply to each anchor and may extend into adjacent years. Overlapping dates are deduplicated. The `year` argument selects the **anchor year**, not a filter on output dates. An output outside the engine's supported range is rejected. Applications building a Gregorian-year view should evaluate the relevant neighboring anchor years and filter the returned dates.

### Source-backed changes

`EventDateOverride(ruleId, year, dates, sourceId, reason)` replaces all calculated occurrences for one anchor year. An empty date array cancels that year's occurrences. It can supply explicit dates outside the rule's normal effective-year interval. The ID and anchor year must match the evaluated rule; source and reason are required, and duplicate dates are rejected.

An `EventOccurrence` includes `ruleId`, `date`, `basis` (`calculated` or `source_override`) and nullable `sourceId`. The source ID refers to the manager's source record; the engine does not fetch it or verify the publication. Labels, official leave, substitute days, amendments and one-off historical records belong to versioned event data maintained by the manager. A calculated festival does not automatically become a government public holiday.
