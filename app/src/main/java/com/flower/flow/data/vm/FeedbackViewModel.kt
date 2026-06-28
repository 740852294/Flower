package com.flower.flow.data.vm

import com.flower.flow.data.model.entity.SysTypeItem
import com.flower.flow.data.repository.CommonRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class FeedbackViewModel : BaseViewModel() {

    var feedbackTypes: List<SysTypeItem> = emptyList()
        private set

    fun loadFeedbackTypes() = request {
        onRequest {
            val list = CommonRepository.getSysTypeList(TYPE_FEEDBACK).await()
            feedbackTypes = list
            list
        }
        loadingType = LoadingType.LOADING_DIALOG
    }

    fun submitFeedback(content: String, contact: String, sysTypeId: Int) = request {
        onRequest {
            CommonRepository.addAdvice(content, contact, sysTypeId).await()
        }
        loadingType = LoadingType.LOADING_DIALOG
    }

    private companion object {
        const val TYPE_FEEDBACK = 1
    }
}
