// Compare the actual packaged JavaScript library with the JVM test output.
// This is a cross-target consistency check, not independent calendar evidence.
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { pathToFileURL } from 'node:url';

const packagePath = resolve(process.argv[2] ?? 'build/npm/index.mjs');
const baselinePath = resolve(process.argv[3] ?? 'build/reports/engine-baseline.tsv');
const {
  KhmerCalendarEngine,
  GregorianDate,
  RecurrenceRule,
  EventDateOverride,
  createRule,
  ChineseLunisolarEngine,
  FestivalProfile,
  ChineseLunarDate,
  getChineseFestivalDates,
} = await import(pathToFileURL(packagePath).href);

const engine = new KhmerCalendarEngine();
const rules = {
  fixed: createRule({ id: 'fixed', type: 'solar', month: 5, day: 14 }),
  weekday: createRule({ id: 'weekday', type: 'solar_nth_weekday', month: 5, day: 7, occurrence: 2 }),
  lent: createRule({ id: 'lent', type: 'khmer_lunar', month: 7, day: 1, waxing: false, monthPolicy: 'ordinary_or_second_asadh' }),
  first: createRule({ id: 'first', type: 'new_year_first' }),
  middle: createRule({ id: 'middle', type: 'new_year_middle' }),
  last: createRule({ id: 'last', type: 'new_year_last' }),
};
const recurrence = new Map();
let days = 0, years = 0, occurrences = 0;
for (const row of readFileSync(baselinePath, 'utf8').trim().split(/\r?\n/)) {
  const fields = row.split('\t');
  if (fields[0] === 'D') {
    const [year, month, day] = fields[1].split('-').map(Number);
    const v = engine.fromGregorian(year, month, day), l = v.lunar;
    const actual = ['D', v.date.iso, l.day, l.waxing, l.month, l.buddhistYear,
      l.monthLength, l.isHolyDay, l.isShavingDay, v.animalYear, v.sak,
      v.animalYearChangesToday, v.sakChangesToday].join('\t');
    assert.equal(actual, row, fields[1]);
    assert.equal(engine.toGregorian(l.buddhistYear, l.month, l.day, l.waxing).iso, fields[1]);
    days++;
  } else if (fields[0] === 'N') {
    const year = Number(fields[1]), v = engine.newYear(year);
    assert.equal(['N', year, v.start.iso, v.days, v.arrivalEstimate.minuteOfDay].join('\t'), row);
    assert.equal(v.arrivalEstimate.minuteOfDay % 24, 0, `${year} arrival estimate lattice`);
    years++;
  } else if (fields[0] === 'R') {
    const key = `${fields[1]}/${fields[2]}`;
    if (!recurrence.has(key)) recurrence.set(key, []);
    recurrence.get(key).push(fields[3]);
    occurrences++;
  } else assert.fail(`Unknown baseline record: ${fields[0]}`);
}
assert.equal(days, 146462);
assert.equal(years, 401);
for (let year = 1800; year <= 2200; year++) for (const [id, rule] of Object.entries(rules)) {
  assert.deepEqual(engine.evaluateRule(year, rule).map(v => v.date.iso), recurrence.get(`${year}/${id}`) ?? [], `${year}/${id}`);
}

// JS callers are not constrained by Kotlin's integer types or TypeScript at runtime.
for (const bad of [NaN, Infinity, -Infinity, 2024.5]) assert.throws(() => engine.newYear(bad));
assert.throws(() => new GregorianDate(2024, 1.5, 1));
assert.throws(() => new GregorianDate(2024, 1, 1.5));
assert.throws(() => engine.toGregorian(2568, 5, 1.5, true));
assert.throws(() => new RecurrenceRule('bad', 'solar', 5, 1.5));
assert.throws(() => createRule({ id: 'bad', type: 'khmer_lunar', month: 7, day: 1, secondAsadh: true }));
assert.throws(() => createRule({ id: 'bad', type: 'khmer_lunar', month: 7, day: 1, monthPolicy: 'typo' }));
assert.equal(engine.newYear(2012).start.iso, '2012-04-13');
// The estimate is a frozen, estimate-typed value; published clocks are not computed here.
assert.equal(engine.newYear(2012).arrivalEstimate.minuteOfDay, 1152);
assert.equal(engine.newYear(2012).arrivalEstimate.hour, 19);
assert.equal(engine.newYear(2025).arrivalEstimate.minuteOfDay, 288);
assert.throws(() => { engine.newYear(2012).arrivalEstimate.minuteOfDay = 0; }, TypeError);

// A caller must not be able to corrupt shared caches or bypass validated rule inputs.
assert.throws(() => { engine.newYear(2012).start.day = 14; }, TypeError);
assert.throws(() => { engine.newYear(2012).days = 9; }, TypeError);
assert.throws(() => { rules.lent.monthPolicy = 'typo'; }, TypeError);
assert.equal(engine.newYear(2012).start.iso, '2012-04-13');
const override = new EventDateOverride('fixed', 2024, [new GregorianDate(2024, 5, 15)], 'notice-2024', 'Published correction');
assert.equal(engine.evaluateRule(2024, rules.fixed, override)[0].sourceId, 'notice-2024');
assert.equal(engine.evaluateRule(2024, rules.fixed, new EventDateOverride('fixed', 2024, [], 'notice', 'Cancelled')).length, 0);

// --- Chinese Lunisolar Engine JS Verification ---
const defaultChineseEngine = new ChineseLunisolarEngine();
assert.equal(defaultChineseEngine.profile.id, 'archive-v1');
assert.deepEqual(defaultChineseEngine.getFestivalDates(2024, 'chinese_new_year_days'), ['2024-02-10', '2024-02-11', '2024-02-12']);
assert.deepEqual(defaultChineseEngine.getFestivalDates(2026, 'chinese_new_year_days'), ['2026-02-17', '2026-02-18', '2026-02-19']);
assert.deepEqual(defaultChineseEngine.getFestivalDates(2013, 'chinese_zongzi_festival'), ['2013-06-13']);

const cnEngine = new ChineseLunisolarEngine(FestivalProfile.CN_REFERENCE_UTC8);
assert.deepEqual(cnEngine.getFestivalDates(2013, 'chinese_zongzi_festival'), ['2013-06-12']);
assert.deepEqual(cnEngine.getFestivalDates(2009, 'chinese_qingming_festival'), ['2009-04-04']);
assert.deepEqual(defaultChineseEngine.getFestivalDates(2009, 'chinese_qingming_festival'), ['2009-04-05']);

// Convenience helper tests
assert.deepEqual(getChineseFestivalDates(2026, 'chinese_new_year_days'), ['2026-02-17', '2026-02-18', '2026-02-19']);
assert.deepEqual(getChineseFestivalDates(2013, 'chinese_zongzi_festival', 'cn-reference-utc8'), ['2013-06-12']);
assert.deepEqual(getChineseFestivalDates(2013, 'chinese_zongzi_festival', 'archive-v1'), ['2013-06-13']);

// Conversions & boundary fragment
const lunar2026 = cnEngine.gregorianToLunar('2026-02-17');
assert.equal(lunar2026.year, 2026);
assert.equal(lunar2026.month, 1);
assert.equal(lunar2026.day, 1);
assert.equal(lunar2026.isLeap, false);
assert.equal(cnEngine.lunarToGregorian(2026, 1, 1), '2026-02-17');

const frag = cnEngine.gregorianToLunar('1900-01-01');
assert.equal(frag.year, 1899);
assert.equal(frag.month, 12);
assert.equal(frag.day, 1);

// Boundary checks & JS exception validation
for (const bad of [NaN, Infinity, -Infinity, 2024.5]) {
  assert.throws(() => cnEngine.getFestivalDates(bad, 'chinese_new_year_days'));
  assert.throws(() => cnEngine.lunarToGregorian(bad, 1, 1));
}
assert.throws(() => cnEngine.getFestivalDates(1899, 'chinese_new_year_days'));
assert.throws(() => cnEngine.getFestivalDates(2101, 'chinese_new_year_days'));
assert.throws(() => cnEngine.getFestivalDates(2024, 'unknown_festival'));
assert.throws(() => cnEngine.gregorianToLunar('1899-12-31'));
assert.throws(() => getChineseFestivalDates(2024, 'chinese_new_year_days', 'invalid-profile'));

// Immutability
assert.throws(() => { defaultChineseEngine.profile = FestivalProfile.CN_REFERENCE_UTC8; }, TypeError);
assert.throws(() => { lunar2026.year = 2025; }, TypeError);


// Recurrence rule integration for Chinese festivals
const cnyRecurrence = createRule({ id: 'chinese_new_year_days', type: 'chinese_festival' });
assert.deepEqual(engine.evaluateRule(2026, cnyRecurrence).map(v => v.date.iso), ['2026-02-17', '2026-02-18', '2026-02-19']);
const zongziUtc8Rule = createRule({ id: 'chinese_zongzi_festival', type: 'chinese_festival', monthPolicy: 'cn-reference-utc8' });
assert.deepEqual(engine.evaluateRule(2013, zongziUtc8Rule).map(v => v.date.iso), ['2013-06-12']);
const zongziArchiveRule = createRule({ id: 'chinese_zongzi_festival', type: 'chinese_festival', monthPolicy: 'archive-v1' });
assert.deepEqual(engine.evaluateRule(2013, zongziArchiveRule).map(v => v.date.iso), ['2013-06-13']);
assert.throws(() => createRule({ id: 'unknown_fest', type: 'chinese_festival' }));

console.log(JSON.stringify({
  days,
  years,
  occurrences,
  chineseFestivalEngine: 'verified',
  parity: 'passed',
  javascriptBoundaryChecks: 'passed',
}, null, 2));
