package com.tonapps.portfolio.analytics

import com.tonapps.blockchain.model.legacy.WalletEntity

/**
 * Compatibility facade for callers that used to report wallet-open analytics.
 * Opening a wallet remains the caller's responsibility; no telemetry is emitted.
 */
object PortfolioAnalytics {
    fun open(wallet: WalletEntity) = Unit
}
