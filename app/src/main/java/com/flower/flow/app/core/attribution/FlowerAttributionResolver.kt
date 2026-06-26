package com.flower.flow.app.core.attribution

import android.app.Application
import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior
import com.facebook.appevents.AppEventsLogger
import com.facebook.internal.AttributionIdentifiers
import com.flower.flow.BuildConfig
import com.flower.flow.data.model.CacheConfig
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

object FlowerAttributionResolver {

    private const val SOURCE_ORGANIC = 1
    private const val SOURCE_META = 2
    private const val GAID_FALLBACK = "00000000-0000-0000-0000-000000000000"

    private val metaKeywords = listOf(
        "facebook",
        "meta",
        "fb4a",
        "fbclid",
        "pli=1",
        "meta_ads",
        "utm_source=meta_ads",
        "utm_source=facebook",
        "facebook_ads",
        "instagram",
    )

    suspend fun resolve(context: Context): AttributionPayload {
        val appContext = context.applicationContext
        val referrerResult = readInstallReferrer(appContext)
        val sourceResult = matchSourceByReferrer(
            referrer = referrerResult.referrer,
            context = appContext,
            prefix = referrerResult.stepPrefix,
        )
        val advertisingId = readAdvertisingId(appContext)
        if (referrerResult.referrer.isNotBlank()) {
            CacheConfig.deviceSourceFlag = referrerResult.referrer
        }
        reportMetaActivationSafely(appContext)
        return AttributionPayload(
            code = advertisingId.id,
            source = sourceResult.source,
            sourceFlag = referrerResult.referrer,
            step = "${sourceResult.step}_rDI_7_${advertisingId.stepSuffix}",
        )
    }

    private suspend fun readInstallReferrer(context: Context): ReferrerResult =
        suspendCancellableCoroutine { continuation ->
            val client = try {
                InstallReferrerClient.newBuilder(context).build()
            } catch (_: Exception) {
                continuation.resume(ReferrerResult("", "rAS_4"))
                return@suspendCancellableCoroutine
            }

            try {
                client.startConnection(object : InstallReferrerStateListener {
                    override fun onInstallReferrerSetupFinished(responseCode: Int) {
                        if (!continuation.isActive) {
                            closeReferrerClient(client)
                            return
                        }
                        if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                            val raw = try {
                                client.installReferrer?.installReferrer.orEmpty()
                            } catch (_: Exception) {
                                ""
                            }
                            closeReferrerClient(client)
                            continuation.resume(ReferrerResult(raw, "rAS_1"))
                        } else {
                            closeReferrerClient(client)
                            continuation.resume(ReferrerResult("", "rAS_2"))
                        }
                    }

                    override fun onInstallReferrerServiceDisconnected() {
                        if (continuation.isActive) {
                            continuation.resume(ReferrerResult("", "rAS_3"))
                        }
                    }
                })
            } catch (_: Exception) {
                closeReferrerClient(client)
                if (continuation.isActive) {
                    continuation.resume(ReferrerResult("", "rAS_4"))
                }
            }
        }

    private fun matchSourceByReferrer(
        referrer: String,
        context: Context,
        prefix: String,
    ): SourceResult {
        if (referrer.isBlank()) {
            val metaResult = checkMetaAttribution(context)
            return if (metaResult.hit) {
                SourceResult(SOURCE_META, "${prefix}_pSFR_1_${metaResult.stepTag}")
            } else {
                SourceResult(SOURCE_ORGANIC, "${prefix}_pSFR_2_${metaResult.stepTag}")
            }
        }

        val isMetaReferrer = metaKeywords.any { keyword ->
            referrer.contains(keyword, ignoreCase = true)
        }
        if (isMetaReferrer) {
            return SourceResult(SOURCE_META, "${prefix}_pSFR_3_iMABFS_0")
        }

        val metaResult = checkMetaAttribution(context)
        return if (metaResult.hit) {
            SourceResult(SOURCE_META, "${prefix}_pSFR_4_${metaResult.stepTag}")
        } else {
            SourceResult(SOURCE_ORGANIC, "${prefix}_pSFR_5_${metaResult.stepTag}")
        }
    }

    private fun checkMetaAttribution(context: Context): MetaResult {
        return try {
            val ids = AttributionIdentifiers.getAttributionIdentifiers(context)
            if (ids?.attributionId.isNullOrBlank()) {
                MetaResult(false, "iMABFS_2")
            } else {
                MetaResult(true, "iMABFS_1")
            }
        } catch (_: Exception) {
            MetaResult(false, "iMABFS_3")
        }
    }

    private suspend fun readAdvertisingId(context: Context): AdvertisingIdResult {
        return withContext(Dispatchers.IO) {
            try {
                val id = AdvertisingIdClient.getAdvertisingIdInfo(context).id.orEmpty()
                when {
                    id == GAID_FALLBACK -> AdvertisingIdResult(GAID_FALLBACK, "rAIWS_2")
                    id.isBlank() -> AdvertisingIdResult(GAID_FALLBACK, "rAIWS_3")
                    else -> AdvertisingIdResult(id, "rAIWS_1")
                }
            } catch (_: Exception) {
                AdvertisingIdResult(GAID_FALLBACK, "rAIWS_4")
            }
        }
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

    private fun closeReferrerClient(client: InstallReferrerClient) {
        try {
            client.endConnection()
        } catch (_: Exception) {
        }
    }

    private data class ReferrerResult(
        val referrer: String,
        val stepPrefix: String,
    )

    private data class SourceResult(
        val source: Int,
        val step: String,
    )

    private data class MetaResult(
        val hit: Boolean,
        val stepTag: String,
    )

    private data class AdvertisingIdResult(
        val id: String,
        val stepSuffix: String,
    )
}
