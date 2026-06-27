package com.flower.flow.ui.activity

import android.os.Bundle
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.databinding.ActivityIntegralRechargeBinding
import me.hgj.jetpackmvvm.base.vm.BaseViewModel

class IntegralRechargeActivity : BaseActivity<BaseViewModel, ActivityIntegralRechargeBinding>() {

    override val title: String
        get() = FlowCopyStore.get(FlowCopyKey.POINT_PRODUCT_PICK)

    override fun initView(savedInstanceState: Bundle?) {
    }
}
