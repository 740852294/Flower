package com.flower.flow.domain.startup

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.flower.flow.data.model.CacheConfig
import com.flower.flow.data.model.entity.RegisterResponse
import me.hgj.jetpackmvvm.core.data.ApiResult
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.finishAllActivity

object StartUserGate {

    fun checkUser(
        lifecycleOwner: LifecycleOwner,
        context: Context,
        registerUser: (Context) -> LiveData<ApiResult<RegisterResponse>>,
        onGoMain: () -> Unit,
    ) {
        if (CacheConfig.userId.isEmpty()) {
            registerUser(context).obs(lifecycleOwner) {
                onSuccess { response ->
                    CacheConfig.userId = response.elephantfloat
                    onGoMain()
                }
                onError {
                    finishAllActivity()
                }
            }
        } else {
            onGoMain()
        }
    }
}
