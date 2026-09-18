// Names and object-argument convenience only. All calendar calculations live in Kotlin.
import { RecurrenceRule, ChineseLunisolarEngine, FestivalProfile } from './kotlin/khmer-calendar-engine.mjs';
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
