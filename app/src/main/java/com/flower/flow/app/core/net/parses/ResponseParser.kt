package com.flower.flow.app.core.net.parses

import com.flower.flow.app.core.net.AccountCancelledHandler
import com.flower.flow.app.core.net.ConfuseDataRoute
import com.flower.flow.app.core.net.NetUrl
import com.flower.flow.app.core.net.parses.ConfuseJsonUtils.asIntOrThrow
import com.flower.flow.app.core.net.parses.ConfuseJsonUtils.asStringOrThrow
import com.flower.flow.app.core.net.parses.ConfuseJsonUtils.requireField
import com.flower.flow.app.core.net.parses.ConfuseJsonUtils.resolveDataRouteOrNull
import com.flower.flow.data.model.entity.ApiResponse
import com.google.gson.JsonObject
import me.hgj.jetpackmvvm.core.net.AppException
import okhttp3.Response
import rxhttp.wrapper.annotation.Parser
import rxhttp.wrapper.parse.TypeParser
import rxhttp.wrapper.utils.GsonUtil
import rxhttp.wrapper.utils.convert
import rxhttp.wrapper.utils.convertTo
import java.io.IOException
import java.lang.reflect.Type

@Parser(name = "Response")
open class ResponseParser<T> : TypeParser<T> {

    protected constructor() : super()

    constructor(type: Type) : super(type)

    @Throws(IOException::class)
    override fun onParse(response: Response): T {
        val aliasPath = response.request.url.encodedPath.trim('/')
        val dataRoute = ConfuseDataRoute.routeOf(aliasPath)
        return if (dataRoute != null) {
            parseObfuscatedResponse(response, dataRoute)
        } else {
            parseLegacyResponse(response)
        }
    }

    @Throws(IOException::class)
    private fun parseObfuscatedResponse(response: Response, dataRoute: String): T {
        val root: JsonObject = response.convert(JsonObject::class.java)
        val errorCode = root.requireField(ObfuscatedDefaults.CODE).asIntOrThrow(ObfuscatedDefaults.CODE)
        val errorMessage = root.requireField(ObfuscatedDefaults.MSG).asStringOrThrow(ObfuscatedDefaults.MSG)

        if (errorCode == NetUrl.EXPIRED_CODE) {
            AccountCancelledHandler.handle(errorMessage)
        }
        if (errorCode != NetUrl.SUCCESS_CODE) {
            throw AppException(errorCode.toString(), errorMessage)
        }

        val data = resolveDataRouteOrNull(root, dataRoute) ?: return normalizeNullData(null)

        return try {
            normalizeNullData(GsonUtil.buildGson().fromJson<T>(data, types[0]))
        } catch (exception: Exception) {
            throw IOException(
                "Unable to deserialize dataRoute '$dataRoute' as ${types[0]}",
                exception,
            )
        }
    }

    @Throws(IOException::class)
    private fun parseLegacyResponse(response: Response): T {
        val data: ApiResponse<T> = response.convertTo(ApiResponse::class, *types)
        if (data.errorCode == NetUrl.EXPIRED_CODE) {
            AccountCancelledHandler.handle(data.errorMsg)
        }
        if (data.errorCode != NetUrl.SUCCESS_CODE) {
            throw AppException(data.errorCode.toString(), data.errorMsg)
        }
        return normalizeNullData(data.data)
    }

    @Suppress("UNCHECKED_CAST")
    private fun normalizeNullData(value: T?): T {
        if (value != null) return value
        if (types[0] == String::class.java || types[0] == Any::class.java) {
            return "" as T
        }
        return null as T
    }
}
