package com.tonapps.perps.security

import com.tonapps.log.L
import com.tonapps.perps.data.PerpsPosition
import java.util.concurrent.ConcurrentHashMap

class PerpsMarketListing(
    private val securityConfig: PerpsSecurityConfigRepository,
) {

    private val reportedMarkets = ConcurrentHashMap.newKeySet<Int>()

    suspend fun signedSymbols(): Map<Int, String>? {
        return listingConfig()?.markets?.mapValues { (_, market) -> market.symbol }
    }

    suspend fun isListed(marketIndex: Int, catalogSymbol: String): Boolean {
        val config = listingConfig() ?: return true
        return config.markets[marketIndex]?.symbol == catalogSymbol
    }

    suspend fun reportUnlisted(positions: List<PerpsPosition>) {
        if (positions.isEmpty()) {
            return
        }
        val config = listingConfig() ?: return
        positions
            .filter { it.marketIndex !in config.markets && reportedMarkets.add(it.marketIndex) }
            .forEach { position ->
                L.e(PerpsUnlistedPositionException(position.marketIndex))
            }
    }

    private suspend fun listingConfig(): PerpsSecurityConfig? {
        return securityConfig.verifiedWithoutWaiting()?.takeIf { it.markets.isNotEmpty() }
    }

}

class PerpsUnlistedPositionException(marketIndex: Int) :
    IllegalStateException("perps position on market $marketIndex, which the signed perps config does not list")
