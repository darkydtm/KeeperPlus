package com.tonapps.wallet.data.attribution

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/** Compatibility surface for legacy deep-link callers; no attribution SDK is initialized. */
class AttributionManager(@Suppress("UNUSED_PARAMETER") private val context: Context) {

    private val deepLinks = Channel<AttributionDeepLink>(Channel.BUFFERED)
    val deepLinkFlow: Flow<AttributionDeepLink> = deepLinks.receiveAsFlow()

    fun initialize(
        @Suppress("UNUSED_PARAMETER") application: Application,
        @Suppress("UNUSED_PARAMETER") config: AttributionConfig,
    ) = Unit

    fun isAttributionLink(uri: Uri): Boolean = uri.isRemovedServiceLink()

    fun interceptLink(uri: Uri, @Suppress("UNUSED_PARAMETER") isColdStart: Boolean): Boolean {
        return uri.isRemovedServiceLink()
    }

    fun setCustomerUserId(@Suppress("UNUSED_PARAMETER") customerUserId: String) = Unit

    fun onLauncherActivityCreated(@Suppress("UNUSED_PARAMETER") activity: Activity) = Unit

    fun withPrivateLinkHidden(activity: Activity, block: () -> Unit) {
        val intent = activity.intent
        val link = intent?.data
        if (link == null || isAttributionLink(link)) {
            block()
            return
        }
        activity.intent = Intent(intent).setData(null)
        try {
            block()
        } finally {
            activity.intent = intent
        }
    }

    private fun Uri.isRemovedServiceLink(): Boolean {
        if (host?.lowercase() in REMOVED_SERVICE_HOSTS) {
            return true
        }
        return scheme == APP_SCHEME && host == BRANCH_FALLBACK_HOST && getQueryParameter(BRANCH_LINK_CLICK_ID) != null
    }

    private companion object {
        const val APP_SCHEME = "tonkeeper"
        const val BRANCH_FALLBACK_HOST = "open"
        const val BRANCH_LINK_CLICK_ID = "link_click_id"
        val REMOVED_SERVICE_HOSTS = setOf(
            "x1zxr.app.link",
            "x1zxr-alternate.app.link",
            "link.tonkeeper.com",
        )
    }
}
