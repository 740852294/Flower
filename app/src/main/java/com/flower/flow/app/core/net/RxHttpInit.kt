package com.flower.flow.app.core.net

import com.flower.flow.app.core.net.interception.HeaderInterceptor
import me.hgj.jetpackmvvm.core.appContext
import me.hgj.jetpackmvvm.core.net.interception.LogInterceptor
import okhttp3.OkHttpClient
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.cookie.CookieStore
import rxhttp.wrapper.ssl.HttpsUtils
import java.io.File
import java.util.concurrent.TimeUnit


object RxHttpInit {
    fun init(){
        RxHttpPlugins.init(getDefaultOkHttpClient().build())
    }

    private fun getDefaultOkHttpClient():  OkHttpClient.Builder {
        val sslParams = HttpsUtils.getSslSocketFactory()
        return OkHttpClient.Builder()
            .cookieJar(CookieStore(File(appContext.externalCacheDir, "RxHttpCookie")))
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .addInterceptor(HeaderInterceptor())
            .addInterceptor(LogInterceptor())//添加Log拦截器
            .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager) //添加信任证书
            .hostnameVerifier { hostname, session -> true } //忽略host验证
    }
}