package com.tonapps.wallet.api.tron

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TronBalanceResultsTest {
    @Test
    fun completeZeroAndNonzeroBalancesAreAccepted() {
        assertTrue(hasCompleteTronBalanceResults(mapOf(0 to "0x0", 1 to "0xAB", 2 to "0x01", 3 to "0xff"), 2))
    }

    @Test
    fun missingOrInvalidResultsAreRejected() {
        assertFalse(hasCompleteTronBalanceResults(emptyMap(), 1))
        assertFalse(hasCompleteTronBalanceResults(mapOf(0 to "0x0"), 1))
        assertFalse(hasCompleteTronBalanceResults(mapOf(0 to "0x0", 1 to "0x"), 1))
        assertFalse(hasCompleteTronBalanceResults(mapOf(0 to "0x0", 1 to "0xno"), 1))
        assertFalse(hasCompleteTronBalanceResults(mapOf(0 to "0x0", 1 to "-1"), 1))
    }
}
