package com.tonapps.tonkeeper.ui.screen.browser.analytics

import android.os.Parcelable
import android.os.SystemClock
import com.tonapps.bus.core.contract.UtmParameters
import com.tonapps.bus.generated.Events.DappBrowser.DappAppClickFrom
import com.tonapps.bus.generated.Events.DappBrowser.DappBrowserAssetChain
import com.tonapps.bus.generated.Events.DappBrowser.DappSharingCopyFrom
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlin.concurrent.atomics.AtomicBoolean

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

    @IgnoredOnParcel
    private var loadTracked = AtomicBoolean(false)

    @IgnoredOnParcel
    private var clickedAt = 0L

    fun click() {
        clickedAt = SystemClock.elapsedRealtime()
    }

    // The webview reports every navigation as finished, but the ratio with dapp_app_click only
    // holds if a single open produces at most one loaded or load_failed event.
    fun loaded() {
        if (!loadTracked.compareAndSet(expectedValue = false, newValue = true)) {
            return
        }

    }

    fun loadFailed(errorCode: String, httpStatus: Int?) {
        if (!loadTracked.compareAndSet(expectedValue = false, newValue = true)) {
            return
        }

    }

    fun sharingCopy(from: DappSharingCopyFrom) {
    }

    /** Null for a screen restored from its saved state: its click was reported by another one. */
    fun sinceClickMs(): Long? {
        if (clickedAt == 0L) {
            return null
        }
        return (SystemClock.elapsedRealtime() - clickedAt).coerceAtLeast(0L)
    }

    private fun loadDurationMs(): Long = sinceClickMs() ?: 0L
}
