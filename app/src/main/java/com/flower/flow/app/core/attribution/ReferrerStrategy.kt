package com.flower.flow.app.core.attribution

data class SourceResolution(
    val source: Int,
    val step: String,
)

/**
 * 根据 Install Referrer 与 Meta 兜底结果判定 source。
 */
class ReferrerStrategy(
    private val metaProbe: MetaAttributionProbe,
) {

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

    fun resolve(referrer: String, prefix: String): SourceResolution {
        if (referrer.isBlank()) {
            val meta = metaProbe.probe()
            val step = StepTraceBuilder.sourceWhenReferrerEmpty(prefix, meta.hit, meta.stepTag)
            return SourceResolution(
                source = if (meta.hit) SOURCE_META else SOURCE_ORGANIC,
                step = step,
            )
        }

        val isMetaReferrer = metaKeywords.any { keyword ->
            referrer.contains(keyword, ignoreCase = true)
        }
        if (isMetaReferrer) {
            return SourceResolution(
                source = SOURCE_META,
                step = StepTraceBuilder.sourceWhenMetaReferrer(prefix),
            )
        }

        val meta = metaProbe.probe()
        val step = StepTraceBuilder.sourceWhenReferrerPresent(prefix, meta.hit, meta.stepTag)
        return SourceResolution(
            source = if (meta.hit) SOURCE_META else SOURCE_ORGANIC,
            step = step,
        )
    }

    companion object {
        const val SOURCE_ORGANIC = 1
        const val SOURCE_META = 2
    }
}
