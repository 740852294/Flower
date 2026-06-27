package com.flower.flow.ui.fragment

import android.os.Bundle
import com.flower.flow.app.core.base.BaseFragment
import com.flower.flow.data.vm.TagViewModel
import com.flower.flow.databinding.FragmentTagBinding

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

    }

    override fun createObserver() {

    }
}
