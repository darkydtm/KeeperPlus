package com.tonapps.wallet.data.raffle

import android.content.Context
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.settings.SettingsRepository
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.wasNot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RaffleRepositoryTest {
    @Test fun disabledBlocksCachePrefetchAndImportReport() = runBlocking {
        val enabled = MutableStateFlow(false)
        val settings = mockk<SettingsRepository>()
        every { settings.rewardsEnabledFlow } returns enabled
        every { settings.rewardsEnabled } answers { enabled.value }
		every { settings.promotionsEnabled } returns true
		every { settings.recommendationsEnabled } returns true
        val api = mockk<API>()
        val repository = RaffleRepository(mockk<Context>(), api, settings)

        assertEquals(emptyList<Any>(), repository.getRafflesFlow("wallet", forced = true).first())
        assertEquals(emptyList<Any>(), repository.getRafflesFlowAfterPrefetch("wallet").first())
        assertNull(repository.getRaffle("wallet", null, forced = true))
        assertNull(repository.getRaffleWithStory("wallet", "story"))
        repository.prefetch("wallet")
        repository.reportImport("wallet", "imported")
        verify { api wasNot Called }
    }
}
