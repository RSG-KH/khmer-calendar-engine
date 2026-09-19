# Calendar evidence and references

Updated **19 September 2026**. This is the engine's self-contained record of external calendar evidence: what each source supports, which results are corroborated, and what remains unresolved. The calculations and their tests are maintained in this repository.

## Evidence standard

- Cite the actual calendar, almanac, publication or original algorithm.
- Record the year, date or time supported, the review method, and any conflict.
- Preserve manual calendar transcriptions as manual observations.
- Treat agreement with another implementation as a compatibility check. It does not independently prove a calendar result.
- Keep traditional festival dates, arrival times, ceremony schedules, government holidays and substitute leave as separate fields.

The sources below were reviewed on 15 September 2026 unless an earlier review date is stated. Scanned TempleNews dates were checked by the user; the assistant located the publication links but did not independently inspect those scans. Reports may share an underlying almanac, so multiple reports do not necessarily represent independent derivations.

## Reviewed New Year cases

The following table brings the upstream calculation, override and external evidence together. The shared engine's date-only tests cover the reviewed modern dates. Since 0.3.0 the traditional arithmetic's time path is exposed as `arrivalEstimate` — the "formula" column below, an estimate on a 24-minute lattice that is never an authority; published arrival clocks are maintained per year by the manager as source-tagged data. The 1879/1897 upstream exceptions are rejected without historical support.

| Year | MomentKH formula without its table | MomentKH with its table | Result supported by the reviewed sources |
| --- | --- | --- | --- |
| 1879 | 12 April, 10:00 | 12 April, 11:36 | Unverified: no historical calendar or almanac found |
| 1897 | 12 April, 02:00 | 13 April, 02:00 | Unverified: no historical calendar or almanac found |
| 2010 | 14 April, 07:36 | — | **14 April, 07:36** — [TVK broadcast archive](#tvk-national-television-broadcast-archive-19-september-2026) |
| 2011 | 14 April, 13:36 | 14 April, 13:12 | **14 April, 13:12** — [N01](#n01--the-cambodia-daily-15-april-2011), [TVK](#tvk-national-television-broadcast-archive-19-september-2026) |
| 2012 | 13 April, 19:12 | 14 April, 19:11 | **13 April, 19:11** — [T06](#t06--bodhikaram-new-year-announcement-2-april-2012), [N02](#n02--the-cambodia-daily-11-april-2012), [TVK](#tvk-national-television-broadcast-archive-19-september-2026); **13–15 April** holiday dates also supported by [G01](#g01--eccc-information-circular-e1651-official-holidays-for-2012) |
| 2013 | 14 April, 02:24 | 14 April, 02:12 | **14 April, 02:12** — [N03](#n03--the-cambodia-daily-13-april-2013), [S06](#moha-sangkran-arrival-time-research-18-september-2026) inspected calendar, [TVK](#tvk-national-television-broadcast-archive-19-september-2026) |
| 2014 | 14 April, 08:24 | 14 April, 08:07 | **14 April, 08:07** — [N04](#n04--the-cambodia-daily-15-april-2014), [TVK](#tvk-national-television-broadcast-archive-19-september-2026) |
| 2015 | 14 April, 14:24 | 14 April, 14:02 | **14 April, 14:01** — [TVK broadcast archive](#tvk-national-television-broadcast-archive-19-september-2026) primary Khmer announcement; [T03](#t03--templenews-2015-calendar-observation) and [N07](#n07--visit-angkor-2015) previously reported 14:02 |
| 2016 | 13 April, 20:00 | — | **13 April, 20:00** — [S08](#moha-sangkran-arrival-time-research-18-september-2026) inspected calendar, [TVK](#tvk-national-television-broadcast-archive-19-september-2026) |
| 2017 | 14 April, 03:12 | — | **14 April, 03:12** — [TVK broadcast archive](#tvk-national-television-broadcast-archive-19-september-2026) |
| 2018 | 14 April, 09:12 | — | **14 April, 09:12** — [S09](#moha-sangkran-arrival-time-research-18-september-2026) inspected calendar, [S10](#moha-sangkran-arrival-time-research-18-september-2026) contemporaneous report, [TVK](#tvk-national-television-broadcast-archive-19-september-2026) |
| 2019 | 14 April, 15:12 | — | **14 April, 15:12** — [TVK broadcast archive](#tvk-national-television-broadcast-archive-19-september-2026) |
| 2020 | 13 April, 20:48 | — | **13 April, 20:48** — [TVK broadcast archive](#tvk-national-television-broadcast-archive-19-september-2026), resolving the printed diaspora calendar typo ([S13–S14](#moha-sangkran-arrival-time-research-18-september-2026)); the engine's 13 April start is confirmed |
| 2021 | 14 April, 04:00 | — | **14 April, 04:00** — [TVK broadcast archive](#tvk-national-television-broadcast-archive-19-september-2026) |
| 2022 | 14 April, 10:00 | — | **14 April, 10:00** — [S15](#moha-sangkran-arrival-time-research-18-september-2026) inspected proclamation, [S17](#moha-sangkran-arrival-time-research-18-september-2026) contemporaneous report, [TVK](#tvk-national-television-broadcast-archive-19-september-2026) |
| 2023 | 14 April, 16:00 | — | **14 April, 16:00** — [TVK broadcast archive](#tvk-national-television-broadcast-archive-19-september-2026) |
| 2024 | 13 April, 22:24 | 13 April, 22:17 | **13 April, 22:17:24** — [TVK broadcast archive](#tvk-national-television-broadcast-archive-19-september-2026) second precision, resolving the 22:24 traditional lattice ([T04](#t04--templenews-2024-calendar-observation)) vs 22:17 ceremony dispute |
| 2025 | 14 April, 04:48 | — | **14 April, 04:48** — [S23](#moha-sangkran-arrival-time-research-18-september-2026), AKP government news (grade A), [TVK](#tvk-national-television-broadcast-archive-19-september-2026) |
| 2026 | 14 April, 10:48 | — | **14 April, 10:48** — [S24–S25](#moha-sangkran-arrival-time-research-18-september-2026), AKP government news (grade A), [TVK](#tvk-national-television-broadcast-archive-19-september-2026) |

For **2012**, the formula's date is supported and the table's date override is wrong. The formula's arrival minute still differs from the reviewed 19:11. For **2011, 2013, 2014 and 2015**, the table's time corrections have supporting evidence (with 2015 established at 14:01 by TVK Khmer broadcast). For **1997, 2010, 2016–2023, 2025 and 2026**, the formula matches the official broadcast to the exact minute (0m error on the 24m lattice). For **2024**, official national television broadcast confirmed **13 April, 22:17:24**, resolving the 22:24 lattice calculation and 22:17 ceremony dispute. For **2020**, TVK broadcast confirmed the arrival on the night of **13 April, 20:48**, resolving the diaspora calendar typo. Date accuracy and time accuracy must be tested separately.

### How the formula and table were compared

A Node.js probe loaded the [pinned MomentKH implementation](#a01--momentkh-pinned-baseline) in two VM contexts: unchanged, and with only `khNewYearMoments` replaced by an empty table in memory. Both called the actual `getNewYear` function with the process time zone set to `Asia/Phnom_Penh`. No arithmetic was changed. This establishes what the code outputs, not historical accuracy. The modern time-zone setting does not establish the applicable time convention for 1879 or 1897.

## Moha Sangkran arrival-time research (18 September 2026)

A dedicated evidence review investigated whether the arrival time-of-day can be computed or must be sourced per year. Its decision: **a curated, source-tagged per-year dataset** — the shipped festival-date arithmetic stays untouched, and the engine exposes only the estimate documented above. The full report, seed dataset and diagnostics live in the `research-khmer-new-year-time` package; the load-bearing findings follow.

### Structural limitation: the 24-minute lattice

The pinned time path selects the first of four candidate day-counts whose corrected within-sign degree is zero, then converts its integer arc-minute remainder (`libda`) by `clockMinutes = 1440 − 24 × libda`. Every computed minute-of-day is therefore a multiple of 24; within an hour only :00, :12, :24, :36 and :48 can appear. Published clocks such as 19:11, 08:07 or 14:01 are unreachable by **any** retuning of the six-segment interpolation table — a structural restriction, not a constants problem. The published-minus-estimate residuals for 2011–2015 are −24, −1, −12, −17 and −23 minutes; no single zone shift or zero-point correction equals them all.

### No known astronomical formula reproduces the published times

A negative-control experiment with Swiss Ephemeris 2.10.03 (Moshier backend) solved apparent geocentric solar longitude zero under tropical, Lahiri-sidereal and Fagan/Bradley-sidereal definitions for the eight best-evidenced years. The tropical ingress falls in March; Fagan/Bradley differs substantially (2024: 14 April 20:12:29 UTC+7); Lahiri comes close in some recent years but never to the published minute, and fails across 2011–2015. The ministry almanac's actual definition — epoch, ayanamsha, solar model, meridian, rounding — remains unrecovered.

### The publishing authority

The 2012–2013 Cambodia Daily reports attribute their times to the Ministry of Cults and Religion's almanac. A 2022 VOD interview [S18](#moha-sangkran-arrival-time-research-18-september-2026) identifies Im Borin, then an adviser to that ministry, as author of the annual *Moha Sangkran* publication, issued by a ministry-established astrology committee since 1998. Exact minutes are an annual editorial act by an unpublished method; no open machine-readable archive of the almanac series was found.

### Lerng Sak second-level clocks (research only)

Several inspected calendars print a closing (Lerng Sak) time to the second. The identity `seconds = 108 × ((292207·y + 373) mod 800)` with `y = year − 638` — equivalently `(800 − kromthupul) × 108` — matches 2012 (23:43:48, [T06](#t06--bodhikaram-new-year-announcement-2-april-2012)), 2015, 2018, 2020 and 2022 exactly, but the inspected 2016 proclamation prints **00:24:12** where the identity computes 00:34:12. With a recorded counterexample and shared publisher lineage, this stays a research diagnostic; the upstream `lerngSakMoment` is initialized to midnight and must not be cited as implementing the tradition.

### 1879 and 1897: rejected

The pinned table's claims (12 April 1879 11:36; 13 April 1897 02:00) have no era-appropriate historical support; code and commit history are not evidence. Both entries are **rejected from production arrival data** unless historical evidence is acquired — "unverified and excluded" rather than disproven. The concrete archival lead is F. G. Faraut, *Astronomie cambodgienne* (1910), catalogued by the Center for Khmer Studies; it was not obtained or inspected.

### TVK National Television broadcast archive (19 September 2026)

Official broadcast descriptions and announcements from the National Television of Cambodia (TVK, ទូរទស្សន៍ជាតិកម្ពុជា) established an unbroken 17-year timeline across **2010–2026**, alongside older anchors in **1997** (22:48) and **2009** (01:30).

Key findings from the national broadcaster:
- **2024 dispute resolved:** TVK broadcast explicitly gave **13 April 2024 at 22:17:24** (២២និង១៧នាទី និង២៤វិនាទី). The printed calendar's 22:24 is explained as the traditional lattice calculation, while the official broadcast used second-level precision.
- **2020 resolution:** TVK announced on the evening of 13 April 2020 that the angel arrives tonight at **20:48** (២០និង ៤៨ នាទីយប់នេះ), resolving the diaspora printed calendar's typo (which printed 14 April with an invalid weekday) and confirming the engine's 13 April date.
- **2015 correction:** TVK primary Khmer text explicitly gave **14:01** (១៤:០១), correcting the previous 14:02 reading.
- **6-hour progression:** Consecutive non-leap years advance by exactly 6 hours (e.g. 2017 03:12 → 2018 09:12 → 2019 15:12, and 2021 04:00 → 2022 10:00 → 2023 16:00), reflecting the ~0.25-day solar year offset.
- **Engine comparison:** 12 of the 19 evidenced years match the engine's traditional calculation to the exact minute (0m error: 1997, 2010, 2016–2023, 2025, 2026). Off-lattice minutes (e.g. 19:11, 08:07, 14:01, 22:17:24) differ by 1–24 minutes because the traditional formula is mathematically bound to multiples of 24.

### Research source register (S02–S25)

All items retrieved 18 September 2026. Grades: **A** direct government statement; **B** contemporaneous reporting (B† without explicit almanac attribution); **C** calendar scan personally inspected by the review; **E** computed or unsourced website data. Zone labels are absent from all arrival statements below except where noted; modern Cambodian wall clocks are interpreted as UTC+07:00, stored separately from the source wording.

| ID | Source | Supports |
| --- | --- | --- |
| S02 | [Cambodia Daily, 15 Apr 2011](https://english.cambodiadaily.com/2011/04/15/cambodia-welcomes-its-angel-for-the-new-year/) — B | 14 Apr 2011, 13:12 |
| S03 | [Cambodia Daily, 11 Apr 2012](https://english.cambodiadaily.com/2012/04/11/cambodians-prepare-for-arrival-of-khmer-new-year-angel/) — B | **13 Apr 2012, 19:11**, almanac attribution |
| S04 | [Cambodia Daily, 13 Apr 2013](https://english.cambodiadaily.com/2013/04/13/khmer-new-year-devada-set-to-welcome-age-of-consumerism/) — B | 14 Apr 2013, 02:12 |
| S05 | [Cambodia Daily, 15 Apr 2014](https://english.cambodiadaily.com/2014/04/15/phnom-penh-welcomes-the-new-years-angel/) — B | 14 Apr 2014, 08:07 |
| S06 | [Wat Ratanarangsey 2013 calendar PDF](https://www.templenews.org/wp-content/uploads/2012/10/Calendar-Wat-Revere-2013-2557.pdf) — C | 14 Apr 2013, 02:12 (April page) |
| S07 | [Wat Kiryvongsa Bopharam 2015 calendar PDF](https://www.templenews.org/wp-content/uploads/2014/12/KhmerCalendar2559-2015.pdf) — C | 14 Apr 2015, 14:02; Lerng Sak 16 Apr 18:21:36 |
| S08 | [Wat Kiryvongsa Bopharam 2016 calendar PDF](https://www.templenews.org/wp-content/uploads/2015/12/Khmer-Calendar-2560-2016.pdf) — C | 13 Apr 2016, 20:00; Lerng Sak printed 00:24:12 (counterexample) |
| S09 | [Wat Kiryvongsa Bopharam 2018 calendar PDF](https://www.templenews.org/wp-content/uploads/2017/12/Khmer-2562-2018-Calendar.pdf) — C | 14 Apr 2018, 09:12; Lerng Sak 12:59:24 |
| S10 | [Phnom Penh Post, 13 Apr 2018](https://phnompenhpost.com/national/trouble-foreseen-khmer-new-year-almanac/) — B | 14 Apr 2018, 09:12 |
| S11 | [Bodhikaram announcement, 2 Apr 2012](https://bodhikaram.wordpress.com/2012/04/02/khmer-new-year-april-13-14-2012/) — E* temple HTML | Same as [T06](#t06--bodhikaram-new-year-announcement-2-april-2012) |
| S12 | [Bodhikaram 2012 calendar PDF](https://bodhikaram.wordpress.com/wp-content/uploads/2012/04/bodhikaram-calendar-2012.pdf) — C, dates only | Festival-date block; no clock on the April page |
| S13 | [2020 calendar year panel](https://www.templenews.org/wp-content/uploads/2019/12/%E1%9E%81%E1%9F%82%E1%9E%98%E1%9E%B7%E1%9E%82%E1%9E%9F%E1%9E%B7%E1%9E%9A%E1%9F%A2.jpg) — C | Prints 14 Apr 2020, 20:48 with inconsistent weekday |
| S14 | [2020 calendar proclamation](https://www.templenews.org/wp-content/uploads/2019/12/%E1%9E%81%E1%9F%82%E1%9E%98%E1%9E%B7%E1%9E%82%E1%9E%9F%E1%9E%B7%E1%9E%9A%E1%9F%A3.jpg) — C | Repeats 14 Apr 20:48; closing 16 Apr 01:24:36 |
| S15 | [2022 calendar proclamation](https://www.templenews.org/wp-content/uploads/2021/12/%E1%9E%9F%E1%9E%84%E1%9F%92%E1%9E%80%E1%9F%92%E1%9E%9A%E1%9E%B6%E1%9E%93%E1%9F%92%E1%9E%8A.jpg) — C | 14 Apr 2022, 10:00; Lerng Sak 13:49:48 |
| S16 | [2022 companion panel](https://www.templenews.org/wp-content/uploads/2021/12/%E1%9E%94%E1%9F%92%E1%9E%9A%E1%9E%8F%E1%9E%B7%E1%9E%91%E1%9E%B7%E1%9E%93-%E1%9E%86%E1%9F%92%E1%9E%93%E1%9E%B6%E1%9F%86%E1%9E%81%E1%9E%B6%E1%9E%9B-%E1%9E%85%E1%9E%8F%E1%9F%92%E1%9E%9C%E1%9E%B6%E1%9E%9F%E1%9F%90%E1%9E%80-%E1%9E%96.%E1%9E%9F.%E1%9F%A2%E1%9F%A5%E1%9F%A6%E1%9F%A6.jpg) — C | Heading 13 Apr vs list 14 Apr; clock 10:00 — internal conflict |
| S17 | [Cambodianess, 14 Apr 2022](https://cambodianess.com/article/the-three-days-of-khmer-new-year) — B† | 14 Apr 2022, 10:00 |
| S18 | [VOD, 8 Apr 2022](https://vodenglish.news/cambodias-new-year-angel-will-have-a-gun-to-protect-the-border/) — B | Almanac authorship and committee provenance |
| S19 | [Wat Kiryvongsa Bopharam 2024 calendar PDF](https://www.templenews.org/wp-content/uploads/2023/12/Khmer-Calendar-2567-2568.pdf) — C | 13 Apr 2024, 22:24 (upgrades [T04](#t04--templenews-2024-calendar-observation)) |
| S20 | [Cambodianess, 13 Apr 2024](https://cambodianess.com/article/khmer-new-year-things-to-know-about-cambodias-largest-festival) — B† | 13 Apr 2024, 22:24 |
| S21 | [Cambodianess, 29 Mar 2024](https://cambodianess.com/article/wat-phnom-to-host-phnom-penhs-khmer-new-year) — B, ceremony | Wat Phnom ceremony 22:17, not the arrival |
| S22 | [Fresh News, 13 Apr 2024](https://freshnews.com.kh/localnews/336733-2024-04-13-14-36-09.html) — B†, Khmer | Nationwide/diaspora welcome 22:17:24 |
| S23 | [AKP, 13 Apr 2025](https://www.akp.gov.kh/post/detail/334275) — **A** | **14 Apr 2025, 04:48** |
| S24 | [AKP, 14 Apr 2026](https://akp.gov.kh/post/detail/367694) — **A** | **14 Apr 2026, 10:48** |
| S25 | [AKP, 13 Apr 2026](https://akp.gov.kh/post/detail/367629) — **A** | Separates 13 Apr merit ceremony from the 10:48 start |

The [TempleNews calendar archive](https://www.templenews.org/category/calendar/) (T01) supplied the calendar editions; announcements and over-size PDFs for 2021, 2023, 2025 and 2026 editions were located but not successfully inspected, so no C-grade claim is made for those years.

## Printed calendars and user observations

### T01 — TempleNews calendar archive

[Calendar category](https://www.templenews.org/category/calendar/), supplied by the user; catalogue inspected on 15 September 2026. It provides yearly calendar publications and download links. It is a discovery source, not a government holiday decree. The user's observation that these calendars omit the King's birthday does not establish that the birthday was not a public holiday.

### T02 — TempleNews 2013 calendar

[The 2557 / 2013 Khmer Calendar image](https://www.templenews.org/wp-content/uploads/2012/10/The-2557-2013-Khmer-Calendar.jpg), supplied by the user as the oldest TempleNews calendar they had found. **Located only:** the assistant has not inspected this scan, and no specific 2013 date/time has yet been transcribed from it in this review. This is not a claim that no earlier calendar exists elsewhere.

### T03 — TempleNews 2015 calendar observation

[Wat Kiry Vongsa Bopharam calendar publication](https://www.templenews.org/2015/01/02/wat-kiry-vongsa-bopharam/), 2 January 2015; [2015 calendar PDF](https://www.templenews.org/wp-content/uploads/2014/12/KhmerCalendar2559-2015.pdf).

On 15 September 2026 the user checked this edition and reported **14 April 2015, 14:02**. This is **user-transcribed evidence**, corroborating MomentKH's time override and [N07](#n07--visit-angkor-2015). The 18 September 2026 [arrival-time research](#moha-sangkran-arrival-time-research-18-september-2026) later inspected the PDF directly (proclamation page 2, April page 6) and confirmed the transcription; the record was noted via [S07](#moha-sangkran-arrival-time-research-18-september-2026). The [TVK broadcast archive](#tvk-national-television-broadcast-archive-19-september-2026) established **14:01** from primary Khmer announcements.

### T04 — TempleNews 2024 calendar observation

[Free Copy: The 2568 2024 Khmer Calendar](https://www.templenews.org/2023/12/17/free-copy-2568-2024-khmer-calendar/), published 17 December 2023; [Khmer Calendar 2567–2568 PDF](https://www.templenews.org/wp-content/uploads/2023/12/Khmer-Calendar-2567-2568.pdf). The article and 14-page PDF metadata were retrieved on 15 September 2026.

On 15 September 2026 the user reported **13 April 2024, 22:24** after checking the TempleNews calendar. This is **user-transcribed evidence**, agreeing with [N09](#n09--cambodianess-13-april-2024) and [N10](#n10--camboja-9-april-2024). The 18 September 2026 [arrival-time research](#moha-sangkran-arrival-time-research-18-september-2026) inspected the PDF's year panel and proclamation (pages 13–14) and confirmed the transcription; the record was noted via [S19](#moha-sangkran-arrival-time-research-18-september-2026). The national broadcaster [TVK](#tvk-national-television-broadcast-archive-19-september-2026) confirmed **13 April 2024, 22:17:24** in its official broadcast, explaining 22:24 as the traditional 24-minute lattice calculation.

### T05 — Bodhikaram 2012 calendar

The user supplied the [Bodhikaram homepage](https://bodhikaram.wordpress.com/). The review located its [Khmer-English Calendar 2012 post](https://bodhikaram.wordpress.com/2012/04/14/khmer-english-calendar-2012/), dated 14 April 2012, and [12-page calendar PDF](https://bodhikaram.wordpress.com/wp-content/uploads/2012/04/bodhikaram-calendar-2012.pdf).

The PDF's legacy Khmer text did not extract usefully; its April page was not visually verified. Do not cite this scan as a verified 2012 date. The separately readable announcement below supplies the actual evidence.

### T06 — Bodhikaram New Year announcement, 2 April 2012

[Khmer New Year, April 13 & 14, 2012](https://bodhikaram.wordpress.com/2012/04/02/khmer-new-year-april-13-14-2012/), **directly reviewed text** on 15 September 2026. It states traditional arrival on **13 April at 19:11**, and Loeung Sak on **15 April at 23:43:48**. Its separate Ottawa celebration schedule spans 13–14 April; that local schedule is not the traditional festival's duration. The announcement does not explicitly label the time zone. The precise Loeung Sak time has not been independently corroborated or adopted as an engine rule.

### T07 — Wat Khemara Ratana Ram calendar archive

[WatKhmers calendar category](https://watkhmers.org/category/ប្រតិទិន-calendar/), supplied by the user and its catalogue inspected on **16 September 2026**. It lists yearly Khmer lunar calendars and New Year articles. Recorded as a source for further calendar checks; no individual date or arrival time has yet been validated from this source in this review.

## Contemporary New Year reporting

Relevant article text was reviewed on 15 September 2026. These reports corroborate particular dates or times; references to a Ministry almanac are not direct inspection of that almanac. The reviewed results are collected in the table above.

### N01 — The Cambodia Daily, 15 April 2011

[Cambodia Welcomes Its Angel for the New Year](https://english.cambodiadaily.com/2011/04/15/cambodia-welcomes-its-angel-for-the-new-year/): the previous day's arrival and Wat Phnom ceremony at **13:12**, supporting **14 April 2011, 13:12**.

### N02 — The Cambodia Daily, 11 April 2012

[Cambodians Prepare for Arrival of Khmer New Year Angel](https://english.cambodiadaily.com/2012/04/11/cambodians-prepare-for-arrival-of-khmer-new-year-angel/): **13 April, 19:11**, attributed to the Ministry of Cults and Religions' almanac. Supports the date and arrival time alongside [T06](#t06--bodhikaram-new-year-announcement-2-april-2012).

### N03 — The Cambodia Daily, 13 April 2013

[Khmer New Year Devada Set to Welcome Age of Consumerism](https://english.cambodiadaily.com/2013/04/13/khmer-new-year-devada-set-to-welcome-age-of-consumerism/): **14 April, 02:12**, in coverage of the Ministry's annual almanac. Supports MomentKH's time correction from 02:24 to 02:12.

### N04 — The Cambodia Daily, 15 April 2014

[Phnom Penh Welcomes the New Year's Angel](https://english.cambodiadaily.com/2014/04/15/phnom-penh-welcomes-the-new-years-angel/): the preceding Monday's arrival at **08:07**, supporting **14 April 2014, 08:07**. Earlier ceremony preparations are not the arrival instant.

### N05 — The Cambodia Daily, 14 April 2015

[Bloodthirsty Angel to Ring in New Year, for Better or Worse](https://english.cambodiadaily.com/2015/04/14/bloodthirsty-angel-to-ring-in-new-year-for-better-or-worse/): arrival that Tuesday around **14:00**. Supports the date, but its rounded time does not independently establish 14:02.

### N06 — The Cambodia Daily, 15 April 2015

[City Dwellers Mark New Year at Wat Phnom](https://english.cambodiadaily.com/2015/04/15/city-dwellers-mark-new-year-at-wat-phnom/): describes candle lighting the preceding day at **14:01**. Ceremony action time is not necessarily the prescribed arrival time; this does not contradict or independently establish 14:02.

### N07 — Visit Angkor, 2015

[Khmer New Year 2015: Welcome Angel Reaksa Tevy](https://www.visit-angkor.org/blog/khmer-new-year-2015-welcome-angel-reaksa-tevy/), posted 14 April 2015 and subsequently updated: explicitly gives **14 April, 14:02 Cambodian time**. Secondary corroboration, strengthened by the user's [TempleNews check](#t03--templenews-2015-calendar-observation).

### N08 — Cambodianess, 29 March 2024

[Wat Phnom to Host Phnom Penh's Khmer New Year](https://cambodianess.com/article/wat-phnom-to-host-phnom-penhs-khmer-new-year): reports the Phnom Penh Administration's planned welcoming ceremony at **22:17 on 13 April**. Retained as ceremony-schedule evidence. It does not resolve why MomentKH's 22:17 entry differs from the traditional-arrival reports below.

### N09 — Cambodianess, 13 April 2024

[Khmer New Year: Things to Know About Cambodia's Largest Festival](https://cambodianess.com/article/khmer-new-year-things-to-know-about-cambodias-largest-festival): traditional arrival **13 April, 22:24**, Vanabat on 14–15 April and Laeung Sak on 16 April.

### N10 — CamboJA, 9 April 2024

[Schools, Tourist Sites, Provinces Hold Sankranta to Ring in Khmer New Year](https://cambojanews.com/schools-tourist-sites-provinces-hold-sankranta-to-ring-in-khmer-new-year/): gives **13 April, 22:24**. Agrees with N09, the user-transcribed TempleNews observation and the formula's pre-override time. This agreement does not prove every source used an independent almanac.

## Government and institutional holiday references

### G01 — ECCC information circular E165.1, Official Holidays for 2012

**Directly reviewed:** both scanned pages of the user-supplied circular, dated **1 December 2011**, on 15 September 2026. A repository copy is available as [E165.1_EN.pdf](E165.1_EN.pdf).

| Identifier | Value |
| --- | --- |
| Repository copy | `docs/E165.1_EN.pdf` |
| File size | 71,061 bytes |
| SHA-256 of the retained PDF, checked 15 September 2026 | `9c3e7bef5d1a5a701cff91824a42324adc1512b51330fc6eaef8a68512d42177` |
| Page record numbers | 00775760–00775761 |
| Addressees | ECCC/UNAKRT staff |
| Government instrument cited by the circular | Royal Government Sub-Decree 231, 14 October 2011; original decree not inspected |

Page 1 distinguishes the national-staff dates, citing the government sub-decree, from a separate UNAKRT schedule:

| Holiday | National-staff government dates in 2012 | Separate UNAKRT dates |
| --- | --- | --- |
| Khmer New Year | 13–15 April | 13 and 16 April |
| King's birthday | 13–15 May | 14 and 15 May |
| Pchum Ben | 14–16 October | 15 and 16 October |
| Water Festival | 27–29 November | 27–29 November |

Page 1 also lists **Visak Bochea on 5 May** and **Royal Ploughing on 9 May**. These are the 2012 lunar-festival anchors used by the engine tests.

Page 2 says weekend holidays are observed on Monday, with only one additional Monday when holidays fall on both Saturday and Sunday. This explains **16 April as substitute leave**, not as the third traditional festival day. That policy belongs to this circular's staff schedule; it has not been established for other years or employment scopes. The circular is primary evidence of ECCC's schedule and corroborates the government dates it cites. The original decree and any later amendments have not been inspected.

The [Cambodia Tribunal Case 002 Ieng Sary filings catalogue](https://cambodiatribunal.org/court-filings/case-002-ieng-sary/) and [document index](https://cambodiatribunal.org/sites/default/files/documents/) were located during the review. The index lists `E165.1_EN.PDF`; the remote PDF bytes were not verified against the local copy. The retained local document is the evidence actually inspected.

### G02 — DFDL's contemporary 2012 holiday summary

[Legal & Regulatory Updates in Cambodia](https://www.dfdl.com/insights/legal-and-tax-updates/legal-a-regulatory-updates-in-cambodia-2/), published **24 November 2011**, reviewed 15 September 2026. Its summary of Ministry of Labour and Vocational Training **Prakas 228, 28 October 2011**, lists **13–15 April 2012** New Year and **13–15 May** King's birthday. It also describes following-working-day substitution for Sunday holidays. This is a professional secondary summary, not the original Prakas; preserve its instrument and scope separately from G01.

### G03 — Investment Guidebook 2012: conflicting New Year dates

[Cambodia Investment Guidebook PDF hosted by Open Development Mekong](https://data.opendevelopmentmekong.net/dataset/97248af3-78d8-483e-bd9c-7d5aa55c187d/resource/b8f2294e-1b2d-46c2-9bc9-cbad04e19c51/download/1ad175dc-a6be-437c-877a-270af0faa3d5.pdf), Council for the Development of Cambodia, **January 2012**. Table **I-5-6** is on printed page **I-3**, PDF page **18** (zero-based index 17) of this chapter file. Inspected during the 15 September review and its locator rechecked during documentation. It lists New Year **14–16 April 2012** and King's birthday **13–15 May**.

The New Year entry conflicts with G01, G02, T06 and N02. Keep this reference as contrary evidence; it does not outweigh the reviewed festival reports and decree-citing circular. The cause of its differing dates has not been established.

### G04 — 2013 paid-holiday Prakas, NCDD-hosted translation

[Prakas 223 paid-holiday translation PDF](https://lib.ncdd.gov.kh/storage/app/public/library_backend/CAT_16505_1/2012-223-Paid%20holiday-en.pdf), **12 November 2012**, hosted in the NCDD library. Reviewed 15 September 2026. The CAMFEBA English translation is marked **unofficial**; PDF page **1**, **Art1New**, lists **14–16 April 2013** New Year and **13–15 May** King's birthday. Its preamble references Sub-Decree 186 of 22 October 2012, amending Article 1 of Sub-Decree 156 for the 2013 calendar.

This supplies dated holiday evidence for 2013, not the New Year arrival minute and not a perpetual three-day birthday rule. The original Khmer instrument was not independently inspected in this review.

The same page lists **Visak Bochea on 24 May**, **Royal Ploughing on 28 May** and **Water Festival on 16–18 November**, used as date anchors in the engine tests.

### G05 — Published calendar anchors

The following publications were reviewed on **10 September 2026**:

- [Ministry of Economy and Finance: 2025 calendar](https://mef.gov.kh/calendar-holiday-2025/), including Royal Ploughing on **15 May 2025**.
- [Legal Reform Committee: 2026 annual holiday calendar](https://lrc.gov.kh/en/annual-holiday-calendar-2026/), including Royal Ploughing on **5 May 2026**.
- [National Radio of Cambodia: 2024 New Year preparations announcement](https://rnk.gov.kh/index.php/interior-minister-calls-on-authorities-at-all-levels-to-be-well-prepared-to-ensure-safety-security-and-social-order-during-the-coming-traditional-khmer-new-year-celebration), corroborating the **13–16 April 2024** four-day festival range.

These are year-specific date anchors. A calculation of a festival date does not determine whether a government designates it as a public holiday in another year.

## Other calendar and event references

### D01 — TimeBie Cambodia 2012

The user supplied [Cambodia 2012 calendar page](https://www.timebie.com/calendar/cambodia2012.php). The page did not reliably serve the calendar during the review; the [13-page PDF](https://timebie.com/calendar/pdf/cambodia2012.pdf) was accessible and lists **13–15 April** New Year and **13–15 May** King's birthday.

Reviewed 15 September 2026 as a **secondary cross-check**. No government instrument citation was found in the PDF. Its agreement is useful corroboration, but it is not the authority for official leave or arrival times.

## Original algorithm sources

These references establish code ancestry and reproducibility. Agreement between related implementations is not independent calendar evidence.

### A01 — MomentKH pinned baseline

[ThyrithSor/momentkh at `ff2bfd558385bd5403780b671fd2eca43b3e2228`](https://github.com/ThyrithSor/momentkh/tree/ff2bfd558385bd5403780b671fd2eca43b3e2228), MIT. The 15 September audit inspected the actual [TypeScript implementation](https://github.com/ThyrithSor/momentkh/blob/ff2bfd558385bd5403780b671fd2eca43b3e2228/momentkh.ts), [JavaScript build](https://github.com/ThyrithSor/momentkh/blob/ff2bfd558385bd5403780b671fd2eca43b3e2228/momentkh.js) and [upstream New Year tests](https://github.com/ThyrithSor/momentkh/blob/ff2bfd558385bd5403780b671fd2eca43b3e2228/test/new-year.test.js). The table above records the measured outputs with and without its exceptions.

Exception provenance:

- [2019 introduction, `498fa8825a49e9698492b9e76ac724765e94d2f0`](https://github.com/ThyrithSor/momentkh/commit/498fa8825a49e9698492b9e76ac724765e94d2f0); [original constant.js](https://raw.githubusercontent.com/ThyrithSor/momentkh/498fa8825a49e9698492b9e76ac724765e94d2f0/constant.js). Included the 1879 and 2011–2015 entries, including the incorrect 2012 day. No year-specific historical citation for that day was found.
- [1897 addition, `2d94e75f7251033e9ce415c84aa4c94a2062c4b8`](https://github.com/ThyrithSor/momentkh/commit/2d94e75f7251033e9ce415c84aa4c94a2062c4b8), 4 March 2021. Supplies engineering context about multiple New Year candidates, not independent historical proof of the 1897 date.
- [Restructure containing the 2024 entry, `499965c7d0c321339e1e911c71f72db9a5ec6d1f`](https://github.com/ThyrithSor/momentkh/commit/499965c7d0c321339e1e911c71f72db9a5ec6d1f), 16 December 2025. The 2024 time still requires source review despite being in the table.
- The 2019 README also names [Dahlina's Khmer New Year time page](http://www.dahlina.com/education/khmer_new_year_time.html) (tracking query omitted) and [CAM-CC's lunar-calendar site](http://www.cam-cc.org). Dahlina was unavailable during the earlier review; CAM-CC is recorded here only as an upstream citation, without a content review. Neither source's contents substantiate an individual exception in this audit.

The **1879 and 1897 entries remain historically unverified**. The exact 2012, 2015 and 2024 findings are field-specific; do not infer that every formula date or minute is correct.

### A02 — MetheaX lunar calendar port

[MetheaX/khmer-chhankitek-calendar at `4d1df001de73fac715d6e4c54f06f69618ff49dd`](https://github.com/MetheaX/khmer-chhankitek-calendar/tree/4d1df001de73fac715d6e4c54f06f69618ff49dd), MIT. Basis of the Kotlin lunar arithmetic, with lineage credited to Phylypo Tum and Thyrith Sor. Retain the original MIT license and attribution notices when incorporating these algorithms.

## Validation still required

1. Arrival times: while TVK national broadcast records provide 19 evidenced years (1997, 2009, 2010–2026 unbroken), obtaining the ministry's original printed Moha Sangkran almanac archive and internal computational algorithm (angular precision, meridian, rounding) remains an open research topic. Until then the per-year dataset carries the published clocks, and the estimate stays labeled as such.
2. A second independently transcribed 2016 proclamation would test the Lerng Sak 24-versus-34-minute conflict in the closing-clock candidate.
3. Verify lunar dates around extra months, extra days and month endings against printed calendars or almanacs.
4. Verify Buddhist holy days and Buddhist Era, animal-year and Sak transitions, distinguishing calendar dates from precise instants.
5. Resolve 1879/1897 only when suitable historical evidence becomes available (Faraut 1910 is the archival lead). They stay rejected meanwhile.

Each adopted rule or exception should have a focused regression case, a source locator and a documented calendar/time convention. A single verified date does not validate an entire year or formula. The goal is the strongest accuracy we can substantiate, with remaining uncertainty stated explicitly.
