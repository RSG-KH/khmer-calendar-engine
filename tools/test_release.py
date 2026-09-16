"""Exercise release failures and archives without publishing or touching real build outputs."""

import hashlib
import io
import json
from pathlib import Path
import tarfile
import tempfile
import unittest
import zipfile

from package_release import ENGINE_SOURCE, check_tag, engine_version, stage


class ReleaseTests(unittest.TestCase):
    def setUp(self):
        self.workspace = tempfile.TemporaryDirectory(prefix="engine-release-test-")
        self.addCleanup(self.workspace.cleanup)
        self.root = Path(self.workspace.name)
        self.write("build.gradle.kts", 'version = "0.1.0"\n')
        self.write(ENGINE_SOURCE, 'val version: String get() = "0.1.0"\n')
        self.write("verification/android/build.gradle.kts", 'implementation("com.rsgkh:khmer-calendar-engine-jvm:0.1.0")\n')
        self.write("LICENSE", "Test license")
        self.write("NOTICE", "Test notice")
        self.pack("0.1.0")
        self.publications("0.1.0")

    def write(self, relative, content):
        path = self.root / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8")

    def pack(self, version):
        path = self.root / "build/khmer-calendar-engine-0.1.0.tgz"
        path.parent.mkdir(parents=True, exist_ok=True)
        manifest = json.dumps({"name": "khmer-calendar-engine", "version": version}).encode()
        with tarfile.open(path, "w:gz") as archive:
            entry = tarfile.TarInfo("package/package.json")
            entry.size = len(manifest)
            archive.addfile(entry, io.BytesIO(manifest))

    def publications(self, version):
        for name in ("khmer-calendar-engine", "khmer-calendar-engine-jvm", "khmer-calendar-engine-js"):
            for suffix in ("pom", "module", "jar", "sources.jar"):
                self.write(f"build/repository/com/rsgkh/{name}/{version}/{name}-{version}.{suffix}", "fixture")

    def test_release_requires_matching_tag(self):
        check_tag("0.1.0", "tag", "v0.1.0")
        check_tag("0.2.0-rc.1", "tag", "v0.2.0-rc.1")
        for kind, name in (("branch", "v0.1.0"), ("tag", "v0.2.0"), ("tag", "latest")):
            with self.subTest(kind=kind, name=name), self.assertRaises(ValueError):
                check_tag("0.1.0", kind, name)

    def test_runtime_and_consumer_versions_must_match(self):
        self.assertEqual(engine_version(self.root), "0.1.0")
        self.write(ENGINE_SOURCE, 'val version: String get() = "0.0.9"')
        with self.assertRaisesRegex(ValueError, "runtime engine version"):
            engine_version(self.root)
        self.write(ENGINE_SOURCE, 'val version: String get() = "0.1.0"')
        self.write("verification/android/build.gradle.kts", 'implementation("com.rsgkh:khmer-calendar-engine-jvm:0.0.9")')
        with self.assertRaisesRegex(ValueError, "Android consumer"):
            engine_version(self.root)

    def test_invalid_versions_cannot_be_used_as_paths(self):
        for version in ("../0.1.0", "01.2.3", "0.1.0-01", "0.1.0+local"):
            with self.subTest(version=version):
                self.write("build.gradle.kts", f'version = "{version}"\n')
                with self.assertRaisesRegex(ValueError, "Set a release version"):
                    engine_version(self.root)

    def test_mismatched_package_is_rejected(self):
        self.pack("0.2.0")
        with self.assertRaisesRegex(ValueError, "packed JavaScript package"):
            stage(self.root, "0.1.0")

    def test_maven_metadata_is_required(self):
        (self.root / "build/repository/com/rsgkh/khmer-calendar-engine-jvm/0.1.0/khmer-calendar-engine-jvm-0.1.0.pom").unlink()
        with self.assertRaisesRegex(ValueError, "Missing Maven pom"):
            stage(self.root, "0.1.0")

    def test_archives_include_only_current_version_and_checksums_match(self):
        self.publications("0.0.9")
        output = stage(self.root, "0.1.0")
        checksums = (output / "SHA256SUMS").read_text().splitlines()
        self.assertEqual(len(checksums), 3)
        for line in checksums:
            digest, name = line.split("  ")
            self.assertEqual(hashlib.sha256((output / name).read_bytes()).hexdigest(), digest)
        with zipfile.ZipFile(output / "khmer-calendar-engine-maven-0.1.0.zip") as archive:
            self.assertIsNone(archive.testzip())
            self.assertTrue(all("0.0.9" not in name for name in archive.namelist()))
            self.assertIn("LICENSE", archive.namelist())
            self.assertIn("NOTICE", archive.namelist())
            self.assertIn("repository/com/rsgkh/khmer-calendar-engine-jvm/0.1.0/khmer-calendar-engine-jvm-0.1.0.pom", archive.namelist())
        self.assertEqual(stage(self.root, "0.1.0"), output)


if __name__ == "__main__":
    unittest.main()
