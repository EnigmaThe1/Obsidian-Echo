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
        for (name in listOf("Passwords", "credentials", "Secrets", "private-keys", "TOKENS")) {
            assertTrue(name, ObsidianImporter.shouldSkipName(name))
        }
    }

    @Test fun ordinaryVaultContentIsAccepted() {
        for (name in listOf("00 - Inbox", "Projects", "My First Memory.md", "Planning.md")) {
            assertFalse(name, ObsidianImporter.shouldSkipName(name))
        }
    }
}
