package com.flower.flow.data.vm

import com.flower.flow.app.core.util.LanguageConfigHelper
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class StartViewModel : BaseViewModel() {

    fun initLanguageConfig() = request {
        onRequest {
            LanguageConfigHelper.loadAndCache()
        }
        loadingType = LoadingType.LOADING_DIALOG
        loadingMessage = "正在加载..."
    }
}
