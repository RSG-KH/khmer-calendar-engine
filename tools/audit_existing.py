# Copyright (c) 2026 RSG-KH | Apache-2.0 License
"""Compare existing Android and PWA algorithms without copying either implementation."""
import argparse
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--android', type=Path, required=True)
    parser.add_argument('--pwa', type=Path, required=True)
    parser.add_argument('--skip-build', action='store_true', help='Use already compiled Android debug classes.')
    args = parser.parse_args()
    android, pwa = args.android.resolve(), args.pwa.resolve()
    root = Path(__file__).resolve().parents[1]
    output = root / 'build/audit'
    output.mkdir(parents=True, exist_ok=True)
    if not args.skip_build:
        wrapper = android / ('gradlew.bat' if os.name == 'nt' else 'gradlew')
        subprocess.run([str(wrapper), ':app:compileDebugKotlin', '--console=plain'], cwd=android, check=True)
    classes = android / 'app/build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes'
    if not (classes / 'com/rsgkh/calendar/domain/KhmerCalendar.class').is_file():
        raise SystemExit('Android debug classes not found. Check the build output layout or rebuild first.')
    gradle_cache = Path(os.environ.get('GRADLE_USER_HOME', str(Path.home() / '.gradle')))
    jars = list((gradle_cache / 'caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib').glob('*/*/kotlin-stdlib-*.jar'))
    jars = [path for path in jars if not path.name.endswith(('-sources.jar', '-javadoc.jar'))]
    if not jars:
        raise SystemExit('Kotlin standard library not found in the Gradle cache.')
    stdlib = max(jars, key=lambda path: tuple(int(part) for part in path.parents[1].name.split('.') if part.isdigit()))
    java_home = os.environ.get('JAVA_HOME')
    java = str(Path(java_home) / 'bin' / ('java.exe' if os.name == 'nt' else 'java')) if java_home else shutil.which('java')
    node = shutil.which('node')
    if not java or not node:
        raise SystemExit('A JDK and Node.js must be available.')
    classpath = os.pathsep.join(map(str, [classes, android / 'app/src/main/resources', stdlib]))
    baseline = output / 'android-baseline.tsv'
    subprocess.run([java, '--class-path', classpath, str(root / 'tools/audit/ExportAndroidBaseline.java'), str(baseline)], check=True)
    report = output / 'comparison.json'
    report.unlink(missing_ok=True)
    result = subprocess.run([node, '--input-type=module', '-', str(baseline),
        str(android / 'app/src/test/resources/momentkh-reference.txt'), str(report)],
        cwd=pwa, input=(root / 'tools/audit/compare-pwa.mjs').read_text(encoding='utf-8'), encoding='utf-8')
    if report.is_file():
        data = json.loads(report.read_text(encoding='utf-8'))
        data['sourceRevisions'] = {name: subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=path, text=True).strip()
            for name, path in [('android', android), ('pwa', pwa)]}
        data['sourceWorkingTreeStatus'] = {name: subprocess.check_output(['git', 'status', '--short'], cwd=path, text=True).strip()
            for name, path in [('android', android), ('pwa', pwa)]}
        report.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
        print(f'Full report: {report}', flush=True)
    return result.returncode


if __name__ == '__main__':
    sys.exit(main())
