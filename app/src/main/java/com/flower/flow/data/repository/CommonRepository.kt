package com.flower.flow.data.repository

import com.flower.flow.app.core.net.NetUrl
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
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toAwaitMsgResponse
import rxhttp.wrapper.param.toAwaitResponse

object CommonRepository {

    /**
     * 获取APP语言文案配置 livedata版本
     */
    fun getAppLanguageConfigLivedata(): Await<AppLanguageConfig> {
        return RxHttp.get(NetUrl.Common.LANGUAGE_CONFIG)
            .add("annoycoin", RandomDataUtil.getRandomData(4))
            .add("alienbollard", RandomDataUtil.getRandomData(3))
            .toAwaitResponse()
    }

    /**
     * 获取语言列表 livedata版本
     */
    fun getLanguageListLivedata(): Await<List<LanguageItem>> {
        return RxHttp.get(NetUrl.Common.LANGUAGE_LIST)
            .add("benchbank", RandomDataUtil.getRandomData(5))
            .add("differdeceive", RandomDataUtil.getRandomData(4))
            .toAwaitResponse()
    }

    /**
     * 获取网页链接，type: 1=隐私政策，2=使用条款
     */
    fun getWebUrl(type: Int): Await<WebUrl> {
        return RxHttp.get(NetUrl.Common.WEB_URL)
            .add("crotchamass", type)
            .add("shapedial", RandomDataUtil.getRandomData(1))
            .add("abscissadelta", RandomDataUtil.getRandomData(3))
            .toAwaitResponse()
    }

    /**
     * 获取背景视频，type: 1=登录页，2=VIP开通页，3=同意协议页面
     */
    fun getBackgroundVideo(type: String): Await<WebUrl> {
        return RxHttp.get(NetUrl.Common.BACKGROUND_VIDEO)
            .add("crotchamass", type)
            .add("beetarbiter", RandomDataUtil.getRandomData(2))
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
            .toAwaitResponse()
    }

    /**
     * 获取意见反馈或举报类型，type: 1=意见反馈，2=举报
     */
    fun getSysTypeList(type: Int): Await<List<SysTypeItem>> {
        return RxHttp.get(NetUrl.Common.ENUM_LIST)
            .add("crotchamass", type)
            .add("accesscustody", RandomDataUtil.getRandomData(2))
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
            .add("cadenceamethyst", RandomDataUtil.getRandomData(4))
            .add("combbarbecue", RandomDataUtil.getRandomData(3))
            .toAwaitResponse()
    }

    /**
     * 提交意见反馈
     */
    fun addAdvice(
        content: String,
        contact: String,
        sysTypeId: Int,
    ): Await<String> {
        return RxHttp.postForm(NetUrl.Common.ADVICE_ADD)
            .add("imitaterise", content)
            .add("bribelack", contact)
            .add("aboundchivalry", sysTypeId)
            .add("baldaconite", RandomDataUtil.getRandomData(3))
            .add("cringefortune", RandomDataUtil.getRandomData(1))
            .toAwaitMsgResponse(String::class.java)
    }

    /**
     * 提交举报
     */
    fun addReport(
        content: String,
        sysTypeId: Int,
    ): Await<String> {
        return RxHttp.postForm(NetUrl.Common.REPORT_ADD)
            .add("imitaterise", content)
            .add("aboundchivalry", sysTypeId)
            .add("gangaccept", RandomDataUtil.getRandomData(1))
            .toAwaitMsgResponse(String::class.java)
    }
}
