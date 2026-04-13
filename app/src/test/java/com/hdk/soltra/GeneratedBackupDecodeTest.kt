package com.hdk.soltra

import com.hdk.soltra.data.repository.BackupSnapshot
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

class GeneratedBackupDecodeTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `generated real backup json decodes with app serializer`() {
        val file = File("backup_real_data.json")
        assumeTrue("backup_real_data.json not found in project root", file.exists())

        val content = file.readText(Charsets.UTF_8)
        val snapshot = json.decodeFromString(BackupSnapshot.serializer(), content)

        assertTrue(snapshot.expenses.isNotEmpty())
        assertTrue(snapshot.categories.isNotEmpty())
        assertTrue(snapshot.budgetConfig.currencyCode.isNotBlank())
    }
}
