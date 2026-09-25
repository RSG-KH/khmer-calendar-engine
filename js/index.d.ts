import type {
  RecurrenceRule,
  FestivalProfile,
  GregorianDate,
  HeavenlyStem,
  EarthlyBranch,
  GanzhiPillar,
  FourPillars,
  WesternHoroscope,
  WesternZodiacSign,
  ZodiacPosition,
  AscendantStatus
} from './kotlin/khmer-calendar-engine.mjs';
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

export function getDayPillar(year: number, month: number, day: number): GanzhiPillar;
export function getDayPillar(date: GregorianDate): GanzhiPillar;

export function gregorianToJdn(year: number, month: number, day: number): number;
export function getHourBranch(hourOfDay: number): EarthlyBranch;

export function getHourPillar(dayStem: HeavenlyStem, hourOfDay: number): GanzhiPillar;
export function getHourPillar(date: GregorianDate, hourOfDay: number): GanzhiPillar;
export function getHourPillar(year: number, month: number, day: number, hourOfDay: number): GanzhiPillar;

export function getSectionalTermDay(year: number, month: number): number;

export function getYearPillar(year: number, month: number, day: number): GanzhiPillar;
export function getYearPillar(date: GregorianDate): GanzhiPillar;

export function getMonthPillar(year: number, month: number, day: number): GanzhiPillar;
export function getMonthPillar(date: GregorianDate): GanzhiPillar;

export function getFourPillars(year: number, month: number, day: number, hourOfDay: number): FourPillars;
export function getFourPillars(date: GregorianDate, hourOfDay: number): FourPillars;

export interface HoroscopeUtcOptions {
  yearUtc: number;
  monthUtc: number;
  dayUtc: number;
  hourUtc: number;
  minuteUtc: number;
  secondUtc?: number;
  latitudeDeg?: number;
  latitude?: number;
  longitudeDeg?: number;
  longitude?: number;
}

export interface HoroscopeOptions {
  year: number;
  month: number;
  day: number;
  hour: number;
  minute: number;
  second?: number;
  utcOffsetHours: number;
  latitudeDeg?: number;
  latitude?: number;
  longitudeDeg?: number;
  longitude?: number;
}

export function calculateHoroscopeUtc(options: HoroscopeUtcOptions): WesternHoroscope;
export function calculateHoroscopeUtc(
  yearUtc: number,
  monthUtc: number,
  dayUtc: number,
  hourUtc: number,
  minuteUtc: number,
  secondUtc: number | undefined,
  latitudeDeg: number,
  longitudeDeg: number
): WesternHoroscope;

export function calculateHoroscope(options: HoroscopeOptions): WesternHoroscope;
export function calculateHoroscope(
  year: number,
  month: number,
  day: number,
  hour: number,
  minute: number,
  second: number | undefined,
  utcOffsetHours: number,
  latitudeDeg: number,
  longitudeDeg: number
): WesternHoroscope;

