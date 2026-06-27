package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName

data class GlobalConfig(
    @SerializedName("integralEntranceJumpState") val integralEntranceJumpState: Int = 0,
    @SerializedName("signInShow") val signInShow: Int = 0,
    @SerializedName("templateAbduceIntegralShow") val templateAbduceIntegralShow: Int = 0,
    @SerializedName("integralAndVipEntranceShow") val integralAndVipEntranceShow: Int = 0,
    @SerializedName("reportEntranceShow") val reportEntranceShow: Int = 0,
    @SerializedName("userChangeShow") val userChangeShow: Int = 0,
    @SerializedName("textToVideoIntegralNum") val textToVideoIntegralNum: Int = 0,
    @SerializedName("textToImgIntegralNum") val textToImgIntegralNum: Int = 0,
    @SerializedName("fbAppId") val fbAppId: String = "",
    @SerializedName("fbClientToken") val fbClientToken: String = "",
)
