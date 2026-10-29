# Security and privacy — experimental build

This public repository must never contain passwords, transcripts, recording audio, Obsidian vault content, backup JSON files, or signing secrets.

- This is a development prototype, **not** a validated password manager.
- GitHub-hosted builds never receive your notes, recordings, credentials, or private keys.
- The isolated debug app uses a distinct Android application ID. Do not uninstall your existing Echo build.
- **Do not put passwords in ordinary Obsidian notes.** Password retrieval is planned using a separate encrypted credential vault and user authentication. Raw secrets must not go to language-model prompts, embeddings, logs, or backups.
- The initial Obsidian index uses Android's folder picker and read-only access. It excludes hidden directories and obvious credential-related filenames, but those names are not a complete confidentiality boundary. Select a vault with ordinary notes only.
- Echo's existing JSON export remains readable plaintext for its existing recordings. Treat those backups as confidential. The development build excludes Obsidian-imported notes from JSON backup.
- This prototype changes Android system backup eligibility to disabled; this does not encrypt user-initiated exports.
- A successful CI job proves build/test execution on a hosted runner, not end-to-end AI behavior on a physical phone.
- Dependency downloads (Android build tools and open AI model files) require network; normal on-device Q&A remains local after models are downloaded.

## Acceptance requirements before handling credentials

Authentication gate, protected vault integration, no raw secrets in LLM input, encrypted backup/recovery, OS clipboard lifetime controls, Android security review, no debug receiver in production, a tested signing and upgrade plan, and real-device security testing.
