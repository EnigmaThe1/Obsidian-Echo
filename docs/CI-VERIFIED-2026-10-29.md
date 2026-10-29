# Verified initial cloud build — 2026-10-29

## Completed result

- GitHub Actions run: https://github.com/EnigmaThe1/Obsidian-Echo/actions/runs/40856921326
- Source commit: `5b883011c58652a6566e46de281f33920b4ada7c`
- Workflow outcome: **success**
- Test report artifact: `android-jvm-test-results`
- Test suites: **42**, tests: **416**, failures: **0**, errors: **0**, skipped: **0**
- Obsidian-specific filename filter tests: 3, all passed
- Android APK artifact: `obsidian-echo-experimental-apk`
- Artifact ID: `12889887079`
- APK filename: `Obsidian-Echo-experimental-debug.apk`
- APK uncompressed size: **322,033,194 bytes**
- SHA-256: `ea18953013e2e024b8163da92c71d3e56208094aaacec6d13576622edba47001`
- CI script verified package ID: `com.daedalusapps.echo.obsidian`

## Verification method

The APK ZIP and test report ZIP were downloaded from GitHub Actions and inspected independently. `sha256sum -c SHA256SUMS.txt` returned OK for the APK. The JUnit XML test reports were parsed to count tests and failures.

## Important limits

This proves build and JVM unit-test execution on GitHub runners; it does **not** prove successful on-device AI model loading, Android Storage Access Framework import, recording playback, permission behavior, or password-vault security. This is an experimental **debug** build, not a production credential-storage product.

Subsequent source changes, including regression protection for `Passwords.md`-style filenames, require another successful CI run and their own artifact SHA before replacing this baseline.
