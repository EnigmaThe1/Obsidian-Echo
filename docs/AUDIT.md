# Obsidian Echo — initial source audit (2026-10-29)

This is a static source review and development record, **not** a penetration test or a passed APK/device qualification.

## Baseline
DaedalusApps/daedalus-echo upstream revision `6c308fabaf7c54734b5f769bfc5de569a05edcce`, MIT licensed. Existing user app: 1.2.4-debug (Build 1) on Galaxy S24 Ultra; independently tested local Q&A retrieved a LiveKit note correctly but also returned unrelated citations.

## Confirmed findings and implemented experimental mitigations

1. **Upstream Android system backup enabled** in `android/app/src/main/AndroidManifest.xml`. Prototype patch disables backup and provides explicit extraction exclusions. This does not encrypt manual exports.
2. **Manual and scheduled backup JSON uses readable plaintext** (transcripts, summaries, metadata and embeddings). Prototype excludes Obsidian-imported `obsidian_` records from Echo JSON backups; existing recording exports remain readable.
3. **Ask Library previously returned top five vectors without minimum similarity**. Prototype introduces an initial 0.35 cosine threshold, subject to accuracy testing with real notes. The original LiveKit answer and unrelated citations were observed on the user's phone.
4. **Release signing can fall back to Android debug keystore** in upstream Gradle. Experimental workflow builds **debug only**, does not claim a production release, and uses an isolated application ID. Production release signing remains a gate.
5. **Internet permission exists in upstream app** because model downloads need networking. No evidence of recording upload from the code inspected, but no complete runtime network capture has been performed.
6. **Debug diagnostic broadcast receiver exists** in upstream; protected by the Android DUMP permission and gated by debug code. Full threat assessment for production remains outstanding.
7. **Room database stores recording transcripts**; not designed as an encrypted credential vault. New importer is for ordinary notes; credential lookup remains separate P0 issue.
8. **Source file access**: Android SAF user-selected tree; importer requests only a persisted read permission, bounds Markdown file size to 256 KiB and visit count to 1,000; excludes obvious secret directories and hidden files, without claiming those names protect all sensitive content.
9. **Source integrity**: GitHub CI clones a pinned upstream commit and applies checked-in patches with exact-once anchors, failing on unexpected upstream changes. CI does not use private user data.
10. **No real-device validation yet**: GitHub JVM tests and debug build must pass before any Android installation. Even after that, Gemma/GPU, folder picker, screen flow, backup/recovery and network behavior need physical-phone testing.

## Release blockers
- Verified passing GitHub CI plus artifact SHA.
- Physical-device Obsidian import and Q&A acceptance.
- Document deletion/update behavior and reliable citation navigation.
- Encrypted credential-vault design, authenticated unlock and security tests.
- Production signing, deterministic update/migration, independent encrypted backup and recovery.
- Android runtime permissions, diagnostics, local logging and network-capture audit.

See [tests](TESTING.md), [security policy](../SECURITY.md) and [credential-gate issue](https://github.com/EnigmaThe1/Obsidian-Echo/issues/1).
