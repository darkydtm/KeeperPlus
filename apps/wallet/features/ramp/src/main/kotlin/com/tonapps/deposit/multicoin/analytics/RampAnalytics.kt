package com.tonapps.deposit.multicoin.analytics

import com.tonapps.blockchain.model.ConfirmInitiator
import com.tonapps.bus.generated.Events.DepositFlow.DepositFlowFrom
import com.tonapps.bus.generated.Events.SendNative.SendNativeFrom
import com.tonapps.bus.generated.Events.WithdrawFlow.WithdrawFlowFrom

fun depositFlowFromOrNull(key: String?): DepositFlowFrom? {
    return DepositFlowFrom.entries.firstOrNull { it.key == key }
}

fun withdrawFlowFromOrNull(key: String?): WithdrawFlowFrom? {
    return WithdrawFlowFrom.entries.firstOrNull { it.key == key }
}

fun WithdrawFlowFrom.asDepositFlowFrom(): DepositFlowFrom {
    return depositFlowFromOrNull(key) ?: DepositFlowFrom.Other
}

fun DepositFlowFrom.asWithdrawFlowFrom(): WithdrawFlowFrom {
    return withdrawFlowFromOrNull(key) ?: WithdrawFlowFrom.Other
}

fun SendNativeFrom.asDepositFlowFrom(): DepositFlowFrom {
    return when (this) {
        SendNativeFrom.WalletScreen -> DepositFlowFrom.WalletScreen
        SendNativeFrom.JettonScreen -> DepositFlowFrom.JettonScreen
        SendNativeFrom.DeepLink -> DepositFlowFrom.DeepLink
        SendNativeFrom.QrCode -> DepositFlowFrom.QrCode
        SendNativeFrom.Walletconnect -> DepositFlowFrom.Walletconnect
        SendNativeFrom.TonconnectLocal, SendNativeFrom.TonconnectRemote -> DepositFlowFrom.Tonconnect
    }
}

fun ConfirmInitiator.asDepositFlowFrom(): DepositFlowFrom {
    return when (this) {
        ConfirmInitiator.User -> DepositFlowFrom.WalletScreen
        ConfirmInitiator.DeepLink -> DepositFlowFrom.DeepLink
        ConfirmInitiator.QrCode -> DepositFlowFrom.QrCode
    }
}

fun WithdrawFlowFrom.asSendNativeFrom(): SendNativeFrom {
    return SendNativeFrom.entries.firstOrNull { it.key == key } ?: SendNativeFrom.WalletScreen
}
