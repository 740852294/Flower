package com.flower.flow.data.vm

import com.flower.flow.data.repository.TemplateRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class TagViewModel : BaseViewModel() {

    fun loadTagList() = request {
        onRequest {
            TemplateRepository.getTagList().await()
        }
        loadingType = LoadingType.LOADING_DIALOG
    }
}
