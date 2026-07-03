package com.flower.flow.app.core.attribution

import org.junit.Assert.assertEquals
import org.junit.Test

class StepTraceBuilderTest {

    @Test
    fun referrerSteps_matchLegacyCodes() {
        assertEquals("rAS_1", StepTraceBuilder.referrerSuccess())
        assertEquals("rAS_2", StepTraceBuilder.referrerBadResponse())
        assertEquals("rAS_3", StepTraceBuilder.referrerDisconnected())
        assertEquals("rAS_4", StepTraceBuilder.referrerClientError())
    }

    @Test
    fun sourceWhenReferrerEmpty_organic() {
        assertEquals(
            "rAS_1_pSFR_2_iMABFS_2",
            StepTraceBuilder.sourceWhenReferrerEmpty("rAS_1", metaHit = false, metaTag = "iMABFS_2"),
        )
    }

    @Test
    fun sourceWhenReferrerEmpty_meta() {
        assertEquals(
            "rAS_1_pSFR_1_iMABFS_1",
            StepTraceBuilder.sourceWhenReferrerEmpty("rAS_1", metaHit = true, metaTag = "iMABFS_1"),
        )
    }

    @Test
    fun sourceWhenMetaReferrer() {
        assertEquals(
            "rAS_1_pSFR_3_iMABFS_0",
            StepTraceBuilder.sourceWhenMetaReferrer("rAS_1"),
        )
    }

    @Test
    fun sourceWhenReferrerPresent_organic() {
        assertEquals(
            "rAS_1_pSFR_5_iMABFS_3",
            StepTraceBuilder.sourceWhenReferrerPresent("rAS_1", metaHit = false, metaTag = "iMABFS_3"),
        )
    }

    @Test
    fun sourceWhenReferrerPresent_meta() {
        assertEquals(
            "rAS_1_pSFR_4_iMABFS_1",
            StepTraceBuilder.sourceWhenReferrerPresent("rAS_1", metaHit = true, metaTag = "iMABFS_1"),
        )
    }

    @Test
    fun composeFinalStep() {
        assertEquals(
            "rAS_1_pSFR_2_iMABFS_2_rDI_7_rAIWS_1",
            StepTraceBuilder.composeFinal("rAS_1_pSFR_2_iMABFS_2", "rAIWS_1"),
        )
    }
}
