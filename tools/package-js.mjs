import { cpSync, existsSync, mkdirSync, readFileSync, readdirSync, rmSync, writeFileSync } from 'node:fs';
import { dirname, join, relative, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const distribution = join(root, 'build/dist/js/productionLibrary');
const target = resolve(root, 'build/npm');
if (relative(root, target) !== join('build', 'npm')) throw new Error('Unexpected package output directory');
if (!existsSync(join(distribution, 'khmer-calendar-engine.mjs'))) {
  throw new Error('Build jsNodeProductionLibraryDistribution before packaging JavaScript');
}
// Delete only this repository's generated npm output, never a caller-provided path.
rmSync(target, { recursive: true, force: true });
mkdirSync(target, { recursive: true });
cpSync(distribution, join(target, 'kotlin'), { recursive: true });
// TypeScript resolves .mjs imports through .d.mts declarations.
for (const file of readdirSync(join(target, 'kotlin'))) if (file.endsWith('.d.ts')) {
  cpSync(join(target, 'kotlin', file), join(target, 'kotlin', file.replace(/\.d\.ts$/, '.d.mts')));
}
for (const file of ['index.mjs', 'index.d.ts']) cpSync(join(root, 'js', file), join(target, file));
for (const file of ['LICENSE', 'NOTICE', 'README.md']) cpSync(join(root, file), join(target, file));
cpSync(join(root, 'docs'), join(target, 'docs'), { recursive: true });
mkdirSync(join(target, 'verification'));
cpSync(join(root, 'verification/README.md'), join(target, 'verification/README.md'));
const version = readFileSync(join(root, 'build.gradle.kts'), 'utf8').match(/^version = "([^"]+)"/m)?.[1];
if (!version) throw new Error('Missing engine version');
writeFileSync(join(target, 'package.json'), JSON.stringify({
  name: 'khmer-calendar-engine', version,
  description: 'Shared Khmer calendar calculations and recurrence rules',
  type: 'module', main: './index.mjs', types: './index.d.ts',
  exports: { '.': { types: './index.d.ts', import: './index.mjs' } },
  files: ['index.mjs', 'index.d.ts', 'kotlin', 'docs', 'verification/README.md', 'LICENSE', 'NOTICE', 'README.md'],
  license: '(Apache-2.0 AND MIT)',
}, null, 2) + '\n');
console.log(`Packaged ${relative(root, target)}`);
