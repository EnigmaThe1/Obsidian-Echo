# Obsidian Echo

A local-first Android second-brain prototype based on [Daedalus Echo](https://github.com/DaedalusApps/daedalus-echo) (MIT licensed).

## Status

**Experimental development repository. Do not install over your existing Daedalus Echo application.** The initial CI pipeline uses an isolated Android application ID and unsigned-for-production debug APK. On-device functionality must be tested separately.

## Aim

- Reuse Echo's on-device Gemma, Whisper, and semantic-retrieval stack.
- Read a user-selected Obsidian folder via Android's Storage Access Framework and index Markdown read-only.
- Answer questions with citations to selected source notes and reject irrelevant retrievals.
- Preserve the existing Echo app, recordings, and backup files.
- Keep passwords and credentials accessible through a separately encrypted, user-authenticated credential vault (planned; **not implemented**). The AI must not ingest raw vault secrets.
- Avoid automatic upload of personal documents and secrets; no plaintext credential export.

## Build and tests

GitHub Actions workflow: [Android validation](.github/workflows/android.yml). See [the actual workflow runs](https://github.com/EnigmaThe1/Obsidian-Echo/actions/workflows/android.yml), [the latest reported status](ci-status/latest.json), and [the device-testing checklist](docs/TESTING.md). It fetches and checks out a **pinned** upstream source revision, applies independently maintained local changes, then runs unit tests and builds a debug APK. A successful workflow publishes an APK as an Actions artifact for a limited retention period.

No secrets, personal recordings, or password databases are committed to this public repository. Read [SECURITY.md](SECURITY.md) before installing or testing the APK.

## Attribution

Copyright 2026 DaedalusApps. Original project distributed under the MIT License. See [UPSTREAM_LICENSE](UPSTREAM_LICENSE) for the original licence. Obsidian Echo changes are independently maintained.

## Experimental APK

Only after the workflow completes successfully, open its GitHub Actions run and download the `obsidian-echo-experimental-apk` artifact ZIP. Verify its `SHA256SUMS.txt` file before installing the APK. This is **not** an audited production password manager, and it has no credential-vault access yet.

The test build uses a separate package ID, `com.daedalusapps.echo.obsidian`, to avoid overwriting your installed Daedalus Echo. Its models may need separate downloads because of Android's app sandbox. CI does not test phone-specific model acceleration or folder picker behavior.
