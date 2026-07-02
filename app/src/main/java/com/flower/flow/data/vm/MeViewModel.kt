package com.flower.flow.data.vm

import com.flower.flow.app.core.util.UserManager
import com.flower.flow.data.repository.AiArtRepository
import com.flower.flow.data.repository.UserRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class MeViewModel : BaseViewModel() {

    private var workListPage = 1

    fun initData(isLoading: Boolean) = request {
        onRequest {
            val info = UserRepository.getUserInfoLivedata().await()
            UserManager.saveUser(info)
            workListPage = 1
            AiArtRepository.getWorkTaskList(workListPage).await()
        }
        loadingType = if (isLoading) LoadingType.LOADING_DIALOG else LoadingType.LOADING_NULL
    }

    fun loadWorkList(refresh: Boolean) = request {
        onRequest {
            if (refresh) {
                workListPage = 1
            } else {
                workListPage++
            }
            try {
                AiArtRepository.getWorkTaskList(workListPage).await()
            } catch (e: Exception) {
                if (!refresh) {
                    workListPage--
                }
                throw e
            }
        }
        loadingType = if (refresh) LoadingType.LOADING_DIALOG else LoadingType.LOADING_NULL
    }
}