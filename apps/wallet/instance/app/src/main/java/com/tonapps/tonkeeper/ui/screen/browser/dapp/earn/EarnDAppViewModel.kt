package com.tonapps.tonkeeper.ui.screen.browser.dapp.earn

import com.tonapps.mvi.AsyncViewModel
import com.tonapps.webview.bridge.InAppBridgeContract.Bridges

internal class EarnDAppViewModel(
    private val earnBridges: EarnDAppBridges,
) : AsyncViewModel(), Bridges by earnBridges {

    init {
        earnBridges.start(stateScope)
    }

    fun onThemeChanged() {
        earnBridges.onThemeChanged()
    }
}
