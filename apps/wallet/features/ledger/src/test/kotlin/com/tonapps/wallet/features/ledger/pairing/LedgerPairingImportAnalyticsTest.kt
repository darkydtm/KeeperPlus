package com.tonapps.wallet.features.ledger.pairing

import android.util.Log
import androidx.lifecycle.ViewModelStore
import com.tonapps.bus.walletimport.WalletImportPairingSource
import com.tonapps.bus.walletimport.WalletImportPairingSessions
import com.tonapps.chainkit.core.chain.model.account.Address
import com.tonapps.chainkit.core.chain.model.account.Chain
import com.tonapps.ledgerkit.LedgerAppId
import com.tonapps.ledgerkit.LedgerFailure
import com.tonapps.ledgerkit.controller.LedgerController
import com.tonapps.ledgerkit.controller.LedgerOpenLink
import com.tonapps.ledgerkit.controller.LedgerOperationState
import com.tonapps.ledgerkit.session.LedgerAppInfo
import com.tonapps.ledgerkit.transport.LedgerLinkKind
import com.tonapps.ledgerkit.transport.LedgerModel
import com.tonapps.ledgerkit.transport.ble.LedgerBleDevice
import com.tonapps.ledgerkit.transport.ble.LedgerBleEnvironment
import com.tonapps.ledgerkit.transport.ble.LedgerBleReadiness
import com.tonapps.test.TestDispatchersRule
import com.tonapps.wallet.data.ledger.LedgerChainApp
import com.tonapps.wallet.data.ledger.discovery.LedgerAppDiscoveryState
import com.tonapps.wallet.data.ledger.discovery.LedgerDiscoveredAccount
import com.tonapps.wallet.data.ledger.discovery.LedgerDiscoveryInteractor
import com.tonapps.wallet.data.ledger.discovery.LedgerDiscoveryResult
import com.tonapps.wallet.data.multichain.account.McAccountRepository
import com.tonapps.wallet.data.multichain.hardware.HardwareAccountDraft
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import com.tonapps.ledgerkit.transport.ble.LedgerBleScanner
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList

class LedgerPairingImportAnalyticsTest {

    @get:Rule
    val dispatchers = TestDispatchersRule()

    private val scheduler get() = dispatchers.scheduler

    private val link = MutableStateFlow<LedgerOpenLink?>(null)
    private val environment = mockk<LedgerBleEnvironment> { every { check() } returns LedgerBleReadiness.Ready }
    private val controller = mockk<LedgerController>(relaxed = true) {
        every { state } returns MutableStateFlow<LedgerOperationState>(LedgerOperationState.Idle)
        every { link } returns this@LedgerPairingImportAnalyticsTest.link
    }
    private val discovery = mockk<LedgerDiscoveryInteractor>()
    private val accounts = mockk<McAccountRepository> {
        coEvery { findWalletIdsByAddress(any(), any()) } returns emptyList()
    }
    private val store = ViewModelStore()
    private val scanner = mockk<LedgerBleScanner> { every { scan() } returns flowOf(listOf(DEVICE), listOf(DEVICE)) }
    private val tracked = CopyOnWriteArrayList<Pair<String, Map<String, Any>>>()

    @Before
    fun setUp() {
            override fun trackEvent(eventName: String, props: Map<String, Any>) {
                tracked += eventName to props
            }
        }
        mockkStatic(Log::class)
        every { Log.println(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        store.clear()
        unmockkAll()
    }

    private fun feature(pairingSessions: WalletImportPairingSessions = WalletImportPairingSessions()): LedgerPairingFeature {
        val feature = LedgerPairingFeature(
            LedgerPairingMode.New(WalletFlowFrom.Main),
            environment,
            scanner,
            mockk(relaxed = true),
            controller,
            discovery,
            mockk(relaxed = true),
            accounts,
            pairingSessions,
        )
        store.put("pairing", feature)
        scheduler.advanceUntilIdle()
        return feature
    }

    private fun discovered() = LedgerDiscoveryResult(
        accounts = listOf(
            LedgerDiscoveredAccount(
                index = 0,
                accounts = listOf(HardwareAccountDraft(Chain.Ton.Mainnet, Address.Type.TonV4R2, TON_ADDRESS, TON_KEY)),
                bitcoinMasterFingerprint = null,
                bitcoinAccountXpub = null,
            ),
        ),
        apps = mapOf(
            LedgerChainApp.Ton to LedgerAppDiscoveryState.Done,
            LedgerChainApp.Ethereum to LedgerAppDiscoveryState.Done,
            LedgerChainApp.Tron to LedgerAppDiscoveryState.Skipped(LedgerFailure.AppNotInstalled(LedgerAppId.Tron)),
        ),
    )

    @Test
    fun `closing pairing finishes the active session`() {
        val pairingSessions = WalletImportPairingSessions()
        val session = pairingSessions.start(WalletImportPairingSource.Ledger)
        feature(pairingSessions)

        store.clear()

        assertEquals(false, session.isActive)
    }

    @Test
    fun `an import session handed to the import screen does not report leaving`() {
        coEvery { discovery.discover(any(), any(), any(), any(), any(), any(), any()) } returns discovered()
        val pairingSessions = WalletImportPairingSessions()
        val session = pairingSessions.start(WalletImportPairingSource.Ledger)
        val feature = feature(pairingSessions)
        session.signerAppOpened()
        link.value = LedgerOpenLink(DEVICE.address, LedgerAppInfo("TON", "2.9.1"), LedgerLinkKind.Bluetooth)
        feature.selectDevice(DEVICE)
        scheduler.advanceUntilIdle()

        assertEquals(false, session.isAwaitingSignerReturn)
        feature.importSelected()
        scheduler.advanceUntilIdle()
        store.clear()

        assertEquals(true, session.isActive)
    }

    private companion object {
        const val TON_KEY = "0b1f4a8e6c2d9e7f30a5b4c3d2e1f0112233445566778899aabbccddeeff0011"
        const val TON_ADDRESS = "UQLedgerAddress"
        val DEVICE = LedgerBleDevice("C1:2B:3C:4D:5E:6F", "Stax", LedgerModel.Stax, rssi = 0, lastSeenAt = 0)
    }
}
