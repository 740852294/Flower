package me.hgj.jetpackmvvm.core.init

/**
 * 作者　：hegaojian
 * 时间　：2025/11/4
 * 说明　：
 */
abstract class BaseInitTask : InitTask {
    @Volatile
    var isFinished = false
}
