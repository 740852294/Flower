package com.flower.flow.app.core.net.parses

import com.flower.flow.app.core.net.AccountCancelledHandler
import com.flower.flow.app.core.net.NetUrl
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.hgj.jetpackmvvm.core.net.AppException
import okhttp3.Response
import rxhttp.wrapper.CallFactory
import rxhttp.wrapper.coroutines.CallAwait
import rxhttp.wrapper.parse.TypeParser
import rxhttp.wrapper.utils.GsonUtil
import rxhttp.wrapper.utils.convert
import rxhttp.wrapper.utils.javaTypeOf
import java.io.IOException
import java.lang.reflect.Type

data class ObfuscatedResponseConfig(
    val codeField: String,
    val messageField: String,
    val dataRoute: String,
) {
    init {
        require(codeField.isNotBlank()) { "codeField must not be blank" }
        require(messageField.isNotBlank()) { "messageField must not be blank" }
        require(dataRoute.split('.').all { it.isNotBlank() }) {
            "dataRoute must contain only non-blank path segments"
        }
    }
}

class ObfuscatedResponseParser<T>(
    type: Type,
    private val config: ObfuscatedResponseConfig,
) : TypeParser<T>(type) {

    @Throws(IOException::class)
    override fun onParse(response: Response): T {
        val root: JsonObject = response.convert(JsonObject::class.java)
        val errorCode = root.requireField(config.codeField).asIntOrThrow(config.codeField)
        val errorMessage = root.requireField(config.messageField).asStringOrThrow(config.messageField)

        if (errorCode == NetUrl.EXPIRED_CODE) {
            AccountCancelledHandler.handle(errorMessage)
        }
        if (errorCode != NetUrl.SUCCESS_CODE) {
            throw AppException(errorCode.toString(), errorMessage)
        }

        var data: JsonElement = root
        for (field in config.dataRoute.split('.')) {
            if (!data.isJsonObject) {
                throw IOException(
                    "Unable to resolve dataRoute '${config.dataRoute}': '$field' parent is not an object",
                )
            }
            data = data.asJsonObject.get(field)
                ?: throw IOException(
                    "Unable to resolve dataRoute '${config.dataRoute}': missing field '$field'",
                )
        }

        if (data.isJsonNull) {
            throw IOException("Unable to resolve dataRoute '${config.dataRoute}': data is null")
        }

        return try {
            GsonUtil.buildGson().fromJson<T>(data, types[0])
                ?: throw IOException("Unable to deserialize dataRoute '${config.dataRoute}'")
        } catch (exception: IOException) {
            throw exception
        } catch (exception: Exception) {
            throw IOException(
                "Unable to deserialize dataRoute '${config.dataRoute}' as ${types[0]}",
                exception,
            )
        }
    }

    private fun JsonObject.requireField(field: String): JsonElement {
        val value = get(field)
            ?: throw IOException("Missing obfuscated response field '$field'")
        if (value.isJsonNull) {
            throw IOException("Obfuscated response field '$field' is null")
        }
        return value
    }

    private fun JsonElement.asIntOrThrow(field: String): Int = try {
        asInt
    } catch (exception: Exception) {
        throw IOException("Obfuscated response field '$field' is not a number", exception)
    }

    private fun JsonElement.asStringOrThrow(field: String): String = try {
        asString
    } catch (exception: Exception) {
        throw IOException("Obfuscated response field '$field' is not a string", exception)
    }
}

inline fun <reified T> CallFactory.toAwaitObfuscatedResponse(
    config: ObfuscatedResponseConfig,
): CallAwait<T> = CallAwait(this, ObfuscatedResponseParser(javaTypeOf<T>(), config))
