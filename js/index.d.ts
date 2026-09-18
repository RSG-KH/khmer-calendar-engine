import type { RecurrenceRule, FestivalProfile } from './kotlin/khmer-calendar-engine.mjs';
export * from './kotlin/khmer-calendar-engine.mjs';

interface RuleBase {
  id: string;
  offset?: number;
  duration?: number;
  fromYear?: number;
  throughYear?: number;
}
export type RuleInput = RuleBase & (
  | { type: 'solar'; month: number; day: number }
  | { type: 'khmer_lunar'; month: number; day: number; waxing: boolean; monthPolicy?: 'exact' | 'ordinary_or_second_asadh' }
  | { type: 'solar_nth_weekday'; month: number; day: number; occurrence: number }
  | { type: 'new_year_first' | 'new_year_middle' | 'new_year_last' }
  | { type: 'chinese_festival'; monthPolicy?: 'cn-reference-utc8' | 'archive-v1' }
);
export function createRule(input: RuleInput): RecurrenceRule;

export type FestivalProfileId = 'archive-v1' | 'cn-reference-utc8' | 'cn-lunar-utc7-solar' | 'local-utc7-model';
export function getChineseFestivalDates(
  year: number,
  festivalId: string,
  profile?: FestivalProfile | FestivalProfileId
): Array<string>;
