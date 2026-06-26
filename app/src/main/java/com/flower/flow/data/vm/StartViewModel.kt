package com.flower.flow.data.vm

import android.content.Context
import com.flower.flow.app.core.attribution.FlowerRegisterCoordinator
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

    fun registerUserByAttribution(context: Context) = request {
        onRequest {
            val registerResponse = FlowerRegisterCoordinator.registerByAttribution(context)
            if (registerResponse.uid.isBlank()) {
                throw IllegalStateException("register uid is empty")
            }
            registerResponse
        }
        loadingType = LoadingType.LOADING_DIALOG
        loadingMessage = "正在加载..."
    }
}
