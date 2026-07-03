package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName

data class ShareInfo(
    //title
    @SerializedName("sevenasset") val sevenasset: String = "",
    //content
    @SerializedName("imitaterise") val imitaterise: String = "",
)
