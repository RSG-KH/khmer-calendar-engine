# Khmer Calendar Engine

One Kotlin implementation, compiled into an **Android-compatible JVM library** and a **JavaScript package with TypeScript definitions**. Calendar calculations run offline, without Android resources, browser APIs or a bundled event archive.

## Status

**0.1.0 — initial implementation.** Packages can be built locally; the [release workflow](docs/releasing.md) publishes tested packages to GitHub when a matching version tag is pushed. It does not publish to Maven Central or npm. The existing Android and PWA applications have not yet been migrated.

Implemented:

- Gregorian ↔ Khmer lunar conversion, including leap months and leap days.
- Buddhist Era, animal-year and Sak labels, holy days and shaving days.
- Khmer New Year dates and festival duration, including the corrected **13–15 April 2012** result.
- Fixed-date, weekday, lunar and New Year recurrence rules; ordinary/second-Asadh selection.
- Source-backed event date replacements and cancellations.

The engine calculates dates. The separate manager will maintain event definitions, translations, historical records and government yearly holiday publications, with developer imports and versioned exports. Calculating a traditional festival does not establish official leave.

## Use it

### Android / Kotlin

Add the built Maven repository (`build/repository`), or the extracted `repository/` directory from a published Maven ZIP, to your project's repositories, then depend on:

```kotlin
implementation("com.rsgkh:khmer-calendar-engine-jvm:0.1.0")
```

```kotlin
import com.rsgkh.calendar.engine.KhmerCalendarEngine
import com.rsgkh.calendar.engine.RecurrenceRule

val engine = KhmerCalendarEngine()
val date = engine.fromGregorian(2026, 7, 30)
val newYear = engine.newYear(2012) // April 13–15
val lent = RecurrenceRule(
    id = "lent", type = "khmer_lunar", month = 7, day = 1,
    waxing = false, monthPolicy = "ordinary_or_second_asadh",
)
val occurrences = engine.evaluateRule(2026, lent) // July 30, second Asadh
```

### JavaScript / PWA

Install the locally built tarball with `npm install <path-to-tarball>`, or install a published release's versioned `.tgz` URL as described in [release installation](docs/releasing.md#install-a-published-release):

```typescript
import { KhmerCalendarEngine, createRule } from 'khmer-calendar-engine';

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
```

[API and calendar conventions](docs/api.md) explains month indices, date-level year transitions, rule parameters, effective years and source-backed changes.

## Build and test

Requirements: **JDK 17 recommended** (JDK 11 also tested), Node.js on `PATH`, and internet access for the first dependency download. Gradle 8.14.3 and Kotlin 2.2.10 are pinned. Do not run this Gradle wrapper with JDK 25. Node 26 was used for the recorded verification.

From this repository (`gradlew.bat` on Windows):

```text
bash ./gradlew check jvmJar publishAllPublicationsToLocalBuildRepository
npm pack ./build/npm --pack-destination ./build
```

Outputs:

| Artifact | Location |
| --- | --- |
| JVM library | `build/libs/khmer-calendar-engine-jvm-0.1.0.jar` |
| Maven repository, including dependency metadata | `build/repository` |
| ESM package and TypeScript declarations | `build/npm` |
| Installable npm tarball | `build/khmer-calendar-engine-0.1.0.tgz` |

The JVM artifact targets Java 11 bytecode. Use Maven metadata to obtain its Kotlin standard-library dependency. The JavaScript package includes its compiled runtime; consumers do not need Kotlin or Java. Both artifacts include license notices.

## Verification and accuracy

Recorded **16 September 2026**:

- **14 JVM tests and 12 JavaScript tests pass**, including every supported date's Gregorian/lunar round trip and calendar boundary checks.
- Compiled JVM and packaged JavaScript agree on **146,462 dates**, **401 New Year results** and **2,474 occurrences** across all six recurrence families.
- Lunar hashes match the pinned MomentKH compatibility fixture for **1900–2100**. Its incorrect 2012 New Year date is explicitly rejected by the regression test.
- Reviewed modern New Year dates and published lunar-festival anchors pass focused tests.
- The Maven artifact passes an Android consumer test and APK/DEX assembly. The npm package passes strict TypeScript, Vite production compilation and execution in headless Chrome. See [consumer verification](verification/README.md).

The computational range is **1800–2200**, not a claim that every historical date has independent confirmation. [Calendar evidence and references](docs/references.md) records direct publications, reviewed observations and open questions. **1879/1897 remain historically unresolved; precise arrival times are not exposed.** Broader independent checks of leap boundaries, holy days and year transitions remain necessary before claiming authoritative coverage of the entire range.

The [legacy implementation comparison](docs/source-audit.md) is a development audit, separate from calendar evidence. Consumer migration should preserve explicitly dated events and official holiday records while replacing embedded calculations.

## License

[Apache License 2.0](LICENSE), with incorporated MIT-licensed algorithms and their full attribution in [NOTICE](NOTICE).
