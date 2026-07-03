package com.flower.flow.app.core.attribution

import android.content.Context
import com.facebook.internal.AttributionIdentifiers

/**
 * Facebook / Meta 归因兜底探测。
 */
class MetaFallbackStrategy(
    private val context: Context,
) : MetaAttributionProbe {

    override fun probe(): MetaAttributionSnapshot {
        return try {
            val ids = AttributionIdentifiers.getAttributionIdentifiers(context)
            if (ids?.attributionId.isNullOrBlank()) {
                MetaAttributionSnapshot(hit = false, stepTag = "iMABFS_2")
            } else {
                MetaAttributionSnapshot(hit = true, stepTag = "iMABFS_1")
            }
        } catch (_: Exception) {
            MetaAttributionSnapshot(hit = false, stepTag = "iMABFS_3")
        }
    }
}
