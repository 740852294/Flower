package me.hgj.jetpackmvvm.core.net

import kotlinx.coroutines.CoroutineScope
import me.hgj.jetpackmvvm.R
import me.hgj.jetpackmvvm.ext.util.getStringExt


data class LoadingEntity(
    @LoadingType var loadingType: Int = LoadingType.LOADING_NULL,
    var isShow: Boolean = false,
    var coroutineScope: CoroutineScope? = null //请求绑定的作用域
)