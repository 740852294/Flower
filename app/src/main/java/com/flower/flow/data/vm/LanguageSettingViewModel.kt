package com.flower.flow.data.vm

import com.flower.flow.app.App
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.data.model.CacheConfig
import com.flower.flow.data.model.FlowCopyMapper
import com.flower.flow.data.model.entity.LanguageItem
import com.flower.flow.data.model.entity.toRemoteTextMap
import com.flower.flow.data.repository.CommonRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType
import me.hgj.jetpackmvvm.ext.util.toJsonStr

class LanguageSettingViewModel : BaseViewModel() {

    fun loadLanguages() = request {
        onRequest {
            val list = CommonRepository.getLanguageListLivedata().await()
            CacheConfig.languageListJson = list.toJsonStr()
            list
        }
        loadingType = LoadingType.LOADING_DIALOG
    }

    fun applyLanguage(selected: LanguageItem, previousLanguageId: Int) = request {
        onRequest {
            App.currentLanguageId = selected.id
            try {
                val config = CommonRepository.getAppLanguageConfigLivedata().await()
                val remoteTexts = config.toRemoteTextMap()
                val localTexts = FlowCopyMapper.toLocalTexts(remoteTexts)
                FlowCopyStore.save(localTexts)
                CacheConfig.selectedLanguageId = selected.id
                selected
            } catch (e: Exception) {
                App.currentLanguageId = previousLanguageId
                throw e
            }
        }
        loadingType = LoadingType.LOADING_DIALOG
    }
}
