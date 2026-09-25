# Changelog

All notable changes to the Khmer Calendar Engine. Versions are published as GitHub releases (see [docs/releasing.md](docs/releasing.md)); each heading links to its release.

## [0.5.1] — 2026-09-25

Maintenance release: Four Pillars day-hour synchronization, festival registry isolation, and JavaScript runtime validation hardening.

### Fixed

- Four Pillars day-hour alignment at 23:00 (`V05-001`): `ChineseZodiacCalculator.getFourPillars` now rolls both the day pillar and hour pillar forward at 23:00 (late Rat / *Zi* hour) to match the engine's documented convention, ensuring both pillars share the same effective day while retaining the civil date for year and month pillars.
- Festival ID validation isolation (`V05-002`): `ChineseLunisolarEngine.FESTIVAL_IDS` now returns a defensive, frozen array copy to prevent external mutation from corrupting shared recurrence validation state. Added internal `isKnownFestivalId` helper.
- JavaScript dynamic boundary input validation (`V05-003`): `requireInteger` now verifies JS primitive numeric type, finite integrality, and signed 32-bit `Int` range, rejecting `null`, non-numeric strings, booleans, arrays, and out-of-range integers ($2^{32}$) across exported Kotlin APIs and wrappers.
- FestivalProfile metadata immutability (`V05-007`): Froze `FestivalProfile` enum instances in JavaScript to prevent external tampering of profile identifiers.

### Added

- Regression test suite `EngineV050AuditTest` covering late-Zi day/hour transitions across 1900–2100 boundaries, festival ID isolation, and explicit profile parity.

## [0.5.0] — 2026-09-23

Astrological solar calendar, Four Pillars (BaZi) and clash branches.

### Added

- Astrological solar calendar on `ChineseZodiacCalculator`: Lichun-anchored **year pillars** (`getYearPillar`), **month pillars** driven by the 12 sectional solar terms and the Five Tigers rule (`getMonthPillar`), `getSectionalTermDay`, and the complete **Four Pillars** with all four clash branches (`getFourPillars`, `FourPillars`).
- Clash branches (*Liu Chong* / ឆុង): `clashBranch`, `clashAnimal` and `clashKhmerAnimal` on every `EarthlyBranch` and `GanzhiPillar`, plus `yearClash` through `hourClash` on `FourPillars`.
- Packed 1900–2100 sectional solar term table (UTC+8, China Standard reference), regenerated from an astronomical ephemeris and audited against the published almanac record — the Hong Kong Observatory year tables and the Chinese lunisolar engine's own data. Qingming days equal the engine's `cn-reference-utc8` table in **201/201 years**; 34 near-midnight term days are individually pinned to the published day.
- `tools/generate_solar_terms.py`: ephemeris-based regeneration (`generate`, requires pyephem) and a dependency-free `check` mode that validates the committed table — Qingming equality, day windows and published anchors — and fails loudly on any regression.
- JavaScript: named wrappers `getYearPillar`, `getMonthPillar`, `getFourPillars` and `getSectionalTermDay` with TypeScript overload declarations, following the established multi-arity dispatch pattern.

### Changed

- Documented boundary conventions: year and month pillars follow UTC+8 (standard *Tong Shu*/BaZi reference) and change at 00:00 of the term's civil date; their range is 1900..2100, while day and hour pillars remain valid for 1..9999. Within `getFourPillars`, only the day and hour pillars roll at 23:00.
- Verification record updated: 33 JVM and 31 JavaScript tests; JS boundary checks extended to cover the solar calendar (`ganzhiSolarCalendar: verified`).

## [0.4.0] — 2026-09-22

Chinese daily and hourly zodiac (Ganzhi).

### Added

- `HeavenlyStem`, `EarthlyBranch` and `GanzhiPillar` models with Chinese names, pinyin, elements, and English plus Khmer zodiac animals.
- Standalone `ChineseZodiacCalculator`: integer Julian Day Number conversion (`gregorianToJdn`), day pillars via the unbroken `(JDN + 49) mod 60` counter (`getDayPillar`), two-hour window branches (`getHourBranch`), and Five-Rats hour pillars with the automatic 23:00 late-Rat day-stem rollover (`getHourPillar`, `getHourPillarForDate`). Day and hour pillars cover the proleptic Gregorian range 1..9999 in local civil time.
- Golden-anchor tests on both JVM and JS targets (1949–2026 day pillars, full hour mapping, Five-Rats rollover) and Ganzhi assertions in the JavaScript verification script.

### Changed

- All calculator operations are exposed to JavaScript as typed named exports (`gregorianToJdn`, `getHourBranch`, `getDayPillar`, `getHourPillar`); Kotlin/JS exports singletons as classes behind `getInstance()`, so wrappers provide the clean, typed surface.

## [0.3.0] — 2026-09-19

New Year arrival estimate.

### Added

- Traditional Moha Sangkran arrival estimate on `NewYearCelebration`: `arrivalEstimate` with `minuteOfDay`, `hour` and `minute`, derived from the traditional arithmetic on its 24-minute lattice. Estimate-typed by design and documented as never authoritative — published arrival clocks remain per-year source-tagged data maintained by the manager.

### Changed

- Festival dates and duration are unchanged; the estimate is an additional, clearly-labeled field.
- Calendar evidence expanded in `docs/references.md`: research sources S02–S25, the TVK national-television broadcast archive, and user-transcribed 2015 almanac evidence corroborating the time overrides.

## [0.2.0] — 2026-09-18

Chinese lunisolar engine and festival rules.

### Added

- `ChineseLunisolarEngine`: Gregorian ↔ Chinese lunar conversion for 1900–2100 (including leap months and the 1899/12 boundary fragment) and nine festival dates — Chinese New Year days and eve, Kitchen God, Spirit Parade, Zongzi, Ghost, Mid-Autumn, Qingming and Winter Solstice.
- Festival computation profiles with distinct packed tables: `archive-v1` (legacy, default for backwards compatibility), `cn-reference-utc8`, `cn-lunar-utc7-solar` and `local-utc7-model`; none claims to certify Cambodian community practice.
- `chinese_festival` recurrence rule type with `cn-reference-utc8`/`archive-v1` month policies, integrated into `evaluateRule` alongside the existing rule families.
- JavaScript helper `getChineseFestivalDates(year, festivalId, profile?)` accepting a profile enum or string identifier.

## [0.1.0] — 2026-09-16

Initial release: one Kotlin Multiplatform implementation, compiled into an Android-compatible JVM library (Java 11 bytecode) and an ES-module JavaScript package with TypeScript declarations.

### Added

- Gregorian ↔ Khmer lunar conversion, including leap months and leap days, over the supported range 1800–2200.
- Buddhist Era, animal-year and Sak labels, holy days and shaving days, with date-level year-transition flags.
- Khmer New Year dates and festival duration, including the corrected 13–15 April 2012 result that rejects the pinned legacy implementation's date.
- Recurrence rules: fixed-date, nth-weekday, Khmer lunar (with `ordinary_or_second_asadh` month policy) and New Year day roles, with offsets, durations, effective years and source-backed event date overrides and cancellations (`EventDateOverride`, `EventOccurrence`).
- JavaScript object-argument rule construction (`createRule`).
- GitHub release workflow: builds, tests and publishes JVM, JavaScript and Maven ZIP assets from a matching version tag; cross-target parity verified across all 146,462 supported dates and 401 New Year results.

[0.5.1]: https://github.com/RSG-KH/khmer-calendar-engine/releases/tag/v0.5.1
[0.5.0]: https://github.com/RSG-KH/khmer-calendar-engine/releases/tag/v0.5.0
[0.4.0]: https://github.com/RSG-KH/khmer-calendar-engine/releases/tag/v0.4.0
[0.3.0]: https://github.com/RSG-KH/khmer-calendar-engine/releases/tag/v0.3.0
[0.2.0]: https://github.com/RSG-KH/khmer-calendar-engine/releases/tag/v0.2.0
[0.1.0]: https://github.com/RSG-KH/khmer-calendar-engine/releases/tag/v0.1.0
