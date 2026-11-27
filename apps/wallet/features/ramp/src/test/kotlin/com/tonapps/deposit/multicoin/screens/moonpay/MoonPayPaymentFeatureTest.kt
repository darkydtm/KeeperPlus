package com.tonapps.deposit.multicoin.screens.moonpay

import androidx.lifecycle.SavedStateHandle
import com.tonapps.deposit.multicoin.moonpay.api.MoonPayAsset
import com.tonapps.deposit.multicoin.moonpay.api.MoonPayBuyQuote
import com.tonapps.deposit.multicoin.moonpay.api.MoonPayMoney
import com.tonapps.deposit.multicoin.moonpay.api.MoonPayPaymentMethodReference
import com.tonapps.deposit.multicoin.moonpay.domain.MoonPayPaymentFailure
import com.tonapps.deposit.multicoin.moonpay.domain.MoonPayButtonPayments
import com.tonapps.deposit.multicoin.moonpay.domain.MoonPayPaymentInteractor
import com.tonapps.deposit.multicoin.moonpay.domain.MoonPayPaymentState
import com.tonapps.deposit.testing.PlatformNumberFormat
import com.tonapps.wallet.testing.BaseUnitTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class MoonPayPaymentFeatureTest : BaseUnitTest() {

    private val updates = MutableStateFlow<MoonPayPaymentState>(MoonPayPaymentState.Starting)
    private val interactor = mockk<MoonPayPaymentInteractor> {
        every { pay(any(), any(), any(), any(), any()) } returns updates
    }
















    @Test
    fun `a failure before the start marks the first step`() {
        val feature = feature()
        updates.value = MoonPayPaymentState.Failed(MoonPayPaymentFailure.Unavailable, transactionId = null)

        assertEquals(MoonPayPaymentStepStatus.Failed, feature.steps.value.started.status)
        assertEquals(MoonPayPaymentStepStatus.Waiting, feature.steps.value.pending.status)
        assertEquals(MoonPayPaymentRetry.Again, feature.retry.value)
        assertFalse(feature.showsDetails.value)
        assertEquals(MoonPayPaymentDismissal.Back, feature.dismissal.value)
    }

    @Test
    fun `the first step is done only once MoonPay names the transaction`() {
        val feature = feature()
        updates.value = MoonPayPaymentState.Started(transactionId = null)

        assertEquals(MoonPayPaymentStepStatus.InProgress, feature.steps.value.started.status)
        assertEquals(MoonPayPaymentStepStatus.Waiting, feature.steps.value.pending.status)

        updates.value = MoonPayPaymentState.Started(transactionId = "tx-1")

        assertEquals(MoonPayPaymentStepStatus.Done, feature.steps.value.started.status)
        assertEquals(MoonPayPaymentStepStatus.InProgress, feature.steps.value.pending.status)
    }

    @Test
    fun `a failure after the start marks the second step`() {
        val feature = feature()
        updates.value = MoonPayPaymentState.Started(transactionId = "tx-1")
        updates.value = MoonPayPaymentState.Failed(MoonPayPaymentFailure.Declined, transactionId = "tx-1")

        assertEquals(MoonPayPaymentStepStatus.Done, feature.steps.value.started.status)
        assertEquals(
            MoonPayPaymentStep(MoonPayPaymentStepText.Failed(MoonPayPaymentFailure.Declined), MoonPayPaymentStepStatus.Failed),
            feature.steps.value.pending,
        )
        assertEquals(MoonPayPaymentRetry.AnotherCard, feature.retry.value)
        assertEquals("tx-1", feature.orderId.value)
    }

    @Test
    fun `a cancel after the start before MoonPay named a transaction goes back to buying`() = runTest {
        val feature = feature()
        val events = mutableListOf<MoonPayPaymentEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { feature.events.toList(events) }
        updates.value = MoonPayPaymentState.Started(transactionId = null)

        updates.value = MoonPayPaymentState.Failed(MoonPayPaymentFailure.Cancelled, transactionId = null)

        assertEquals(listOf(MoonPayPaymentEvent.Cancelled), events)
        assertNull(feature.retry.value)
    }

    @Test
    fun `a failure after a start MoonPay named no transaction for marks the first step`() {
        val feature = feature()
        updates.value = MoonPayPaymentState.Started(transactionId = null)
        updates.value = MoonPayPaymentState.Failed(MoonPayPaymentFailure.Declined, transactionId = null)

        assertEquals(
            MoonPayPaymentStep(MoonPayPaymentStepText.Failed(MoonPayPaymentFailure.Declined), MoonPayPaymentStepStatus.Failed),
            feature.steps.value.started,
        )
        assertEquals(MoonPayPaymentStepStatus.Waiting, feature.steps.value.pending.status)
        assertEquals(MoonPayPaymentDismissal.Back, feature.dismissal.value)
    }

    @Test
    fun `a decline naming its transaction only at the end marks the first step`() {
        val feature = feature()
        updates.value = MoonPayPaymentState.Started(transactionId = null)
        updates.value = MoonPayPaymentState.Failed(MoonPayPaymentFailure.Declined, transactionId = "tx-1")

        assertEquals(
            MoonPayPaymentStep(MoonPayPaymentStepText.Failed(MoonPayPaymentFailure.Declined), MoonPayPaymentStepStatus.Failed),
            feature.steps.value.started,
        )
        assertEquals(MoonPayPaymentStepStatus.Waiting, feature.steps.value.pending.status)
        assertEquals("tx-1", feature.orderId.value)
    }

    @Test
    fun `an unknown outcome with no transaction is shown on the first step`() {
        val feature = feature()
        updates.value = MoonPayPaymentState.Started(transactionId = null)
        updates.value = MoonPayPaymentState.Unknown(transactionId = null)

        assertEquals(
            MoonPayPaymentStep(MoonPayPaymentStepText.Unknown, MoonPayPaymentStepStatus.Failed),
            feature.steps.value.started,
        )
        assertEquals(MoonPayPaymentStepStatus.Waiting, feature.steps.value.completed.status)
        assertFalse(feature.showsDetails.value)
        assertEquals(MoonPayPaymentDismissal.CloseAll, feature.dismissal.value)
    }

    @Test
    fun `a closed sheet goes back to buying unless the payment went through, and is locked while MoonPay's frames take it`() {
        val feature = feature()
        assertEquals(MoonPayPaymentDismissal.Back, feature.dismissal.value)

        updates.value = MoonPayPaymentState.Started(transactionId = null)
        assertEquals(MoonPayPaymentDismissal.Blocked, feature.dismissal.value)

        updates.value = MoonPayPaymentState.Processing(transactionId = "tx-1")
        assertEquals(MoonPayPaymentDismissal.Leave, feature.dismissal.value)
        assertEquals(MoonPayPaymentStepStatus.InProgress, feature.steps.value.completed.status)

        updates.value = MoonPayPaymentState.Completed(transactionId = "tx-1")
        assertEquals(MoonPayPaymentDismissal.CloseAll, feature.dismissal.value)
        assertEquals(MoonPayPaymentStepStatus.Done, feature.steps.value.completed.status)
    }

    @Test
    fun `a payment cancelled before the start goes back without a failure`() = runTest {
        val feature = feature()
        val events = mutableListOf<MoonPayPaymentEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { feature.events.toList(events) }

        updates.value = MoonPayPaymentState.Failed(MoonPayPaymentFailure.Cancelled, transactionId = null)

        assertEquals(listOf(MoonPayPaymentEvent.Cancelled), events)
        assertEquals(MoonPayPaymentStepStatus.InProgress, feature.steps.value.started.status)
        assertNull(feature.retry.value)
    }

    @Test
    fun `a cancelled transaction MoonPay named is shown as failed`() = runTest {
        val feature = feature()
        val events = mutableListOf<MoonPayPaymentEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { feature.events.toList(events) }

        updates.value = MoonPayPaymentState.Failed(MoonPayPaymentFailure.TransactionCancelled, transactionId = "tx-1")

        assertTrue(events.isEmpty())
        assertEquals(MoonPayPaymentRetry.Again, feature.retry.value)
    }

    @Test
    fun `a verification failure offers no retry`() {
        val feature = feature()
        updates.value = MoonPayPaymentState.Failed(MoonPayPaymentFailure.VerificationPending, transactionId = null)

        assertNull(feature.retry.value)
        assertEquals(MoonPayPaymentDismissal.Back, feature.dismissal.value)
    }

    @Test
    fun `a screen restored before the start goes back without paying`() = runTest {
        val handle = SavedStateHandle()
        feature(handle)

        val restored = feature(handle)
        val events = mutableListOf<MoonPayPaymentEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { restored.events.toList(events) }

        verify(exactly = 1) { interactor.pay(any(), any(), any(), any(), any()) }
        assertEquals(listOf(MoonPayPaymentEvent.Cancelled), events)
    }

    @Test
    fun `a screen restored after a cancel goes back once`() = runTest {
        val handle = SavedStateHandle()
        feature(handle)
        updates.value = MoonPayPaymentState.Failed(MoonPayPaymentFailure.Cancelled, transactionId = null)

        val restored = feature(handle)
        val events = mutableListOf<MoonPayPaymentEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { restored.events.toList(events) }

        verify(exactly = 1) { interactor.pay(any(), any(), any(), any(), any()) }
        assertEquals(listOf(MoonPayPaymentEvent.Cancelled), events)
    }

    @Test
    fun `a screen restored after MoonPay named the transaction follows it`() {
        val handle = SavedStateHandle()
        feature(handle)
        updates.value = MoonPayPaymentState.Started(transactionId = "tx-1")
        every { interactor.verify("tx-1", any()) } returns flowOf(MoonPayPaymentState.Verifying("tx-1"))

        val checking = feature(handle)

        assertEquals(MoonPayPaymentStepStatus.Done, checking.steps.value.started.status)
        assertEquals(MoonPayPaymentStepStatus.InProgress, checking.steps.value.pending.status)

        every { interactor.verify("tx-1", any()) } returns flowOf(
            MoonPayPaymentState.Verifying("tx-1"),
            MoonPayPaymentState.Completed("tx-1"),
        )
        val restored = feature(handle)

        verify(exactly = 1) { interactor.pay(any(), any(), any(), any(), any()) }
        assertEquals(MoonPayPaymentStepStatus.Done, restored.steps.value.completed.status)
        assertEquals("tx-1", restored.orderId.value)
    }

    @Test
    fun `a transaction named only as the frames end is checked with MoonPay, nothing done until it answers`() {
        val feature = feature()
        updates.value = MoonPayPaymentState.Started(transactionId = null)
        updates.value = MoonPayPaymentState.Verifying("tx-1")

        assertEquals(
            MoonPayPaymentSteps(
                MoonPayPaymentStep(MoonPayPaymentStepText.Checking, MoonPayPaymentStepStatus.InProgress),
                feature.steps.value.pending.copy(status = MoonPayPaymentStepStatus.Waiting),
                feature.steps.value.completed.copy(status = MoonPayPaymentStepStatus.Waiting),
            ),
            feature.steps.value,
        )
        assertEquals(MoonPayPaymentDismissal.Leave, feature.dismissal.value)
        assertEquals("tx-1", feature.orderId.value)

        updates.value = MoonPayPaymentState.Failed(MoonPayPaymentFailure.Declined, "tx-1")

        assertEquals(
            MoonPayPaymentStep(MoonPayPaymentStepText.Failed(MoonPayPaymentFailure.Declined), MoonPayPaymentStepStatus.Failed),
            feature.steps.value.started,
        )
        assertEquals(MoonPayPaymentDismissal.Back, feature.dismissal.value)
    }

    @Test
    fun `a transaction checked with MoonPay that gives up shows the unknown outcome on the first step`() {
        val feature = feature()
        updates.value = MoonPayPaymentState.Started(transactionId = null)
        updates.value = MoonPayPaymentState.Verifying("tx-1")
        updates.value = MoonPayPaymentState.Unknown("tx-1")

        assertEquals(
            MoonPayPaymentStep(MoonPayPaymentStepText.Unknown, MoonPayPaymentStepStatus.Failed),
            feature.steps.value.started,
        )
        assertEquals(MoonPayPaymentStepStatus.Waiting, feature.steps.value.pending.status)
        assertTrue(feature.showsDetails.value)
        assertEquals(MoonPayPaymentDismissal.CloseAll, feature.dismissal.value)
        assertEquals("tx-1", feature.orderId.value)
    }

    @Test
    fun `a transaction MoonPay named before the frames ended keeps its start done while checked`() {
        val feature = feature()
        updates.value = MoonPayPaymentState.Started(transactionId = "tx-1")
        updates.value = MoonPayPaymentState.Verifying("tx-1")

        assertEquals(MoonPayPaymentStepStatus.Done, feature.steps.value.started.status)
        assertEquals(MoonPayPaymentStepStatus.InProgress, feature.steps.value.pending.status)

        updates.value = MoonPayPaymentState.Failed(MoonPayPaymentFailure.Declined, "tx-1")

        assertEquals(MoonPayPaymentStepStatus.Done, feature.steps.value.started.status)
        assertEquals(MoonPayPaymentStepStatus.Failed, feature.steps.value.pending.status)
    }

    @Test
    fun `a screen restored while checking a transaction checks it again`() {
        val handle = SavedStateHandle()
        feature(handle)
        updates.value = MoonPayPaymentState.Started(transactionId = null)
        updates.value = MoonPayPaymentState.Verifying("tx-1")
        every { interactor.verify("tx-1", any()) } returns flowOf(MoonPayPaymentState.Verifying("tx-1"))

        val restored = feature(handle)

        verify(exactly = 1) { interactor.pay(any(), any(), any(), any(), any()) }
        assertEquals(
            MoonPayPaymentStep(MoonPayPaymentStepText.Checking, MoonPayPaymentStepStatus.InProgress),
            restored.steps.value.started,
        )
        assertEquals(MoonPayPaymentDismissal.Leave, restored.dismissal.value)
    }

    @Test
    fun `a screen restored after a start without a transaction cannot learn the outcome`() {
        val handle = SavedStateHandle()
        feature(handle)
        updates.value = MoonPayPaymentState.Started(transactionId = null)

        val restored = feature(handle)

        verify(exactly = 1) { interactor.pay(any(), any(), any(), any(), any()) }
        assertEquals(
            MoonPayPaymentStep(MoonPayPaymentStepText.Unknown, MoonPayPaymentStepStatus.Failed),
            restored.steps.value.started,
        )
        assertEquals(
            MoonPayPaymentStep(MoonPayPaymentStepText.Pending(cleared = false), MoonPayPaymentStepStatus.Waiting),
            restored.steps.value.pending,
        )
        assertNull(restored.retry.value)
        assertEquals(MoonPayPaymentDismissal.CloseAll, restored.dismissal.value)
    }

    @Test
    fun `while the closed widget is looked up the sheet says so and stays`() = runTest {
        val feature = feature()
        val events = mutableListOf<MoonPayPaymentEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { feature.events.toList(events) }

        updates.value = MoonPayPaymentState.Checking
        assertEquals(
            MoonPayPaymentStep(MoonPayPaymentStepText.Checking, MoonPayPaymentStepStatus.InProgress),
            feature.steps.value.started,
        )
        assertEquals(MoonPayPaymentDismissal.Blocked, feature.dismissal.value)

        updates.value = MoonPayPaymentState.Failed(MoonPayPaymentFailure.Cancelled, transactionId = null)
        assertEquals(listOf<MoonPayPaymentEvent>(MoonPayPaymentEvent.Cancelled), events)
        assertEquals(MoonPayPaymentDismissal.Back, feature.dismissal.value)
    }

    @Test
    fun `an open widget keeps the sheet, and a restored one looks its payment up`() {
        every { interactor.lookUpWidget(1_000L, any()) } returns flowOf(MoonPayPaymentState.Checking)
        val handle = SavedStateHandle()
        val feature = feature(handle)
        updates.value = MoonPayPaymentState.WidgetOpened(1_000L)
        assertEquals(MoonPayPaymentDismissal.Blocked, feature.dismissal.value)

        val restored = feature(handle)

        verify(exactly = 1) { interactor.pay(any(), any(), any(), any(), any()) }
        verify(exactly = 1) { interactor.lookUpWidget(1_000L, any()) }
        assertEquals(MoonPayPaymentDismissal.Blocked, restored.dismissal.value)
    }

    @Test
    fun `a screen restored during the lookup looks again`() {
        every { interactor.lookUpWidget(1_000L, any()) } returns flowOf(MoonPayPaymentState.Checking)
        val handle = SavedStateHandle()
        feature(handle)
        updates.value = MoonPayPaymentState.WidgetOpened(1_000L)
        updates.value = MoonPayPaymentState.Checking

        feature(handle)

        verify(exactly = 1) { interactor.lookUpWidget(1_000L, any()) }
    }

    @Test
    fun `a screen restored while still processing polls to the same deadline`() {
        val handle = SavedStateHandle()
        feature(handle)
        updates.value = MoonPayPaymentState.Processing("tx-1")
        updates.value = MoonPayPaymentState.StillProcessing("tx-1", pollsUntil = 1_800_000L)
        every { interactor.followUntil("tx-1", any(), 1_800_000L) } returns flowOf(
            MoonPayPaymentState.StillProcessing("tx-1", pollsUntil = 1_800_000L),
            MoonPayPaymentState.Completed("tx-1"),
        )

        val restored = feature(handle)

        verify(exactly = 1) { interactor.pay(any(), any(), any(), any(), any()) }
        assertEquals(MoonPayPaymentStepStatus.Done, restored.steps.value.completed.status)
    }

    @Test
    fun `a payment still processing can be closed and still completes`() {
        val feature = feature()
        updates.value = MoonPayPaymentState.Processing("tx-1")
        updates.value = MoonPayPaymentState.StillProcessing("tx-1", pollsUntil = 1_800_000L)

        assertEquals(MoonPayPaymentStepText.StillProcessing, feature.steps.value.completed.text)
        assertEquals(MoonPayPaymentDismissal.Leave, feature.dismissal.value)

        updates.value = MoonPayPaymentState.Completed("tx-1")

        assertEquals(MoonPayPaymentStepStatus.Done, feature.steps.value.completed.status)
    }

    @Test
    fun `a failure after still processing keeps the start done`() {
        val feature = feature()
        updates.value = MoonPayPaymentState.Processing("tx-1")
        updates.value = MoonPayPaymentState.StillProcessing("tx-1", pollsUntil = 1_800_000L)
        updates.value = MoonPayPaymentState.Failed(MoonPayPaymentFailure.Declined, transactionId = "tx-1")

        assertEquals(MoonPayPaymentStepStatus.Done, feature.steps.value.started.status)
        assertEquals(MoonPayPaymentStepStatus.Failed, feature.steps.value.pending.status)
    }

    @Test
    fun `an unknown outcome is shown on the last step without a retry`() {
        val feature = feature()
        updates.value = MoonPayPaymentState.Processing("tx-1")
        updates.value = MoonPayPaymentState.Unknown("tx-1")

        assertEquals(MoonPayPaymentStepStatus.Done, feature.steps.value.pending.status)
        assertEquals(
            MoonPayPaymentStep(MoonPayPaymentStepText.Unknown, MoonPayPaymentStepStatus.Failed),
            feature.steps.value.completed,
        )
        assertNull(feature.retry.value)
        assertTrue(feature.showsDetails.value)
        assertEquals(MoonPayPaymentDismissal.CloseAll, feature.dismissal.value)
        assertEquals("tx-1", feature.orderId.value)
    }

    @Test
    fun `a screen restored after a failure shows it again`() {
        val handle = SavedStateHandle()
        feature(handle)
        updates.value = MoonPayPaymentState.Started(transactionId = "tx-1")
        updates.value = MoonPayPaymentState.Failed(MoonPayPaymentFailure.Declined, transactionId = "tx-1")

        val restored = feature(handle)

        verify(exactly = 1) { interactor.pay(any(), any(), any(), any(), any()) }
        assertEquals(MoonPayPaymentStepStatus.Failed, restored.steps.value.pending.status)
        assertEquals(MoonPayPaymentRetry.AnotherCard, restored.retry.value)
    }

    @Test
    fun `fees in another currency are left out of the rows and the total`() {
        val feature = feature(
            quote = quote(
                fees = mapOf(
                    "moonpay" to usd("2.5"),
                    "network" to usd("0.5"),
                    "defi" to MoonPayMoney(amount = "1", asset = MoonPayAsset(code = "ton")),
                ),
            ),
        )

        assertTrue(feature.summary.feesTotal, feature.summary.feesTotal.contains("3.00"))
        assertEquals(
            listOf(MoonPayPaymentFeeKind.MoonPay, MoonPayPaymentFeeKind.Network),
            feature.summary.fees.map { it.kind },
        )
    }

    @Test
    fun `the deprecated partner fee is neither a row nor counted`() {
        val feature = feature(
            quote = quote(fees = mapOf("ecosystem" to usd("1.5"), "partner" to usd("1.5"))),
        )

        assertTrue(feature.summary.feesTotal, feature.summary.feesTotal.contains("1.50"))
        assertEquals(listOf(MoonPayPaymentFeeKind.Ecosystem), feature.summary.fees.map { it.kind })
    }

    @Test
    fun `fees are shown in the design's order with the rest as one row`() {
        val feature = feature(
            quote = quote(
                fees = mapOf(
                    "defi" to usd("0.25"),
                    "network" to usd("0.5"),
                    "extra" to usd("0.25"),
                    "ecosystem" to usd("1"),
                    "moonpay" to usd("2"),
                ),
            ),
        )

        assertEquals(
            listOf(
                MoonPayPaymentFeeKind.MoonPay,
                MoonPayPaymentFeeKind.Ecosystem,
                MoonPayPaymentFeeKind.Network,
                MoonPayPaymentFeeKind.Other,
            ),
            feature.summary.fees.map { it.kind },
        )
        assertTrue(feature.summary.fees.last().amount, feature.summary.fees.last().amount.contains("0.50"))
        assertTrue(feature.summary.feesTotal, feature.summary.feesTotal.contains("4.00"))
    }

    @Test
    fun `fee keys and currencies match whatever their case`() {
        val feature = feature(
            quote = quote(
                fees = mapOf(
                    "MoonPay" to MoonPayMoney(amount = "2", asset = MoonPayAsset(code = "USD")),
                    "moonpay" to usd("1"),
                    "Partner" to usd("5"),
                ),
            ),
        )

        assertEquals(listOf(MoonPayPaymentFeeKind.MoonPay), feature.summary.fees.map { it.kind })
        assertTrue(feature.summary.fees.single().amount, feature.summary.fees.single().amount.contains("3.00"))
        assertTrue(feature.summary.feesTotal, feature.summary.feesTotal.contains("3.00"))
    }

    @Test
    fun `a round paid amount has no cents, the received one is rounded to two decimals`() {
        val feature = feature(
            quote = quote(
                source = MoonPayMoney(amount = "200", asset = MoonPayAsset(code = "usd")),
                destination = MoonPayMoney(amount = "1234.567891", asset = MoonPayAsset(code = "ton")),
            ),
        )

        assertTrue(feature.summary.paid, feature.summary.paid.contains("200") && !feature.summary.paid.contains("200.00"))
        assertTrue(feature.summary.receive, feature.summary.receive.filter { it.isDigit() || it == '.' } == "1234.57")
    }

    @Test
    fun `the received asset is named as on the buying screen, small amounts keep three significant digits`() {
        val feature = feature(
            quote = quote(destination = MoonPayMoney(amount = "0.006738808", asset = MoonPayAsset(code = "eth_arbitrum"))),
            assetLabel = "ETH (Arbitrum)",
        )

        assertEquals("0.00674 ETH (Arbitrum)", feature.summary.receive)
    }

    @Test
    fun `amounts paid keep every digit MoonPay sent`() {
        val feature = feature(
            quote = quote(
                source = usd("1234.5"),
                fees = mapOf("moonpay" to usd("2.955"), "network" to usd("1.045")),
            ),
        )

        assertTrue(feature.summary.paid, feature.summary.paid.contains("1234.50"))
        assertEquals(listOf("2.955", "1.045"), feature.summary.fees.map { it.amount.substringBefore(' ') })
        assertTrue(feature.summary.feesTotal, feature.summary.feesTotal.contains("4.00"))
    }

    @Test
    fun `an amount MoonPay did not send is not shown as zero`() {
        val feature = feature(quote = quote(destination = MoonPayMoney(amount = null, asset = MoonPayAsset(code = "ton"))))

        assertEquals("—", feature.summary.receive)
    }

    @Test
    fun `after a tap on MoonPay's button the sheet closes like a failure until MoonPay names a transaction`() {
        val quote = quote(method = "google_pay")
        val buttons = MoonPayButtonPayments(interactor)
        buttons.start(MoonPayPaymentData(quote, "Google Pay", "order-1", theme = null))
        val feature = MoonPayPaymentFeature(MoonPayPaymentData(quote, "Google Pay", "order-1", theme = null, , interactor, buttons, SavedStateHandle())
        assertEquals(MoonPayPaymentDismissal.Back, feature.dismissal.value)

        updates.value = MoonPayPaymentState.ButtonPressed
        updates.value = MoonPayPaymentState.WidgetOpened(1_000L)

        assertEquals(MoonPayPaymentDismissal.Back, feature.dismissal.value)
    }

    @Test
    fun `a declined payment by another method than a card offers another method`() {
        val feature = feature(quote = quote(method = "paypal"))
        updates.value = MoonPayPaymentState.Failed(MoonPayPaymentFailure.Declined, transactionId = null)

        assertEquals(MoonPayPaymentRetry.AnotherMethod, feature.retry.value)
    }

    private fun feature(
        handle: SavedStateHandle = SavedStateHandle(),
        quote: MoonPayBuyQuote = quote(),
        assetLabel: String? = null,
    ): MoonPayPaymentFeature {
        val data = MoonPayPaymentData(
            quote,
            paymentTitle = "Visa ··4242",
            externalTransactionId = "order-1",
            theme = null,
            assetLabel = assetLabel,

        )
        return MoonPayPaymentFeature(data, interactor, MoonPayButtonPayments(interactor), handle)
    }

    private fun usd(amount: String): MoonPayMoney = MoonPayMoney(amount = amount, asset = MoonPayAsset(code = "usd"))

    private fun quote(
        source: MoonPayMoney = MoonPayMoney(amount = "100", asset = MoonPayAsset(code = "usd")),
        destination: MoonPayMoney = MoonPayMoney(amount = "20", asset = MoonPayAsset(code = "ton")),
        fees: Map<String, MoonPayMoney>? = null,
        method: String = "card",
    ): MoonPayBuyQuote {
        return MoonPayBuyQuote(
            source = source,
            destination = destination,
            fees = fees,
            paymentMethod = MoonPayPaymentMethodReference(type = method),
            signature = "signature",
        )
    }

    companion object {

        @JvmStatic
        @BeforeClass
        fun stubPlatformNumberFormat() {
            PlatformNumberFormat.stub()
        }
    }
}
