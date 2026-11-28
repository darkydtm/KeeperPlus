package com.tonapps.wallet.data.ledger.connection

import com.tonapps.log.L
import android.content.Context
import com.tonapps.ledgerkit.LedgerFailure
import com.tonapps.ledgerkit.controller.LedgerPurpose
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

internal interface LedgerActivityStore {

    fun load(): List<LedgerActivity>

    fun save(entries: List<LedgerActivity>)

    object None : LedgerActivityStore {

        override fun load(): List<LedgerActivity> = emptyList()

        override fun save(entries: List<LedgerActivity>) = Unit
    }
}

internal class LedgerActivityPrefsStore(context: Context) : LedgerActivityStore {

    private val prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    private var saved: String? = null

    override fun load(): List<LedgerActivity> {
        val raw = prefs.getString(KEY_ENTRIES, null) ?: return emptyList()
        return try {
            LedgerActivityCodec.decode(raw)
        } catch (e: IllegalArgumentException) {
            L.w(e)
            emptyList()
        }
    }

    override fun save(entries: List<LedgerActivity>) {
        val encoded = LedgerActivityCodec.encode(entries)
        if (encoded != saved) {
            saved = encoded
            prefs.edit().putString(KEY_ENTRIES, encoded).apply()
        }
    }

    private companion object {
        const val NAME = "ledger_activity"
        const val KEY_ENTRIES = "entries"
    }
}

internal object LedgerActivityCodec {

    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(StoredActivity.serializer())

    fun encode(entries: List<LedgerActivity>): String = json.encodeToString(serializer, entries.map { it.toStored() })

    fun decode(raw: String): List<LedgerActivity> = json.decodeFromString(serializer, raw).map { it.toActivity() }

    private fun LedgerActivity.toStored() = StoredActivity(
        id = id,
        deviceId = deviceId,
        purpose = purpose,
        startedAt = startedAt,
        done = outcome == LedgerActivityOutcome.Done,
        failure = (outcome as? LedgerActivityOutcome.Failed)?.failure,
    )

    @Serializable
    private class StoredActivity(
        val id: Long,
        val deviceId: String,
        val purpose: LedgerPurpose,
        val startedAt: Long,
        val done: Boolean = false,
        val failure: LedgerFailure? = null,
    ) {

        fun toActivity() = LedgerActivity(
            id = id,
            deviceId = deviceId,
            purpose = purpose,
            startedAt = startedAt,
            outcome = when {
                done -> LedgerActivityOutcome.Done
                failure != null -> LedgerActivityOutcome.Failed(failure)
                else -> LedgerActivityOutcome.Interrupted
            },
        )
    }
}
