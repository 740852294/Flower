package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName

data class LanguageItem(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("language")
    val language: String = "",
    @SerializedName("tag")
    val tag: String = "",
    @SerializedName("isDefault")
    val isDefault: Int = 0,
)
