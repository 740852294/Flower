package com.flower.flow.app.core.attribution

/**
 * 归因 step 字符串拼装，与现网编码规则保持一致。
 */
object StepTraceBuilder {

    fun referrerClientError(): String = "rAS_4"

    fun referrerSuccess(): String = "rAS_1"

    fun referrerBadResponse(): String = "rAS_2"

    fun referrerDisconnected(): String = "rAS_3"

    fun sourceWhenReferrerEmpty(prefix: String, metaHit: Boolean, metaTag: String): String {
        return if (metaHit) {
            "${prefix}_pSFR_1_$metaTag"
        } else {
            "${prefix}_pSFR_2_$metaTag"
        }
    }

    fun sourceWhenMetaReferrer(prefix: String): String = "${prefix}_pSFR_3_iMABFS_0"

    fun sourceWhenReferrerPresent(prefix: String, metaHit: Boolean, metaTag: String): String {
        return if (metaHit) {
            "${prefix}_pSFR_4_$metaTag"
        } else {
            "${prefix}_pSFR_5_$metaTag"
        }
    }

    fun composeFinal(sourceStep: String, gaidStepSuffix: String): String {
        return "${sourceStep}_rDI_7_$gaidStepSuffix"
    }
}
