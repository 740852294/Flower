package com.flower.flow.app.core.base

import androidx.viewbinding.ViewBinding
import com.flower.flow.app.core.ext.dismissAppLoadingExt
import com.flower.flow.app.core.ext.showAppLoadingExt
import me.hgj.jetpackmvvm.base.ui.BaseVbFragment
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.net.LoadingEntity


abstract class BaseFragment<VM : BaseViewModel, VB : ViewBinding> (): BaseVbFragment<VM, VB>() {

    override fun showLoading(setting: LoadingEntity) {
        showAppLoadingExt(setting.coroutineScope)
    }

    override fun dismissLoading(setting: LoadingEntity) {
        dismissAppLoadingExt()
    }
}