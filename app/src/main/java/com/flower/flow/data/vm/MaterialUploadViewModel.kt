package com.flower.flow.data.vm

import com.flower.flow.app.core.util.PhotoCompressUtil
import com.flower.flow.data.repository.AiArtRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType
import java.io.File

class MaterialUploadViewModel : BaseViewModel() {

    var singleUploadPath: String? = null
    var leftUploadPath: String? = null
    var rightUploadPath: String? = null

    fun getCreateSubmitPage(id: Int) = request {
        onRequest {
            AiArtRepository.getCreateSubmitPage(id).await()
        }
        loadingType = LoadingType.LOADING_NULL
    }

    fun generateWork(aiartId: Int, sourceFiles: List<File>, workDir: File) = request {
        onRequest {
            val uploadFiles = PhotoCompressUtil.prepareUploadFiles(sourceFiles, workDir)
            AiArtRepository.generateWork(aiartId, uploadFiles).await()
        }
        loadingType = LoadingType.LOADING_DIALOG
    }
}
