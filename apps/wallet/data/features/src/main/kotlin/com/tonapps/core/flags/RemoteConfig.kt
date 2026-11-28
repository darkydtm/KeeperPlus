package com.tonapps.core.flags

import com.tonapps.apps.wallet.data.features.BuildConfig

/**
 * Local feature defaults.
 *
 * The former Firebase Remote Config transport was removed so feature evaluation does not
 * initialize a telemetry SDK or make an implicit network request. Runtime overrides can still
 * be supplied through [FeatureManager]'s existing developer/static override mechanism.
 */
@Deprecated("Use this class through Features")
class RemoteConfig : RemoteConfigProvider {

    private val values: Map<String, Any> = mapOf(
        "inAppUpdateAvailable" to false,
        "onboarding_stories_enabled" to true,
        WalletFeatureKey.IS_SWAPKIT_HARD_SWITCH_ENABLED.featureKey to false,
        WalletFeatureKey.IS_SWAP_EXACT_OUTPUT_ENABLED.featureKey to false,
        WalletFeatureKey.IS_SWAP_V3_ENABLED.featureKey to true,
        WalletFeatureKey.IS_SWAP_REDESIGN_ENABLED.featureKey to false,
        WalletFeatureKey.IS_LIQUID_GLASS_ENABLED.featureKey to BuildConfig.DEBUG,
		WalletFeatureKey.IS_PERPS_ENABLED.featureKey to true,
        WalletFeatureKey.IS_PERPS_BATTERY_FEE_ENABLED.featureKey to false,
        WalletFeatureKey.IS_EARN_ENABLED.featureKey to false,
        WalletFeatureKey.LOG_DEFAULT_ON.featureKey to false,
        WalletFeatureKey.IS_IN_APP_REVIEW_ENABLED.featureKey to false,
        WalletFeatureKey.TONCONNECT_SIGN_MESSAGE_ENABLED.featureKey to true,
        WalletFeatureKey.IS_LEDGER_MULTICHAIN_ENABLED.featureKey to BuildConfig.DEBUG,
        WalletFeatureKey.OPERATION_TRANSFER_ENABLED.featureKey to true,
        WalletFeatureKey.OPERATION_DAPP_ENABLED.featureKey to true,
        WalletFeatureKey.IS_MOONPAY_RAMP_ENABLED.featureKey to false,
        WalletFeatureKey.IS_MOONPAY_GOOGLE_PAY_ENABLED.featureKey to false,
        WalletFeatureKey.IS_LOYALTY_ENABLED.featureKey to false,
        WalletFeatureKey.IMPORT_BIP39_AS_LEGACY.featureKey to false,
        WalletFeatureKey.IS_LEGACY_BIP39_MIGRATION_ENABLED.featureKey to false,
    )

    val inAppUpdateAvailable: Boolean
        get() = values["inAppUpdateAvailable"] as? Boolean ?: false

    val isOnboardingStoriesEnabled: Boolean
        get() = values["onboarding_stories_enabled"] as? Boolean ?: false

    override fun isFeatureEnabled(feature: FeatureKey): Boolean {
        return values[feature.featureKey] as? Boolean ?: false
    }

    override fun getFeatureValue(feature: FeatureKey, default: String): String {
        return values[feature.featureKey]?.toString() ?: default
    }

    override fun optFeatureValue(feature: FeatureKey): String? {
        return values[feature.featureKey]?.toString()
    }

    override fun isRemoteResolved(feature: FeatureKey): Boolean = false

    fun fetchAndActivate() = Unit
}
