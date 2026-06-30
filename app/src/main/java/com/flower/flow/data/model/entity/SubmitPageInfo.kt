package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName

data class SubmitPageInfo(
    @SerializedName("repeatPopupTitle")
    val repeatPopupTitle: String = "",
    @SerializedName("repeatPopupMsg")
    val repeatPopupMsg: String = "",
    @SerializedName("isConsumeIntegralPopup")
    val isConsumeIntegralPopup: Boolean = false,
    @SerializedName("consumeIntegralPopupTitle")
    val consumeIntegralPopupTitle: String = "",
    @SerializedName("consumeIntegralPopupMsg")
    val consumeIntegralPopupMsg: String = "",
    @SerializedName("isGenerateFreeEverydayPopup")
    val isGenerateFreeEverydayPopup: Boolean = false,
    @SerializedName("generateFreeEverydayPopupTitle")
    val generateFreeEverydayPopupTitle: String = "",
    @SerializedName("generateFreeEverydayPopupMsg")
    val generateFreeEverydayPopupMsg: String = "",
)
