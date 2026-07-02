package com.flower.flow.data.repository

import com.flower.flow.app.core.net.NetUrl
import com.flower.flow.app.core.net.parses.MsgResponseParser
import com.flower.flow.app.core.net.parses.ObfuscatedResponseConfig
import com.flower.flow.app.core.net.parses.toAwaitObfuscatedResponse
import com.flower.flow.app.core.util.RandomDataUtil
import com.flower.flow.data.model.entity.AppLanguageConfig
import com.flower.flow.data.model.entity.GlobalConfig
import com.flower.flow.data.model.entity.LanguageItem
import com.flower.flow.data.model.entity.ShareInfo
import com.flower.flow.data.model.entity.SysNotifyItem
import com.flower.flow.data.model.entity.SysTypeItem
import com.flower.flow.data.model.entity.VersionCheckInfo
import com.flower.flow.data.model.entity.WebUrl
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.coroutines.CallAwait
import rxhttp.wrapper.coroutines.CallFlow
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toAwaitResponse
import rxhttp.wrapper.param.toFlowResponse

object CommonRepository {

    private val globalConfigResponseConfig = ObfuscatedResponseConfig(
        codeField = "ninesection",
        messageField = "standardrecent",
        dataRoute = "dentblanch.avarice.doe.attain.ceramic.empty",
    )

    /**
     * 获取APP语言文案配置 livedata版本
     */
    fun getAppLanguageConfigLivedata(): Await<AppLanguageConfig> {
        return RxHttp.get(NetUrl.Common.LANGUAGE_CONFIG)
            .toAwaitResponse()
    }

    /**
     * 获取语言列表 livedata版本
     */
    fun getLanguageListLivedata(): Await<List<LanguageItem>> {
        return RxHttp.get(NetUrl.Common.LANGUAGE_LIST)
            .toAwaitResponse()
    }

    /**
     * 获取网页链接，type: 1=隐私政策，2=使用条款
     */
    fun getWebUrl(type: Int): Await<WebUrl> {
        return RxHttp.get(NetUrl.Common.WEB_URL)
            .add("type", type)
            .toAwaitResponse()
    }

    /**
     * 获取背景视频，type: 1=登录页，2=VIP开通页，3=同意协议页面
     */
    fun getBackgroundVideo(type: String): Await<WebUrl> {
        return RxHttp.get(NetUrl.Common.BACKGROUND_VIDEO)
            .add("type", type)
            .toAwaitResponse()
    }

    fun checkVersion(): Await<VersionCheckInfo> {
        return RxHttp.get(NetUrl.Common.UPDATE_INFO)
            .toAwaitResponse()
    }

    fun getGlobalConfig(): Await<GlobalConfig> {
        return RxHttp.get(NetUrl.Common.SYSTEM_CONFIG)
            .add("creepfood", RandomDataUtil.getRandomData(2))
            .add("wetdump", RandomDataUtil.getRandomData(3))
            .toAwaitObfuscatedResponse(globalConfigResponseConfig)
    }

    /**
     * 获取意见反馈或举报类型，type: 1=意见反馈，2=举报
     */
    fun getSysTypeList(type: Int): Await<List<SysTypeItem>> {
        return RxHttp.get(NetUrl.Common.ENUM_LIST)
            .add("type", type)
            .toAwaitResponse()
    }

    /**
     * 获取系统通知列表
     */
    fun getSysNotifyList(): Await<List<SysNotifyItem>> {
        return RxHttp.get(NetUrl.Common.SYS_NOTIFY_LIST)
            .toAwaitResponse()
    }

    /**
     * 获取分享信息
     */
    fun getShareInfo(): Await<ShareInfo> {
        return RxHttp.get(NetUrl.Common.SHARE_INFO)
            .toAwaitResponse()
    }

    /**
     * 提交意见反馈
     */
    fun addAdvice(
        content: String,
        contact: String,
        sysTypeId: Int,
    ): Await<Any> {
        return RxHttp.postForm(NetUrl.Common.ADVICE_ADD)
            .add("content", content)
            .add("contact", contact)
            .add("sysTypeId", sysTypeId)
            .toAwaitResponse()
    }

    /**
     * 提交举报
     */
    fun addReport(
        content: String,
        sysTypeId: Int,
    ): Await<String> {
        return CallAwait(
            RxHttp.postForm(NetUrl.Common.REPORT_ADD)
                .add("content", content)
                .add("sysTypeId", sysTypeId),
            MsgResponseParser(String::class.java),
        )
    }
}
