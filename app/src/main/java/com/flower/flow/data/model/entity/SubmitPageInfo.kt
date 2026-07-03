package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName

data class SubmitPageInfo(
    //repeatPopupTitle
    @SerializedName("hatchampion") val hatchampion: String = "",
    //repeatPopupMsg
    @SerializedName("tryamount") val tryamount: String = "",
    //isConsumeIntegralPopup
    @SerializedName("consider") val consider: Boolean = false,
    //consumeIntegralPopupTitle
    @SerializedName("ecologybestial") val ecologybestial: String = "",
    //consumeIntegralPopupMsg
    @SerializedName("hirenoun") val hirenoun: String = "",
    //isGenerateFreeEverydayPopup
    @SerializedName("valuefunny") val valuefunny: Boolean = false,
    //generateFreeEverydayPopupTitle
    @SerializedName("foolcyst") val foolcyst: String = "",
    //generateFreeEverydayPopupMsg
    @SerializedName("apieceasteroid") val apieceasteroid: String = "",
)
