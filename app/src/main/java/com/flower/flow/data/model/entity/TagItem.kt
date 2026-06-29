package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName

data class TagItem(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String = "",
)
