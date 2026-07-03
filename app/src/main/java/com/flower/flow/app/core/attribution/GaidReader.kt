package com.flower.flow.app.core.attribution

import android.content.Context
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GaidSnapshot(
    val id: String,
    val stepSuffix: String,
)

/**
 * Google Advertising ID 读取。
 */
class GaidReader {

    suspend fun read(context: Context): GaidSnapshot {
        return withContext(Dispatchers.IO) {
            try {
                val id = AdvertisingIdClient.getAdvertisingIdInfo(context).id.orEmpty()
                when {
                    id == GAID_FALLBACK -> GaidSnapshot(GAID_FALLBACK, "rAIWS_2")
                    id.isBlank() -> GaidSnapshot(GAID_FALLBACK, "rAIWS_3")
                    else -> GaidSnapshot(id, "rAIWS_1")
                }
            } catch (_: Exception) {
                GaidSnapshot(GAID_FALLBACK, "rAIWS_4")
            }
        }
    }

    companion object {
        const val GAID_FALLBACK = "00000000-0000-0000-0000-000000000000"
    }
}
