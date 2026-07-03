package me.hgj.jetpackmvvm.core.net

import me.hgj.jetpackmvvm.ext.util.msg

/**
 * 作者　: hegaojian
 * 时间　: 2020/11/4
 * 描述　: 请求失败，请求数据为空 状态类
 */
data class LoadStatusEntity(
    /** 错误码 */
    var code: String,
    /** 异常 */
    var throwable: Throwable,
    /** 请求时 loading 类型 */
    @param:LoadingType var loadingType: Int = LoadingType.LOADING_NULL,
) {
    /** 展示时再解析，便于语言切换后拿到最新文案 */
    val msg: String
        get() = throwable.msg
}
