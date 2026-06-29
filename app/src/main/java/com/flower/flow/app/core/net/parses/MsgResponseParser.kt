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

@Parser(name = "MsgResponse")
open class MsgResponseParser : TypeParser<String> {

    protected constructor() : super()

    constructor(type: Type) : super(type)

    @Throws(IOException::class)
    override fun onParse(response: Response): String {
        val data: ApiResponse<Any> = response.convertTo(ApiResponse::class, *types)
        if (data.errorCode == NetUrl.EXPIRED_CODE) {
            UserManager.clearUser()
            throw AppException(data.errorCode.toString(), "登录信息已经过期，请重新登录")
        }
        if (data.errorCode != NetUrl.SUCCESS_CODE) {
            throw AppException(data.errorCode.toString(), data.errorMsg)
        }
        return data.errorMsg
    }
}
