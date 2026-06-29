package com.flower.flow.data.vm

import com.flower.flow.data.repository.TemplateRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class TagListViewModel : BaseViewModel() {

    private var currentPage = 1

    fun loadTemplates(tagId: Int, refresh: Boolean) = request {
        onRequest {
            if (refresh) {
                currentPage = 1
            }
            val result = TemplateRepository.getTagTemplateList(tagId, currentPage).await()
            if (result.hasMore()) {
                currentPage++
            }
            result
        }
        loadingType = LoadingType.LOADING_XML
    }
}
