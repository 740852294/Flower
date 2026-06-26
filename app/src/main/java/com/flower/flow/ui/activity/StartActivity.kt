package com.flower.flow.ui.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.data.model.CacheConfig
import com.flower.flow.data.vm.StartViewModel
import com.flower.flow.databinding.ActivityStartBinding
import me.hgj.jetpackmvvm.ext.util.intent.openActivity

class StartActivity : BaseActivity<StartViewModel, ActivityStartBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        mBind.main.postDelayed({
            if (CacheConfig.isAgree) {

            } else {
                openActivity<PrivacyActivity>()
            }
        }, 500)
    }

    override val showTitle = false
}