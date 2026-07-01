package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName

data class WorkGenerateResult(
    @SerializedName("taskId")
    val taskId: String = "",
    @SerializedName("showMsgOne")
    val showMsgOne: String = "",
    @SerializedName("showMsgTwo")
    val showMsgTwo: String = "",
    @SerializedName("popupTitle")
    val popupTitle: String = "",
    @SerializedName("popupTimeMsg")
    val popupTimeMsg: String? = "",
    @SerializedName("popupDescMsg")
    val popupDescMsg: String = "",
    @SerializedName("popupButtonMsg")
    val popupButtonMsg: String = "",
    @SerializedName("state")
    val state: Int = STATE_WAITING,
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
