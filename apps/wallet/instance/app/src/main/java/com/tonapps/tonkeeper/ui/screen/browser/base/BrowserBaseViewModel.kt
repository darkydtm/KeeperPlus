package com.tonapps.tonkeeper.ui.screen.browser.base

import android.app.Application
import androidx.core.view.WindowInsetsCompat
import com.tonapps.extensions.MutableEffectFlow
import com.tonapps.tonkeeper.Environment
import com.tonapps.tonkeeper.ui.base.BaseWalletVM
import com.tonapps.blockchain.model.legacy.WalletEntity
import com.tonapps.chainkit.core.chain.model.account.Network
import com.tonapps.wallet.data.browser.BrowserRepository
import com.tonapps.wallet.data.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

class BrowserBaseViewModel(
    app: Application,
    private val wallet: WalletEntity,
    private val browserRepository: BrowserRepository,
    private val settingsRepository: SettingsRepository,
    private val environment: Environment,
): BaseWalletVM(app) {

    private val _childBottomScrolled = MutableEffectFlow<Boolean>()
    val childBottomScrolled = _childBottomScrolled.asSharedFlow()

    private val _insetsRootFlow = MutableEffectFlow<WindowInsetsCompat>()
    val insetsRootFlow = _insetsRootFlow.asSharedFlow()

    val pendingChainFlow: StateFlow<Network.Type?> field = MutableStateFlow(null)

    fun setPendingChain(network: String) {
        Network.Type.find(network)?.let { pendingChainFlow.value = it }
    }

    fun consumePendingChain() {
        pendingChainFlow.value = null
    }

    fun setInsetsRoot(value: WindowInsetsCompat) {
        _insetsRootFlow.tryEmit(value)
    }

    fun setBottomScrolled(value: Boolean) {
        _childBottomScrolled.tryEmit(value)
    }

    suspend fun hasCategory(category: String): Boolean = withContext(Dispatchers.IO) {
        if (!settingsRepository.promotionsEnabled) {
            return@withContext false
        }
        val categories = browserRepository.loadCategories(
            country = environment.deviceCountry,
            network = wallet.network,
            locale = settingsRepository.getLocale(),
            walletId = wallet.multichainWalletId,
        )
        categories.any { it == category }
    }

}