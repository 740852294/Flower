package com.flower.flow.app.core.net.parses

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.IOException

internal object ObfuscatedDefaults {
    const val CODE = "ninesection"
    const val MSG = "standardrecent"
    const val DATA = "dentblanch"
}

internal object ConfuseJsonUtils {

    /** 按 dataRoute 导航；路径缺失或节点为 null 时返回 null，不抛异常。 */
    fun resolveDataRouteOrNull(root: JsonObject, dataRoute: String): JsonElement? {
        var current: JsonElement? = root
        for (field in dataRoute.split('.')) {
            if (current == null || current.isJsonNull || !current.isJsonObject) {
                return null
            }
            current = current.asJsonObject.get(field)
        }
        if (current == null || current.isJsonNull) {
            return null
        }
        return current
    }

    @Throws(IOException::class)
    fun JsonObject.requireField(field: String): JsonElement {
        val value = get(field)
            ?: throw IOException("Missing obfuscated response field '$field'")
        if (value.isJsonNull) {
            throw IOException("Obfuscated response field '$field' is null")
        }
        return value
    }

    @Throws(IOException::class)
    fun JsonElement.asIntOrThrow(field: String): Int = try {
        asInt
    } catch (exception: Exception) {
        throw IOException("Obfuscated response field '$field' is not a number", exception)
    }

    @Throws(IOException::class)
    fun JsonElement.asStringOrThrow(field: String): String = try {
        asString
    } catch (exception: Exception) {
        throw IOException("Obfuscated response field '$field' is not a string", exception)
    }
}
