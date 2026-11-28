package com.tonapps.deposit.multicoin.screens.confirm

import com.tonapps.wallet.localization.Localization
import org.junit.Assert.assertEquals
import org.junit.Test

class TopUpLabelTest {
	@Test
	fun `top up label reflects fiat availability`() {
		assertEquals(Localization.buy_ton, topUpLabel(true))
		assertEquals(Localization.deposit_asset, topUpLabel(false))
	}
}
