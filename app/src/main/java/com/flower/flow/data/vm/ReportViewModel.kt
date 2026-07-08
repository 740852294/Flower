package com.flower.flow.data.vm

import com.flower.flow.data.repository.CommonRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class ReportViewModel : BaseViewModel() {

    fun loadReportTypes() = request {
        onRequest {
            CommonRepository.getSysTypeList(TYPE_REPORT).await()
        }
        loadingType = LoadingType.LOADING_DIALOG
    }

    fun submitReport(content: String, sysTypeId: Int) = request {
        onRequest {
            CommonRepository.addReport(content, sysTypeId).await()
        }
        loadingType = LoadingType.LOADING_DIALOG
    }

    private companion object {
        const val TYPE_REPORT = 2
    }
}
