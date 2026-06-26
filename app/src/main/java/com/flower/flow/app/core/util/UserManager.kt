package com.flower.flow.app.core.util

import androidx.lifecycle.MutableLiveData
import com.flower.flow.data.model.entity.UserInfo
import me.hgj.jetpackmvvm.core.data.postValue
import me.hgj.jetpackmvvm.ext.util.cacheNullable
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.cookie.ICookieJar

object UserManager {

    // 使用 Cache 委托 获取本地缓存的用户信息
    private var userCache : UserInfo? by cacheNullable()

    // 内存缓存（加 @Volatile 确保多线程安全）
    @Volatile
    private var cachedUser: UserInfo? = null

    //  监听
    private val userLiveData = MutableLiveData(userCache)

    var user: UserInfo?
        get() {
            if (cachedUser == null) {
                cachedUser = userCache
                userLiveData.postValue = cachedUser
            }
            return cachedUser
        }
        set(value) {
            cachedUser = value
            userCache = value
            userLiveData.postValue = value
        }

    /** 保存用户信息 */
    fun saveUser(userInfo: UserInfo) {
        user = userInfo
    }

    /** 清空用户信息 */
    fun clearUser() {
        user = null
        val iCookieJar = RxHttpPlugins.getOkHttpClient().cookieJar as ICookieJar
        iCookieJar.removeAllCookie()
        userLiveData.postValue = user
    }

    /** 监听用户信息变化 */
    fun observeUser(): MutableLiveData<UserInfo?> = userLiveData
}