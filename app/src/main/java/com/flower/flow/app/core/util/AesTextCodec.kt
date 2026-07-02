package com.flower.flow.app.core.util

import com.flower.flow.BuildConfig
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object AesTextCodec {

    private const val AES_MODE = "AES/ECB/PKCS5Padding"
    private val UTF_8 = StandardCharsets.UTF_8

    //加密
    fun encode(plainText: String, key: String = BuildConfig.CIPHER_KEY): String? {
        if (plainText.isEmpty()) return plainText
        return runCatching {
            val encrypted = newAesCipher(Cipher.ENCRYPT_MODE, key)
                .doFinal(plainText.toByteArray(UTF_8))
            toUpperHex(encrypted)
        }.getOrNull()
    }

    //解密
    fun decode(cipherHex: String, key: String = BuildConfig.CIPHER_KEY): String? {
        if (cipherHex.isEmpty()) return cipherHex
        return runCatching {
            val decrypted = newAesCipher(Cipher.DECRYPT_MODE, key)
                .doFinal(parseHex(cipherHex))
            String(decrypted, UTF_8)
        }.getOrNull()
    }

    private fun newAesCipher(cipherMode: Int, key: String): Cipher {
        val secretKey = SecretKeySpec(key.toByteArray(UTF_8), "AES")
        return Cipher.getInstance(AES_MODE).also { it.init(cipherMode, secretKey) }
    }

    private fun toUpperHex(source: ByteArray): String {
        val builder = StringBuilder(source.size shl 1)
        for (value in source) {
            val hex = (value.toInt() and 0xFF).toString(16)
            if (hex.length == 1) builder.append('0')
            builder.append(hex)
        }
        return builder.toString().uppercase()
    }

    private fun parseHex(source: String): ByteArray {
        if (source.isEmpty()) return ByteArray(0)
        val length = source.length / 2
        return ByteArray(length) { offset ->
            val start = offset * 2
            val high = source.substring(start, start + 1).toInt(16)
            val low = source.substring(start + 1, start + 2).toInt(16)
            ((high shl 4) or low).toByte()
        }
    }
}
