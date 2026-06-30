package com.flower.flow.data.model

import me.hgj.jetpackmvvm.ext.util.Cache


object CacheConfig {

    var isAgree by Cache(false)

    var userId by Cache("")

    var deviceSourceFlag by Cache("")

    var selectedLanguageId by Cache(0)

    /** 文案 Map 的 JSON 缓存，key 为 FlowCopyKey 本地常量 */
    var copyTextsJson by Cache("")

    /** 语言列表 JSON 缓存 */
    var languageListJson by Cache("")

    /** 上次成功提交的模板 id */
    var lastGenerateTemplateId by Cache(0)

    /** 上次成功提交的压缩前图片路径 JSON 列表 */
    var lastGenerateSourcePathsJson by Cache("")

    fun hasLanguageConfigCache(): Boolean {
        return copyTextsJson.isNotBlank() && languageListJson.isNotBlank()
    }
}
