package com.flower.flow.app.core.net

import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.RxHttpFormParam
import rxhttp.wrapper.param.RxHttpNoBodyParam

/**
 * 统一 HTTP 入口：自动挂载路径对应的 puzzle 参数。
 */
object FlowHttp {

    fun get(path: String): RxHttpNoBodyParam {
        val param = RxHttp.get(path)
        PuzzleParamRegistry.apply(path) { name, value -> param.add(name, value) }
        return param
    }

    fun postForm(path: String): RxHttpFormParam {
        val param = RxHttp.postForm(path)
        PuzzleParamRegistry.apply(path) { name, value -> param.add(name, value) }
        return param
    }
}
