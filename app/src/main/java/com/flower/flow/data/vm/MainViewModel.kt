package com.flower.flow.data.vm

import com.flower.flow.app.App
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.data.model.MainInitResult
import com.flower.flow.data.repository.CommonRepository
import com.flower.flow.data.repository.UserRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class MainViewModel : BaseViewModel() {

    fun initMain() = request {
        onRequest {
            val versionInfo = CommonRepository.checkVersion().await()
            if (versionInfo.upgradeState == FORCE_UPDATE) {
                return@onRequest MainInitResult.ForceUpdate(
                    title = versionInfo.title,
                    content = versionInfo.content,
                    buttonMsg = versionInfo.buttonMsg,
                )
            }
            val globalConfig = CommonRepository.getGlobalConfig().await()
            App.globalConfig = globalConfig
            MainInitResult.Ready(globalConfig)
        }
        loadingType = LoadingType.LOADING_DIALOG
    }

    fun fetchUserInfo(isFirst: Boolean) = request {
        onRequest {
            val info = UserRepository.getUserInfoLivedata().await()
            UserManager.saveUser( info)
            info
        }
        loadingType = if(isFirst )LoadingType.LOADING_DIALOG else LoadingType.LOADING_NULL
    }

    private companion object {
        const val FORCE_UPDATE = 1
    }
}
