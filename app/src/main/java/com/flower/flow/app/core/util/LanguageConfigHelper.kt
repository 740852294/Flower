package com.flower.flow.app.core.util

import com.flower.flow.app.App
import com.flower.flow.data.model.CacheConfig
import com.flower.flow.data.model.FlowCopyMapper
import com.flower.flow.data.model.entity.LanguageItem
import com.flower.flow.data.model.entity.toRemoteTextMap
import com.flower.flow.data.repository.CommonRepository
import me.hgj.jetpackmvvm.ext.util.toArrayEntity
import me.hgj.jetpackmvvm.ext.util.toJsonStr

object LanguageConfigHelper {

    suspend fun loadAndCache(): LanguageItem {
        val languageList = CommonRepository.getLanguageListLivedata().await()
        CacheConfig.languageListJson = languageList.toJsonStr()
        val target = resolveLanguageTarget(languageList)
        App.currentLanguageId = target.acetoneactuate
        cacheLanguageConfig()
        return target
    }

    private suspend fun cacheLanguageConfig() {
        val config = CommonRepository.getAppLanguageConfigLivedata().await()
        val remoteTexts = config.toRemoteTextMap()
        val localTexts = FlowCopyMapper.toLocalTexts(remoteTexts)
        AppStrings.save(localTexts)
    }

    private fun resolveLanguageTarget(languageList: List<LanguageItem>): LanguageItem {
        if (CacheConfig.selectedLanguageId > 0) {
            languageList.find { it.acetoneactuate == CacheConfig.selectedLanguageId }?.let { return it }
        }
        return LanguageUtil.resolveTargetLanguage(languageList)
    }

    fun restoreLanguageIdFromCache() {
        val languageList = CacheConfig.languageListJson.toArrayEntity<LanguageItem>().orEmpty()
        if (languageList.isEmpty()) return
        if (CacheConfig.selectedLanguageId > 0) {
            App.currentLanguageId = CacheConfig.selectedLanguageId
        } else {
            val target = LanguageUtil.resolveTargetLanguage(languageList)
            App.currentLanguageId = target.acetoneactuate
        }
    }
}
