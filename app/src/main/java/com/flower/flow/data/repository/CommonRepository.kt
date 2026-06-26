package com.flower.flow.data.repository

import com.flower.flow.app.core.net.NetUrl
import com.flower.flow.data.model.entity.AppLanguageConfig
import com.flower.flow.data.model.entity.LanguageItem
import com.flower.flow.data.model.entity.WebUrl
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.coroutines.CallFlow
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toAwaitResponse
import rxhttp.wrapper.param.toFlowResponse

object CommonRepository {

    /**
     * 获取APP语言文案配置 livedata版本
     */
    fun getAppLanguageConfigLivedata(): Await<AppLanguageConfig> {
        return RxHttp.get(NetUrl.Common.LANGUAGE_CONFIG)
            .toAwaitResponse()
    }

    /**
     * 获取APP语言文案配置 flow 版本
     */
    fun getAppLanguageConfigFlow(): CallFlow<AppLanguageConfig> {
        return RxHttp.get(NetUrl.Common.LANGUAGE_CONFIG)
            .toFlowResponse()
    }

    /**
     * 获取语言列表 livedata版本
     */
    fun getLanguageListLivedata(): Await<List<LanguageItem>> {
        return RxHttp.get(NetUrl.Common.LANGUAGE_LIST)
            .toAwaitResponse()
    }

    /**
     * 获取语言列表 flow 版本
     */
    fun getLanguageListFlow(): CallFlow<List<LanguageItem>> {
        return RxHttp.get(NetUrl.Common.LANGUAGE_LIST)
            .toFlowResponse()
    }

    /**
     * 获取网页链接，type: 1=隐私政策，2=使用条款
     */
    fun getWebUrl(type: Int): Await<WebUrl> {
        return RxHttp.get(NetUrl.Common.WEB_URL)
            .add("type", type)
            .toAwaitResponse()
    }
}
