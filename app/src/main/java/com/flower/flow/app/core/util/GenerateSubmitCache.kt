package com.flower.flow.app.core.util

import com.flower.flow.data.model.CacheConfig
import com.flower.flow.data.model.entity.SubmitPageInfo
import me.hgj.jetpackmvvm.ext.util.toArrayEntity
import me.hgj.jetpackmvvm.ext.util.toJsonStr

object GenerateSubmitCache {

    fun isRepeatCheckEnabled(pageInfo: SubmitPageInfo): Boolean {
        return pageInfo.repeatPopupTitle.isNotBlank() || pageInfo.repeatPopupMsg.isNotBlank()
    }

    fun isDuplicateUpload(templateId: Int, sourcePaths: List<String>): Boolean {
        if (CacheConfig.lastGenerateTemplateId <= 0 || CacheConfig.lastGenerateSourcePathsJson.isBlank()) {
            return false
        }
        val lastPaths = CacheConfig.lastGenerateSourcePathsJson.toArrayEntity<String>().orEmpty()
            .filter { it.isNotBlank() }
            .sorted()
        val currentPaths = sourcePaths.filter { it.isNotBlank() }.sorted()
        return CacheConfig.lastGenerateTemplateId == templateId && lastPaths == currentPaths
    }

    fun saveLastSuccess(templateId: Int, sourcePaths: List<String>) {
        CacheConfig.lastGenerateTemplateId = templateId
        CacheConfig.lastGenerateSourcePathsJson = sourcePaths
            .filter { it.isNotBlank() }
            .sorted()
            .toJsonStr()
    }
}
