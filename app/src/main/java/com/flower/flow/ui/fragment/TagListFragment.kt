package com.flower.flow.ui.fragment

import android.os.Bundle
import androidx.core.view.isVisible
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.flower.flow.R
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseFragment
import com.flower.flow.app.core.ext.loadImage
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.model.entity.TagTemplateItem
import com.flower.flow.data.vm.TagListViewModel
import com.flower.flow.databinding.FragmentTagListBinding
import com.flower.flow.databinding.LayoutItemTagTemplateBinding
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.loadListError
import me.hgj.jetpackmvvm.ext.util.loadListSuccess
import me.hgj.jetpackmvvm.ext.util.loadMore
import me.hgj.jetpackmvvm.ext.util.refresh
import me.hgj.jetpackmvvm.ext.util.toast
import me.hgj.jetpackmvvm.ext.view.grid

class TagListFragment : BaseFragment<TagListViewModel, FragmentTagListBinding>() {

    private var tagId: Int = 0

    override fun initView(savedInstanceState: Bundle?) {
        tagId = requireArguments().getInt(ARG_TAG_ID)

        mBind.refreshLayout.refresh {
            loadTemplates(refresh = true)
        }
        mBind.refreshLayout.loadMore {
            loadTemplates(refresh = false)
        }

        mBind.rvList.grid(SPAN_COUNT)
            .setup {
                addType<TagTemplateItem>(R.layout.layout_item_tag_template)

                onBind {
                    getBindingOrNull<LayoutItemTagTemplateBinding>()?.run {
                        val model = getModel<TagTemplateItem>()
                        tvTitle.text = model.name
                        ivCover.loadImage(
                            url = model.img,
                            cornerRadiusDp = COVER_CORNER_RADIUS_DP,
                            borderWidthDp = COVER_BORDER_WIDTH_DP,
                        )
                        bindLockBadge(this, model)
                        bindSampleImages(this, model.sampleImgList)
                    }
                }

                onClick(R.id.rootItem) {
                    // 点击事件待实现
                }
            }
    }

    override fun lazyLoadData() {
        loadTemplates(refresh = true)
    }

    private fun loadTemplates(refresh: Boolean) {
        mViewModel.loadTemplates(tagId, refresh).obs(this) {
            onSuccess { page ->
                loadListSuccess(
                    page,
                    mBind.rvList.bindingAdapter,
                    mBind.refreshLayout,
                    this@TagListFragment,
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
        model: TagTemplateItem,
    ) {
        val showCost = model.lockIntegral > 0 && model.lockType != 0
        val showConfig = (App.globalConfig?.templateAbduceIntegralShow ?: 0) in arrayOf(3, 4)
        val showLock = showCost && showConfig
        binding.llLockBadge.isVisible = showLock
        if (showLock) {
            binding.tvLockIntegral.text = model.lockIntegral.toString()
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
                )
                binding.ivSampleRight.loadImage(
                    url = samples.getOrNull(1),
                    cornerRadiiDp = floatArrayOf(
                        0f,
                        SAMPLE_CORNER_RADIUS_DP,
                        SAMPLE_CORNER_RADIUS_DP,
                        0f,
                    ),
                )
            }
        }
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
