package com.flower.flow.data.model.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RegisterResponse(
    //id
    @SerializedName("acetoneactuate") val acetoneactuate: Int = 0,
    //uid
    @SerializedName("elephantfloat") var elephantfloat: String = "",
    //type
    @SerializedName("crotchamass") val crotchamass: Int = 0,
) : Parcelable
