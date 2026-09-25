# API and calendar conventions

Version **0.6.0** adds the Western astrology ("Big 3" + Angles: Sun, Moon, Ascendant, Midheaven) engine based on Jean Meeus algorithms and Espenak & Meeus piecewise Delta-T (1800–2200); 0.5.1 hardened JavaScript validation, isolated the festival registry, and synchronized Four Pillars day-hour alignment at 23:00; 0.5.0 added the astrological solar calendar — Lichun-anchored year pillars, sectional-term month pillars, the Four Pillars (BaZi) and clash branches; 0.4.0 added Chinese sexagenary cycle (Ganzhi) day and hour zodiac calculations; 0.3.0 added the New Year arrival estimate; 0.1.0 established the API and 0.2.0 added recurrence rules and the Chinese lunisolar engine. Kotlin and Java use package `com.rsgkh.calendar.engine` (and `com.rsgkh.calendar.engine.western`); JavaScript uses named exports from `khmer-calendar-engine`. Results contain facts and indices; applications supply translations and display formatting.

## Dates

Create one `KhmerCalendarEngine` and reuse it. Its immutable calculation tables are initialized lazily and shared between instances.

| Operation | Result |
| --- | --- |
| `fromGregorian(year, month, day)` | `CalendarDate`: Gregorian date, lunar date, animal year, Sak and date-level transition flags |
| `toGregorian(buddhistYear, month, day, waxing)` | The corresponding `GregorianDate`; rejects nonexistent lunar dates |
| `newYear(year)` | First date, number of days, last date, all festival dates, and the arrival `ArrivalEstimate` below |
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

These are calendar-date labels, not claims about the precise transition instant. Historical validation limits and direct evidence are in [references.md](references.md).

### New Year arrival estimate

`NewYearCelebration.arrivalEstimate` is an `ArrivalEstimate`: the traditional arithmetic's `minuteOfDay` (with `hour`/`minute` views) on the festival's first date. The type name is deliberate — this is an **estimate, never an authority**:

- The traditional conversion multiplies an integer solar arc-minute remainder by 24, so `minuteOfDay` is always a multiple of 24. Published clocks such as 19:11, 08:07 or 14:01 are structurally unreachable by this arithmetic.
- Reviewed publications and national broadcaster (TVK) archives cover 19 evidenced years (1997, 2009, 2010–2026 unbroken): 12 years match the estimate to the exact minute (0m error: 1997, 2010, 2016–2023, 2025, 2026), while off-lattice years differ by 1–24 minutes. Agreement in some years is not evidence of correctness elsewhere.

There is **no certified arrival-time API**. Published arrival clocks are an annual editorial act by the Ministry of Cults and Religion's almanac; they are maintained per year as source-tagged data by the manager, with evidence grades and dispute states. Applications should present the estimate as a prediction and the graded record — where one exists — as the official time. See references.md for the full findings.

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

## Chinese daily, hourly and astrological zodiac (Ganzhi)

The standalone `ChineseZodiacCalculator` provides pure integer sexagenary calculations (*Ganzhi*, 干支): day and hour pillars across the proleptic Gregorian calendar (1..9999), and the astrological solar calendar — year pillars, month pillars, the Four Pillars (*BaZi*) and clash branches — bounded by the sectional solar term table (1900..2100).

| Operation | Result |
| --- | --- |
| `gregorianToJdn(year, month, day)` | Integer astronomical Julian Day Number (JDN) |
| `getDayPillar(year, month, day)` | `GanzhiPillar`: Stem, Branch, Chinese name, Pinyin, English and Khmer animal |
| `getHourBranch(hourOfDay)` | `EarthlyBranch`: 0..23 mapped to 12 two-hour windows |
| `getHourPillar(dayStem, hourOfDay)` | `GanzhiPillar`: Hour stem and branch using the Five Rats rule |
| `getHourPillarForDate(year, month, day, hour)` | `GanzhiPillar`: Hour pillar with automated 23:00 day-stem rollover |
| `getSectionalTermDay(year, month)` | Civil day (UTC+8) on which a sectional solar term (*Jie*) begins, 1900..2100 |
| `getYearPillar(year, month, day)` | `GanzhiPillar`: astrological year; changes at Lichun (early February) |
| `getMonthPillar(year, month, day)` | `GanzhiPillar`: astrological month from the 12 sectional terms and the Five Tigers rule |
| `getFourPillars(year, month, day, hour)` | `FourPillars`: year, month, day and hour pillars plus the four clash branches (`yearClash` … `hourClash`, opposite branch at 180°) |

Every `EarthlyBranch` and `GanzhiPillar` also exposes `clashBranch`, `clashAnimal` and `clashKhmerAnimal` (the *Liu Chong* / ឆុង opposite).

### Conventions & Time Boundary

1. **Unbroken Sexagenary Count:** Day pillars follow the continuous modulo-60 counter `(JDN + 49) mod 60`, verified without interruption across historical records.
2. **Local Civil Time:** Input hours (0–23) represent local civil clock time (UTC+7 in Cambodia). Geographical longitude and Equation of Time adjustments are intentionally left out of scope.
3. **The 23:00 Zi Hour Rollover:** In traditional Chinese timekeeping, the early Rat (*Zi*, 子) hour begins at 23:00. `getHourPillarForDate` automatically advances the effective day stem by +1 day when `hourOfDay == 23`. Callers using the primitive `getHourPillar(dayStem, hourOfDay)` are expected to pass tomorrow's stem if evaluating at 23:00.
4. **Astrological year and month boundaries (UTC+8):** Year pillars change at *Lichun* and month pillars at the 12 sectional terms (*Jie*), tabulated for 1900..2100 at the China Standard reference meridian (UTC+8) — the standard *Tong Shu* / BaZi convention, kept identical for Qingming to the Chinese lunisolar engine's `cn-reference-utc8` data. Term days follow the published almanac record (Hong Kong Observatory tables); for terms falling within a few minutes of UTC+8 midnight the published day is stored, which for 15 terms differs from a raw modern recomputation. `tools/generate_solar_terms.py` regenerates and validates the table, including that Qingming equality.
5. **Day-boundary transitions:** A solar-term transition takes effect at 00:00 of the term's civil date — the civil calendar day model, not minute-level natal-chart casting. Within `getFourPillars`, only the day and hour pillars roll at 23:00; the year and month pillars keep the calendar date.

## Western astrology ("Big 3" + Angles)

The standalone `WesternZodiacCalculator` calculates continuous-time celestial coordinates for Western natal astrology: the **"Big 3"** (Sun sign, Moon sign, Ascendant) plus Midheaven (MC), expressed in tropical ecliptic coordinates across the supported interval **1800–2200**.

| Operation | Input parameters | Result |
| --- | --- | --- |
| `calculateHoroscopeUtc(...)` | `yearUtc, monthUtc, dayUtc, hourUtc, minuteUtc, secondUtc = 0, latitudeDeg, longitudeDeg` | `WesternHoroscope`: Sun, Moon, Ascendant, Midheaven, polar flag, and `AscendantStatus` |
| `calculateHoroscope(...)` | `year, month, day, hour, minute, second = 0, utcOffsetHours, latitudeDeg, longitudeDeg` | `WesternHoroscope`: Normalized UTC horoscope with floating-point carry reconciliation |

In JavaScript/TypeScript, `calculateHoroscope` and `calculateHoroscopeUtc` are exported both as positional functions and as ergonomic single options-object functions:
```typescript
calculateHoroscope({
  year: 2026, month: 4, day: 14,
  hour: 10, minute: 30, second: 0,
  utcOffsetHours: 7.0,
  latitude: 11.5564, longitude: 104.9282
})
```

### Models and Data Structures

- `WesternZodiacSign`: Enum of the 12 signs (`ARIES` .. `PISCES`) with `index` (0..11), `symbol` (♈..♓), `englishName`, `khmerName` (មេស..មីន), `element` (Fire, Earth, Air, Water), and `modality` (Cardinal, Fixed, Mutable).
- `ZodiacPosition`: Represents celestial position with `sign`, `degreeInSign` ($[0, 30)$), `wholeDegree` ($0..29$), `minute` ($0..59$), `second` ($[0, 60)$), `totalLongitude` ($[0, 360)$), and `formatted` string (e.g. `Aries 24° 12' 13"`).
- `WesternHoroscope`: Contains `sun`, `moon`, nullable `ascendant`, `midheaven`, `isPolarLatitude` (`Boolean`), and `ascendantStatus` (`AscendantStatus`).
- `AscendantStatus`: Enum describing the state of the horizon intersection:
  - `CALCULATED`: Normal unique rising horizon intersection ($|\phi| < 90^\circ - \epsilon$).
  - `POLAR_NON_RISING`: Polar latitude ($|\phi| \ge 90^\circ - \epsilon$) where ecliptic does not set; Ascendant represents geometric intersection.
  - `COINCIDENT_PLANES`: Ecliptic and horizon planes coincide ($r^2 < 10^{-10}$ at $\text{RAMC}=270^\circ, \phi=+(90^\circ-\epsilon)$ or $\text{RAMC}=90^\circ, \phi=-(90^\circ-\epsilon)$); `ascendant` is `null`.
  - `DEGENERATE_POLE`: Exact geographic pole ($|\phi| \ge 89.99^\circ$); diurnal rotation is parallel to horizon; `ascendant` is `null`.

### Computational Algorithms & Accuracy

1. **Algorithms:** Implementation is pure Kotlin with zero runtime dependencies, based on Jean Meeus (*Astronomical Algorithms*, 2nd ed.):
   - True obliquity and nutation in longitude (Ch. 22).
   - Greenwich Apparent Sidereal Time (GAST) and local RAMC (Ch. 12 & 13).
   - Low-precision solar coordinates with nutation and aberration (Ch. 25).
   - 60-term truncated lunar periodic series from Table 47.A with nutation (Ch. 47).
   - Scaled vector Ascendant formulation with singularity detection.
2. **Delta-T ($\Delta T$):** Full 10-interval piecewise polynomial model from Fred Espenak & Jean Meeus (2004/2006, *Five Millennium Canon of Solar Eclipses*) evaluated on fractional decimal years over 1800–2200.
3. **Accuracy Benchmarks (vs. Swiss Ephemeris analytical Moshier backend):** Comparing all four positions in the six dated B1, B2, B3, B6, B7, and B8 charts against the retained reference output, the largest circular residuals are Sun $31.95''$, Moon $3.26''$, Ascendant $5.48''$, and Midheaven $5.17''$. B4 and B5 are geometric Ascendant checks. These are sampled comparisons, not maximum error guarantees throughout 1800–2200. Formula comparisons require matched TT for the Sun and Moon and matched UT1 and frame conventions for the angles. Civil-input results also depend on the engine's time-scale approximation and the supplied time and location; near a sign boundary, different numerical or time models can produce different signs.
4. **Host Responsibilities:** The engine is purely astronomical; host applications are strictly responsible for resolving timezones, historical Daylight Saving Time (DST), and geographical coordinates before passing civil time and decimal UTC offset into the engine.
