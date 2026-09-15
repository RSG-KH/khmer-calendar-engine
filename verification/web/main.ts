import { KhmerCalendarEngine, GregorianDate, EventDateOverride, createRule, type CalendarDate, type RuleInput } from 'khmer-calendar-engine';

const engine = new KhmerCalendarEngine();
const ruleInput: RuleInput = {
  id: 'lent', type: 'khmer_lunar', month: 7, day: 1, waxing: false,
  monthPolicy: 'ordinary_or_second_asadh',
};
const rule = createRule(ruleInput);
const result: CalendarDate = engine.fromGregorian(2026, 7, 30);
const dates = engine.evaluateRule(2026, rule).map(value => value.date.iso);
if (engine.newYear(2012).start.iso !== '2012-04-13') throw new Error('Incorrect 2012 date');
if (dates.join() !== '2026-07-30' || result.lunar.month !== 13) throw new Error('Incorrect second-Asadh recurrence');
if (engine.toGregorian(result.lunar.buddhistYear, 13, 1, false).iso !== '2026-07-30') throw new Error('Inverse conversion failed');
const correction = new EventDateOverride('lent', 2026, [new GregorianDate(2026, 7, 31)], 'example-notice', 'Verification only');
if (engine.evaluateRule(2026, rule, correction)[0].sourceId !== 'example-notice') throw new Error('Source reference lost');
document.querySelector('#result')!.textContent = JSON.stringify({
  status: 'passed', version: engine.version, newYear2012: engine.newYear(2012).start.iso,
  secondAsadh2026: dates[0], buddhistYear: result.lunar.buddhistYear,
}, null, 2);
