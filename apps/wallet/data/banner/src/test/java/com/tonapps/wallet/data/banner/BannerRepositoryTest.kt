package com.tonapps.wallet.data.banner

import android.content.Context
import android.content.SharedPreferences
import com.tonapps.blockchain.ton.TonNetwork
import com.tonapps.wallet.api.API
import com.tonapps.wallet.api.entity.BannerEntity
import com.tonapps.wallet.data.banner.entities.BannerDataEntity
import com.tonapps.wallet.data.banner.source.LocalDataSource
import com.tonapps.wallet.data.settings.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class BannerRepositoryTest {
    @After fun tearDown() = unmockkAll()

    @Test fun disabledHidesCacheAndRequestsResumeWhenEnabled() = runBlocking {
        val enabled = MutableStateFlow(false)
        val settings = mockk<SettingsRepository>()
        every { settings.promotionsEnabledFlow } returns enabled
        every { settings.promotionsEnabled } answers { enabled.value }
		every { settings.rewardsEnabled } returns false
		every { settings.recommendationsEnabled } returns false
        every { settings.hadWalletsOnMultichainRelease } returns true
        val prefs = mockk<SharedPreferences>()
        every { prefs.getStringSet(any(), any()) } returns emptySet()
        val context = mockk<Context>()
        every { context.getSharedPreferences(any(), any()) } returns prefs
        val api = mockk<API>()
        val cached = BannerEntity("cached", "Cached", "", null, null, null, null)
        val remote = BannerEntity("remote", "Remote", "", null, null, null, null)
        every { api.getBanners(any(), any(), any()) } returns listOf(remote)
        mockkConstructor(LocalDataSource::class)
        every { anyConstructed<LocalDataSource>().getCache(any()) } returns BannerDataEntity(listOf(cached))
        every { anyConstructed<LocalDataSource>().setCache(any(), any()) } returns Unit
        val repository = BannerRepository(context, api, settings)

        assertEquals(emptyList<BannerEntity>(), repository.getBanners("wallet", true, refresh = true))
        assertEquals(emptyList<BannerEntity>(), repository.getBannersFlow("wallet", true).first())
        verify(exactly = 0) { api.getBanners(any(), any(), any()) }

        enabled.value = true
        assertEquals(listOf(listOf(cached), listOf(remote)), repository.getBannersFlow("wallet", true).take(2).toList())
        verify(exactly = 1) { api.getBanners(TonNetwork.MAINNET, "wallet", false) }

        enabled.value = false
        assertEquals(emptyList<BannerEntity>(), repository.getBannersFlow("wallet", true).first())
        verify(exactly = 1) { api.getBanners(any(), any(), any()) }
        enabled.value = true
        assertEquals(listOf(remote), repository.getBanners("wallet", true, refresh = true))
        verify(exactly = 2) { api.getBanners(any(), any(), any()) }
    }
}
