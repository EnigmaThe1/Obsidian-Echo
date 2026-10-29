#!/usr/bin/env python3
"""Fail-closed, deterministic customisation of pinned Daedalus Echo source."""
from pathlib import Path
import shutil
import sys

root = Path(sys.argv[1]).resolve()
overlay = Path(__file__).resolve().parents[1] / "overrides"
if not (root / "android/app/build.gradle.kts").is_file():
    raise SystemExit("Expected Daedalus Echo source checkout")

def patch(relative: str, before: str, after: str) -> None:
    path = root / relative
    text = path.read_text(encoding="utf-8")
    matches = text.count(before)
    if matches != 1:
        raise RuntimeError(f"{relative}: expected one patch anchor, found {matches}: {before[:85]!r}")
    path.write_text(text.replace(before, after, 1), encoding="utf-8")

manifest = "android/app/src/main/AndroidManifest.xml"
patch(manifest, 'android:allowBackup="true"', '''android:allowBackup="false"
        android:fullBackupContent="@xml/backup_rules"
        android:dataExtractionRules="@xml/data_extraction_rules"''')
patch(manifest, 'android:label="Daedalus Echo"', 'android:label="Obsidian Echo (Test)"')

build = "android/app/build.gradle.kts"
patch(build, 'applicationIdSuffix = ".debug"', 'applicationIdSuffix = ".obsidian"')

settings = "android/app/src/main/java/com/daedalusapps/echo/ui/screens/SettingsScreen.kt"
patch(settings, 'import com.daedalusapps.echo.data.backup.BackupWorker',
      'import com.daedalusapps.echo.data.backup.BackupWorker\nimport com.daedalusapps.echo.data.obsidian.ObsidianImporter')

patch(settings,
      '    var isBackingUp by remember { mutableStateOf(false) }',
      '''    var isBackingUp by remember { mutableStateOf(false) }
    var obsidianFolderUri by remember {
        mutableStateOf(prefs.getString("obsidian_vault_uri", null))
    }
    var isIndexingObsidian by remember { mutableStateOf(false) }''')

anchor = '    Scaffold(\n        topBar = {'
launcher = '''    val chooseObsidianFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    prefs.edit().putString("obsidian_vault_uri", uri.toString()).apply()
                    obsidianFolderUri = uri.toString()
                } catch (e: Exception) {
                    scope.launch { snackbar.showSnackbar("Cannot open vault: ${e.message}") }
                }
            }
        }
    )

'''
patch(settings, anchor, launcher + anchor)

anchor = '                // Backup & Recovery'
ui = '''                // Obsidian notes: no directory writes, no background crawl or uploads.
                Text(
                    "Obsidian Memory (experimental)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Select your Obsidian vault, then index its Markdown notes locally. " +
                        "Private folders and hidden files are skipped. " +
                        "Passwords belong in a separate encrypted credential vault.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = { chooseObsidianFolderLauncher.launch(null) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Choose Obsidian vault") }
                Text(
                    if (obsidianFolderUri == null) "No vault selected" else "Vault folder selected",
                    style = MaterialTheme.typography.bodySmall
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = obsidianFolderUri != null && !isIndexingObsidian,
                    onClick = {
                        val selected = obsidianFolderUri
                        if (selected != null) {
                            isIndexingObsidian = true
                            scope.launch {
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        ObsidianImporter(context).index(Uri.parse(selected))
                                    }
                                    snackbar.showSnackbar(
                                        "Obsidian: ${result.indexed} indexed, " +
                                            "${result.unchanged} unchanged, ${result.skipped} skipped, " +
                                            "${result.removed} stale removed"
                                    )
                                } catch (e: Exception) {
                                    snackbar.showSnackbar("Index failed: ${e.message}")
                                } finally {
                                    isIndexingObsidian = false
                                }
                            }
                        }
                    }
                ) { Text(if (isIndexingObsidian) "Indexing..." else "Index Obsidian notes") }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isIndexingObsidian,
                    onClick = {
                        isIndexingObsidian = true
                        scope.launch {
                            try {
                                val removed = withContext(Dispatchers.IO) {
                                    ObsidianImporter(context).clearIndexed()
                                }
                                snackbar.showSnackbar(
                                    "Cleared ${removed} cached Obsidian notes. Original vault unchanged."
                                )
                            } catch (e: Exception) {
                                snackbar.showSnackbar("Clear failed: ${e.message}")
                            } finally {
                                isIndexingObsidian = false
                            }
                        }
                    }
                ) { Text("Clear Obsidian index (vault stays untouched)") }

'''
patch(settings, anchor, ui + anchor)

backup = "android/app/src/main/java/com/daedalusapps/echo/data/backup/BackupManager.kt"
patch(backup, '            recordings.forEach { r ->',
      '            recordings.filterNot { it.filename.startsWith("obsidian_") }.forEach { r ->')

viewmodel = "android/app/src/main/java/com/daedalusapps/echo/viewmodel/RecordingViewModel.kt"
patch(viewmodel,
      'val sources = repo.semanticSearch(queryEmbed, withEmbeddings, topK = 5)',
      'val sources = repo.semanticSearch(queryEmbed, withEmbeddings, topK = 5, minScore = 0.35f)')

for f in overlay.rglob("*"):
    if f.is_file():
        rel = f.relative_to(overlay)
        target = root / rel
        if target.exists():
            raise RuntimeError(f"Overlay refuses to overwrite existing upstream file: {rel}")
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(f, target)
print("Obsidian Echo patch applied; original app data is untouched")
