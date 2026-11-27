package com.tonapps.deposit.multicoin.screens.method

import com.tonapps.blockchain.model.legacy.WalletCurrency
import com.tonapps.bus.core.contract.UtmParameters
import com.tonapps.bus.generated.Events.DepositFlow.DepositFlowFrom
import com.tonapps.deposit.data.ExchangeRepository
import com.tonapps.deposit.multicoin.data.RampAssetDetail
import com.tonapps.deposit.multicoin.data.RampMethod
import com.tonapps.deposit.multicoin.data.RampRepository
import com.tonapps.deposit.multicoin.loadKnownFiats
import com.tonapps.deposit.multicoin.pickFiat
import com.tonapps.deposit.multicoin.supportedFiats
import com.tonapps.deposit.screens.ramp.RampType
import com.tonapps.extensions.lazyUnsafe
import com.tonapps.log.L
import com.tonapps.mvi.AsyncViewModel
import com.tonapps.wallet.data.multichain.account.McAccountRepository
import com.tonapps.wallet.data.multichain.account.UnifiedAccountRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import io.exchangeapi.models.CreateP2PSessionRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface PaymentMethodState {
    data object Loading : PaymentMethodState
    data object Empty : PaymentMethodState
    data class Data(
        val asset: RampAssetDetail,
        val fiatCode: String,
    ) : PaymentMethodState {
        val methods: List<RampMethod> by lazyUnsafe {
            asset.methods.filter { method -> method.providers.any { it.fiat == fiatCode } }
        }

        val supportedFiatCodes: List<String> by lazyUnsafe {
            asset.methods
                .flatMap { method -> method.providers.map { it.fiat } }
                .distinct()
        }
    }
}

sealed interface PaymentMethodEvent {
    data class OpenP2P(val url: String) : PaymentMethodEvent
    data object ShowP2PError : PaymentMethodEvent
}

data class PaymentMethodFeatureData(
    val walletId: String,
    val rampType: RampType,
    val assetId: String,
    val analyticsFrom: DepositFlowFrom,
    val preferredCurrency: String? = null,
    val utm: UtmParameters = UtmParameters.Empty,
)

class PaymentMethodFeature(
    private val data: PaymentMethodFeatureData,
    private val rampRepository: RampRepository,
    private val exchangeRepository: ExchangeRepository,
    private val accountRepository: UnifiedAccountRepository,
    private val mcAccountRepository: McAccountRepository,
    private val settingsRepository: SettingsRepository,
) : AsyncViewModel() {

    val state: StateFlow<PaymentMethodState> field = MutableStateFlow<PaymentMethodState>(PaymentMethodState.Loading)
    val events: SharedFlow<PaymentMethodEvent> field = MutableSharedFlow(extraBufferCapacity = 1)

    init {
        load()
    }

    fun retry() {
        load()
    }

    fun openP2PMethod() {
        bgScope.launch {
            openP2P()
        }
    }

    private suspend fun openP2P() {
        val current = state.value as? PaymentMethodState.Data ?: return
        val address = mcAccountRepository.findAccount(data.walletId, data.assetId)?.data?.displayAddress
        if (address.isNullOrBlank()) {
            events.tryEmit(PaymentMethodEvent.ShowP2PError)
            return
        }
        try {
            val response = exchangeRepository.createP2PSession(
                CreateP2PSessionRequest(
                    wallet = address,
                    assetId = data.assetId,
                    fiatCurrency = current.fiatCode,
                )
            )
            events.tryEmit(PaymentMethodEvent.OpenP2P(response.deeplinkUrl))
        } catch (e: Throwable) {
            L.e(e)
            events.tryEmit(PaymentMethodEvent.ShowP2PError)
        }
    }

    fun selectCurrency(currency: WalletCurrency) {
        val current = state.value as? PaymentMethodState.Data ?: return
        state.tryEmit(current.copy(fiatCode = currency.code))
    }

    private fun load() {
        state.tryEmit(PaymentMethodState.Loading)
        bgScope.launch {
            try {
                val asset = rampRepository.getAsset(data.walletId, data.rampType, data.assetId)
                val fiatCode = resolveFiatCode(asset)
                val loaded = PaymentMethodState.Data(
                    asset = asset,
                    fiatCode = fiatCode,
                )
                state.emit(loaded)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                L.e(e)
                state.emit(PaymentMethodState.Empty)
            }
        }
    }

    private suspend fun resolveFiatCode(asset: RampAssetDetail): String {
        val known = loadKnownFiats(data.walletId, accountRepository, exchangeRepository, settingsRepository)
        return pickFiat(asset.supportedFiats, known, data.preferredCurrency ?: settingsRepository.currency.code)
    }

}
