package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName

data class WorkGenerateResult(
    //taskId
    @SerializedName("baptismdictate") val baptismdictate: String = "",
    //showMsgOne
    @SerializedName("concedearbiter") val concedearbiter: String = "",
    //showMsgTwo
    @SerializedName("fillblitz") val fillblitz: String = "",
    //popupTitle
    @SerializedName("seekgrass") val seekgrass: String = "",
    //popupTimeMsg
    @SerializedName("bookcasedoe") val bookcasedoe: String? = "",
    //popupDescMsg
    @SerializedName("wallbow") val wallbow: String = "",
    //popupButtonMsg
    @SerializedName("entirebomb") val entirebomb: String = "",
    //state
    @SerializedName("afflict") val afflict: Int = STATE_WAITING,
) {
    companion object {
        /** 生产等待提醒弹窗 */
        const val STATE_WAITING = 0

        /** VIP 拦截弹窗 */
        const val STATE_VIP_INTERCEPT = 1

        /** 充值积分拦截弹窗 */
        const val STATE_RECHARGE_INTERCEPT = 2
    }
}
