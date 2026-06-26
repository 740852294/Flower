package com.flower.flow.app.core.net.interception

import com.flower.flow.BuildConfig
import com.flower.flow.app.App
import com.flower.flow.data.model.CacheConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 公共请求头拦截器
 */
class HeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
            .header(HeaderKey.PLATFORM_TYPE, PLATFORM_ANDROID)
            .header(
                HeaderKey.PLATFORM_INFO,
                "${android.os.Build.MODEL}-------${android.os.Build.VERSION.RELEASE}"
            )
            .header(HeaderKey.PACKAGE_NAME, BuildConfig.APPLICATION_ID)
            .header(HeaderKey.VERSION, BuildConfig.VERSION_INT)
            .header(HeaderKey.LANGUAGE_ID, resolveLanguageId().toString())

        CacheConfig.userId.takeIf { it.isNotBlank() }?.let {
            builder.header(HeaderKey.UID, it)
        }

        return chain.proceed(builder.build())
    }

    private fun resolveLanguageId(): Int {
        return when {
            App.currentLanguageId > 0 -> App.currentLanguageId
            CacheConfig.selectedLanguageId > 0 -> CacheConfig.selectedLanguageId
            else -> 0
        }
    }

    private companion object {
        const val PLATFORM_ANDROID = "1"

        object HeaderKey {
            const val PLATFORM_TYPE = "platformType"
            const val PLATFORM_INFO = "platformInfo"
            const val PACKAGE_NAME = "package"
            const val VERSION = "versionId"
            const val UID = "uid"
            const val LANGUAGE_ID = "languageId"
        }
    }
}
