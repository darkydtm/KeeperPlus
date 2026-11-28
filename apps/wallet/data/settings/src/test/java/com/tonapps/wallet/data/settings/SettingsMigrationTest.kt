package com.tonapps.wallet.data.settings

import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class SettingsMigrationTest {
	@Test fun migratesLegacyFalse() {
		val prefs = mockk<SharedPreferences>(relaxed = true)
		every { prefs.getBoolean("promotions_enabled", true) } returns false
		every { prefs.contains(any()) } returns false
		migrateContentSettings(prefs)
		verify { prefs.edit().putBoolean("ads_enabled", false) }
		verify { prefs.edit().putBoolean("rewards_enabled", false) }
		verify { prefs.edit().putBoolean("recommendations_enabled", false) }
	}

	@Test fun preservesExistingIndependentValues() {
		val prefs = mockk<SharedPreferences>(relaxed = true)
		every { prefs.getBoolean("promotions_enabled", true) } returns true
		every { prefs.contains("ads_enabled") } returns true
		every { prefs.contains("rewards_enabled") } returns true
		every { prefs.contains("recommendations_enabled") } returns false
		migrateContentSettings(prefs)
		verify(exactly = 0) { prefs.edit().putBoolean("ads_enabled", any()) }
		verify(exactly = 0) { prefs.edit().putBoolean("rewards_enabled", any()) }
		verify { prefs.edit().putBoolean("recommendations_enabled", true) }
	}
}
