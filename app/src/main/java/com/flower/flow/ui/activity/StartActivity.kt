package com.flower.flow.ui.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.data.vm.StartViewModel
import com.flower.flow.databinding.ActivityStartBinding

class StartActivity : BaseActivity<StartViewModel, ActivityStartBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
    }

    override fun getTitleBarView(): View? {
        return null
    }
}