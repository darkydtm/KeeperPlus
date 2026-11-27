package com.tonapps.portfolio.analytics

import com.tonapps.bus.core.contract.UtmParameters

/** Compatibility facade for the removed staking telemetry events. */
object StakingAnalytics {

    data class Props(
        val jettonSymbol: String,
        val providerName: String,
        val providerDomain: String,
    )

    fun open(from: String, utm: UtmParameters) = Unit

    fun stakeInput(from: String, props: Props, utm: UtmParameters) = Unit

    fun stakeConfirm(props: Props, utm: UtmParameters) = Unit

    fun stakeSuccess(props: Props, utm: UtmParameters) = Unit

    fun unstakeInput(from: String, props: Props, utm: UtmParameters) = Unit

    fun unstakeConfirm(props: Props, utm: UtmParameters) = Unit

    fun unstakeSuccess(props: Props, utm: UtmParameters) = Unit
}
