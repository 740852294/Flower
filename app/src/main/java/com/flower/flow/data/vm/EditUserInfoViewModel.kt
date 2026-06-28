package com.flower.flow.data.vm

import com.flower.flow.data.repository.UserRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType
import java.io.File

class EditUserInfoViewModel : BaseViewModel() {

    /** 用户新选头像经压缩后的本地绝对路径，空表示未更换；后续保存/上传时读取此路径 */
    var pendingAvatarPath: String = ""
        private set

    fun setPendingAvatarPath(path: String) {
        pendingAvatarPath = path
    }

    fun clearPendingAvatarPath() {
        pendingAvatarPath = ""
    }

    fun hasPendingAvatar(): Boolean = pendingAvatarPath.isNotEmpty()

    fun updateUserInfo(name: String, avatarPath: String?) = request {
        onRequest {
            val avatarFile = avatarPath
                ?.takeIf { it.isNotEmpty() }
                ?.let(::File)
                ?.takeIf { it.exists() }
            UserRepository.updateUserInfoLivedata(name, avatarFile).await()
        }
        loadingType = LoadingType.LOADING_DIALOG
    }
}
