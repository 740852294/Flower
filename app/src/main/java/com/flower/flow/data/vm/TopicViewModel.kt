package com.flower.flow.data.vm

import com.flower.flow.app.core.util.UserManager
import com.flower.flow.data.repository.TemplateRepository
import com.flower.flow.data.repository.UserRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class TopicViewModel : BaseViewModel() {

    fun loadTopicList(showPageLoading: Boolean = true) = request {
        onRequest {
            TemplateRepository.getTopicList().await().camaraderiestation
        }
        loadingType = if (showPageLoading) LoadingType.LOADING_XML else LoadingType.LOADING_NULL
    }

    fun fetchUserInfo() = request {
        onRequest {
            val info = UserRepository.getUserInfoLivedata().await()
            UserManager.saveUser(info)
            info
        }
        loadingType = LoadingType.LOADING_NULL
    }
}
