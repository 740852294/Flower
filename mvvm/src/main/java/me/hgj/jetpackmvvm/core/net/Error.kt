package me.hgj.jetpackmvvm.core.net

/**
 * 作者　：hegaojian
 * 时间　：2025/9/24
 * 说明　：错误枚举类
 */

enum class Error(private val code: Int, private val err: String) {

    /**
     * 未知错误
     */
    UNKNOWN(1000, ""),
    /**
     * 解析错误
     */
    PARSE_ERROR(1001, ""),
    /**
     * 网络错误
     */
    NETWORK_ERROR(1002, ""),

    /**
     * 证书出错
     */
    SSL_ERROR(1004, ""),

    /**
     * 连接超时
     */
    TIMEOUT_ERROR(1006, "");

    fun getValue(): String {
        return err
    }

    fun getKey(): Int {
        return code
    }

    companion object {
        fun fromCode(code: String): Error? = entries.find { it.code.toString() == code }
    }

}