package com.flower.flow.app.event

import com.flower.flow.data.model.entity.UserInfo
import com.kunminx.architecture.domain.message.MutableResult

/**
 * 在这里发送全局事件总线
 */
object EventViewModel  {
    /** 通知fragment请求数据 */
    val mainFragmentDataEvent = MutableResult<Boolean>()

    /** 消息通知红点 */
    val msgRedDotEvent = MutableResult<Boolean>()

    /** 用户作品红点 */
    val workRedDotEvent = MutableResult<Boolean>()
}