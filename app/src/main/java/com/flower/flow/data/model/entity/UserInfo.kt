package com.flower.flow.data.model.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserInfo(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("integralBalance")
    val integralBalance: Int = 0,
    @SerializedName("isVip")
    val isVip: Boolean = false,
    @SerializedName("mineRedDot")
    val mineRedDot: Boolean = false,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("avatar")
    val avatar: String = "",
    @SerializedName("password")
    val password: String = "",
    @SerializedName("sysNotifyRedDot")
    val sysNotifyRedDot: Boolean = false,
    @SerializedName("clearTaskMsg")
    val clearTaskMsg: String = "",
) : Parcelable
