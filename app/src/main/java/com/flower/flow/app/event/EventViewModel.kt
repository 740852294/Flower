package com.flower.flow.app.event

import com.kunminx.architecture.domain.message.MutableResult

/**
 * 在这里发送全局事件总线
 */
object EventViewModel  {
    /** 通知fragment请求数据 */
    val mainFragmentDataEvent = MutableResult<Boolean>()
}