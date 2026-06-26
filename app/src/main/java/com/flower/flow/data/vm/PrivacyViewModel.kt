package com.flower.flow.data.vm

import com.flower.flow.app.core.util.LanguageConfigHelper
import com.flower.flow.data.repository.CommonRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class PrivacyViewModel : BaseViewModel() {

    fun initLanguageConfig() = request {
        onRequest {
            LanguageConfigHelper.loadAndCache()
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
