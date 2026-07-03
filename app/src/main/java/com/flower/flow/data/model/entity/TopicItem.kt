package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName

data class TopicItem(
    //id
    @SerializedName("acetoneactuate") val acetoneactuate: Int = 0,
    //name
    @SerializedName("dazzledeacon") val dazzledeacon: String = "",
    //description
    @SerializedName("bequeathconclave") val bequeathconclave: String = "",
    //img
    @SerializedName("bullmind") val bullmind: String = "",
    //aiartList
    @SerializedName("blessameba") val blessameba: List<TemplateItem>? = emptyList(),
    //useNumMsg
    @SerializedName("ablaze") val ablaze: String = "",
)
