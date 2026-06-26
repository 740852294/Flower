package com.flower.flow.data.model.entity

import com.google.gson.reflect.TypeToken
import me.hgj.jetpackmvvm.ext.util.gson
import me.hgj.jetpackmvvm.ext.util.toJsonStr

fun AppLanguageConfig.toRemoteTextMap(): Map<String, String> {
    val type = object : TypeToken<Map<String, String>>() {}.type
    return gson.fromJson(toJsonStr(), type)
}
