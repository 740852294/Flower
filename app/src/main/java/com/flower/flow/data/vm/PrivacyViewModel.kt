package com.flower.flow.data.vm

import android.content.Context
import com.flower.flow.app.core.attribution.FlowerRegisterCoordinator
import com.flower.flow.app.core.util.LanguageConfigHelper
import com.flower.flow.data.repository.CommonRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class PrivacyViewModel : BaseViewModel() {

    fun initLanguageConfig() = request {
        onRequest {
            LanguageConfigHelper.ensureLanguageConfigForStartup()
        }
        loadingType = LoadingType.LOADING_DIALOG
    }

    fun openPolicyPage(requestType: Int) = request {
        onRequest {
            CommonRepository.getWebUrl(requestType).await()
        }
        loadingType = LoadingType.LOADING_DIALOG
    }

    fun getBackgroundVideo() = request {
        onRequest {
            CommonRepository.getBackgroundVideo("3").await()
        }
        loadingType = LoadingType.LOADING_NULL
    }

    fun registerUserByAttribution(context: Context) = request {
        onRequest {
            val registerResponse = FlowerRegisterCoordinator.registerByAttribution(context)
            if (registerResponse.elephantfloat.isBlank()) {
                throw IllegalStateException("register uid is empty")
            }
            registerResponse
        }
        loadingType = LoadingType.LOADING_DIALOG
    }
}
