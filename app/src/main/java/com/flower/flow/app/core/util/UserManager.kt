package com.flower.flow.app.core.util

import androidx.lifecycle.MutableLiveData
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.model.entity.UserInfo
import me.hgj.jetpackmvvm.core.data.postValue
import me.hgj.jetpackmvvm.ext.util.cacheNullable
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.cookie.ICookieJar

object UserManager {

    private var userCache: UserInfo? by cacheNullable()

    @Volatile
    private var cachedUser: UserInfo? = null

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

    fun saveUser(userInfo: UserInfo) {
        user = userInfo
        EventViewModel.msgRedDotEvent.postValue = userInfo.sysNotifyRedDot
        EventViewModel.workRedDotEvent.postValue = userInfo.mineRedDot
    }

    fun clearUser() {
        user = null
        val iCookieJar = RxHttpPlugins.getOkHttpClient().cookieJar as ICookieJar
        iCookieJar.removeAllCookie()
        userLiveData.postValue = user
    }

    fun observeUser(): MutableLiveData<UserInfo?> = userLiveData

}
