package com.flower.flow.data.vm

import com.flower.flow.data.repository.CommonRepository
import com.flower.flow.data.repository.UserRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class SettingViewModel : BaseViewModel() {


    fun openPolicyPage(requestType: Int) = request {
        onRequest {
            CommonRepository.getWebUrl(requestType).await()
        }
        loadingType = LoadingType.LOADING_DIALOG
    }

    fun getShareInfo() = request {
        onRequest {
            CommonRepository.getShareInfo().await()
        }
        loadingType = LoadingType.LOADING_DIALOG
    }

    fun switchAccount(uid: String, password: String) = request {
        onRequest {
            val response = UserRepository.switchUserLivedata(uid, password).await()
            if (response.uid.isBlank()) {
                throw IllegalStateException("switch uid is empty")
            }
            response
        }
        loadingType = LoadingType.LOADING_DIALOG
    }

    fun updatePassword(password: String) = request {
        onRequest {
            UserRepository.updatePasswordLivedata(password).await()
        }
        loadingType = LoadingType.LOADING_DIALOG
    }

}
