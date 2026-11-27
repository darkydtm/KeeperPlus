package com.tonapps.tonkeeper.ui.screen.browser.dapp.earn

import android.content.res.Configuration
import com.tonapps.blockchain.model.legacy.WalletEntity
import com.tonapps.extensions.lazyUnsafe
import com.tonapps.tonkeeper.koin.walletViewModel
import com.tonapps.tonkeeper.manager.earn.EarnUrlResolver
import com.tonapps.tonkeeper.ui.component.TonConnectWebView
import com.tonapps.tonkeeper.ui.screen.browser.dapp.inapp.InAppDAppScreen
import com.tonapps.wallet.api.API
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

internal class EarnDAppScreen(wallet: WalletEntity) : InAppDAppScreen(wallet) {

    override val fragmentName: String = "EarnDAppScreen"

    override val drawsOwnPagesUnderSystemBars: Boolean = true

    override val dappBridges: EarnDAppViewModel by walletViewModel {
        parametersOf(dappAnalytics.from)
    }

    private val earnUrlResolver: EarnUrlResolver by inject()

    private val api: API by inject()

    override val originRules: Set<String> by lazyUnsafe {
        earnUrlResolver.originRules(startUri)
    }

    override fun onWebViewCreated(webView: TonConnectWebView) {
        super.onWebViewCreated(webView)
        applyUserAgent(webView)
    }

    // Every theme change recreates the activity today, and the new document gets the theme in its
    // config; these only matter if this screen ever outlives one.
    override fun onResume() {
        super.onResume()
        dappBridges.onThemeChanged()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        dappBridges.onThemeChanged()
    }

    // Earn is told which app it runs in; the token goes after the default agent so the page keeps
    // seeing a browser it recognises.
    private fun applyUserAgent(webView: TonConnectWebView) {
        val agent = api.userAgent
        val current = webView.settings.userAgentString.orEmpty()
        if (!current.contains(agent)) {
            webView.settings.userAgentString = "$current $agent".trim()
        }
    }

}