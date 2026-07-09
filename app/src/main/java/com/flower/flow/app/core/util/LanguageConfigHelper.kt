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
        cacheLanguageConfig(target.acetoneactuate)
        return target
    }

    suspend fun ensureLanguageConfigForStartup(): LanguageItem {
        val cachedLanguageList = getCachedLanguageList()
        val languageList = if (cachedLanguageList.isNotEmpty()) {
            cachedLanguageList
        } else {
            CommonRepository.getLanguageListLivedata().await().also { list ->
                CacheConfig.languageListJson = list.toJsonStr()
            }
        }
        val target = resolveLanguageTarget(languageList)
        val targetLanguageId = target.acetoneactuate
        App.currentLanguageId = targetLanguageId

        if (CacheConfig.copyTextsJson.isBlank() ||
            CacheConfig.languageConfigLanguageId != targetLanguageId
        ) {
            cacheLanguageConfig(targetLanguageId)
        }
        return target
    }

    private suspend fun cacheLanguageConfig(languageId: Int) {
        val config = CommonRepository.getAppLanguageConfigLivedata().await()
        val remoteTexts = config.toRemoteTextMap()
        val localTexts = FlowCopyMapper.toLocalTexts(remoteTexts)
        AppStrings.save(localTexts)
        CacheConfig.languageConfigLanguageId = languageId
    }

    private fun resolveLanguageTarget(languageList: List<LanguageItem>): LanguageItem {
        if (CacheConfig.selectedLanguageId > 0) {
            return languageList.find { it.acetoneactuate == CacheConfig.selectedLanguageId }
                ?: LanguageItem(acetoneactuate = CacheConfig.selectedLanguageId)
        }
        return LanguageUtil.resolveTargetLanguage(languageList)
    }

    fun restoreLanguageIdFromCache() {
        val languageList = getCachedLanguageList()
        if (languageList.isEmpty()) return
        App.currentLanguageId = resolveLanguageTarget(languageList).acetoneactuate
    }

    private fun getCachedLanguageList(): List<LanguageItem> {
        return CacheConfig.languageListJson.toArrayEntity<LanguageItem>().orEmpty()
    }
}
