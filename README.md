# Khmer Calendar Engine

One Kotlin implementation, compiled into an **Android-compatible JVM library** and a **JavaScript package with TypeScript definitions**. Calendar calculations run offline, without Android resources, browser APIs or a bundled event archive.

## Status

**0.6.0 — Western astrology ("Big 3" + Angles) engine.** Packages can be built locally; the [release workflow](docs/releasing.md) publishes tested packages to GitHub when a matching version tag is pushed. It does not publish to Maven Central or npm. Release history: [CHANGELOG.md](CHANGELOG.md).

Implemented:

- Gregorian ↔ Khmer lunar conversion, including leap months and leap days.
- Buddhist Era, animal-year and Sak labels, holy days and shaving days.
- Khmer New Year dates and festival duration, including the corrected **13–15 April 2012** result.
- The traditional arithmetic's **arrival-time estimate** (`arrivalEstimate`) — estimate-typed, minute precision, documented as never authoritative; published clocks are per-year data maintained by the manager.
- Fixed-date, weekday, lunar and New Year recurrence rules; ordinary/second-Asadh selection.
- Source-backed event date replacements and cancellations.
- Chinese daily and hourly zodiac (*Ganzhi*) via the standalone `ChineseZodiacCalculator`: day pillars, hour branches and Five-Rats hour pillars over the proleptic Gregorian range 1..9999, in local civil time.
- The astrological solar calendar on the same calculator: Lichun-anchored year pillars, sectional-term month pillars, the full Four Pillars (*BaZi*) and clash branches, over a verified 1900–2100 UTC+8 solar term table.
- Western astrology (*Big 3* + Angles) via `WesternZodiacCalculator`: continuous-time celestial coordinates for Sun, Moon, Ascendant, and Midheaven (MC) across 1800–2200 using Jean Meeus algorithms and Espenak & Meeus piecewise Delta-T with singularity detection.

The engine calculates dates and one clearly-labeled estimate. The separate manager maintains event definitions, translations, historical records, government yearly holiday publications and the source-tagged arrival-time dataset, with developer imports and versioned exports. Calculating a traditional festival does not establish official leave.

## Use it

### Android / Kotlin

Add the built Maven repository (`build/repository`), or the extracted `repository/` directory from a published Maven ZIP, to your project's repositories, then depend on:

```kotlin
implementation("com.rsgkh:khmer-calendar-engine-jvm:0.6.0")
```

```kotlin
import com.rsgkh.calendar.engine.KhmerCalendarEngine
import com.rsgkh.calendar.engine.RecurrenceRule
import com.rsgkh.calendar.engine.western.WesternZodiacCalculator

val engine = KhmerCalendarEngine()
val date = engine.fromGregorian(2026, 7, 30)
val newYear = engine.newYear(2012) // April 13–15
val lent = RecurrenceRule(
    id = "lent", type = "khmer_lunar", month = 7, day = 1,
    waxing = false, monthPolicy = "ordinary_or_second_asadh",
)
val occurrences = engine.evaluateRule(2026, lent) // July 30, second Asadh

// Western Horoscope ("Big 3" + Angles)
val chart = WesternZodiacCalculator.calculateHoroscope(
    year = 2026, month = 4, day = 14,
    hour = 10, minute = 30, second = 0.0,
    utcOffsetHours = 7.0,
    latitudeDeg = 11.5564, longitudeDeg = 104.9282
)
println("Sun: ${chart.sun.formatted}, Moon: ${chart.moon.formatted}")
println("Ascendant: ${chart.ascendant?.formatted}, MC: ${chart.midheaven.formatted}")
```

### JavaScript / PWA

Install the locally built tarball with `npm install <path-to-tarball>`, or install a published release's versioned `.tgz` URL as described in [release installation](docs/releasing.md#install-a-published-release):

```typescript
import {
  KhmerCalendarEngine,
  createRule,
  calculateHoroscope
} from 'khmer-calendar-engine';

const engine = new KhmerCalendarEngine();
const date = engine.fromGregorian(2026, 7, 30);
const original = engine.toGregorian(
  date.lunar.buddhistYear, date.lunar.month, date.lunar.day, date.lunar.waxing,
);
const lent = createRule({
  id: 'lent', type: 'khmer_lunar', month: 7, day: 1,
  waxing: false, monthPolicy: 'ordinary_or_second_asadh',
});
const occurrences = engine.evaluateRule(2026, lent);

// Western Horoscope options-object overload
const chart = calculateHoroscope({
  year: 2026, month: 4, day: 14,
  hour: 10, minute: 30, second: 0,
  utcOffsetHours: 7.0,
  latitude: 11.5564, longitude: 104.9282
});
console.log(chart.sun.formatted, chart.moon.formatted, chart.ascendant?.formatted);
```

[API and calendar conventions](docs/api.md) explains month indices, date-level year transitions, rule parameters, effective years, and Western horoscope options.

## Build and test

Requirements: **JDK 25**, Node.js on `PATH`, and internet access for the first dependency download. Gradle 9.6.0 and Kotlin 2.4.20 are pinned. The Gradle daemon and JVM toolchain select an installed JDK 25; point `JAVA_HOME` to that installation if needed. GitHub Actions provisions Zulu JDK 25. Node 26 was used for the recorded verification.

From this repository (`gradlew.bat` on Windows):

```text
bash ./gradlew check jvmJar publishAllPublicationsToLocalBuildRepository
npm pack ./build/npm --pack-destination ./build
```

Outputs:

| Artifact | Location |
| --- | --- |
| JVM library | `build/libs/khmer-calendar-engine-jvm-0.6.0.jar` |
| Maven repository, including dependency metadata | `build/repository` |
| ESM package and TypeScript declarations | `build/npm` |
| Installable npm tarball | `build/khmer-calendar-engine-0.6.0.tgz` |

JDK 25 runs the build; the JVM artifact still targets Java 11 bytecode. Use Maven metadata to obtain its Kotlin standard-library dependency. The JavaScript package includes its compiled runtime; consumers do not need Kotlin or Java. Both artifacts include license notices.

The Kotlin compiler/plugin is 2.4.20, while the library's language/API level and metadata remain Kotlin 2.2 and its standard library stays at 2.2.10. This lets existing Kotlin 2.2 Android consumers use the engine without upgrading their compiler. The Android fixture verifies both Java and Kotlin callers.

## Verification and accuracy

Recorded **25 September 2026**:

- **47 JVM and JS tests pass**, including every supported date's Gregorian/lunar round trip, calendar boundary checks, Ganzhi day/hour golden anchors, and Western astrology benchmarks B1–B8 on both targets.
- **281/281 retained JavaScript input and model checks pass** across strict primitive numeric validation, boundary argument rejections, model contracts, object freezing, and an Ascendant-only UTC+1.1 comparison. A separate **38/38 package regression runner** checks all four positions, signs, polar flags, and status through raw and wrapper calls, including near-midnight carry, coordinate aliases, missing coordinates, and omitted versus explicit-null seconds. Both runners are part of the root `check` task.
- The 12 sectional solar terms (1900–2100, UTC+8) were regenerated from an astronomical ephemeris and audited against the published almanac record (Hong Kong Observatory year tables) and the Chinese engine's own Qingming data — 201/201 Qingming days agree; 34 near-midnight term days are pinned to the published record.
- Compiled JVM and packaged JavaScript agree on **146,462 dates**, **401 New Year results with arrival estimates**, **2,474 occurrences** across all six recurrence families, and a **4,032-chart Western sample** covering 1800–2200. The Western sample compares all four longitudes, signs, polar flags and Ascendant statuses; it is a cross-target consistency check, not an independent accuracy bound. Every New Year estimate sits on the 24-minute lattice.
- Lunar hashes match the pinned MomentKH compatibility fixture for **1900–2100**. Its incorrect 2012 New Year date is explicitly rejected by the regression test.
- Reviewed modern New Year dates and published lunar-festival anchors pass focused tests.
- The Maven artifact passes an Android consumer test and APK/DEX assembly. The npm package passes strict TypeScript, Vite production compilation and execution in headless Chrome. See [consumer verification](verification/README.md).

The computational range is **1800–2200**, not a claim that every historical date has independent confirmation. [Calendar evidence and references](docs/references.md) records direct publications, reviewed observations and open questions. **1879/1897 remain historically unresolved; precise arrival times are not computed — the estimate lies on a 24-minute lattice and published clocks are carried as per-year sourced data.** Broader independent checks of leap boundaries, holy days and year transitions remain necessary before claiming authoritative coverage of the entire range.

The [legacy implementation comparison](docs/source-audit.md) is a development audit, separate from calendar evidence. Consumer migration should preserve explicitly dated events and official holiday records while replacing embedded calculations.

## License

[Apache License 2.0](LICENSE), with incorporated MIT-licensed algorithms and their full attribution in [NOTICE](NOTICE).
