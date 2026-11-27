package com.tonapps.tonkeeper.ui.screen.browser.analytics

import android.os.Parcelable
import com.tonapps.bus.core.contract.UtmParameters
import com.tonapps.bus.generated.Events.DappBrowser.DappAppClickFrom
import com.tonapps.bus.generated.Events.DappBrowser.DappBrowserAssetChain
import kotlinx.parcelize.Parcelize

@Parcelize
data class DappOpenAnalytics(
    val from: DappAppClickFrom,
    private val url: String,
    private val assetChain: DappBrowserAssetChain,
    private val appId: String,
    private val bannerId: String?,
    private val location: String,
    private val utm: UtmParameters = UtmParameters.Empty,
): Parcelable {
}
