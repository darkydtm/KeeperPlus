package com.tonapps.tonkeeper.ui.screen.browser.dapp.earn

import com.tonapps.bus.generated.Events.EarnFlow.EarnFlowChain
import com.tonapps.bus.generated.Events.EarnFlow.EarnFlowMethod
import com.tonapps.chainkit.core.chain.model.account.Chain
import com.tonapps.log.L
import com.tonapps.tonkeeper.ui.screen.browser.dapp.evm.EvmProviderProtocol
import java.net.URI
import java.net.URISyntaxException

internal class EarnDAppAnalytics {
    internal companion object {

        private const val EARN_SEGMENT = "earn"
        private const val VAULT_ROUTE_SIZE = 3

        /** The vault of an `/earn/{network}/{address}` page as its Asset ID, null on any other page. */
        fun vaultId(pageUrl: String?): String? {
            val path = try {
                pageUrl?.let { URI(it).path }
            } catch (_: URISyntaxException) {
                null
            } ?: return null
            val segments = path.split('/').filter { it.isNotEmpty() }
            val earnIndex = segments.lastIndexOf(EARN_SEGMENT)
            if (earnIndex < 0 || segments.size != earnIndex + VAULT_ROUTE_SIZE) {
                return null
            }
            val chainAndType = when (segments[earnIndex + 1].lowercase()) {
                "mainnet" -> "eth/mainnet/erc20"
                "base" -> "base/mainnet/erc20"
                "arbitrum" -> "arb/mainnet/erc20"
                "bsc" -> "bsc/mainnet/bep20"
                else -> return null
            }
            return "$chainAndType/${segments[earnIndex + 2].lowercase()}"
        }

        fun method(method: String): EarnFlowMethod? {
            return when (method) {
                EvmProviderProtocol.METHOD_SEND_TRANSACTION -> EarnFlowMethod.SendTransaction
                "personal_sign" -> EarnFlowMethod.PersonalSign
                "eth_signTypedData_v4" -> EarnFlowMethod.SignTypedData
                else -> {
                    L.w("No Earn sign method for $method")
                    null
                }
            }
        }

        fun chain(chain: Chain.Evm): EarnFlowChain {
            return when (chain.coinAssetId.substringBefore('/')) {
                "eth" -> EarnFlowChain.Eth
                "base" -> EarnFlowChain.Base
                "arb" -> EarnFlowChain.Arb
                "bsc" -> EarnFlowChain.Bsc
                "polygon" -> EarnFlowChain.Polygon
                "avax" -> EarnFlowChain.Avax
                "robinhood" -> EarnFlowChain.Robinhood
                else -> EarnFlowChain.Multichain
            }
        }
    }
}
