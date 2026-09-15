# Calendar evidence and references

Updated **16 September 2026**. This is the engine's self-contained record of external calendar evidence: what each source supports, which results are corroborated, and what remains unresolved. The calculations and their tests are maintained in this repository.

## Evidence standard

- Cite the actual calendar, almanac, publication or original algorithm.
- Record the year, date or time supported, the review method, and any conflict.
- Preserve manual calendar transcriptions as manual observations.
- Treat agreement with another implementation as a compatibility check. It does not independently prove a calendar result.
- Keep traditional festival dates, arrival times, ceremony schedules, government holidays and substitute leave as separate fields.

The sources below were reviewed on 15 September 2026 unless an earlier review date is stated. Scanned TempleNews dates were checked by the user; the assistant located the publication links but did not independently inspect those scans. Reports may share an underlying almanac, so multiple reports do not necessarily represent independent derivations.

## Reviewed New Year cases

The following table brings the upstream calculation, override and external evidence together. The shared engine's date-only tests cover the reviewed modern dates; arrival minutes are retained as evidence and are not exposed by the API. The 1879/1897 upstream exceptions have not been adopted without historical support.

| Year | MomentKH formula without its table | MomentKH with its table | Result supported by the reviewed sources |
| --- | --- | --- | --- |
| 1879 | 12 April, 10:00 | 12 April, 11:36 | Unverified: no historical calendar or almanac found |
| 1897 | 12 April, 02:00 | 13 April, 02:00 | Unverified: no historical calendar or almanac found |
| 2011 | 14 April, 13:36 | 14 April, 13:12 | **14 April, 13:12** — [N01](#n01--the-cambodia-daily-15-april-2011) |
| 2012 | 13 April, 19:12 | 14 April, 19:11 | **13 April, 19:11** — [T06](#t06--bodhikaram-new-year-announcement-2-april-2012), [N02](#n02--the-cambodia-daily-11-april-2012); **13–15 April** holiday dates also supported by [G01](#g01--eccc-information-circular-e1651-official-holidays-for-2012) |
| 2013 | 14 April, 02:24 | 14 April, 02:12 | **14 April, 02:12** — [N03](#n03--the-cambodia-daily-13-april-2013) |
| 2014 | 14 April, 08:24 | 14 April, 08:07 | **14 April, 08:07** — [N04](#n04--the-cambodia-daily-15-april-2014) |
| 2015 | 14 April, 14:24 | 14 April, 14:02 | **14 April, 14:02** — user's [T03](#t03--templenews-2015-calendar-observation) calendar check and [N07](#n07--visit-angkor-2015) |
| 2024 | 13 April, 22:24 | 13 April, 22:17 | **13 April, 22:24** — user's [T04](#t04--templenews-2024-calendar-observation) calendar check, [N09](#n09--cambodianess-13-april-2024), [N10](#n10--camboja-9-april-2024); retain the 22:17 ceremony report as contextual evidence |

For **2012**, the formula's date is supported and the table's date override is wrong. The formula's arrival minute still differs from the reviewed 19:11. For **2011, 2013, 2014 and 2015**, the table's time corrections have supporting evidence. For **2024**, reviewed traditional-arrival evidence supports the formula's 22:24; the cause of the separate 22:17 ceremony report remains unresolved. Date accuracy and time accuracy must be tested separately.

### How the formula and table were compared

A Node.js probe loaded the [pinned MomentKH implementation](#a01--momentkh-pinned-baseline) in two VM contexts: unchanged, and with only `khNewYearMoments` replaced by an empty table in memory. Both called the actual `getNewYear` function with the process time zone set to `Asia/Phnom_Penh`. No arithmetic was changed. This establishes what the code outputs, not historical accuracy. The modern time-zone setting does not establish the applicable time convention for 1879 or 1897.

## Printed calendars and user observations

### T01 — TempleNews calendar archive

[Calendar category](https://www.templenews.org/category/calendar/), supplied by the user; catalogue inspected on 15 September 2026. It provides yearly calendar publications and download links. It is a discovery source, not a government holiday decree. The user's observation that these calendars omit the King's birthday does not establish that the birthday was not a public holiday.

### T02 — TempleNews 2013 calendar

[The 2557 / 2013 Khmer Calendar image](https://www.templenews.org/wp-content/uploads/2012/10/The-2557-2013-Khmer-Calendar.jpg), supplied by the user as the oldest TempleNews calendar they had found. **Located only:** the assistant has not inspected this scan, and no specific 2013 date/time has yet been transcribed from it in this review. This is not a claim that no earlier calendar exists elsewhere.

### T03 — TempleNews 2015 calendar observation

[Wat Kiry Vongsa Bopharam calendar publication](https://www.templenews.org/2015/01/02/wat-kiry-vongsa-bopharam/), 2 January 2015; [2015 calendar PDF](https://www.templenews.org/wp-content/uploads/2014/12/KhmerCalendar2559-2015.pdf).

On 15 September 2026 the user checked this edition and reported **14 April 2015, 14:02**. This is **user-transcribed evidence**, corroborating MomentKH's time override and [N07](#n07--visit-angkor-2015). The assistant located the publisher's catalogue link and PDF metadata but did not independently inspect the scan. A specific page number remains to be recorded.

### T04 — TempleNews 2024 calendar observation

[Free Copy: The 2568 2024 Khmer Calendar](https://www.templenews.org/2023/12/17/free-copy-2568-2024-khmer-calendar/), published 17 December 2023; [Khmer Calendar 2567–2568 PDF](https://www.templenews.org/wp-content/uploads/2023/12/Khmer-Calendar-2567-2568.pdf). The article and 14-page PDF metadata were retrieved on 15 September 2026.

On 15 September 2026 the user reported **13 April 2024, 22:24** after checking the TempleNews calendar. This is **user-transcribed evidence**, agreeing with [N09](#n09--cambodianess-13-april-2024) and [N10](#n10--camboja-9-april-2024). The links above locate the publisher's matching-year calendar; the user did not provide a file hash or page number. The assistant has not inspected the scan. Preserve [N08](#n08--cambodianess-29-march-2024) as a separate 22:17 ceremony report.

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

1. Check New Year arrival times in years without upstream exceptions.
2. Verify lunar dates around extra months, extra days and month endings against printed calendars or almanacs.
3. Verify Buddhist holy days and Buddhist Era, animal-year and Sak transitions, distinguishing calendar dates from precise instants.
4. Add page locators for the 2015 and 2024 TempleNews observations and obtain original almanacs where current evidence is indirect.
5. Resolve 1879/1897 only when suitable historical evidence becomes available. Keep them unverified meanwhile.

Each adopted rule or exception should have a focused regression case, a source locator and a documented calendar/time convention. A single verified date does not validate an entire year or formula. The goal is the strongest accuracy we can substantiate, with remaining uncertainty stated explicitly.
