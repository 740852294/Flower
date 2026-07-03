package com.flower.flow.app.core.attribution

import org.junit.Assert.assertEquals
import org.junit.Test

class ReferrerStrategyTest {

    @Test
    fun blankReferrer_metaMiss_isOrganic() {
        val strategy = ReferrerStrategy(FakeMetaProbe(hit = false, tag = "iMABFS_2"))
        val result = strategy.resolve(referrer = "", prefix = "rAS_1")
        assertEquals(ReferrerStrategy.SOURCE_ORGANIC, result.source)
        assertEquals("rAS_1_pSFR_2_iMABFS_2", result.step)
    }

    @Test
    fun blankReferrer_metaHit_isMeta() {
        val strategy = ReferrerStrategy(FakeMetaProbe(hit = true, tag = "iMABFS_1"))
        val result = strategy.resolve(referrer = "", prefix = "rAS_2")
        assertEquals(ReferrerStrategy.SOURCE_META, result.source)
        assertEquals("rAS_2_pSFR_1_iMABFS_1", result.step)
    }

    @Test
    fun metaKeywordReferrer_isMetaWithoutFallbackProbe() {
        var probeCount = 0
        val strategy = ReferrerStrategy(
            FakeMetaProbe(hit = true, tag = "iMABFS_1") { probeCount++ },
        )
        val result = strategy.resolve(
            referrer = "utm_source=facebook&campaign=test",
            prefix = "rAS_1",
        )
        assertEquals(0, probeCount)
        assertEquals(ReferrerStrategy.SOURCE_META, result.source)
        assertEquals("rAS_1_pSFR_3_iMABFS_0", result.step)
    }

    @Test
    fun nonMetaReferrer_metaMiss_isOrganic() {
        val strategy = ReferrerStrategy(FakeMetaProbe(hit = false, tag = "iMABFS_3"))
        val result = strategy.resolve(
            referrer = "utm_source=google",
            prefix = "rAS_3",
        )
        assertEquals(ReferrerStrategy.SOURCE_ORGANIC, result.source)
        assertEquals("rAS_3_pSFR_5_iMABFS_3", result.step)
    }

    private class FakeMetaProbe(
        private val hit: Boolean,
        private val tag: String,
        private val onProbe: () -> Unit = {},
    ) : MetaAttributionProbe {
        override fun probe(): MetaAttributionSnapshot {
            onProbe()
            return MetaAttributionSnapshot(hit = hit, stepTag = tag)
        }
    }
}
