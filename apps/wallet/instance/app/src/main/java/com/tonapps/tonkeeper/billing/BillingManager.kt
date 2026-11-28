package com.tonapps.tonkeeper.billing

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/** Local billing models kept as a compatibility surface for the battery UI. */
data class BillingProduct(
    val productId: String,
    val priceFormatted: CharSequence = "",
)

data class BillingPurchase(
    val products: List<String>,
    val purchaseToken: String,
)

data class BillingResult(
    val isSuccess: Boolean,
    val responseCode: Int = BillingResponseCode.ERROR,
    val debugMessage: String? = null,
)

data class PurchasesUpdated(
    val result: BillingResult,
    val purchases: List<BillingPurchase>,
)

object BillingResponseCode {
    const val OK = 0
    const val USER_CANCELED = 1
    const val ERROR = 2
}

/**
 * Google Play Billing is intentionally unavailable in this build.
 * The API remains local so non-Play recharge methods can still be displayed and the rest of the
 * application does not need to know about the removed SDK.
 */
class BillingManager(@Suppress("UNUSED_PARAMETER") context: Context) {

    private val _productsFlow = MutableStateFlow<List<BillingProduct>?>(emptyList())
    val productsFlow = _productsFlow.asStateFlow()

    private val _purchaseResultsFlow = MutableSharedFlow<PurchasesUpdated>(extraBufferCapacity = 1)
    val purchaseResultsFlow = _purchaseResultsFlow.asSharedFlow()

    suspend fun consumeProduct(@Suppress("UNUSED_PARAMETER") purchaseToken: String) = Unit

    suspend fun loadProducts(@Suppress("UNUSED_PARAMETER") productIds: List<String>, @Suppress("UNUSED_PARAMETER") productType: String = "inapp") {
        _productsFlow.value = emptyList()
    }

    fun setEmptyProducts() {
        _productsFlow.value = emptyList()
    }

    suspend fun requestPurchase(
        @Suppress("UNUSED_PARAMETER") activity: Activity,
        @Suppress("UNUSED_PARAMETER") product: BillingProduct,
    ): Boolean = false

    suspend fun getCountry(): String? = null

    suspend fun restorePurchases(): List<BillingPurchase> = emptyList()
}
