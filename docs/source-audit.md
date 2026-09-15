# Legacy implementation comparison

Recorded **15 September 2026**. This document is a development record for finding regressions and migration bugs. The compared applications are not authoritative sources for the engine's calendar rules. External calendar evidence is maintained in [references.md](references.md).

## Compared revisions

| Implementation | Git revision |
| --- | --- |
| Android | `33ced69e1c5dd00f6deb990ca3e18846b38212b4` |
| PWA | `3cd6b6d66d982ac506a451fcdb54218aa769d263` |

The comparison calls compiled Kotlin classes and the actual TypeScript modules. It contains no duplicate calendar algorithm. Event comparisons exercise recurrence directly, including years where the applications normally select stored records.

## Results

| Check | Result |
| --- | --- |
| Daily calendar fields, 1800–2200 | All **146,462 dates** agree |
| New Year start dates and festival lengths, 1800–2200 | All **401 years** agree |
| Calculated recurring occurrences, 1800–2200 | Android: **27,040**; PWA: **26,596**; **444 missing from PWA** |
| Duplicate comparison rows | None |
| PWA against checked-in MomentKH yearly hashes and New Year starts | All **201 reference years, 1900–2100**, agree |
| Existing Android calendar, date-detail, zodiac, reference, and event tests | **30 passed** |
| Existing PWA calendar tests | **8 passed** |

Daily fields include lunar day, waxing/waning phase, lunar month, Buddhist Era, month length, holy day, shaving day, animal year, Sak, the animal-year transition flag, and western zodiac classification. Event-title translations and device notification behavior are outside this comparison.

### Confirmed PWA recurrence defect

Three rule records contain `monthPolicy: "ordinary_or_second_asadh"`, while the PWA evaluator checks a `secondAsadh` Boolean. Android reads `monthPolicy` correctly.

This causes three events to be omitted in each of **148 leap-month years**:

- Beginning of Buddhist Lent.
- Buddhist Lent Candles Making Day.
- The Ordained Dragon Monk.

For example, the Android recurrence evaluator produces these 2031 dates:

| Event | Calculated date |
| --- | --- |
| Buddhist Lent Candles Making Day | 2031-07-27 |
| The Ordained Dragon Monk | 2031-08-02 |
| Beginning of Buddhist Lent | 2031-08-04 |

The PWA recurrence evaluator produces none of the three. Of the **444** omitted calculated occurrences, **411** are outside 2000–2030 and therefore affect the normal PWA event results. Stored records conceal the defect within covered years, including 2026.

The existing PWA test suite passes because it does not exercise these recurrence cases. The shared engine must preserve the second-Asadh behavior, validate rule fields, and test every rule across the supported range. This audit records the PWA defect; it does not modify either application's implementation.

## Other findings retained for development

- The pinned MomentKH fixture covers **1900–2100**. The broader **1800–2200** comparison demonstrates agreement between implementations, not independent historical validation.
- A probe of the compiled Kotlin calculation before its override gives **13 April 2012, three festival days**. The inherited override changes the first date to **14 April**. The other modern date overrides (2011, 2013, 2014, 2015 and 2024) do not change the formula's date. This is a defect to correct, not behavior the shared engine should preserve.
- Stored event records supply the correct **13–15 April 2012** festival dates, concealing the date override in event lists. Stored arrival-time titles still disagree with reviewed evidence: 2012 says **19:12** rather than **19:11**; 2013 says **02:24** rather than **02:12**.
- Existing New Year APIs return dates and duration. Animal-year and Sak labels use calendar-date transitions, not calculated arrival instants.
- Kotlin currently depends on `java.time`, JVM resource loading and application translations; TypeScript uses UTC epoch days and `BigInt`. The shared engine needs a portable date representation, explicit division/modulo rules and structured facts independent of application labels.
- There are **100** recurrence rules. Some effective start years represent the first captured occurrence rather than a historically established beginning. Such event definitions require their own evidence.
- Snapshot years currently take precedence as a whole. A future consumer migration must preserve explicitly dated events without validated rules, including Chinese festivals, and keep government designations separate from calculated traditions.
- The original 31-year JSON/SQLite capture archive is not included in the audited Git revision. Captured labels and software fixtures must not be promoted to independent calendar evidence.

## Reproduce the comparison

Prerequisites: Python 3.10+, a JDK compatible with the existing Android project, its Android SDK/Gradle setup, and Node.js with the PWA's dependencies installed. The audit is a transitional developer tool coupled to the current Android build-output layout.

From the engine repository, supply the locations of your Android and PWA repositories (replace the placeholders):

```powershell
python tools/audit_existing.py --android "<android-repository>" --pwa "<pwa-repository>"
```

The tool compiles Android's debug Kotlin classes, calls them through a Java source launcher, and compares the results with PWA modules. `--skip-build` reuses compiled classes and should only be used immediately after a successful build of the unchanged sources.

Outputs are ignored development files:

- `build/audit/android-baseline.tsv`: canonical Android results.
- `build/audit/comparison.json`: counts, every difference, source revisions, and working-tree status.

Exit code **0** means the comparison agrees. Exit code **1** currently reports the known PWA recurrence differences; inspect the report and command output to distinguish differences from an execution error.

The Android baseline SHA-256 for the revisions above is:

```text
161b2c5cb82b4e025a7cfdb965cfbd264f06941fb5268af308cad4b2db5888d3
```
