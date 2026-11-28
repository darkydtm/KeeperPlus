package com.tonapps.bus.walletimport

class WalletImportPairingSessions {
    private var current: WalletImportPairingSession? = null

    @Synchronized
    fun start(walletSource: WalletImportPairingSource): WalletImportPairingSession {
        current?.exit()
        return WalletImportPairingSession(walletSource).also {
            current = it
            it.start()
        }
    }

    @Synchronized
    fun active(walletSource: WalletImportPairingSource): WalletImportPairingSession? =
        current?.takeIf { it.isActive && it.walletSource == walletSource }
}
