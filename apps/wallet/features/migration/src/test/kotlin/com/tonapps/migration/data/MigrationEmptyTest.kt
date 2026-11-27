package com.tonapps.migration.data

import io.tonapi.models.AccountStatus
import io.tonapi.models.MigrationWalletValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MigrationEmptyTest {
    private fun emptyValue(account: String) = MigrationWalletValue(
        account = account,
        balance = 0,
        status = AccountStatus.nonexist,
        jettons = emptyList(),
        nftCount = 0,
    )

    @Test
    fun emptyTonResponseRequiresEveryAccount() {
        val values = mapOf("a" to emptyValue("a"), "b" to emptyValue("b"))
        assertTrue(completeTonMigrationValues(listOf("a", "b"), values))
        assertFalse(completeTonMigrationValues(listOf("a", "b"), null))
        assertFalse(completeTonMigrationValues(listOf("a", "b"), mapOf("a" to emptyValue("a"))))
        assertFalse(completeTonMigrationValues(listOf("a"), mapOf("a" to emptyValue("a").copy(balance = -1))))
        assertFalse(completeTonMigrationValues(listOf("a"), mapOf("a" to emptyValue("a").copy(nftCount = -1))))
    }

    @Test
    fun confirmedEmptyIsRememberedOnlyForCapturedWallet() {
        val preferences = mutableMapOf<String, Boolean>()
        val save: (String) -> Unit = { preferences[it] = true }
        val capturedDestination = "wallet-a"
        val newlySelectedDestination = "wallet-b"
        rememberEmptyMigration(capturedDestination, true, true, save)
        assertEquals(true, preferences[capturedDestination])
        assertFalse(preferences.containsKey(newlySelectedDestination))
    }

    @Test
    fun unknownNonemptyBackgroundAndMissingDestinationDoNotPersist() {
        val preferences = mutableMapOf<String, Boolean>()
        val save: (String) -> Unit = { preferences[it] = true }
        rememberEmptyMigration("wallet-a", true, false, save)
        rememberEmptyMigration("wallet-a", false, true, save)
        rememberEmptyMigration(null, true, true, save)
        assertTrue(preferences.isEmpty())
    }
}
