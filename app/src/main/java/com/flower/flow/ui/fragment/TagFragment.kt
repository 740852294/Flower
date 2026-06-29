package com.flower.flow.ui.fragment

import android.os.Bundle
import com.flower.flow.app.core.base.BaseFragment
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.vm.TagViewModel
import com.flower.flow.databinding.FragmentTagBinding
import me.hgj.jetpackmvvm.core.net.interception.logging.util.LogUtils

class TagFragment : BaseFragment<TagViewModel, FragmentTagBinding>() {

    companion object {
        fun newInstance(): TagFragment {
            val args = Bundle()
            val fragment = TagFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun lazyLoadData() {
        LogUtils.debugInfo("TagFragment", "lazyLoadData")
    }

    override fun createObserver() {
        EventViewModel.mainFragmentDataEvent.observe(viewLifecycleOwner) {
            LogUtils.debugInfo("TagFragment", "收到通知")
        }

        EventViewModel.languageEvent.observe(this) {
            setText()
        }
    }

    private fun setText() {

    }
}
