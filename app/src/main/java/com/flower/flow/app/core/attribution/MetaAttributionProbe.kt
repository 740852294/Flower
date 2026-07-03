package com.flower.flow.app.core.attribution

data class MetaAttributionSnapshot(
    val hit: Boolean,
    val stepTag: String,
)

interface MetaAttributionProbe {
    fun probe(): MetaAttributionSnapshot
}
