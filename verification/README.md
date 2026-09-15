# Consumer verification

These small projects consume the built artifacts without modifying production applications. They contain no calendar algorithm.

## Android

First run the engine's `publishAllPublicationsToLocalBuildRepository` task. The fixture requires Android SDK 37 and an AGP 9.4-compatible Gradle/JDK installation (verified with Gradle 9.6 and JDK 25). Set `ANDROID_HOME` to your SDK location or create an ignored `android/local.properties`.

```text
gradle -p verification/android testDebugUnitTest assembleDebug
```

The unit test calls the JVM artifact from Java. APK assembly also runs Android's DEX conversion. This verifies library consumption and packaging; it is not an emulator or device test. The verification APK has no activity and is not intended for installation.

## Web / PWA

First run the engine's `packageJs` task, then:

```text
cd verification/web
npm ci
npm run build
```

This consumes the npm package with strict TypeScript checking and a Vite production build. To execute the checks in a browser, run `npx vite --host 127.0.0.1` and open its local URL. The page reports `status: passed` or throws on failure.

Alternatively, from the repository root, run the headless check with Python 3.10+ and an installed Chromium browser:

```text
python tools/verify_browser.py --browser "<chrome-or-chromium-executable>"
```

The script serves only the built verification page on a temporary loopback port and saves its result to `build/reports/browser-verification.json`.

The full JVM/JavaScript parity check is part of the root `check` task. Browser compatibility does not require a Kotlin installation, a Java runtime, or network access once the JavaScript bundle is delivered.
