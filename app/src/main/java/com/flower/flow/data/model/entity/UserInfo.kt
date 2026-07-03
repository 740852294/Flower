package com.flower.flow.data.model.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserInfo(
    //id
    @SerializedName("acetoneactuate") val acetoneactuate: Int = 0,
    //uid
    @SerializedName("elephantfloat") val elephantfloat: String = "",
    //integralBalance
    @SerializedName("beastamalgam") val beastamalgam: Int = 0,
    //isVip
    @SerializedName("shareengage") val shareengage: Boolean = false,
    //mineRedDot
    @SerializedName("exposedaub") val exposedaub: Boolean = false,
    //name
    @SerializedName("dazzledeacon") val dazzledeacon: String = "",
    //avatar
    @SerializedName("excludephone") val excludephone: String = "",
    //password
    @SerializedName("colonybay") var colonybay: String = "",
    //languageId
    @SerializedName("southgroup") val southgroup: Int = 0,
    //sysNotifyRedDot
    @SerializedName("valleytoward") var valleytoward: Boolean = false,
    //clearTaskMsg
    @SerializedName("hardthough") val hardthough: String = "",
) : Parcelable
