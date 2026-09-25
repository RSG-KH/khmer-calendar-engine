// Retain the review's boundary and malformed-input probes in the normal build.
import { spawnSync } from 'node:child_process';
import { mkdirSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const packagePath = resolve(root, 'build/npm/index.mjs');
const reports = resolve(root, 'build/reports');
mkdirSync(reports, { recursive: true });

for (const [name, script, expectedTotal] of [
  ['carry', 'verify-release-carry.mjs', 38],
  ['input-guards', 'round3-js-probe.mjs', 281],
]) {
  const result = spawnSync(process.execPath, [resolve(root, 'tools/tests', script), packagePath], {
    cwd: root, encoding: 'utf8', maxBuffer: 8 * 1024 * 1024,
  });
  if (result.error) throw result.error;
  const reportPath = resolve(reports, `western-${name}.json`);
  writeFileSync(reportPath, result.stdout);
  if (result.status !== 0) {
    throw new Error(`${script} failed (exit ${result.status}); see ${reportPath}\n${result.stderr}`);
  }
  const report = JSON.parse(result.stdout);
  if (report.summary.total !== expectedTotal || report.summary.passed !== expectedTotal || report.summary.failed !== 0) {
    throw new Error(`${script} has an unexpected result: ${JSON.stringify(report.summary)}`);
  }
  console.log(`${name}: ${report.summary.passed}/${report.summary.total} passed (${reportPath})`);
}
