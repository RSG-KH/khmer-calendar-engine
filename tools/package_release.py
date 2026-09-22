"""Validate the engine version and assemble the tested GitHub release assets."""

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import tarfile
import zipfile


ROOT = Path(__file__).resolve().parent.parent
NUMBER = r"(?:0|[1-9][0-9]*)"
IDENTIFIER = rf"(?:{NUMBER}|[0-9A-Za-z-]*[A-Za-z-][0-9A-Za-z-]*)"
VERSION = re.compile(rf"{NUMBER}\.{NUMBER}\.{NUMBER}(?:-{IDENTIFIER}(?:\.{IDENTIFIER})*)?")
ENGINE_SOURCE = Path("src/commonMain/kotlin/com/rsgkh/calendar/engine/KhmerCalendarEngine.kt")


def engine_version(root: Path) -> str:
    build = (root / "build.gradle.kts").read_text(encoding="utf-8")
    match = re.search(r'^version = "([^"]+)"$', build, re.MULTILINE)
    if not match or not VERSION.fullmatch(match[1]):
        raise ValueError("Set a release version such as 0.1.0 or 0.2.0-rc.1 in build.gradle.kts")
    version = match[1]
    source = (root / ENGINE_SOURCE).read_text(encoding="utf-8")
    runtime = re.search(r'val version: String get\(\) = "([^"]+)"', source)
    if not runtime or runtime[1] != version:
        raise ValueError("The runtime engine version must match build.gradle.kts")
    fixture = (root / "verification/android/build.gradle.kts").read_text(encoding="utf-8")
    if f'com.rsgkh:khmer-calendar-engine-jvm:{version}"' not in fixture:
        raise ValueError("Update the Android consumer fixture to the engine version")
    return version


def check_tag(version: str, ref_type: str, ref_name: str) -> None:
    if ref_type != "tag" or ref_name != f"v{version}":
        raise ValueError(f"Release requires the existing tag v{version}; got {ref_type} {ref_name!r}")


def stage(root: Path, version: str) -> Path:
    build = root / "build"
    package = build / f"khmer-calendar-engine-{version}.tgz"
    with tarfile.open(package, "r:gz") as archive:
        manifest_file = archive.extractfile("package/package.json")
        if manifest_file is None:
            raise ValueError("The JavaScript package has no package.json")
        manifest = json.load(manifest_file)
        if manifest.get("name") != "khmer-calendar-engine" or manifest.get("version") != version:
            raise ValueError("The packed JavaScript package does not match the release version")

    repository = build / "repository"
    publications = []
    for name in ("khmer-calendar-engine", "khmer-calendar-engine-jvm", "khmer-calendar-engine-js"):
        directory = repository / "com/rsgkh" / name / version
        for extension in ("pom", "module"):
            if not (directory / f"{name}-{version}.{extension}").is_file():
                raise ValueError(f"Missing Maven {extension} for {name} {version}; publish the local repository first")
        publications.extend(sorted(directory.iterdir()))
    jvm = repository / "com/rsgkh/khmer-calendar-engine-jvm" / version / f"khmer-calendar-engine-jvm-{version}.jar"
    if not jvm.is_file():
        raise ValueError("The Android-compatible JVM library is missing")
    if any(path.is_symlink() or not path.is_file() for path in publications):
        raise ValueError("Maven publications must contain only regular files")

    output = build / "release" / version
    output.mkdir(parents=True, exist_ok=True)
    maven_name = f"khmer-calendar-engine-maven-{version}.zip"
    expected = {package.name, jvm.name, maven_name, "SHA256SUMS", "RELEASE_NOTES.md"}
    if any(path.name not in expected for path in output.iterdir()):
        raise ValueError("Unexpected files in the release output; use a clean build directory")
    shutil.copyfile(package, output / package.name)
    shutil.copyfile(jvm, output / jvm.name)
    with zipfile.ZipFile(output / maven_name, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for path in publications:
            archive.write(path, (Path("repository") / path.relative_to(repository)).as_posix())
        for name in ("LICENSE", "NOTICE"):
            archive.write(root / name, name)

    assets = sorted((package.name, jvm.name, maven_name))
    checksums = "".join(f"{hashlib.sha256((output / name).read_bytes()).hexdigest()}  {name}\n" for name in assets)
    (output / "SHA256SUMS").write_text(checksums, encoding="utf-8", newline="\n")
    (output / "RELEASE_NOTES.md").write_text(f"""Shared Khmer calendar engine {version}.

| Asset | Use |
| --- | --- |
| `khmer-calendar-engine-{version}.tgz` | JavaScript/TypeScript package for the manager and PWA |
| `khmer-calendar-engine-maven-{version}.zip` | Unpack and use its `repository/` directory with Gradle; includes dependency metadata and source archives |
| `khmer-calendar-engine-jvm-{version}.jar` | Android-compatible JVM library; dependency metadata is in the Maven archive |
| `SHA256SUMS` | SHA-256 hashes of the three packages |

The release workflow runs JVM and JavaScript tests, cross-target parity checks, the TypeScript/Vite/headless-browser consumer, and Android Java/Kotlin consumer tests with APK assembly before publishing.

Calculations run offline. Event catalogs and government holiday lists are maintained separately. The Khmer calendar supported range is 1800–2200; independent historical validation remains incomplete, including 1879/1897. New Year arrival times are exposed only as a clearly-labeled estimate (arrivalEstimate) whose minutes lie on a 24-minute lattice; published arrival clocks are maintained per year as source-tagged data in the manager, never computed here. The standalone ChineseZodiacCalculator provides Ganzhi (sexagenary) day and hour pillars over the proleptic Gregorian range 1..9999 in local civil time. See the tagged source's README and docs/references.md for evidence and limitations.
""", encoding="utf-8", newline="\n")
    return output


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", choices=("version", "stage"))
    args = parser.parse_args()
    version = engine_version(ROOT)
    if os.environ.get("RELEASE_BUILD") == "true":
        check_tag(version, os.environ.get("GITHUB_REF_TYPE", ""), os.environ.get("GITHUB_REF_NAME", ""))
    if args.command == "stage":
        print(stage(ROOT, version).relative_to(ROOT))
    else:
        commit = subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=ROOT, text=True).strip()
        if os.environ.get("GITHUB_OUTPUT"):
            with open(os.environ["GITHUB_OUTPUT"], "a", encoding="utf-8", newline="\n") as output:
                output.write(f"version={version}\ncommit={commit}\n")
        print(f"Engine {version}, commit {commit}")


if __name__ == "__main__":
    main()
