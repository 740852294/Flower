package com.flower.flow.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.flower.flow.R
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.data.vm.PrivacyViewModel
import com.flower.flow.databinding.ActivityPrivacyBinding
import me.hgj.jetpackmvvm.core.data.obs

class PrivacyActivity : BaseActivity<PrivacyViewModel, ActivityPrivacyBinding>() {

    override val showTitle = false

    override fun initView(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        mViewModel.getLanguageList().obs(this) {
            onSuccess { data ->
                // 处理成功数据

            }
            onError { status ->
                // 自定义错误处理
            }
        }
    }
}