package com.flower.flow.data.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.flower.flow.app.core.util.PhotoCompressUtil
import com.flower.flow.data.model.entity.SubmitPageInfo
import com.flower.flow.data.model.entity.WorkGenerateResult
import com.flower.flow.data.repository.AiArtRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType
import java.io.File

class MaterialUploadViewModel : BaseViewModel() {

    var singleUploadPath: String? = null
    var leftUploadPath: String? = null
    var rightUploadPath: String? = null

    private val _submitPageInfo = MutableLiveData<SubmitPageInfo>()
    val submitPageInfo: LiveData<SubmitPageInfo> = _submitPageInfo

    fun loadCreateSubmitPage(id: Int) = request {
        onRequest {
            AiArtRepository.getCreateSubmitPage(id).await().also {
                _submitPageInfo.value = it
            }
        }
        loadingType = LoadingType.LOADING_NULL
    }

    /** @deprecated 使用 [loadCreateSubmitPage] */
    fun getCreateSubmitPage(id: Int) = loadCreateSubmitPage(id)

    fun generateWork(aiartId: Int, sourceFiles: List<File>, workDir: File) = request {
        onRequest {
            val uploadFiles = PhotoCompressUtil.prepareUploadFiles(sourceFiles, workDir)
            AiArtRepository.generateWork(aiartId, uploadFiles).await()
        }
        loadingType = LoadingType.LOADING_DIALOG
    }

    fun collectSourcePaths(isMultiUpload: Boolean): List<String> {
        return if (isMultiUpload) {
            listOfNotNull(leftUploadPath, rightUploadPath)
        } else {
            listOfNotNull(singleUploadPath)
        }
    }

    fun collectSourceFiles(isMultiUpload: Boolean): List<File> {
        return collectSourcePaths(isMultiUpload).map(::File)
    }

    fun hasUploadedPhoto(isMultiUpload: Boolean): Boolean {
        return collectSourceFiles(isMultiUpload).isNotEmpty()
    }
}
