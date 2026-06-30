package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName

data class TopicListResponse(
    @SerializedName("aiartTopicList")
    val aiartTopicList: List<TopicItem> = emptyList(),
)
