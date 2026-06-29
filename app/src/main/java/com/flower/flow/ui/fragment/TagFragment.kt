package com.flower.flow.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.flower.flow.R
import com.flower.flow.app.core.base.BaseFragment
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.model.entity.TagItem
import com.flower.flow.data.vm.TagViewModel
import com.flower.flow.databinding.FragmentTagBinding
import com.flower.flow.databinding.LayoutItemTagTabBinding
import com.flower.flow.ui.adapter.TagPagerAdapter
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.statusPadding
import me.hgj.jetpackmvvm.ext.util.toast
import me.hgj.jetpackmvvm.ext.view.horizontal

class TagFragment : BaseFragment<TagViewModel, FragmentTagBinding>() {

    private var selectedTabPosition = 0
    private lateinit var pagerAdapter: TagPagerAdapter
    private var tagList: List<TagItem> = emptyList()

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            if (selectedTabPosition == position) return
            selectedTabPosition = position
            mBind.rvTagTab.adapter?.notifyDataSetChanged()
            scrollTabToCenter(position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initView(savedInstanceState: Bundle?) {
        mBind.llContent.statusPadding()

        pagerAdapter = TagPagerAdapter(this)
        mBind.viewPager.adapter = pagerAdapter
        mBind.viewPager.registerOnPageChangeCallback(pageChangeCallback)

        mBind.rvTagTab.horizontal()
            .setup {
                addType<TagItem>(R.layout.layout_item_tag_tab)

                onBind {
                    getBindingOrNull<LayoutItemTagTabBinding>()?.run {
                        val model = getModel<TagItem>()
                        tvName.text = model.name
                        val isSelected = modelPosition == selectedTabPosition
                        ivIndicator.isVisible = isSelected
                        tvName.alpha = if (isSelected) 1f else 0.6f
                    }
                }

                onClick(R.id.llTabItem) {
                    val position = modelPosition
                    if (position != selectedTabPosition) {
                        mBind.viewPager.setCurrentItem(position, true)
                    }
                }
            }
    }

    override fun lazyLoadData() {
        mViewModel.loadTagList().obs(this) {
            onSuccess { list ->
                tagList = list
                mBind.rvTagTab.models = list
                pagerAdapter.submitTags(list)
                if (list.isNotEmpty() && selectedTabPosition >= list.size) {
                    selectedTabPosition = 0
                    mBind.viewPager.setCurrentItem(0, false)
                }
            }
            onError { error ->
                error.msg.toast()
            }
        }
    }

    override fun createObserver() {
        EventViewModel.mainFragmentDataEvent.observe(viewLifecycleOwner) {
        }

        EventViewModel.languageEvent.observe(this) {
            mBind.rvTagTab.adapter?.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        mBind.viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        super.onDestroyView()
    }

    private fun scrollTabToCenter(position: Int) {
        val layoutManager = mBind.rvTagTab.layoutManager ?: return
        val itemView = layoutManager.findViewByPosition(position) ?: return
        val recyclerWidth = mBind.rvTagTab.width
        val scrollX = itemView.left - (recyclerWidth - itemView.width) / 2
        mBind.rvTagTab.smoothScrollBy(scrollX, 0)
    }

    companion object {
        fun newInstance(): TagFragment {
            return TagFragment().apply {
                arguments = Bundle()
            }
        }
    }
}
