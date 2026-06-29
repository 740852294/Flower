package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName

data class ShareInfo(
    @SerializedName("title") val title: String = "",
    @SerializedName("content") val content: String = "",
)
