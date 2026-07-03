package com.flower.flow.app.core.net.parses

import com.flower.flow.app.core.net.AccountCancelledHandler
import com.flower.flow.app.core.net.ConfuseDataRoute
import com.flower.flow.app.core.net.NetUrl
import com.flower.flow.app.core.net.parses.ConfuseJsonUtils.asIntOrThrow
import com.flower.flow.app.core.net.parses.ConfuseJsonUtils.asStringOrThrow
import com.flower.flow.app.core.net.parses.ConfuseJsonUtils.requireField
import com.flower.flow.data.model.entity.ApiResponse
import com.google.gson.JsonObject
import me.hgj.jetpackmvvm.core.net.AppException
import okhttp3.Response
import rxhttp.wrapper.annotation.Parser
import rxhttp.wrapper.parse.TypeParser
import rxhttp.wrapper.utils.convert
import rxhttp.wrapper.utils.convertTo
import java.io.IOException
import java.lang.reflect.Type

@Parser(name = "MsgResponse")
open class MsgResponseParser : TypeParser<String> {

    protected constructor() : super()

    constructor(type: Type) : super(type)

    @Throws(IOException::class)
    override fun onParse(response: Response): String {
        val aliasPath = response.request.url.encodedPath.trim('/')
        return if (ConfuseDataRoute.routeOf(aliasPath) != null) {
            parseObfuscatedResponse(response)
        } else {
            parseLegacyResponse(response)
        }
    }

    @Throws(IOException::class)
    private fun parseObfuscatedResponse(response: Response): String {
        val root: JsonObject = response.convert(JsonObject::class.java)
        val errorCode = root.requireField(ObfuscatedDefaults.CODE).asIntOrThrow(ObfuscatedDefaults.CODE)
        val errorMessage = root.requireField(ObfuscatedDefaults.MSG).asStringOrThrow(ObfuscatedDefaults.MSG)

        if (errorCode == NetUrl.EXPIRED_CODE) {
            AccountCancelledHandler.handle(errorMessage)
        }
        if (errorCode != NetUrl.SUCCESS_CODE) {
            throw AppException(errorCode.toString(), errorMessage)
        }
        return errorMessage
    }

    @Throws(IOException::class)
    private fun parseLegacyResponse(response: Response): String {
        val data: ApiResponse<Any> = response.convertTo(ApiResponse::class, *types)
        if (data.errorCode == NetUrl.EXPIRED_CODE) {
            AccountCancelledHandler.handle(data.errorMsg)
        }
        if (data.errorCode != NetUrl.SUCCESS_CODE) {
            throw AppException(data.errorCode.toString(), data.errorMsg)
        }
        return data.errorMsg
    }
}
