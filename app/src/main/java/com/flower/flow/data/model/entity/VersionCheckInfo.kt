package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName

data class VersionCheckInfo(
    //upgradeState
    @SerializedName("aliencello") val aliencello: Int = 0,
    //title
    @SerializedName("sevenasset") val sevenasset: String = "",
    //content
    @SerializedName("imitaterise") val imitaterise: String = "",
    //buttonMsg
    @SerializedName("suggestburden") val suggestburden: String = "",
    //isSignInPopup
    @SerializedName("eightabnormal") val eightabnormal: Boolean = false,
    //package
    @SerializedName("anthracite") val anthracite: String = "",
)
