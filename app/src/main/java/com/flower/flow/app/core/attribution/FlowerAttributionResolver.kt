package com.flower.flow.app.core.attribution

import android.app.Application
import android.content.Context
import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior
import com.facebook.appevents.AppEventsLogger
import com.flower.flow.BuildConfig
import com.flower.flow.data.model.CacheConfig

object FlowerAttributionResolver {

    private val installReferrerReader = InstallReferrerReader()
    private val gaidReader = GaidReader()

    suspend fun resolve(context: Context): AttributionPayload {
        val appContext = context.applicationContext
        val referrerStrategy = ReferrerStrategy(MetaFallbackStrategy(appContext))
        val referrerSnapshot = installReferrerReader.read(appContext)
        val sourceResult = referrerStrategy.resolve(
            referrer = referrerSnapshot.referrer,
            prefix = referrerSnapshot.stepPrefix,
        )
        val advertisingId = gaidReader.read(appContext)
        if (referrerSnapshot.referrer.isNotBlank()) {
            CacheConfig.deviceSourceFlag = referrerSnapshot.referrer
        }
        reportMetaActivationSafely(appContext)
        return AttributionPayload(
            code = advertisingId.id,
            source = sourceResult.source,
            sourceFlag = referrerSnapshot.referrer,
            step = StepTraceBuilder.composeFinal(sourceResult.step, advertisingId.stepSuffix),
        )
    }

    private fun reportMetaActivationSafely(context: Context) {
        try {
            val application = context.applicationContext as? Application ?: return
            @Suppress("DEPRECATION")
            FacebookSdk.sdkInitialize(application) {
                FacebookSdk.setAdvertiserIDCollectionEnabled(true)
                if (BuildConfig.DEBUG) {
                    FacebookSdk.setIsDebugEnabled(true)
                    FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS)
                }
                AppEventsLogger.activateApp(application)
            }
        } catch (_: Exception) {
        }
    }
}
