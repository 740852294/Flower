package com.flower.flow.app.core.attribution

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class ReferrerSnapshot(
    val referrer: String,
    val stepPrefix: String,
)

/**
 * Play Install Referrer 读取。
 */
class InstallReferrerReader {

    suspend fun read(context: Context): ReferrerSnapshot =
        suspendCancellableCoroutine { continuation ->
            val client = try {
                InstallReferrerClient.newBuilder(context).build()
            } catch (_: Exception) {
                continuation.resume(
                    ReferrerSnapshot(referrer = "", stepPrefix = StepTraceBuilder.referrerClientError()),
                )
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
                            continuation.resume(
                                ReferrerSnapshot(
                                    referrer = raw,
                                    stepPrefix = StepTraceBuilder.referrerSuccess(),
                                ),
                            )
                        } else {
                            closeReferrerClient(client)
                            continuation.resume(
                                ReferrerSnapshot(
                                    referrer = "",
                                    stepPrefix = StepTraceBuilder.referrerBadResponse(),
                                ),
                            )
                        }
                    }

                    override fun onInstallReferrerServiceDisconnected() {
                        if (continuation.isActive) {
                            continuation.resume(
                                ReferrerSnapshot(
                                    referrer = "",
                                    stepPrefix = StepTraceBuilder.referrerDisconnected(),
                                ),
                            )
                        }
                    }
                })
            } catch (_: Exception) {
                closeReferrerClient(client)
                if (continuation.isActive) {
                    continuation.resume(
                        ReferrerSnapshot(
                            referrer = "",
                            stepPrefix = StepTraceBuilder.referrerClientError(),
                        ),
                    )
                }
            }
        }

    private fun closeReferrerClient(client: InstallReferrerClient) {
        try {
            client.endConnection()
        } catch (_: Exception) {
        }
    }
}
