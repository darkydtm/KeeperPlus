package com.tonapps.tonkeeper.ui.screen.send

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InsufficientFundsActionTest {
	@Test
	fun `fiat setting hides only the ton purchase action`() {
		assertFalse(showFundingAction(isBattery = false, isTon = true, fiatEnabled = false))
		assertTrue(showFundingAction(isBattery = false, isTon = true, fiatEnabled = true))
		assertTrue(showFundingAction(isBattery = false, isTon = false, fiatEnabled = false))
		assertFalse(showFundingAction(isBattery = true, isTon = false, fiatEnabled = true))
	}
}
