package com.daedalusapps.echo.data.obsidian

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObsidianPolicyTest {
    @Test fun hiddenObsidianConfigurationIsExcluded() {
        assertTrue(ObsidianImporter.shouldSkipName(".obsidian"))
        assertTrue(ObsidianImporter.shouldSkipName(".git"))
    }

    @Test fun obviousCredentialCollectionsAreExcluded() {
        for (name in listOf("Passwords", "credentials", "Secrets", "private-keys", "TOKENS", "Passwords.md", "Credentials.MD", "API-KEYS.md", "recovery-codes.md", "ssh_keys.md")) {
            assertTrue(name, ObsidianImporter.shouldSkipName(name))
        }
    }

    @Test fun ordinaryVaultContentIsAccepted() {
        for (name in listOf("00 - Inbox", "Projects", "My First Memory.md", "Planning.md")) {
            assertFalse(name, ObsidianImporter.shouldSkipName(name))
        }
    }

    @Test fun frontmatterCanExcludeNotesFromAiIndex() {
        assertTrue(ObsidianImporter.shouldSkipContent("---\nai-index: false\n---\nexample"))
        assertTrue(ObsidianImporter.shouldSkipContent("---\nai_index: NO\n---\nexample"))
        assertTrue(ObsidianImporter.shouldSkipContent("---\nprivate: true\n---\nexample"))
        assertTrue(ObsidianImporter.shouldSkipContent("---\nsensitive: YES\n---\nexample"))
        assertFalse(ObsidianImporter.shouldSkipContent("---\nai-index: true\n---\nexample"))
        assertFalse(ObsidianImporter.shouldSkipContent("A normal note without frontmatter"))
    }

    @Test fun deletedFilesOnlyPrunedAfterCompleteScan() {
        val seen = setOf("obsidian_kept.md")
        assertTrue(ObsidianImporter.shouldPruneImport("obsidian_stale.md", seen, true))
        assertFalse(ObsidianImporter.shouldPruneImport("obsidian_kept.md", seen, true))
        assertFalse(ObsidianImporter.shouldPruneImport("obsidian_stale.md", seen, false))
        assertFalse(ObsidianImporter.shouldPruneImport("real_recording.m4a", seen, true))
    }

}
