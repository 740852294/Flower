package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName

data class VersionCheckInfo(
    @SerializedName("upgradeState") val upgradeState: Int = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("content") val content: String = "",
    @SerializedName("buttonMsg") val buttonMsg: String = "",
    @SerializedName("isSignInPopup") val isSignInPopup: Boolean = false,
    @SerializedName("package") val updatePackage: String = "",
)
