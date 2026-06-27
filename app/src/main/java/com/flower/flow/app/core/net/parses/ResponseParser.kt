package com.flower.flow.app.core.net.parses

import com.flower.flow.app.core.net.NetUrl
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.data.model.entity.ApiResponse
import me.hgj.jetpackmvvm.core.net.AppException
import okhttp3.Response
import rxhttp.wrapper.annotation.Parser
import rxhttp.wrapper.parse.TypeParser
import rxhttp.wrapper.utils.convertTo
import java.io.IOException
import java.lang.reflect.Type

@Parser(name = "Response")
open class ResponseParser<T> : TypeParser<T> {

    protected constructor() : super()

    constructor(type: Type) : super(type)

    @Throws(IOException::class)
    override fun onParse(response: Response): T {
        val data: ApiResponse<T> = response.convertTo(ApiResponse::class, *types)
        var t = data.data //获取data字段

        if (t == null && types[0] == String::class.java || t == null && types[0] == Any::class.java) {
            t = "" as T
        }
        if (data.errorCode == NetUrl.EXPIRED_CODE) {
            // 登录过期，清除cookie 和本地 用户信息缓存
            UserManager.clearUser()
//            "登录信息已经过期，请重新登录".toast()
            throw AppException(data.errorCode.toString(), "登录信息已经过期，请重新登录")
        }
        // errCode 不等于 SUCCESS_CODE，抛出异常
        if (data.errorCode != NetUrl.SUCCESS_CODE) {
            throw AppException(data.errorCode.toString(), data.errorMsg)
        }
        return t
    }
}