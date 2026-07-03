package com.flower.flow.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.setup
import com.flower.flow.R
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseFragment
import com.flower.flow.app.core.ext.loadImage
import com.flower.flow.app.core.ext.setImageAnimationRunning
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
import me.hgj.jetpackmvvm.ext.view.grid

class TagListFragment : BaseFragment<TagListViewModel, FragmentTagListBinding>() {

    private var tagId: Int = 0
    private var isPagerTransitioning = false
    private var isContainerVisible = true
    private var cacheGeneration = 0

    override fun initView(savedInstanceState: Bundle?) {
        tagId = requireArguments().getInt(ARG_TAG_ID)
        (parentFragment as? TagFragment)?.let { parent ->
            isPagerTransitioning = parent.isTagPagerTransitioning()
            isContainerVisible = parent.isTagContainerVisible()
            cacheGeneration = parent.getTemplateCacheGeneration()
        }

        mBind.refreshLayout.refresh {
            loadTemplates(refresh = true)
        }
        mBind.refreshLayout.loadMore {
            loadTemplates(refresh = false)
        }

        mBind.rvList.grid(SPAN_COUNT)
            .setup {
                addType<TemplateItem>(R.layout.layout_item_tag_template)

                onBind {
                    getBindingOrNull<LayoutItemTagTemplateBinding>()?.run {
                        val model = getModel<TemplateItem>()
                        tvTitle.text = model.dazzledeacon
                        ivCover.loadImage(
                            url = model.bullmind,
                            cornerRadiusDp = COVER_CORNER_RADIUS_DP,
                            borderWidthDp = COVER_BORDER_WIDTH_DP,
                            isAutoPlay = true,
                            onFinalImageSet = ::syncAnimationPlayback,
                        )
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

        mBind.rvList.addOnChildAttachStateChangeListener(
            object : RecyclerView.OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(view: View) {
                    setItemAnimationRunning(view, canPlayAnimations())
                }

                override fun onChildViewDetachedFromWindow(view: View) = Unit
            }
        )
        mBind.rvList.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        resumeVisibleAnimations()
                    } else {
                        setVisibleAnimationsRunning(false)
                    }
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        resumeVisibleAnimations()
    }

    override fun onPause() {
        if (!isPagerTransitioning || !isContainerVisible) {
            setVisibleAnimationsRunning(false)
        }
        super.onPause()
    }

    override fun lazyLoadData() {
        if (!restoreTemplatesFromCache()) {
            loadTemplates(refresh = true)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            setVisibleAnimationsRunning(false)
        } else {
            resumeVisibleAnimations()
        }
    }

    internal fun setPagerTransitioning(transitioning: Boolean) {
        isPagerTransitioning = transitioning
        if (transitioning || isResumed) {
            resumeVisibleAnimations()
        } else {
            setVisibleAnimationsRunning(false)
        }
    }

    internal fun setContainerVisible(visible: Boolean) {
        isContainerVisible = visible
        if (visible) {
            resumeVisibleAnimations()
        } else if (view != null) {
            setVisibleAnimationsRunning(false)
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

    private fun loadTemplates(refresh: Boolean) {
        mViewModel.loadTemplates(tagId, refresh).obs(this) {
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
            samples.isNullOrEmpty() -> return
            samples.size == 1 -> {
                binding.ivSampleSingle.isVisible = true
                binding.ivSampleSingle.loadImage(
                    url = samples.first(),
                    cornerRadiusDp = SAMPLE_CORNER_RADIUS_DP,
                    isAutoPlay = false,
                    resizeToViewport = true,
                )
            }

            else -> {
                binding.llSampleBottom.isVisible = true
                binding.ivSampleLeft.loadImage(
                    url = samples[0],
                    cornerRadiiDp = floatArrayOf(
                        SAMPLE_CORNER_RADIUS_DP,
                        0f,
                        0f,
                        SAMPLE_CORNER_RADIUS_DP,
                    ),
                    isAutoPlay = false,
                    resizeToViewport = true,
                )
                binding.ivSampleRight.loadImage(
                    url = samples.getOrNull(1),
                    cornerRadiiDp = floatArrayOf(
                        0f,
                        SAMPLE_CORNER_RADIUS_DP,
                        SAMPLE_CORNER_RADIUS_DP,
                        0f,
                    ),
                    isAutoPlay = false,
                    resizeToViewport = true,
                )
            }
        }
    }

    private fun syncAnimationPlayback(imageView: ImageView) {
        imageView.setImageAnimationRunning(
            canPlayAnimations() && imageView.isAttachedToWindow,
        )
    }

    private fun canPlayAnimations(): Boolean {
        if (view == null) return false
        return isContainerVisible &&
                !isHidden &&
                (isResumed || isPagerTransitioning) &&
                mBind.rvList.scrollState == RecyclerView.SCROLL_STATE_IDLE
    }

    private fun resumeVisibleAnimations() {
        if (view == null) return
        mBind.rvList.post {
            if (canPlayAnimations()) {
                setVisibleAnimationsRunning(true)
            }
        }
    }

    private fun setVisibleAnimationsRunning(running: Boolean) {
        mBind.rvList.children.forEach { itemView ->
            setItemAnimationRunning(itemView, running)
        }
    }

    private fun setItemAnimationRunning(itemView: View, running: Boolean) {
        LayoutItemTagTemplateBinding.bind(itemView).ivCover
            .setImageAnimationRunning(running)
    }

    companion object {
        private const val ARG_TAG_ID = "tag_id"
        private const val SPAN_COUNT = 2
        private const val COVER_CORNER_RADIUS_DP = 15f
        private const val COVER_BORDER_WIDTH_DP = 1f
        private const val SAMPLE_CORNER_RADIUS_DP = 5f

        fun newInstance(tagId: Int): TagListFragment {
            return TagListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TAG_ID, tagId)
                }
            }
        }
    }
}
