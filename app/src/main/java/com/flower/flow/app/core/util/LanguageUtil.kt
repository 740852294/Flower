package com.flower.flow.app.core.util

import com.flower.flow.data.model.entity.LanguageItem
import java.util.Locale

object LanguageUtil {

    fun getDeviceLanguageTag(): String = Locale.getDefault().toLanguageTag()

    fun resolveTargetLanguage(languageList: List<LanguageItem>): LanguageItem {
        require(languageList.isNotEmpty()) { "Language list is empty" }
        val deviceTag = getDeviceLanguageTag()
        val matched = languageList.find { isLanguageTagMatch(deviceTag, it.tag) }
        val default = languageList.find { it.isDefault == 1 }
        return matched ?: default ?: languageList.first()
    }

    private fun isLanguageTagMatch(deviceTag: String, serverTag: String): Boolean {
        if (serverTag.isBlank()) return false
        val device = deviceTag.lowercase(Locale.ROOT)
        val server = serverTag.lowercase(Locale.ROOT)
        if (device == server) return true
        if (device.startsWith("$server-")) return true
        if (server.startsWith("$device-")) return true
        val deviceLanguage = device.substringBefore('-')
        val serverLanguage = server.substringBefore('-')
        return deviceLanguage == serverLanguage
    }
}
