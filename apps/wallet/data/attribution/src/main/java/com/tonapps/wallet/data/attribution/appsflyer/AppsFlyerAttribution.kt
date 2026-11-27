package com.tonapps.wallet.data.attribution.appsflyer

import android.app.Activity
import android.app.Application
import android.net.Uri
import com.tonapps.wallet.data.attribution.AttributionDeepLink

/** Local parsing only. Short and deferred OneLinks require the removed attribution service. */
internal class AppsFlyerAttribution {
    fun initialize(
        application: Application,
        devKey: String,
        isDebug: Boolean,
        installReferrer: suspend () -> String?,
        onAppLink: (AttributionDeepLink) -> Unit,
        onInstall: (AppsFlyerInstall) -> Unit,
    ) = Unit

    fun setCustomerUserId(customerUserId: String) = Unit

    fun isOneLink(uri: Uri): Boolean =
        isOneLinkHost(uri.host) && (uri.scheme == "https" || uri.scheme == "http")

    fun isResolvedBySdk(uri: Uri): Boolean =
        isOneLink(uri) && isSdkResolvedOneLink(uri.pathSegments, uri.getQueryParameter(DEEP_LINK_VALUE_PARAM))

    fun appLinkOf(oneLink: Uri): String =
        appsFlyerAppLink(appsFlyerValuesOf(oneLink.queryParameterNames.associateWith(oneLink::getQueryParameter)))

    fun collectLaunchSource(activity: Activity) = Unit
}
