package com.tonapps.bus.walletimport

enum class WalletImportPairingSource { Signer, Ledger, Keystone }

class WalletImportPairingSession(val walletSource: WalletImportPairingSource) {
    private val lock = Any()
    private var active = false
    private var finished = false
    private var awaitingSigner = false
    private var succeeded = false

    val isActive: Boolean get() = synchronized(lock) { active }
    val hasSucceeded: Boolean get() = synchronized(lock) { succeeded }
    val isAwaitingSignerReturn: Boolean get() = synchronized(lock) { active && awaitingSigner }

    fun start() = synchronized(lock) {
        if (!finished) active = true
    }

    fun signerAppOpened() = synchronized(lock) {
        if (active) awaitingSigner = true
    }

    fun connected() = synchronized(lock) {
        if (active) awaitingSigner = false
    }

    fun success() = synchronized(lock) {
        if (active) {
            active = false
            finished = true
            succeeded = true
        }
    }

    fun exit() = synchronized(lock) {
        if (active) {
            active = false
            finished = true
        }
    }
}
