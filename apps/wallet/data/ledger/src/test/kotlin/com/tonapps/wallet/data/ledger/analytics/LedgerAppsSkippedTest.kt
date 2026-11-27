package com.tonapps.wallet.data.ledger.analytics

import com.tonapps.bus.generated.Events.LedgerFlow.LedgerFlowFailureType
import com.tonapps.ledgerkit.LedgerAppId
import com.tonapps.ledgerkit.LedgerFailure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LedgerAppsSkippedTest {

    @Test
    fun `skipped app failures map to their distinct types`() {
        assertEquals(
            LedgerFlowFailureType.AppUpdateRequired,
            LedgerFailure.AppUpdateRequired(LedgerAppId.Bitcoin, "2.1.0", "2.1.2").toFailureType(),
        )
        assertEquals(
            LedgerFlowFailureType.AppNotInstalled,
            LedgerFailure.AppNotInstalled(LedgerAppId.Solana).toFailureType(),
        )
    }

    @Test
    fun `cancelled requests have no failure type`() {
        assertNull(LedgerFailure.Cancelled.toFailureType())
    }
}
