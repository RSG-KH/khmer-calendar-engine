// Names and object-argument convenience only. All calendar calculations live in Kotlin.
import {
  RecurrenceRule,
  ChineseLunisolarEngine,
  FestivalProfile,
  ChineseZodiacCalculator,
  WesternZodiacCalculator,
  GregorianDate
} from './kotlin/khmer-calendar-engine.mjs';
export * from './kotlin/khmer-calendar-engine.mjs';

const ruleFields = new Set(['id', 'type', 'month', 'day', 'waxing', 'offset', 'duration',
  'fromYear', 'throughYear', 'monthPolicy', 'occurrence']);

/** Convert a plain rule record to the shared, validated rule model. */
export function createRule(input) {
  if (!input || typeof input !== 'object' || Array.isArray(input)) throw new TypeError('Expected a rule object');
  for (const field of Object.keys(input)) if (!ruleFields.has(field)) throw new TypeError(`Unknown rule field: ${field}`);
  if (typeof input.id !== 'string' || typeof input.type !== 'string') throw new TypeError('Rule id and type must be strings');
  for (const field of ['month', 'day', 'offset', 'duration', 'fromYear', 'throughYear', 'occurrence']) {
    if (input[field] !== undefined && !Number.isInteger(input[field])) throw new TypeError(`${field} must be an integer`);
  }
  if (input.waxing !== undefined && typeof input.waxing !== 'boolean') throw new TypeError('waxing must be a boolean');
  if (input.monthPolicy !== undefined && typeof input.monthPolicy !== 'string') throw new TypeError('monthPolicy must be a string');
  return new RecurrenceRule(input.id, input.type, input.month, input.day, input.waxing,
    input.offset, input.duration, input.fromYear, input.throughYear, input.monthPolicy, input.occurrence);
}

/** Compute dates for a Chinese festival using an optional profile enum or string identifier. */
export function getChineseFestivalDates(year, festivalId, profile = 'archive-v1') {
  const p = typeof profile === 'string'
    ? FestivalProfile.Companion.fromId(profile)
    : (profile ?? FestivalProfile.ARCHIVE_V1);
  return new ChineseLunisolarEngine(p).getFestivalDates(year, festivalId);
}

// Kotlin exports an object as its class with a static getInstance(); the typed
// surface for JS callers is these named wrappers.
const zodiac = ChineseZodiacCalculator.getInstance();

/** Integer astronomical Julian Day Number for a civil Gregorian date. */
export function gregorianToJdn(year, month, day) {
  return zodiac.gregorianToJdn(year, month, day);
}

/** The Earthly Branch (Zodiac animal) for a civil hour (0..23). */
export function getHourBranch(hourOfDay) {
  return zodiac.getHourBranch(hourOfDay);
}

/** Civil day of the month (UTC+8) on which a sectional solar term (Jie) begins, 1900..2100. */
export function getSectionalTermDay(year, month) {
  return zodiac.getSectionalTermDay(year, month);
}

/**
 * Compute the astrological Year Pillar; the year changes at Lichun (early February).
 * Accepts either (year, month, day) or a GregorianDate instance.
 */
export function getYearPillar(...args) {
  if (args.length === 1 && args[0] instanceof GregorianDate) {
    return zodiac.getYearPillarForGregorianDate(args[0]);
  }
  if (args.length === 3) {
    return zodiac.getYearPillar(args[0], args[1], args[2]);
  }
  throw new TypeError(`getYearPillar expects 1 or 3 arguments, received ${args.length}`);
}

/**
 * Compute the astrological Month Pillar from the sectional solar terms and the
 * Five Tigers rule. Accepts either (year, month, day) or a GregorianDate instance.
 */
export function getMonthPillar(...args) {
  if (args.length === 1 && args[0] instanceof GregorianDate) {
    return zodiac.getMonthPillarForGregorianDate(args[0]);
  }
  if (args.length === 3) {
    return zodiac.getMonthPillar(args[0], args[1], args[2]);
  }
  throw new TypeError(`getMonthPillar expects 1 or 3 arguments, received ${args.length}`);
}

/**
 * Compute the Chinese Day Pillar (Ganzhi and Zodiac Animal).
 * Accepts either (year, month, day) or a GregorianDate instance.
 */
export function getDayPillar(...args) {
  if (args.length === 1 && args[0] instanceof GregorianDate) {
    return zodiac.getDayPillarForGregorianDate(args[0]);
  }
  if (args.length === 3) {
    return zodiac.getDayPillar(args[0], args[1], args[2]);
  }
  throw new TypeError(`getDayPillar expects 1 or 3 arguments, received ${args.length}`);
}

/**
 * Compute the Chinese Hour Pillar. Accepts:
 * - (dayStem: HeavenlyStem, hourOfDay: number)
 * - (date: GregorianDate, hourOfDay: number)
 * - (year: number, month: number, day: number, hourOfDay: number)
 */
export function getHourPillar(...args) {
  if (args.length === 2) {
    if (args[0] instanceof GregorianDate) {
      return zodiac.getHourPillarForGregorianDate(args[0], args[1]);
    }
    return zodiac.getHourPillar(args[0], args[1]);
  }
  if (args.length === 4) {
    return zodiac.getHourPillarForDate(args[0], args[1], args[2], args[3]);
  }
  throw new TypeError(`getHourPillar expects 2 or 4 arguments, received ${args.length}`);
}

/**
 * Compute the complete Four Pillars (BaZi) with the four clash branches.
 * Accepts (date: GregorianDate, hourOfDay) or (year, month, day, hourOfDay); 1900..2100.
 */
export function getFourPillars(...args) {
  if (args.length === 2 && args[0] instanceof GregorianDate) {
    return zodiac.getFourPillarsForGregorianDate(args[0], args[1]);
  }
  if (args.length === 4) {
    return zodiac.getFourPillars(args[0], args[1], args[2], args[3]);
  }
  throw new TypeError(`getFourPillars expects 2 or 4 arguments, received ${args.length}`);
}

const western = WesternZodiacCalculator.getInstance?.() ?? WesternZodiacCalculator;

/**
 * Compute the Western horoscope (Sun, Moon, Ascendant, Midheaven) using UTC inputs.
 * Accepts positional arguments:
 *   (yearUtc, monthUtc, dayUtc, hourUtc, minuteUtc, secondUtc = 0, latitudeDeg, longitudeDeg)
 * or an options object:
 *   ({ yearUtc, monthUtc, dayUtc, hourUtc, minuteUtc, secondUtc?, latitudeDeg, longitudeDeg })
 */
export function calculateHoroscopeUtc(...args) {
  if (args.length === 1 && typeof args[0] === 'object' && args[0] !== null) {
    const o = args[0];
    const secondUtc = o.secondUtc === undefined ? 0.0 : o.secondUtc;
    const lat = o.latitudeDeg !== undefined ? o.latitudeDeg : o.latitude;
    const lon = o.longitudeDeg !== undefined ? o.longitudeDeg : o.longitude;
    return western.calculateHoroscopeUtc(
      o.yearUtc, o.monthUtc, o.dayUtc,
      o.hourUtc, o.minuteUtc, secondUtc,
      lat, lon
    );
  }
  return western.calculateHoroscopeUtc(...args);
}

/**
 * Compute the Western horoscope (Sun, Moon, Ascendant, Midheaven) using local civil time and UTC offset.
 * Accepts positional arguments:
 *   (year, month, day, hour, minute, second = 0, utcOffsetHours, latitudeDeg, longitudeDeg)
 * or an options object:
 *   ({ year, month, day, hour, minute, second?, utcOffsetHours, latitude, longitude })
 */
export function calculateHoroscope(...args) {
  if (args.length === 1 && typeof args[0] === 'object' && args[0] !== null) {
    const o = args[0];
    const second = o.second === undefined ? 0.0 : o.second;
    const lat = o.latitudeDeg !== undefined ? o.latitudeDeg : o.latitude;
    const lon = o.longitudeDeg !== undefined ? o.longitudeDeg : o.longitude;
    return western.calculateHoroscope(
      o.year, o.month, o.day,
      o.hour, o.minute, second,
      o.utcOffsetHours,
      lat, lon
    );
  }
  return western.calculateHoroscope(...args);
}

