package com.flower.flow.data.vm

import com.flower.flow.data.repository.CommonRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class SettingViewModel : BaseViewModel() {


    fun openPolicyPage(requestType: Int) = request {
        onRequest {
            CommonRepository.getWebUrl(requestType).await()
        }
        loadingType = LoadingType.LOADING_DIALOG
    }

}
