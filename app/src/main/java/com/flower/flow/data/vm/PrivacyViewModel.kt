package com.flower.flow.data.vm

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.flower.flow.app.App
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.util.LanguageUtil
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.model.FlowCopyMapper
import com.flower.flow.data.model.entity.toRemoteTextMap
import com.flower.flow.data.repository.CommonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.appContext
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType
import me.hgj.jetpackmvvm.ext.util.toast
import androidx.core.net.toUri

class PrivacyViewModel : BaseViewModel() {

    fun initLanguageConfig() = request {
        onRequest {
            val languageList = CommonRepository.getLanguageListLivedata().await()
            val target = LanguageUtil.resolveTargetLanguage(languageList)
            App.currentLanguageId = target.id

            val config = CommonRepository.getAppLanguageConfigLivedata().await()
            val remoteTexts = config.toRemoteTextMap()
            val localTexts = FlowCopyMapper.toLocalTexts(remoteTexts)
            FlowCopyStore.save(localTexts)
            target
        }
        loadingType = LoadingType.LOADING_DIALOG
        loadingMessage = "正在加载..."
    }

    fun openPolicyPage(requestType: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                CommonRepository.getWebUrl(requestType).await().url
            }.onSuccess { url ->
                withContext(Dispatchers.Main) {
                    if (url.isBlank()) {
                        FlowCopyStore.get(FlowCopyKey.WEB_EMPTY_HINT).toast()
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        appContext.startActivity(intent)
                    }
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    it.message?.toast()
                }
            }
        }
    }
}
