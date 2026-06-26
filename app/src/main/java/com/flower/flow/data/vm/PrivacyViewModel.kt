package com.flower.flow.data.vm

import com.flower.flow.data.repository.CommonRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class PrivacyViewModel : BaseViewModel() {


    fun getLanguageList() = request {
        onRequest {
            CommonRepository.getLanguageListLivedata().await()
        }
        loadingType = LoadingType.LOADING_DIALOG
        loadingMessage = "正在获取中..."
    }
}