# Remote build and phone acceptance checklist

## CI proof

The [Android workflow](../.github/workflows/android.yml) executes pinned upstream source + local overlay, JVM tests and a debug APK build. The [latest machine-readable status](../ci-status/latest.json) records the GitHub Actions run ID and the result. A successful run uploads `obsidian-echo-experimental-apk` and its SHA256 checksum to the Actions run's Artifacts area.

**Do not install an APK unless the workflow result is success and the artifact is from this repository.** The test APK is signed with GitHub's build-time debug key and is not for sensitive or production use.

## Before installing

- Keep the existing Daedalus Echo app, recordings, and backups. Do not uninstall or clear it.
- The test application ID is `com.daedalusapps.echo.obsidian`; the existing debug app uses `com.daedalusapps.echo.debug`.
- The test app is isolated and may need **separate model downloads**, requiring extra storage. It cannot simply access another app's private model files because Android isolates app data.
- No passwords, tokens, or confidential documents should be used in these initial experiments.

## End-to-end acceptance on Samsung Galaxy S24 Ultra

1. Install the completed CI artifact, not a random unsigned copy or APK mirror.
2. Open the separate **Obsidian Echo (Test)** application; download required local models if prompted.
3. In Settings under **Obsidian Memory (experimental)** select your existing vault root (the folder containing `00 - Inbox`), granting read-only access.
4. Tap **Index Obsidian notes** and verify the number of files indexed. Source Markdown files must remain unmodified.
5. Ask an exact factual question answered by `My First Memory.md` and verify the answer and the note title in sources.
6. Add and edit another ordinary Markdown note in Obsidian. Re-index; changed notes should update; unchanged notes should not duplicate.
7. Add YAML frontmatter `ai-index: false` or `private: true` to a previously indexed note. Re-index and verify it disappears from search.
8. Delete a previously indexed Markdown note and re-index completely. Its cached copy must disappear. A deliberately truncated scan must not prune cached notes.
9. Use **Clear Obsidian index (vault stays untouched)**, and confirm the cached copies disappear but the Markdown vault stays intact.
10. Confirm `.obsidian` folders, obviously sensitive folders such as `Passwords`, oversized Markdown notes, and non-Markdown files are skipped.
11. Confirm Echo's existing recording capture and **Ask Your Notes** continue to work in the **test app**.
12. Export an Echo JSON backup from the test app and verify imported `obsidian_` rows are absent; the original Obsidian vault remains the backup source of truth for those notes.
13. Verify phone remains offline during local search, and verify no unexpected network requests. Separate initial model downloads require internet.

## Known gaps — do not treat as completed

- No credential vault connection, authentication UI, or password retrieval feature.
- Indexed note text is stored in Echo's local Room database without a dedicated at-rest database encryption layer. The experimental build disables Android system backup but does not substitute for an audited secrets vault.
- A selected vault is indexed on demand rather than continuously. Successful full scans remove stale cached notes; failed or truncated scans never prune. Switching vaults replaces the prior local index only after a successful full scan.
- Imported sources open in Echo's existing note screen, not necessarily directly in Obsidian.
- Simple basename exclusions are not a guarantee against private content in innocuously named notes.
- Automatic backups, restore and audio preservation require additional device acceptance tests.
- GitHub Actions debug APK signing is not stable across runners. Updating the test app can require uninstalling its previous test-only instance, which removes only that isolated test app's data; the original Echo app and Obsidian vault remain independent.
- CI cannot exercise MediaPipe/Gemma GPU inference, Android SAF picker behavior, UI flows or phone storage permissions without physical-device testing.
