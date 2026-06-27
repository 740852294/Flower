package com.flower.flow.ui.fragment

import android.os.Bundle
import com.flower.flow.app.core.base.BaseFragment
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.vm.MeViewModel
import com.flower.flow.databinding.FragmentMeBinding
import com.flower.flow.ui.dialog.CommonMessageDialog
import me.hgj.jetpackmvvm.core.net.interception.logging.util.LogUtils

class MeFragment : BaseFragment<MeViewModel, FragmentMeBinding>() {

    companion object {
        fun newInstance(): MeFragment {
            val args = Bundle()
            val fragment = MeFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun lazyLoadData() {
        LogUtils.debugInfo("MeFragment", "lazyLoadData")
    }

    override fun createObserver() {
        EventViewModel.mainFragmentDataEvent.observe(viewLifecycleOwner) {
            LogUtils.debugInfo("MeFragment", "收到通知")
        }
    }
}