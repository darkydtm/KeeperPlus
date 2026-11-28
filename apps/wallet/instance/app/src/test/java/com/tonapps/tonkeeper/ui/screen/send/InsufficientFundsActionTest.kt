package com.tonapps.tonkeeper.ui.screen.send

import com.tonapps.wallet.localization.Localization
import org.junit.Assert.assertEquals
import org.junit.Test

class InsufficientFundsActionTest {
	@Test
	fun `fiat setting changes ton purchase to deposit`() {
		assertEquals(Localization.deposit_asset, fundingActionLabel(isTon = true, fiatEnabled = false))
		assertEquals(Localization.buy_ton, fundingActionLabel(isTon = true, fiatEnabled = true))
		assertEquals(Localization.buy_ton, fundingActionLabel(isTon = false, fiatEnabled = false))
	}
}
