package com.tonapps.core.helper

import com.tonapps.blockchain.model.ConfirmInitiator
import com.tonapps.blockchain.model.legacy.WalletEntity
import com.tonapps.bus.core.contract.UtmParameters
import com.tonapps.bus.generated.Events
import com.tonapps.bus.generated.Events.TransactionSent.TransactionSentCategory
import com.tonapps.bus.generated.Events.TransactionSent.TransactionSentCategoryDetail
import com.tonapps.bus.generated.Events.TransactionSent.TransactionSentFeeAsset
import com.tonapps.bus.generated.Events.TransactionSent.TransactionSentInitiatedBy

object TransactionSentAnalytics {

    fun transactionSent(
        wallet: WalletEntity,
        category: TransactionSentCategory,
        categoryDetail: TransactionSentCategoryDetail,
        asset: String,
        amount: Double,
        feeAsset: TransactionSentFeeAsset,
        initiatedBy: TransactionSentInitiatedBy,
        appId: String? = null,
        dappUrl: String? = null,
        isMax: Boolean? = null,
        toAsset: String? = null,
        stakingProvider: String? = null,
        isLiquid: Boolean? = null,
        utm: UtmParameters = UtmParameters.Empty,
    ) = Unit

    fun initiatedBy(from: Events.SendNative.SendNativeFrom): TransactionSentInitiatedBy {
        return when (from) {
            Events.SendNative.SendNativeFrom.WalletScreen,
            Events.SendNative.SendNativeFrom.JettonScreen -> TransactionSentInitiatedBy.User
            Events.SendNative.SendNativeFrom.DeepLink -> TransactionSentInitiatedBy.DeepLink
            Events.SendNative.SendNativeFrom.QrCode -> TransactionSentInitiatedBy.QrCode
            Events.SendNative.SendNativeFrom.TonconnectLocal -> TransactionSentInitiatedBy.TonconnectLocal
            Events.SendNative.SendNativeFrom.TonconnectRemote -> TransactionSentInitiatedBy.TonconnectRemote
            Events.SendNative.SendNativeFrom.Walletconnect -> TransactionSentInitiatedBy.Walletconnect
        }
    }

    fun initiatedBy(initiator: ConfirmInitiator): TransactionSentInitiatedBy {
        return when (initiator) {
            ConfirmInitiator.User -> TransactionSentInitiatedBy.User
            ConfirmInitiator.DeepLink -> TransactionSentInitiatedBy.DeepLink
            ConfirmInitiator.QrCode -> TransactionSentInitiatedBy.QrCode
        }
    }

    fun feeAsset(feeAsset: Events.SendNative.SendNativeFeeAsset): TransactionSentFeeAsset {
        return when (feeAsset) {
            Events.SendNative.SendNativeFeeAsset.Coin -> TransactionSentFeeAsset.Coin
            Events.SendNative.SendNativeFeeAsset.BatteryCharges -> TransactionSentFeeAsset.BatteryCharges
            Events.SendNative.SendNativeFeeAsset.BatteryTonInstantFee -> TransactionSentFeeAsset.BatteryTonInstantFee
            Events.SendNative.SendNativeFeeAsset.Gasless -> TransactionSentFeeAsset.Gasless
        }
    }

}
