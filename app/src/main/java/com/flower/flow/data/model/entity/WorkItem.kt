package com.flower.flow.data.model.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class WorkItem(
    @SerializedName("taskId")
    val taskId: String = "",
    @SerializedName("aiartType")
    val aiartType: Int = 0,
    // 状态，0=待解锁，1=待处理，2=处理中，3=已完成，4=处理失败
    @SerializedName("state")
    val state: Int = 0,
    @SerializedName("outputUrl")
    val outputUrl: String = "",
    @SerializedName("showMsg")
    val showMsg: String? = "",
    @SerializedName("againGenerateButtonMsg")
    val againGenerateButtonMsg: String? = "",
    @SerializedName("inputImgList")
    val inputImgList: List<String>? = emptyList(),
    @SerializedName("saveLocalPopupMsg")
    val saveLocalPopupMsg: String? = "",
    @SerializedName("saveLocalDownloadingMsg")
    val saveLocalDownloadingMsg: String? = "",
    @SerializedName("downloadingMsg")
    val downloadingMsg: String? = "",
    @SerializedName("estimatTimeMsg")
    val estimatTimeMsg: String? = "",
    @SerializedName("videoDurationMsg")
    val videoDurationMsg: String? = "",
    //模板图片地址
    @SerializedName("aiartImg")
    val aiartImg: String? = "",
    @SerializedName("aiartId")
    val aiartId: Int = 0,
) : Parcelable
