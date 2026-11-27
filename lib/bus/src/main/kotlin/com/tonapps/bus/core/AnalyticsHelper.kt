package com.tonapps.bus.core

import android.content.Context
import com.tonapps.bus.core.contract.EventDelegate
import com.tonapps.bus.core.contract.EventExecutor
import com.tonapps.bus.core.contract.UtmParameters
import com.tonapps.bus.core.tracer.AnalyticException
import com.tonapps.bus.generated.DefaultEvents

/** Keeps event call sites harmless without collecting or sending telemetry. */
class AnalyticsHelper private constructor(
    private val executor: EventExecutor = object : EventExecutor {
        override fun trackEvent(eventName: String, props: Map<String, Any>) = Unit
    },
    private val delegate: DefaultEventDelegate = DefaultEventDelegate(executor),
    val events: DefaultEvents = DefaultEvents(executor),
) : EventDelegate by delegate {
    class Config(
        val aptabaseAppKey: String,
        val aptabaseEndpoint: String,
        val installId: String,
        val deviceId: String?,
        val storeCountryCode: String?,
        val deviceCountryCode: String?,
    )

    fun start(context: Context) = Unit
    fun setConfig(context: Context, initConfig: Config) = Unit
    fun eventsTaggedWith(utm: UtmParameters): DefaultEvents = events
    fun eventDelegateTaggedWith(utm: UtmParameters): EventDelegate = delegate
    fun onAppForeground() = Unit
    fun onAppBackground() = Unit

    @Deprecated("Use `delegate` instead")
    fun simpleTrackScreenEvent(eventName: String, from: String) = Unit

    @Deprecated("Use `delegate` instead")
    fun simpleTrackEvent(eventName: String, props: MutableMap<String, Any> = hashMapOf()) = Unit

    fun applyInstallKeys(context: Context) = Unit
    fun captureException(error: AnalyticException) = Unit

    companion object {
        val Default by lazy(LazyThreadSafetyMode.NONE) { AnalyticsHelper() }
    }
}
