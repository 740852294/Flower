package com.flower.flow.data.model

import me.hgj.jetpackmvvm.ext.util.Cache


object CacheConfig {

    /** 是否同意了隐私政策 */
    var isAgree by Cache(false)

    var userId by Cache("")
}