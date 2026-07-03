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
import com.flower.flow.data.model.entity.ApiPagerResponse
import com.flower.flow.data.model.entity.TagItem
import com.flower.flow.data.model.entity.TemplateItem
import com.flower.flow.data.vm.TagViewModel
import com.flower.flow.databinding.FragmentTagBinding
import com.flower.flow.databinding.LayoutItemTagTabBinding
import com.flower.flow.ui.adapter.TagPagerAdapter
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.doDebouncedClick
import me.hgj.jetpackmvvm.ext.util.statusPadding
import me.hgj.jetpackmvvm.ext.util.toast
import me.hgj.jetpackmvvm.ext.view.horizontal

class TagFragment : BaseFragment<TagViewModel, FragmentTagBinding>() {

    private var selectedTabPosition = 0
    private lateinit var pagerAdapter: TagPagerAdapter
    private var tagList: List<TagItem> = emptyList()
    private var isFirstTagLoad = true
    private var isPagerIdle = true

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            if (selectedTabPosition == position) return
            val previousPosition = selectedTabPosition
            selectedTabPosition = position
            mBind.rvTagTab.adapter?.run {
                if (previousPosition in 0 until itemCount) {
                    notifyItemChanged(previousPosition)
                }
                if (position in 0 until itemCount) {
                    notifyItemChanged(position)
                }
            }
            scrollTabToCenter(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
            isPagerIdle = state == ViewPager2.SCROLL_STATE_IDLE
            childFragmentManager.fragments
                .filterIsInstance<TagListFragment>()
                .forEach { fragment ->
                    fragment.setPagerIdle(isPagerIdle)
                }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initView(savedInstanceState: Bundle?) {
        mBind.llContent.statusPadding()

        pagerAdapter = TagPagerAdapter(this)
        mBind.viewPager.adapter = pagerAdapter
        mBind.viewPager.offscreenPageLimit = OFFSCREEN_PAGE_LIMIT
        mBind.viewPager.registerOnPageChangeCallback(pageChangeCallback)

        mBind.rvTagTab.horizontal()
            .setup {
                addType<TagItem>(R.layout.layout_item_tag_tab)

                onBind {
                    getBindingOrNull<LayoutItemTagTabBinding>()?.run {
                        val model = getModel<TagItem>()
                        tvName.text = model.dazzledeacon
                        val isSelected = modelPosition == selectedTabPosition
                        ivIndicator.isVisible = isSelected
                        tvName.alpha = if (isSelected) 1f else 0.6f
                    }
                }

                onClick(R.id.llTabItem) {
                    doDebouncedClick {
                        val position = modelPosition
                        if (position != selectedTabPosition) {
                            mBind.viewPager.setCurrentItem(position, true)
                        }
                    }
                }
            }
    }

    override fun lazyLoadData() {
        loadTagList(showLoading = true)
    }

    override fun createObserver() {
        EventViewModel.languageEvent.observe(this) {
            mViewModel.invalidateTemplateCache()
            loadTagList(showLoading = false)
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

    private fun loadTagList(showLoading: Boolean) {
        mViewModel.loadTagList(showLoading).obs(this) {
            onSuccess { list ->
                tagList = list
                mBind.rvTagTab.models = list
                pagerAdapter.submitTags(
                    newTags = list,
                    recreateFragments = !isFirstTagLoad,
                )
                isFirstTagLoad = false
                if (list.isNotEmpty()) {
                    val targetPosition = selectedTabPosition.coerceIn(list.indices)
                    selectedTabPosition = targetPosition
                    mBind.viewPager.setCurrentItem(targetPosition, false)
                    mBind.rvTagTab.adapter?.notifyDataSetChanged()
                }
            }
            onError { error ->
                error.msg.toast()
            }
        }
    }

    internal fun isTagPagerIdle(): Boolean = isPagerIdle

    internal fun getTemplateCacheGeneration(): Int {
        return mViewModel.getTemplateCacheGeneration()
    }

    internal fun getCachedTemplates(
        tagId: Int,
        generation: Int,
    ): ApiPagerResponse<TemplateItem>? {
        return mViewModel.getCachedTemplates(tagId, generation)
    }

    internal fun cacheTemplates(
        tagId: Int,
        page: ApiPagerResponse<TemplateItem>,
        refresh: Boolean,
        generation: Int,
    ) {
        mViewModel.cacheTemplates(tagId, page, refresh, generation)
    }

    companion object {
        private const val OFFSCREEN_PAGE_LIMIT = 1

        fun newInstance(): TagFragment {
            return TagFragment().apply {
                arguments = Bundle()
            }
        }
    }
}
