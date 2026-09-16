# Engine releases

Push a version tag to build, test and publish a GitHub release with installable packages. Creating an empty release in the GitHub UI is unnecessary.

## Publish a version

1. Set the same version in `build.gradle.kts`, `KhmerCalendarEngine.version`, and the dependency in `verification/android/build.gradle.kts`. Use `0.1.0` for the first release. Versions such as `0.2.0-rc.1` produce GitHub prereleases.
2. Commit and push the changes, including the release workflow. The tag must contain that workflow. Check that **Verify engine** passes for the commit.
3. Create and push a tag for that commit:

   ```sh
   git tag -a v0.1.0 -m "Khmer Calendar Engine 0.1.0"
   git push origin v0.1.0
   ```

4. **Release engine** calls the same verification workflow, then publishes the tested assets. The tag must exactly match `v` plus the package version; a mismatch fails the run. Publishing also checks that the remote tag still points to the tested commit.

The workflow uses GitHub's built-in `GITHUB_TOKEN`. Only the publishing job receives `contents: write`; the build and tests have read access. No npm or Maven registry account or additional token is needed.

## Release assets

| Asset | Purpose |
| --- | --- |
| `khmer-calendar-engine-VERSION.tgz` | Compiled JavaScript package, TypeScript declarations, documentation and license notices. Used by the manager and PWA. |
| `khmer-calendar-engine-jvm-VERSION.jar` | Android-compatible JVM library targeting Java 11 bytecode. |
| `khmer-calendar-engine-maven-VERSION.zip` | Maven repository for this version, including JVM/JS/multiplatform artifacts, dependency metadata, source archives and license notices. Use this for Gradle dependency resolution. |
| `SHA256SUMS` | SHA-256 hashes of the three packages. |

The workflow checks JVM and JavaScript tests, cross-target parity, and the TypeScript/Vite/headless-browser consumer. It does not run the separate Android SDK consumer fixture on GitHub; that fixture's local verification is documented in [consumer verification](../verification/README.md).

Assets are also available as the **release-assets** Actions artifact after a successful verification run. Release archives are staged locally under `build/release/VERSION/`.

## Install a published release

After `v0.1.0` is published, a JavaScript consumer can run:

```sh
npm install --save-exact https://github.com/RSG-KH/khmer-calendar-engine/releases/download/v0.1.0/khmer-calendar-engine-0.1.0.tgz
```

Commit the updated `package.json` and lockfile in that consumer. The package downloads during dependency installation; the built application calculates offline. Adopt a newer release by changing the versioned URL and running the consumer's checks.

For Android, download the Maven ZIP and check its hash against `SHA256SUMS`. Extract it into a project-controlled directory, add the extracted `repository/` directory as a Gradle Maven repository, and depend on `com.rsgkh:khmer-calendar-engine-jvm:0.1.0`. Gradle resolves the Kotlin standard library using the included metadata and Maven Central. A standalone JAR requires its dependencies to be managed separately.

This workflow publishes GitHub release assets. It does not publish to npm, Maven Central or GitHub Packages, and it does not change consumer projects automatically.

## Retry a failed release

Rerun the failed Actions run, or run **Release engine** manually with the existing version tag selected. Selecting a branch fails the tag check.

The workflow does not replace an existing release or overwrite published assets. If uploading was interrupted and left an incomplete **draft**, remove that draft and rerun the workflow, keeping the tag. Changes to a published engine require a new version and tag.

## Local checks

From the repository root (use `gradlew.bat` on Windows):

```sh
python3 -m unittest discover -s tools -p 'test_release.py'
python3 tools/package_release.py version
bash ./gradlew check jvmJar publishAllPublicationsToLocalBuildRepository
npm pack ./build/npm --pack-destination ./build
python3 tools/package_release.py stage
```

The packaging check rejects inconsistent source/runtime versions, a mismatched JavaScript package and missing Maven metadata. It includes only the current version's Maven files and writes checksums for the exact release bytes.
