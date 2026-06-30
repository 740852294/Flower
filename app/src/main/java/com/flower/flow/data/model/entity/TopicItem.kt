package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName

data class TopicItem(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("img")
    val img: String = "",
    @SerializedName("aiartList")
    val aiartList: List<TemplateItem>? = emptyList(),
    @SerializedName("useNumMsg")
    val useNumMsg: String = "",
)
