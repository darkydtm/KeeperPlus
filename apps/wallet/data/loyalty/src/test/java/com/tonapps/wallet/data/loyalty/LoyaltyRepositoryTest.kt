package com.tonapps.wallet.data.loyalty

import android.content.Context
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.LoyaltyProfileEntity
import com.tonapps.wallet.data.settings.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LoyaltyRepositoryTest {
	@get:Rule
	val temporaryFolder = TemporaryFolder()

	@Test
	fun enabledRewardsUseServerVisibility() = runBlocking {
		val context = mockk<Context>()
		every { context.cacheDir } returns temporaryFolder.root
		val settings = mockk<SettingsRepository>()
		every { settings.rewardsEnabled } returns true
		val api = mockk<API>(relaxed = true)
		every { api.loyalty.getProfile("visible") } returns LoyaltyProfileEntity(true, true)
		every { api.loyalty.getProfile("hidden") } returns LoyaltyProfileEntity(false, true)
		val repository = LoyaltyRepository(context, api, settings)

		assertTrue(repository.showsSection("visible"))
		assertFalse(repository.showsSection("hidden"))
	}

	@Test
	fun disabledRewardsDoNotRequestProfile() = runBlocking {
		val settings = mockk<SettingsRepository>()
		every { settings.rewardsEnabled } returns false
		val api = mockk<API>()
		val repository = LoyaltyRepository(mockk(), api, settings)

		assertFalse(repository.showsSection("wallet"))
		verify(exactly = 0) { api.loyalty }
	}
}
