package com.flower.flow.data.vm

import com.flower.flow.data.repository.TemplateRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class TopicTemplateListViewModel : BaseViewModel() {

    var currentPage = 1
        private set

    fun updateCurrentPage(page: Int) {
        currentPage = maxOf(currentPage, page.coerceAtLeast(1))
    }

    fun loadTemplates(topicId: Int, refresh: Boolean) = request {
        onRequest {
            if (refresh) {
                currentPage = 1
            } else {
                currentPage++
            }
            try {
                TemplateRepository.getTopicTemplateList(topicId, currentPage).await()
            } catch (e: Exception) {
                if (!refresh) {
                    currentPage--
                }
                throw e
            }
        }
        loadingType = if (refresh) LoadingType.LOADING_XML else LoadingType.LOADING_NULL
    }
}
