package com.flower.flow.data.vm

import com.flower.flow.data.repository.CommonRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class SystemNotifyViewModel : BaseViewModel() {

    fun loadNotifyList(showLoading: Boolean) = request {
        onRequest {
            CommonRepository.getSysNotifyList().await()
        }
        loadingType = if (showLoading) LoadingType.LOADING_DIALOG else LoadingType.LOADING_NULL
    }
}
