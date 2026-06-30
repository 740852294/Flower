package com.flower.flow.data.model.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TemplateItem(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("img")
    val img: String = "",
    @SerializedName("uploadNum")
    val uploadNum: Int = 0,
    @SerializedName("lockType")
    val lockType: Int = 0,
    @SerializedName("lockIntegral")
    val lockIntegral: Int = 0,
    @SerializedName("useNumMsg")
    val useNumMsg: String = "",
    @SerializedName("sampleImgList")
    val sampleImgList: List<String>? = emptyList(),
) : Parcelable
