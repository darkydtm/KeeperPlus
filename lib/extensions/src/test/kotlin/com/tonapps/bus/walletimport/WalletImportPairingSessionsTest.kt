package com.tonapps.bus.walletimport

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class WalletImportPairingSessionsTest {
    @Test
    fun `signer return and completion retain session state`() {
        val sessions = WalletImportPairingSessions()
        val session = sessions.start(WalletImportPairingSource.Signer)
        assertSame(session, sessions.active(WalletImportPairingSource.Signer))
        assertNull(sessions.active(WalletImportPairingSource.Ledger))
        session.signerAppOpened()
        assertTrue(session.isAwaitingSignerReturn)
        session.connected()
        assertFalse(session.isAwaitingSignerReturn)
        session.success()
        assertTrue(session.hasSucceeded)
        assertFalse(session.isActive)
        assertNull(sessions.active(WalletImportPairingSource.Signer))
        session.start()
        assertFalse(session.isActive)
    }

    @Test
    fun `replacement exits prior session and exit is terminal`() {
        val sessions = WalletImportPairingSessions()
        val previous = sessions.start(WalletImportPairingSource.Signer)
        previous.signerAppOpened()
        val current = sessions.start(WalletImportPairingSource.Ledger)
        assertFalse(previous.isActive)
        assertFalse(previous.isAwaitingSignerReturn)
        assertSame(current, sessions.active(WalletImportPairingSource.Ledger))
        current.exit()
        current.success()
        assertFalse(current.hasSucceeded)
        assertNull(sessions.active(WalletImportPairingSource.Ledger))
    }
}
