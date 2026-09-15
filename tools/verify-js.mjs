// Compare the actual packaged JavaScript library with the JVM test output.
// This is a cross-target consistency check, not independent calendar evidence.
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { pathToFileURL } from 'node:url';

const packagePath = resolve(process.argv[2] ?? 'build/npm/index.mjs');
const baselinePath = resolve(process.argv[3] ?? 'build/reports/engine-baseline.tsv');
const { KhmerCalendarEngine, GregorianDate, RecurrenceRule, EventDateOverride, createRule } = await import(pathToFileURL(packagePath).href);
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
    assert.equal(['N', year, v.start.iso, v.days].join('\t'), row);
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
// A caller must not be able to corrupt shared caches or bypass validated rule inputs.
assert.throws(() => { engine.newYear(2012).start.day = 14; }, TypeError);
assert.throws(() => { engine.newYear(2012).days = 9; }, TypeError);
assert.throws(() => { rules.lent.monthPolicy = 'typo'; }, TypeError);
assert.equal(engine.newYear(2012).start.iso, '2012-04-13');
const override = new EventDateOverride('fixed', 2024, [new GregorianDate(2024, 5, 15)], 'notice-2024', 'Published correction');
assert.equal(engine.evaluateRule(2024, rules.fixed, override)[0].sourceId, 'notice-2024');
assert.equal(engine.evaluateRule(2024, rules.fixed, new EventDateOverride('fixed', 2024, [], 'notice', 'Cancelled')).length, 0);
console.log(JSON.stringify({ days, years, occurrences, parity: 'passed', javascriptBoundaryChecks: 'passed' }, null, 2));
