package com.tonapps.deposit.multicoin

import com.tonapps.deposit.screens.ramp.RampType

// The same flow sells to a card, so the new screen replaces only the buying path.
internal fun buyCryptoEnabled(rampType: RampType, flagEnabled: Boolean): Boolean {
    return rampType == RampType.RampOn && flagEnabled
}

internal fun startRoute(initial: DepositMulticoinRoutes?, buyCrypto: Boolean, fiatEnabled: Boolean = true): DepositMulticoinRoutes {
	val route = if (initial is DepositMulticoinRoutes.TopUp) {
		if (fiatEnabled) DepositMulticoinRoutes.Buy(initial.assetId) else DepositMulticoinRoutes.Receive
	} else {
		initial
	}
	return when {
		route is DepositMulticoinRoutes.Buy && buyCrypto -> DepositMulticoinRoutes.BuyCrypto(route.assetId, route.preferredCurrency)
		route == null && buyCrypto -> DepositMulticoinRoutes.BuyCrypto()
		else -> route ?: DepositMulticoinRoutes.Ramp
	}
}
