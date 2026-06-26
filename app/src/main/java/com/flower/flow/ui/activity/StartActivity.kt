package com.flower.flow.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.flower.flow.MainActivity
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.util.LanguageConfigHelper
import com.flower.flow.data.model.CacheConfig
import com.flower.flow.data.vm.StartViewModel
import com.flower.flow.databinding.ActivityStartBinding
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.finishAllActivity
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.toast

class StartActivity : BaseActivity<StartViewModel, ActivityStartBinding>() {

    override val showTitle = false

    override fun initView(savedInstanceState: Bundle?) {
        mBind.main.postDelayed({ routeNext() }, 500)
    }

    private fun routeNext() {
        if (!CacheConfig.isAgree) {
            openActivity<PrivacyActivity>()
            return
        }
        if (CacheConfig.hasLanguageConfigCache()) {
            LanguageConfigHelper.restoreLanguageIdFromCache()
            goMain()
            return
        }
        mViewModel.initLanguageConfig().obs(this) {
            onSuccess { goMain() }
            onError { error ->
                error.msg.toast()
                finishAllActivity()
            }
        }
    }

    private fun goMain() {
        openActivity<MainActivity>()
        finish()
    }
}
