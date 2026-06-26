package com.flower.flow.ui.activity

import android.os.Bundle
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.util.LanguageConfigHelper
import com.flower.flow.data.model.CacheConfig
import com.flower.flow.data.vm.StartViewModel
import com.flower.flow.databinding.ActivityStartBinding
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.finishAllActivity
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.intent.openActivityForResult
import me.hgj.jetpackmvvm.ext.util.toast

class StartActivity : BaseActivity<StartViewModel, ActivityStartBinding>() {

    override val showTitle = false

    override fun initView(savedInstanceState: Bundle?) {
        mBind.main.postDelayed({ routeNext() }, 500)
    }

    private fun routeNext() {
        if (!CacheConfig.isAgree) {
            openActivityForResult<PrivacyActivity> { _ ->
                if (CacheConfig.isAgree) {
                    checkUser()
                }
            }
            return
        }
        if (CacheConfig.hasLanguageConfigCache()) {
            LanguageConfigHelper.restoreLanguageIdFromCache()
            checkUser()
            return
        }
        mViewModel.initLanguageConfig().obs(this) {
            onSuccess { checkUser() }
            onError { error ->
                error.msg.toast()
                finishAllActivity()
            }
        }
    }

    private fun checkUser() {
        if (CacheConfig.userId.isEmpty()) {
            mViewModel.registerUserByAttribution(applicationContext).obs(this) {
                onSuccess { response ->
                    CacheConfig.userId = response.uid
                    goMain()
                }
                onError {
                    finishAllActivity()
                }
            }
        } else {
            goMain()
        }
    }

    private fun goMain() {
        openActivity<MainActivity>()
        finish()
    }
}
