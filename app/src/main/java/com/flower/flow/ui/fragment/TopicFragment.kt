package com.flower.flow.ui.fragment

import android.os.Bundle
import com.flower.flow.app.core.base.BaseFragment
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.vm.TopicViewModel
import com.flower.flow.databinding.FragmentTopicBinding
import me.hgj.jetpackmvvm.core.net.interception.logging.util.LogUtils

class TopicFragment : BaseFragment<TopicViewModel, FragmentTopicBinding>() {

    companion object {
        fun newInstance(): TopicFragment {
            val args = Bundle()
            val fragment = TopicFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun lazyLoadData() {
        LogUtils.debugInfo("TopicFragment", "lazyLoadData")
    }

    override fun createObserver() {
        EventViewModel.mainFragmentDataEvent.observe(viewLifecycleOwner) {
            LogUtils.debugInfo("TopicFragment", "收到通知")
        }

        EventViewModel.languageEvent.observe(this) {
            setText()
        }
    }

    private fun setText() {

    }
}
