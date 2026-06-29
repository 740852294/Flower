package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName

data class SysNotifyItem(
    @SerializedName("content")
    val content: String = "",
)
