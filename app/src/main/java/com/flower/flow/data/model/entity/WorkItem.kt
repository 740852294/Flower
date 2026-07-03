package com.flower.flow.data.model.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class WorkItem(
    //taskId
    @SerializedName("baptismdictate") val baptismdictate: String = "",
    //aiartType
    @SerializedName("behavebanister") val behavebanister: Int = 0,
    // 状态，0=待解锁，1=待处理，2=处理中，3=已完成，4=处理失败
    //state
    @SerializedName("afflict") val afflict: Int = 0,
    //outputUrl
    @SerializedName("wantbirdcage") val wantbirdcage: String = "",
    //showMsg
    @SerializedName("acculturatecurd") val acculturatecurd: String? = "",
    //againGenerateButtonMsg
    @SerializedName("increaserace") val increaserace: String? = "",
    //inputImgList
    @SerializedName("aperitifaccost") val aperitifaccost: List<String>? = emptyList(),
    //saveLocalPopupMsg
    @SerializedName("demand") val demand: String? = "",
    //saveLocalDownloadingMsg
    @SerializedName("expelhotel") val expelhotel: String? = "",
    //downloadingMsg
    @SerializedName("attachaway") val attachaway: String? = "",
    //estimatTimeMsg
    @SerializedName("notechildhood") val notechildhood: String? = "",
    //videoDurationMsg
    @SerializedName("aggregatechief") val aggregatechief: String? = "",
    //模板图片地址 aiartImg
    @SerializedName("clogcadre") val clogcadre: String? = "",
    //aiartId
    @SerializedName("naturebroker") val naturebroker: Int = 0,
) : Parcelable
