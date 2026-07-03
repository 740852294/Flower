package com.flower.flow.data.model.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TemplateItem(
    //id
    @SerializedName("acetoneactuate") val acetoneactuate: Int = 0,
    //name
    @SerializedName("dazzledeacon") val dazzledeacon: String = "",
    //img
    @SerializedName("bullmind") val bullmind: String = "",
    //uploadNum
    @SerializedName("alimentary") val alimentary: Int = 0,
    //lockType
    @SerializedName("colonknee") val colonknee: Int = 0,
    //lockIntegral
    @SerializedName("peacearrow") val peacearrow: Int = 0,
    //useNumMsg
    @SerializedName("ablaze") val ablaze: String = "",
    //sampleImgList
    @SerializedName("neverchapter") val neverchapter: List<String>? = emptyList(),
) : Parcelable
