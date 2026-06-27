package com.flower.flow.ui.activity

import android.os.Bundle
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.databinding.ActivityVipJoinBinding
import me.hgj.jetpackmvvm.base.vm.BaseViewModel

class VipJoinActivity : BaseActivity<BaseViewModel, ActivityVipJoinBinding>() {

    override val title: String
        get() = FlowCopyStore.get(FlowCopyKey.VIP_OPEN_HINT)

    override fun initView(savedInstanceState: Bundle?) {
    }
}
