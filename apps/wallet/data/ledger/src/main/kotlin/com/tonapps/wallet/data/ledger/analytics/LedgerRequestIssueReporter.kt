package com.tonapps.wallet.data.ledger.analytics

import com.tonapps.ledgerkit.controller.LedgerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class LedgerRequestIssueReporter internal constructor(controller: LedgerController, scope: CoroutineScope) {

    init {
        scope.launch {
            controller.results.collect(LedgerNonFatals::reportRequest)
        }
    }
}
