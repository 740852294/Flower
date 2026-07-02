package com.flower.flow.data.vm

import com.flower.flow.data.repository.TemplateRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class TagListViewModel : BaseViewModel() {

    private var currentPage = 1

    fun restoreCurrentPage(page: Int) {
        currentPage = page.coerceAtLeast(1)
    }

    fun loadTemplates(tagId: Int, refresh: Boolean) = request {
        onRequest {
            val previousPage = currentPage
            if (refresh) {
                currentPage = 1
            } else {
                currentPage++
            }
            try {
                TemplateRepository.getTagTemplateList(tagId, currentPage).await()
            } catch (e: Exception) {
                currentPage = previousPage
                throw e
            }
        }
        loadingType = if (refresh) LoadingType.LOADING_XML else LoadingType.LOADING_NULL
    }
}
