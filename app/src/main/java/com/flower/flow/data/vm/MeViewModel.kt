package com.flower.flow.data.vm

import com.flower.flow.app.core.util.UserManager
import com.flower.flow.data.model.entity.SubmitPageInfo
import com.flower.flow.data.model.entity.WorkGenerateResult
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

    fun loadWorkList(refresh: Boolean, isLoading: Boolean) = request {
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
        loadingType = if (isLoading) LoadingType.LOADING_DIALOG else LoadingType.LOADING_NULL
    }

    fun deleteWorkTasks(taskIds: List<String>) = request {
        onRequest {
            AiArtRepository.deleteWorkTasks(taskIds).await()
        }
        loadingType = LoadingType.LOADING_DIALOG
    }

    fun recordWorkDownloaded(taskId: String) = request {
        onRequest {
            AiArtRepository.recordWorkDownloaded(taskId).await()
        }
        loadingType = LoadingType.LOADING_DIALOG
    }

    fun prepareAgainGenerate(aiartId: Int, taskId: String) = request {
        onRequest {
            val pageInfo = AiArtRepository.getCreateSubmitPage(aiartId).await()
            val generateResult = if (
                pageInfo.valuefunny ||
                pageInfo.consider
            ) {
                null
            } else {
                AiArtRepository.generateWorkAgain(taskId).await()
            }
            AgainGenerateRequestResult(pageInfo, generateResult)
        }
        loadingType = LoadingType.LOADING_DIALOG
    }

    fun continueAgainGenerate(taskId: String) = request {
        onRequest {
            AiArtRepository.generateWorkAgain(taskId).await()
        }
        loadingType = LoadingType.LOADING_DIALOG
    }
}

data class AgainGenerateRequestResult(
    val pageInfo: SubmitPageInfo,
    val generateResult: WorkGenerateResult?,
)
