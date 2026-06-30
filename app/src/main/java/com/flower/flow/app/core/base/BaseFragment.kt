package com.flower.flow.app.core.base

import androidx.viewbinding.ViewBinding
import com.flower.flow.app.core.ext.dismissAppLoadingExt
import com.flower.flow.app.core.ext.showAppLoadingExt
import com.flower.flow.app.core.widget.loadCallBack.EmptyCallback
import com.flower.flow.app.core.widget.loadCallBack.ErrorCallback
import me.hgj.jetpackmvvm.base.ui.BaseVbFragment
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.net.LoadingEntity
import me.hgj.jetpackmvvm.widget.loadsir.callback.Callback


abstract class BaseFragment<VM : BaseViewModel, VB : ViewBinding> (): BaseVbFragment<VM, VB>() {

    /**
     * 这里我自定义了自己项目的 loading ，仅供参考 ，BaseFragment我暂时没有定义，使用的是框架中的默认loading, 如果要改loading，ac/fm都要修改重写
     */
    override fun showLoading(setting: LoadingEntity) {
        showAppLoadingExt(setting.coroutineScope)
    }

    /**
     * 关闭(与showLoading配套使用),BaseFragment我暂时没有定义，使用的是框架中的默认loading , 如果要改loading，ac/fm都要修改重写
     */
    override fun dismissLoading(setting: LoadingEntity) {
        dismissAppLoadingExt()
    }

    override fun getEmptyStateLayout(): Callback? = EmptyCallback()

    override fun getErrorStateLayout(): Callback? = ErrorCallback()
}