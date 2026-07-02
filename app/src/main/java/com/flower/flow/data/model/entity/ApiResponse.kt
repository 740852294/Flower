package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("code") val errorCode: Int,
    @SerializedName("msg") val errorMsg: String,
    @SerializedName("data")
    var data: T
)
