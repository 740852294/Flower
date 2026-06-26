package com.flower.flow.app.core.util

import com.flower.flow.data.model.CacheConfig
import com.google.gson.reflect.TypeToken
import me.hgj.jetpackmvvm.ext.util.gson
import me.hgj.jetpackmvvm.ext.util.toJsonStr

object FlowCopyStore {

    private var texts: Map<String, String> = emptyMap()

    fun save(value: Map<String, String>) {
        texts = value
        CacheConfig.copyTextsJson = value.toJsonStr()
    }

    fun loadCache() {
        val json = CacheConfig.copyTextsJson
        if (json.isBlank()) {
            texts = emptyMap()
            return
        }
        val type = object : TypeToken<Map<String, String>>() {}.type
        texts = runCatching { gson.fromJson<Map<String, String>>(json, type) }.getOrDefault(emptyMap())
    }

    fun get(key: String): String {
        if (texts.isEmpty()) {
            loadCache()
        }
        return texts[key].orEmpty()
    }
}
