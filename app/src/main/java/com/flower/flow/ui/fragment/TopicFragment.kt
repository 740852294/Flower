package com.flower.flow.ui.fragment

import android.os.Bundle
import com.flower.flow.app.core.base.BaseFragment
import com.flower.flow.data.vm.TopicViewModel
import com.flower.flow.databinding.FragmentTopicBinding

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
}
