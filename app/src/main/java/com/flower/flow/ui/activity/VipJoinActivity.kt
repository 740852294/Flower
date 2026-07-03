package com.flower.flow.ui.activity

import android.os.Bundle
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.data.model.StringResId
import com.flower.flow.databinding.ActivityVipJoinBinding
import me.hgj.jetpackmvvm.base.vm.BaseViewModel

class VipJoinActivity : BaseActivity<BaseViewModel, ActivityVipJoinBinding>() {

    override val title: String
        get() = AppStrings.get(StringResId.VIP_OPEN_HINT)

    override fun initView(savedInstanceState: Bundle?) {
    }
}
