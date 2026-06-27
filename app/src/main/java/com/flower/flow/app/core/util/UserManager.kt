package com.flower.flow.app.core.util

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.flower.flow.app.core.ext.dismissAppLoadingExt
import com.flower.flow.app.core.ext.showAppLoadingExt
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.model.entity.UserInfo
import com.flower.flow.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.hgj.jetpackmvvm.core.data.postValue
import me.hgj.jetpackmvvm.ext.util.cacheNullable
import me.hgj.jetpackmvvm.ext.util.msg
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.cookie.ICookieJar

object UserManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

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

    /**
     * 获取用户信息。
     *
     * @param showLoading 是否显示 loading，为 true 时必须传入 [activity] 或 [fragment] 之一
     */
    fun fetchUserInfo(
        showLoading: Boolean = false,
        activity: AppCompatActivity? = null,
        fragment: Fragment? = null,
        onSuccess: ((UserInfo) -> Unit)? = null,
        onError: ((String) -> Unit)? = null,
    ) {
        scope.launch {
            val loadingHost = resolveLoadingHost(showLoading, activity, fragment)
            try {
                loadingHost?.showLoading()
                val userInfo = withContext(Dispatchers.IO) {
                    UserRepository.getUserInfoLivedata().await()
                }
                dispatchUserInfo(userInfo)
                onSuccess?.invoke(userInfo)
            } catch (e: Exception) {
                onError?.invoke(e.msg)
            } finally {
                loadingHost?.dismissLoading()
            }
        }
    }

    private fun resolveLoadingHost(
        showLoading: Boolean,
        activity: AppCompatActivity?,
        fragment: Fragment?,
    ): LoadingHost? {
        if (!showLoading) return null
        require(activity != null || fragment != null) {
            "showLoading=true 时需要传入 activity 或 fragment"
        }
        require(activity == null || fragment == null) {
            "activity 和 fragment 不能同时传入"
        }
        return when {
            activity != null -> LoadingHost.ActivityHost(activity)
            fragment != null -> LoadingHost.FragmentHost(fragment)
            else -> null
        }
    }

    private sealed class LoadingHost {
        abstract fun showLoading()
        abstract fun dismissLoading()

        class ActivityHost(private val activity: AppCompatActivity) : LoadingHost() {
            override fun showLoading() = activity.showAppLoadingExt()
            override fun dismissLoading() = activity.dismissAppLoadingExt()
        }

        class FragmentHost(private val fragment: Fragment) : LoadingHost() {
            override fun showLoading() = fragment.showAppLoadingExt()
            override fun dismissLoading() = fragment.dismissAppLoadingExt()
        }
    }

    fun saveUser(userInfo: UserInfo) {
        user = userInfo
    }

    fun clearUser() {
        user = null
        val iCookieJar = RxHttpPlugins.getOkHttpClient().cookieJar as ICookieJar
        iCookieJar.removeAllCookie()
        userLiveData.postValue = user
    }

    fun observeUser(): MutableLiveData<UserInfo?> = userLiveData

    private fun dispatchUserInfo(userInfo: UserInfo) {
        saveUser(userInfo)
        EventViewModel.msgRedDotEvent.postValue = userInfo.sysNotifyRedDot
        EventViewModel.workRedDotEvent.postValue = userInfo.mineRedDot
    }
}
