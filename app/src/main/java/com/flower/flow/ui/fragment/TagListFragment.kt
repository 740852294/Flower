package com.flower.flow.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.drake.brv.BindingAdapter
import com.drake.brv.utils.bindingAdapter
import com.flower.flow.R
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseFragment
import com.flower.flow.app.core.ext.clearGlideImage
import com.flower.flow.app.core.ext.loadGlideImage
import com.flower.flow.app.core.ext.setGlideAnimationRunning
import com.flower.flow.data.model.entity.TemplateItem
import com.flower.flow.data.vm.TagListViewModel
import com.flower.flow.databinding.FragmentTagListBinding
import com.flower.flow.databinding.LayoutItemTagTemplateBinding
import com.flower.flow.ui.activity.MaterialUploadActivity
import com.flower.flow.ui.activity.TagUseTemplateActivity
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.doDebouncedClick
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.loadListError
import me.hgj.jetpackmvvm.ext.util.loadListSuccess
import me.hgj.jetpackmvvm.ext.util.loadMore
import me.hgj.jetpackmvvm.ext.util.refresh
import me.hgj.jetpackmvvm.ext.util.toast
import com.drake.brv.utils.staggered

class TagListFragment : BaseFragment<TagListViewModel, FragmentTagListBinding>() {

    private var tagId: Int = 0
    private var cacheGeneration = 0

    override fun getLoadingView(): View = mBind.rvList

    override fun initView(savedInstanceState: Bundle?) {
        tagId = requireArguments().getInt(ARG_TAG_ID)
        cacheGeneration = (parentFragment as? TagFragment)?.getTemplateCacheGeneration() ?: 0

        mBind.refreshLayout.refresh {
            loadTemplates(refresh = true, isLoading = false)
        }
        mBind.refreshLayout.loadMore {
            loadTemplates(refresh = false, isLoading = false)
        }

        val adapter = object : BindingAdapter() {
            override fun onViewRecycled(holder: BindingViewHolder) {
                super.onViewRecycled(holder)
                recycleTemplateItem(holder.itemView)
            }

            override fun onViewAttachedToWindow(holder: BindingViewHolder) {
                super.onViewAttachedToWindow(holder)
                setTemplateItemAnimationsRunning(holder.itemView, true)
            }

            override fun onViewDetachedFromWindow(holder: BindingViewHolder) {
                super.onViewDetachedFromWindow(holder)
                setTemplateItemAnimationsRunning(holder.itemView, false)
            }
        }

        mBind.rvList.staggered(SPAN_COUNT)
        adapter.apply {
            addType<TemplateItem>(R.layout.layout_item_tag_template)

            onBind {
                getBindingOrNull<LayoutItemTagTemplateBinding>()?.run {
                    val model = getModel<TemplateItem>()
                    tvTitle.text = model.dazzledeacon
                    ivCover.loadGlideImage(model.bullmind)
                    bindLockBadge(this, model)
                    bindSampleImages(this, model.neverchapter)
                }
            }

            onClick(R.id.rootItem) {
                doDebouncedClick {
                    openActivity<TagUseTemplateActivity>(
                        MaterialUploadActivity.EXTRA_TEMPLATE_ITEM to getModel<TemplateItem>(),
                    )
                }
            }
        }
        mBind.rvList.adapter = adapter
    }

    override fun lazyLoadData() {
        if (!restoreTemplatesFromCache()) {
            loadTemplates(refresh = true, isLoading = true)
        }
    }

    private fun restoreTemplatesFromCache(): Boolean {
        val page = (parentFragment as? TagFragment)
            ?.getCachedTemplates(tagId, cacheGeneration)
            ?: return false
        mViewModel.restoreCurrentPage(page.amenableafford)
        loadListSuccess(
            page,
            mBind.rvList.bindingAdapter,
            mBind.refreshLayout,
            this,
            isRefresh = true,
        )
        return true
    }

    private fun loadTemplates(refresh: Boolean, isLoading: Boolean) {
        mViewModel.loadTemplates(tagId, refresh, isLoading).obs(this) {
            onSuccess { page ->
                (parentFragment as? TagFragment)?.cacheTemplates(
                    tagId,
                    page,
                    refresh,
                    cacheGeneration,
                )
                loadListSuccess(
                    page,
                    mBind.rvList.bindingAdapter,
                    mBind.refreshLayout,
                    this@TagListFragment,
                    isRefresh = refresh,
                )
            }
            onError { status ->
                loadListError(status, mBind.refreshLayout)
                status.msg.toast()
            }
        }
    }

    private fun bindLockBadge(
        binding: LayoutItemTagTemplateBinding,
        model: TemplateItem,
    ) {
        val showCost = model.peacearrow > 0
        val showConfig = (App.globalConfig?.disposenovel ?: 0) in arrayOf(3, 4)
        val showLock = showCost && showConfig
        binding.llLockBadge.isVisible = showLock
        if (showLock) {
            binding.tvLockIntegral.text = model.peacearrow.toString()
        }
    }

    private fun bindSampleImages(
        binding: LayoutItemTagTemplateBinding,
        samples: List<String>?,
    ) {
        binding.ivSampleSingle.isVisible = false
        binding.llSampleBottom.isVisible = false
        when {
            samples.isNullOrEmpty() -> {
                binding.ivSampleSingle.clearGlideImage()
                binding.ivSampleLeft.clearGlideImage()
                binding.ivSampleRight.clearGlideImage()
                return
            }
            samples.size == 1 -> {
                binding.ivSampleSingle.isVisible = true
                binding.ivSampleSingle.loadGlideImage(samples.first())
                binding.ivSampleLeft.clearGlideImage()
                binding.ivSampleRight.clearGlideImage()
            }

            else -> {
                binding.llSampleBottom.isVisible = true
                binding.ivSampleSingle.clearGlideImage()
                binding.ivSampleLeft.loadGlideImage(samples[0])
                binding.ivSampleRight.loadGlideImage(samples.getOrNull(1))
            }
        }
    }

    private fun recycleTemplateItem(itemView: View) {
        LayoutItemTagTemplateBinding.bind(itemView).run {
            ivCover.clearGlideImage()
            ivSampleSingle.clearGlideImage()
            ivSampleLeft.clearGlideImage()
            ivSampleRight.clearGlideImage()
        }
    }

    private fun setTemplateItemAnimationsRunning(itemView: View, running: Boolean) {
        LayoutItemTagTemplateBinding.bind(itemView).run {
            ivCover.setGlideAnimationRunning(running)
            ivSampleSingle.setGlideAnimationRunning(running)
            ivSampleLeft.setGlideAnimationRunning(running)
            ivSampleRight.setGlideAnimationRunning(running)
        }
    }

    companion object {
        private const val ARG_TAG_ID = "tag_id"
        private const val SPAN_COUNT = 2

        fun newInstance(tagId: Int): TagListFragment {
            return TagListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TAG_ID, tagId)
                }
            }
        }
    }
}
