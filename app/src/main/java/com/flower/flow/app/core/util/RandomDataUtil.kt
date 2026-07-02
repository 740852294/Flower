package com.flower.flow.app.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

object RandomDataUtil {

    /** 0-999 范围内硬编码固定值 */
    private const val FIXED_INT_VALUE = "386"

    /** 20 字符以内硬编码固定字符串 */
    private const val FIXED_STRING_VALUE = "k9mP2xQwL8nR4vT6hJ"

    /**
     * 根据 [type] 返回对应随机/固定数据字符串。
     *
     * - 1：0-999 硬编码固定值
     * - 2：20 字符以内硬编码固定字符串
     * - 3：当前时间戳（毫秒）
     * - 4：当前时间或日期，格式 yyyyMMddHHmmss 或 yyyyMMdd
     * - 5：UUID 随机字符串
     */
    fun getRandomData(type: Int): String = when (type) {
        1 -> FIXED_INT_VALUE
        2 -> FIXED_STRING_VALUE
        3 -> System.currentTimeMillis().toString()
        4 -> formatCurrentDateTime()
        5 -> UUID.randomUUID().toString()
        else -> ""
    }

    private fun formatCurrentDateTime(): String {
        val now = Date()
        val pattern = if (Random.nextBoolean()) "yyyyMMddHHmmss" else "yyyyMMdd"
        return SimpleDateFormat(pattern, Locale.US).format(now)
    }
}
