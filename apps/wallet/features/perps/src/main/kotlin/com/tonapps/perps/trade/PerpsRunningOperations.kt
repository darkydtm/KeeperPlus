package com.tonapps.perps.trade

import java.util.concurrent.ConcurrentHashMap

internal class PerpsRunningOperations {

    private val ids = ConcurrentHashMap.newKeySet<String>()

    operator fun contains(operationId: String): Boolean = operationId in ids

    fun start(operationId: String) {
        ids.add(operationId)
    }

    fun finish(operationId: String) {
        ids.remove(operationId)
    }
}
