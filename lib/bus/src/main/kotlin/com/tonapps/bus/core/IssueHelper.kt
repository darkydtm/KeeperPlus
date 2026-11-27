package com.tonapps.bus.core

/** Crash reports are not collected or transmitted. */
object IssueHelper {
    fun recordException(e: Throwable) = Unit
    fun recordException(e: Throwable, keys: Map<String, String>) = Unit
}
