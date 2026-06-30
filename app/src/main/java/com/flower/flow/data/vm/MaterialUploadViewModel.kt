package com.flower.flow.data.vm

import com.flower.flow.data.repository.AiArtRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class MaterialUploadViewModel : BaseViewModel() {

    var singleUploadFilePath: String? = null
    var leftUploadFilePath: String? = null
    var rightUploadFilePath: String? = null

    fun getCreateSubmitPage(id: Int) = request {
        onRequest {
            AiArtRepository.getCreateSubmitPage(id).await()
        }
        loadingType = LoadingType.LOADING_NULL
    }
}
