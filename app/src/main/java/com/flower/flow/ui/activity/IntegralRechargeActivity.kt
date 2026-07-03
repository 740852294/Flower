package com.flower.flow.ui.activity

import android.os.Bundle
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.data.model.StringResId
import com.flower.flow.databinding.ActivityIntegralRechargeBinding
import me.hgj.jetpackmvvm.base.vm.BaseViewModel

class IntegralRechargeActivity : BaseActivity<BaseViewModel, ActivityIntegralRechargeBinding>() {

    override val title: String
        get() = AppStrings.get(StringResId.POINT_PRODUCT_PICK)

    override fun initView(savedInstanceState: Bundle?) {
    }
}
