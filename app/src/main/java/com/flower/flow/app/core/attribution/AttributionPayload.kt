package com.flower.flow.app.core.attribution

data class AttributionPayload(
    val code: String,
    val source: Int,
    val sourceFlag: String,
    val step: String,
)
