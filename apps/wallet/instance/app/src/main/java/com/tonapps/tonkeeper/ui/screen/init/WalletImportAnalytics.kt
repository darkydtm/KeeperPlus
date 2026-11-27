package com.tonapps.tonkeeper.ui.screen.init

import com.tonapps.bus.generated.Events.WalletFlow.WalletFlowFrom
import com.tonapps.bus.generated.Events.WalletFlow.WalletFlowLedgerModel
import com.tonapps.bus.generated.Events.WalletFlow.WalletFlowWalletMode
import com.tonapps.bus.generated.Events.WalletFlow.WalletFlowWalletSource
import com.tonapps.log.L
import com.tonapps.wallet.data.multichain.account.UnifiedAccountRepository
import kotlinx.coroutines.CancellationException

// Import events that happen before InitScreen opens (Ledger pairing, Signer/Keystone QR scanning)
// have no InitViewModel to report them, so they go through here instead.
class WalletImportAnalytics(
    private val unifiedAccountRepository: UnifiedAccountRepository,
) {

    suspend fun trackError(
        walletSource: WalletFlowWalletSource,
        errorType: String,
        errorMessage: String? = null,
        ledgerModel: WalletFlowLedgerModel? = null,
    ) {
    }

    suspend fun trackSignerScanned() {
    }

    suspend fun trackKeystoneScanned() {
    }

    suspend fun trackLedgerBluetoothConnected(ledgerModel: WalletFlowLedgerModel) {
    }

    suspend fun trackLedgerAppOpened(ledgerModel: WalletFlowLedgerModel?) {
    }

    suspend fun from(): WalletFlowFrom {
        val hasAnyWallet = try {
            unifiedAccountRepository.hasAnyWallet()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            L.e(e)
            true
        }
        return if (hasAnyWallet) {
            WalletFlowFrom.Main
        } else {
            WalletFlowFrom.Onboarding
        }
    }
}
