package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName

data class TopicListResponse(
    //aiartTopicList
    @SerializedName("camaraderiestation") val camaraderiestation: List<TopicItem> = emptyList(),
)
