package com.flower.flow.data.vm

import com.flower.flow.data.repository.TemplateRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class TopicUseTemplateViewModel : BaseViewModel() {

    var currentPage = 1
        private set

    fun initializeCurrentPage(page: Int) {
        currentPage = maxOf(currentPage, page.coerceAtLeast(1))
    }

    fun loadNextPage(topicId: Int) = request {
        onRequest {
            val nextPage = currentPage + 1
            TemplateRepository.getTopicTemplateList(topicId, nextPage).await().also {
                currentPage = nextPage
            }
        }
        loadingType = LoadingType.LOADING_DIALOG
    }
}
