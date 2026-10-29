package com.daedalusapps.echo.data.obsidian

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.daedalusapps.echo.ai.EmbeddingService
import com.daedalusapps.echo.data.RecordingRepository
import com.daedalusapps.echo.data.db.AppDatabase
import com.daedalusapps.echo.data.model.Recording
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * Read-only Obsidian Markdown importer. Reads only a user-picked document tree via SAF.
 * The imported material is indexed in Echo's Room DB. No vault file is changed.
 *
 * Ordinary notes only: obvious secret-related paths are excluded, but this is not
 * a cryptographic boundary or a replacement for an encrypted credential vault.
 */
class ObsidianImporter(private val context: Context) {
    data class Result(val indexed: Int, val unchanged: Int, val skipped: Int)

    private val repo = RecordingRepository(AppDatabase.getInstance(context).recordingDao())
    private val maxFileBytes = 256 * 1024
    private val maxFiles = 1000
    private val maxDepth = 12

    suspend fun index(treeUri: Uri): Result = withContext(Dispatchers.IO) {
        val readGranted = context.contentResolver.persistedUriPermissions.any {
            it.isReadPermission && it.uri == treeUri
        }
        require(readGranted) { "Read permission has not been granted for this vault" }
        val root = DocumentFile.fromTreeUri(context, treeUri)
            ?: error("Cannot open selected Obsidian folder")
        require(root.isDirectory) { "Selected location is not a folder" }

        val embedder = EmbeddingService(context)
        require(embedder.isReady) { "Download the semantic search model in Settings first" }
        embedder.ensureLoaded()
        var indexed = 0
        var unchanged = 0
        var skipped = 0
        var visitedFiles = 0

        try {
            suspend fun visit(node: DocumentFile, relative: String, depth: Int) {
                if (depth > maxDepth || visitedFiles >= maxFiles) {
                    skipped++
                    return
                }
                val name = node.name ?: return
                val currentPath = if (relative.isEmpty()) name else "$relative/$name"
                if (shouldSkipName(name)) {
                    skipped++
                    return
                }
                if (node.isDirectory) {
                    for (child in node.listFiles()) {
                        visit(child, currentPath, depth + 1)
                    }
                    return
                }
                if (!node.isFile || !name.endsWith(".md", ignoreCase = true)) return
                visitedFiles++
                if (node.length() > maxFileBytes) {
                    skipped++
                    return
                }
                val bytes = context.contentResolver.openInputStream(node.uri)?.use {
                    // Limit allocation even if a provider reports an incorrect file size.
                    it.readAtMost(maxFileBytes + 1)
                } ?: run {
                    skipped++
                    return
                }
                if (bytes.size > maxFileBytes || bytes.any { it == 0.toByte() }) {
                    skipped++
                    return
                }
                val text = bytes.toString(Charsets.UTF_8).trim()
                if (text.isEmpty()) {
                    skipped++
                    return
                }
                val id = "obsidian_" + sha256(treeUri.toString() + "/" + currentPath).take(32) + ".md"
                val existing = repo.get(id)
                if (existing?.transcript == text) {
                    unchanged++
                    return
                }
                val title = name.substringBeforeLast(".").ifBlank { name }
                val embedding = embedder.embed("$title\n" + text.take(6000))
                    ?: error("Could not generate a local search embedding for $name")
                repo.save(
                    Recording(
                        filename = id,
                        localPath = node.uri.toString(),
                        transcript = text,
                        summary = text.take(3000),
                        shortSummary = text.take(350),
                        title = title,
                        topics = listOf("Obsidian"),
                        createdAt = node.lastModified().takeIf { it > 0L }
                            ?: System.currentTimeMillis(),
                        embedding = embedding
                    )
                )
                indexed++
            }

            // The root is the chosen vault, not a subfolder whose name should be filtered.
            for (child in root.listFiles()) visit(child, "", 0)
            Result(indexed, unchanged, skipped)
        } finally {
            embedder.close()
        }
    }

    companion object {
        /** Reviewable exclusion policy; unrelated ordinary notes are allowed. */
        internal fun shouldSkipName(name: String): Boolean {
            val n = name.lowercase()
            return n.startsWith(".") || n in setOf(
                "credentials", "credential", "passwords", "password", "secrets",
                "secret", "private-keys", "private_keys", "tokens", "token"
            )
        }
    }

    private fun sha256(input: String): String = MessageDigest
        .getInstance("SHA-256")
        .digest(input.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}

/** Bounded input reader; never trusts the metadata size returned by content providers. */
private fun java.io.InputStream.readAtMost(limit: Int): ByteArray {
    val out = java.io.ByteArrayOutputStream()
    val buf = ByteArray(8192)
    while (out.size() < limit) {
        val n = read(buf, 0, minOf(buf.size, limit - out.size()))
        if (n < 0) break
        if (n > 0) out.write(buf, 0, n)
    }
    return out.toByteArray()
}
