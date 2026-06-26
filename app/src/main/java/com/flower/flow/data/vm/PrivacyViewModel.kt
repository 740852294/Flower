package com.flower.flow.data.vm

import com.flower.flow.app.App
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.util.LanguageUtil
import com.flower.flow.data.model.FlowCopyMapper
import com.flower.flow.data.model.entity.toRemoteTextMap
import com.flower.flow.data.repository.CommonRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class PrivacyViewModel : BaseViewModel() {

    fun initLanguageConfig() = request {
        onRequest {
            val languageList = CommonRepository.getLanguageListLivedata().await()
            val target = LanguageUtil.resolveTargetLanguage(languageList)
            App.currentLanguageId = target.id

            val config = CommonRepository.getAppLanguageConfigLivedata().await()
            val remoteTexts = config.toRemoteTextMap()
            val localTexts = FlowCopyMapper.toLocalTexts(remoteTexts)
            FlowCopyStore.save(localTexts)
            target
        }
        loadingType = LoadingType.LOADING_DIALOG
        loadingMessage = "正在加载..."
    }

    fun openPolicyPage(requestType: Int) = request {
        onRequest {
            CommonRepository.getWebUrl(requestType).await()
        }
        loadingType = LoadingType.LOADING_DIALOG
        loadingMessage = "正在加载..."
    }

    fun getBackgroundVideo() = request {
        onRequest {
            CommonRepository.getBackgroundVideo(VIDEO_TYPE_PRIVACY).await()
        }
        loadingType = LoadingType.LOADING_NULL
    }

    companion object {
        /** 同意协议页面背景视频 */
        private const val VIDEO_TYPE_PRIVACY = "3"
    }
}
