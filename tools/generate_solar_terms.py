#!/usr/bin/env python3
"""Generate and validate the packed sectional solar term (Jie) table used by
ChineseZodiacCalculator (1900..2100, China Standard reference meridian UTC+8).

Table encoding: one 24-bit little-endian word per year; 2 bits per Gregorian
month holding (day - BASE_DAY), 0..3; 3 bytes per year, 603 bytes total.

Data provenance and acceptance evidence (audited 22-23 September 2026):

- Base days come from an astronomical recomputation: pyephem (libastro)
  apparent geocentric solar longitude of date, bisected to each term's target
  longitude (Xiaohan 285, Lichun 315, Jingzhe 345, Qingming 15, Lixia 45,
  Mangzhong 75, Xiaoshu 105, Liqiu 135, Bailu 165, Hanlu 195, Lidong 225,
  Daxue 255 degrees), converted to UTC+8 civil days. Use `generate` to
  recompute; it requires `pip install ephem`.
- Terms whose recomputed instant falls within ~15 minutes of UTC+8 midnight
  can differ by one day from the published record. Every such case in
  1900..2100 was checked against the Hong Kong Observatory Gregorian-lunar
  calendar tables (30 year files) and the repo's own CN_TABLE. The committed
  table stores the published day: 15 terms move one day later than the raw
  recomputation (PUBLISHED_PLUS_ONE below); the remaining near-midnight cases
  keep the recomputed day (PUBLISHED_SAME_DAY below, also verified).
- The April (Qingming) bits must equal ChineseLunisolarEngine's CN_TABLE
  Qingming bits for all 201 years; `check` enforces this and fails loudly on
  any mismatch, out-of-range offset, or implausible day.

Commands:
  python tools/generate_solar_terms.py check      # stdlib-only validation (CI-safe)
  python tools/generate_solar_terms.py generate   # recompute via pyephem and diff
"""

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CALCULATOR_KT = ROOT / "src/commonMain/kotlin/com/rsgkh/calendar/engine/ChineseZodiacCalculator.kt"
ENGINE_KT = ROOT / "src/commonMain/kotlin/com/rsgkh/calendar/engine/ChineseLunisolarEngine.kt"

MIN_YEAR, MAX_YEAR = 1900, 2100
BASE_DAYS = [4, 3, 4, 4, 4, 4, 6, 6, 6, 7, 6, 6]
JIE_LONGITUDES = [285.0, 315.0, 345.0, 15.0, 45.0, 75.0, 105.0, 135.0, 165.0, 195.0, 225.0, 255.0]
TERM_NAMES = ["Xiaohan", "Lichun", "Jingzhe", "Qingming", "Lixia", "Mangzhong",
              "Xiaoshu", "Liqiu", "Bailu", "Hanlu", "Lidong", "Daxue"]
# Plausible civil-day windows at UTC+8 (inclusive), from the verified table.
DAY_WINDOWS = [(4, 7), (3, 5), (4, 7), (4, 6), (4, 7), (4, 7),
               (6, 8), (6, 9), (6, 9), (7, 9), (6, 8), (6, 8)]

# Published almanac day is one day after the raw UTC+8 recomputation
# (HKO-verified; 1943-04 also equals the repo CN_TABLE Qingming day).
PUBLISHED_PLUS_ONE = {
    1911: [(5, 7)], 1912: [(1, 7), (10, 9)], 1943: [(4, 6)], 1964: [(6, 6)],
    1980: [(2, 5)], 1981: [(3, 6)], 1982: [(1, 6)], 2014: [(3, 6)],
    2016: [(7, 7)], 2020: [(12, 7)], 2040: [(10, 8)], 2080: [(3, 5)],
    2084: [(6, 5)], 2097: [(5, 5)],
}
# Near-midnight terms whose published day equals the raw recomputed day
# (HKO-verified; kept here so every borderline case is pinned by evidence).
PUBLISHED_SAME_DAY = [
    (1910, 2, 5), (1910, 4, 6), (1915, 3, 6), (1915, 8, 8), (1917, 12, 7),
    (1925, 7, 8), (1927, 9, 8), (1935, 6, 6), (1940, 5, 6), (1944, 8, 8),
    (1947, 2, 4), (1948, 3, 5), (1950, 12, 8), (1976, 4, 4), (1989, 9, 7),
    (2045, 7, 7), (2047, 3, 6), (2051, 9, 7), (2053, 12, 7),
]


def read_kotlin_hex(path: Path, marker: str) -> str:
    """Extract the packed hex after `marker` (a regex) up to the closing paren."""
    text = path.read_text(encoding="utf-8")
    anchor = re.search(marker + r"\(", text, re.S)
    if not anchor:
        raise ValueError(f"Table marker not found in {path.name}: {marker}")
    close = text.index(")", anchor.end())
    hex_str = "".join(re.findall(r'"([0-9a-f]+)"', text[anchor.end():close]))
    if len(hex_str) != (MAX_YEAR - MIN_YEAR + 1) * 6:
        raise ValueError(f"Unexpected table length in {path.name}: {len(hex_str)}")
    return hex_str


CN_TABLE_MARKER = r"private val CN_TABLE = decodeHex"
JIE_TABLE_MARKER = r"private val SOLAR_JIE_TABLE: ByteArray by lazy \{\s*decodeJieHex"


def decode_words(hex_str: str) -> dict:
    words = {}
    for i, y in enumerate(range(MIN_YEAR, MAX_YEAR + 1)):
        b = i * 6
        words[y] = int(hex_str[b:b + 2], 16) | int(hex_str[b + 2:b + 4], 16) << 8 | int(hex_str[b + 4:b + 6], 16) << 16
    return words


def decode_days(hex_str: str) -> dict:
    days = {}
    for i, y in enumerate(range(MIN_YEAR, MAX_YEAR + 1)):
        b = i * 6
        w = (int(hex_str[b:b + 2], 16) | int(hex_str[b + 2:b + 4], 16) << 8 | int(hex_str[b + 4:b + 6], 16) << 16)
        for m in range(1, 13):
            days[(y, m)] = BASE_DAYS[m - 1] + ((w >> ((m - 1) * 2)) & 3)
    return days


def check() -> int:
    table = decode_days(read_kotlin_hex(CALCULATOR_KT, JIE_TABLE_MARKER))
    # CN_TABLE packs Qingming as 4 + bits 17..18 of its own per-year word.
    cn_qingming = {y: 4 + ((w >> 17) & 3) for y, w in decode_words(read_kotlin_hex(ENGINE_KT, CN_TABLE_MARKER)).items()}
    errors = []
    for (y, m), day in sorted(table.items()):
        lo, hi = DAY_WINDOWS[m - 1]
        if not lo <= day <= hi:
            errors.append(f"{y}-{m:02d} {TERM_NAMES[m-1]}: day {day} outside {lo}..{hi}")
        if m == 4 and day != cn_qingming[y]:
            errors.append(f"{y}-04 Qingming: {day} != CN_TABLE {cn_qingming[y]}")
    for y, months in PUBLISHED_PLUS_ONE.items():
        for m, day in months:
            if table[(y, m)] != day:
                errors.append(f"{y}-{m:02d}: {table[(y, m)]} != published {day}")
    for y, m, day in PUBLISHED_SAME_DAY:
        if table[(y, m)] != day:
            errors.append(f"{y}-{m:02d}: {table[(y, m)]} != published {day}")
    for y, m, day in [(2024, 2, 4), (2025, 2, 3), (2026, 2, 4), (2026, 9, 7), (2025, 1, 5)]:
        if table[(y, m)] != day:
            errors.append(f"{y}-{m:02d}: {table[(y, m)]} != expected {day}")
    if errors:
        print("SOLAR TERM TABLE CHECK FAILED:")
        for e in errors:
            print("  -", e)
        return 1
    n_anchors = sum(len(v) for v in PUBLISHED_PLUS_ONE.values()) + len(PUBLISHED_SAME_DAY)
    print(f"solar term table check: OK ({(MAX_YEAR - MIN_YEAR + 1) * 12} terms, "
          f"Qingming == CN_TABLE 201/201, {n_anchors} published anchors)")
    return 0


def generate() -> int:
    try:
        import ephem
    except ImportError:
        print("generate requires pyephem: pip install ephem")
        return 2
    import datetime as dt
    import math

    eph_base = dt.datetime(1899, 12, 31, 12, 0)

    def sun_lon(t):
        return math.degrees(ephem.Ecliptic(ephem.Sun(t), epoch=t).lon)

    def wrap180(x):
        return (x + 180.0) % 360.0 - 180.0

    def solve(year, month, target, guess):
        t0 = ephem.Date(dt.datetime(year, month, guess, 0, 0) - dt.timedelta(days=1))
        t1 = ephem.Date(t0 + 7.0)
        step = 1.0 / 24
        tp, fp = t0, wrap180(sun_lon(t0) - target)
        t = ephem.Date(t0 + step)
        while t <= t1:
            f = wrap180(sun_lon(t) - target)
            if fp * f <= 0 and abs(fp) < 90 and abs(f) < 90:
                lo, hi, flo = tp, t, fp
                for _ in range(44):
                    mid = ephem.Date((lo + hi) / 2)
                    fm = wrap180(sun_lon(mid) - target)
                    if flo * fm <= 0:
                        hi = mid
                    else:
                        lo, flo = mid, fm
                return ephem.Date((lo + hi) / 2)
            tp, fp = t, f
            t = ephem.Date(t + step)
        raise RuntimeError(f"no crossing {year}-{month:02d}")

    raw, margin = {}, []
    for y in range(MIN_YEAR, MAX_YEAR + 1):
        for m in range(1, 13):
            b = eph_base + dt.timedelta(days=float(solve(y, m, JIE_LONGITUDES[m - 1], BASE_DAYS[m - 1]))) + dt.timedelta(hours=8)
            if (b.year, b.month) != (y, m):
                raise RuntimeError(f"term outside its month: {y}-{m:02d} -> {b}")
            raw[(y, m)] = b.day
            frac_min = (b - dt.datetime(b.year, b.month, b.day)).total_seconds() / 60
            if min(frac_min, 1440 - frac_min) < 30:
                margin.append((y, m, b.day, round(frac_min, 1)))
            if not 0 <= b.day - BASE_DAYS[m - 1] <= 3:
                print(f"HARD FAIL: {y}-{m:02d} day {b.day} out of base+0..3")
                return 1

    final = dict(raw)
    for y, months in PUBLISHED_PLUS_ONE.items():
        for m, _day in months:
            final[(y, m)] += 1

    committed = decode_days(read_kotlin_hex(CALCULATOR_KT, JIE_TABLE_MARKER))
    diffs = [(k, raw[k], committed[k]) for k in sorted(committed) if committed[k] != final[k]]
    print(f"ephemeris: {(MAX_YEAR - MIN_YEAR + 1) * 12} terms; near-midnight (<30 min): {len(margin)}")
    print(f"published-day adjustments applied: {sum(len(v) for v in PUBLISHED_PLUS_ONE.values())}")
    if diffs:
        print(f"COMMITTED TABLE DIFFERS FROM REGENERATION in {len(diffs)} places:")
        for (y, m), r, c in diffs:
            print(f"  {y}-{m:02d} {TERM_NAMES[m-1]}: committed {c}, regenerated {r}")
        return 1
    print("committed table matches regeneration exactly")
    return 0


def main() -> int:
    mode = sys.argv[1] if len(sys.argv) > 1 else "check"
    if mode == "check":
        return check()
    if mode == "generate":
        return generate()
    print(f"unknown mode: {mode}")
    return 2


if __name__ == "__main__":
    sys.exit(main())
