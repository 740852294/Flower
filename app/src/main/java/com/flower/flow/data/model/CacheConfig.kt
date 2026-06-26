package com.flower.flow.data.model

import me.hgj.jetpackmvvm.ext.util.Cache


object CacheConfig {

    var isAgree by Cache(false)

    var userId by Cache("")

    var selectedLanguageId by Cache(0)
}