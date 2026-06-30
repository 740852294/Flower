package com.flower.flow.app.event

import com.flower.flow.data.model.entity.LanguageItem
import com.flower.flow.data.model.entity.UserInfo
import com.kunminx.architecture.domain.message.MutableResult

/**
 * 在这里发送全局事件总线
 */
object EventViewModel {
    /** 消息通知红点 */
    val msgRedDotEvent = MutableResult<Boolean>()

    /** 用户作品红点 */
    val workRedDotEvent = MutableResult<Boolean>()

    /** 语言切换  */
    val languageEvent = MutableResult<LanguageItem>()

    /** 刷新系统通知列表 */
    val systemNotifyRefreshEvent = MutableResult<Boolean>()

    /** 首页数据刷新 */
    val homeDataRefreshEvent = MutableResult<Boolean>()
}