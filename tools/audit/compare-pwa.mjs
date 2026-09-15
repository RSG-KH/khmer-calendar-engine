// Copyright (c) 2026 RSG-KH | Apache-2.0 License
// The Python runner feeds this module to Node from the PWA working directory.
import fs from 'node:fs';
import { createHash } from 'node:crypto';
import { createServer } from 'vite';

const [baselinePath, referencePath, reportPath] = process.argv.slice(2);
const android = fs.readFileSync(baselinePath, 'utf8').trimEnd().split('\n');
const server = await createServer({ server: { middlewareMode: true }, appType: 'custom' });
try {
  const { KhmerCalendar, toEpochDay, fromEpochDay } = await server.ssrLoadModule('/src/domain/KhmerCalendar.ts');
  const { KhmerDateDetails } = await server.ssrLoadModule('/src/domain/KhmerDateDetails.ts');
  const { KhmerNewYear } = await server.ssrLoadModule('/src/domain/KhmerNewYear.ts');
  const { RecurringEvents } = await server.ssrLoadModule('/src/data/RecurringEvents.ts');
  const rows = [];
  const dailyHashes = new Map();
  for (let epoch = toEpochDay(1800, 1, 1); epoch <= toEpochDay(2200, 12, 31); epoch++) {
    const { year, month, day } = fromEpochDay(epoch);
    const date = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    const details = KhmerDateDetails.fromGregorian(year, month, day);
    const lunar = details.lunar;
    rows.push(['D', date, lunar.day, lunar.waxing, lunar.month, lunar.buddhistYear,
      lunar.monthLength, lunar.isHolyDay, lunar.isShavingDay, details.animalYear,
      details.sak, details.animalYearChangesToday, details.zodiac.signName.toUpperCase()].join('\t'));
    if (!dailyHashes.has(year)) dailyHashes.set(year, createHash('sha256'));
    dailyHashes.get(year).update(`${date},${lunar.day},${lunar.waxing ? 0 : 1},${lunar.month},${lunar.buddhistYear}\n`);
  }
  for (let year = 1800; year <= 2200; year++) {
    const newYear = KhmerNewYear.forYear(year);
    rows.push(['N', year, newYear.startDate, newYear.days].join('\t'));
    for (const event of RecurringEvents.forYear(year)) rows.push(['R', year, event.id, event.date].join('\t'));
  }
  rows.sort();
  const androidSet = new Set(android);
  const pwaSet = new Set(rows);
  const androidOnly = android.filter(row => !pwaSet.has(row));
  const pwaOnly = rows.filter(row => !androidSet.has(row));
  const sections = {};
  for (const [prefix, label] of [['D\t', 'dailyFields'], ['N\t', 'newYear'], ['R\t', 'recurringOccurrences']]) {
    sections[label] = {
      androidCount: android.filter(row => row.startsWith(prefix)).length,
      pwaCount: rows.filter(row => row.startsWith(prefix)).length,
      androidOnly: androidOnly.filter(row => row.startsWith(prefix)),
      pwaOnly: pwaOnly.filter(row => row.startsWith(prefix)),
    };
  }
  const pinned = fs.readFileSync(referencePath, 'utf8').split(/\r?\n/)
    .filter(line => line && !line.startsWith('#')).map(line => line.split('|'));
  const referenceDifferences = [];
  for (const [yearText, expectedHash, expectedStart] of pinned) {
    const year = Number(yearText);
    if (dailyHashes.get(year).digest('hex') !== expectedHash) referenceDifferences.push(`${year}: lunar fields`);
    if (KhmerNewYear.forYear(year).startDate !== expectedStart) referenceDifferences.push(`${year}: New Year start`);
  }
  const affectedRuleCounts = {};
  for (const row of sections.recurringOccurrences.androidOnly) {
    const id = row.split('\t')[2];
    affectedRuleCounts[id] = (affectedRuleCounts[id] ?? 0) + 1;
  }
  const report = {
    range: [1800, 2200],
    androidBaselineSha256: createHash('sha256').update(android.join('\n') + '\n').digest('hex'),
    pwaBaselineSha256: createHash('sha256').update(rows.join('\n') + '\n').digest('hex'),
    duplicateRows: { android: android.length - androidSet.size, pwa: rows.length - pwaSet.size },
    pinnedReference: { years: pinned.length, range: [pinned[0][0], pinned.at(-1)[0]], differences: referenceDifferences },
    affectedRuleCounts,
    sections,
  };
  fs.writeFileSync(reportPath, JSON.stringify(report, null, 2) + '\n');
  console.log(JSON.stringify({ ...report, sections: Object.fromEntries(Object.entries(sections).map(([key, value]) =>
    [key, { ...value, androidOnly: value.androidOnly.length, pwaOnly: value.pwaOnly.length }])) }, null, 2));
  if (androidOnly.length || pwaOnly.length || referenceDifferences.length || report.duplicateRows.android || report.duplicateRows.pwa) process.exitCode = 1;
} finally {
  await server.close();
}
