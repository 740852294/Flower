package com.flower.flow.ui.activity

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.setup
import com.flower.flow.R
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.ext.initClose
import com.flower.flow.app.core.ext.loadImage
import com.flower.flow.app.core.ext.loadImageFitWidth
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.widget.ActionButton
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.model.entity.TemplateItem
import com.flower.flow.data.vm.TopicTemplateListViewModel
import com.flower.flow.databinding.ActivityTopicTemplateListBinding
import com.flower.flow.databinding.LayoutItemTopicTemplateBinding
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.intent.extraAct
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.loadListError
import me.hgj.jetpackmvvm.ext.util.loadListSuccess
import me.hgj.jetpackmvvm.ext.util.loadMore
import me.hgj.jetpackmvvm.ext.util.statusPadding
import me.hgj.jetpackmvvm.ext.util.toast
import me.hgj.jetpackmvvm.ext.view.grid

class TopicTemplateListActivity :
    BaseActivity<TopicTemplateListViewModel, ActivityTopicTemplateListBinding>() {

    val topicId: Int by extraAct(EXTRA_TOPIC_ID, 0)

    val topicName: String by extraAct(EXTRA_TOPIC_NAME, "")

    val topicDescription: String by extraAct(EXTRA_TOPIC_DESCRIPTION, "")

    val topicImg: String by extraAct(EXTRA_TOPIC_IMG, "")

    override val showTitle: Boolean
        get() = false

    override fun initView(savedInstanceState: Bundle?) {
        mBind.llToolbar.statusPadding()
        mBind.llContent.statusPadding()
        mBind.toolbar.initClose(topicName) {
            finish()
        }

        mBind.tvDes.text = topicDescription

        if (topicImg.isNotEmpty()) {
            mBind.ivImg.loadImageFitWidth(url = topicImg)
        }

        setupList()

        mBind.refreshLayout.loadMore {
            loadTemplates(refresh = false)
        }

        mBind.nestedScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                updateToolbarBackground(scrollY)
            },
        )
        updateToolbarBackground(mBind.nestedScrollView.scrollY)

        loadTemplates(refresh = true)

        val isShowBtn = (App.globalConfig?.reportEntranceShow ?: 0) == 1
        if (isShowBtn) {
            addReportBtn()
        }
    }

    private fun setupList() {
        mBind.rvList.grid(SPAN_COUNT)
            .divider(R.drawable.divider_horizontal_16)
            .divider(R.drawable.divider_vertical_8, DividerOrientation.VERTICAL)
            .setup {
                addType<TemplateItem>(R.layout.layout_item_topic_template)

                onBind {
                    getBindingOrNull<LayoutItemTopicTemplateBinding>()?.run {
                        val model = getModel<TemplateItem>()
                        tvTitle.text = model.name
                        ivCover.loadImage(
                            url = model.img,
                            cornerRadiusDp = 10f,
                        )
                        val showCost = model.lockIntegral > 0
                        val showConfig =
                            (App.globalConfig?.templateAbduceIntegralShow ?: 0) in arrayOf(2, 3)
                        val showLock = showCost && showConfig
                        llLockBadge.isVisible = showLock
                        if (showLock) {
                            tvLockIntegral.text = model.lockIntegral.toString()
                        }
                        bindSampleImages(this, model.sampleImgList)
                    }
                }
            }
    }

    private fun loadTemplates(refresh: Boolean) {
        mViewModel.loadTemplates(topicId, refresh).obs(this) {
            onSuccess { page ->
                loadListSuccess(
                    page,
                    mBind.rvList.bindingAdapter,
                    mBind.refreshLayout,
                    this@TopicTemplateListActivity,
                    isRefresh = refresh,
                )
            }
            onError { status ->
                loadListError(status, mBind.refreshLayout)
                status.msg.toast()
            }
        }
    }

    private fun updateToolbarBackground(scrollY: Int = mBind.nestedScrollView.scrollY) {
        val atTop = scrollY <= 0
        val colorRes = if (atTop) R.color.transparent else R.color.black
        mBind.llToolbar.setBackgroundColor(ContextCompat.getColor(this, colorRes))
    }

    private fun bindSampleImages(
        binding: LayoutItemTopicTemplateBinding,
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
                    cornerRadiusDp = 4f,
                )
            }

            else -> {
                binding.llSampleBottom.isVisible = true
                binding.ivSampleLeft.loadImage(
                    url = samples[0],
                    cornerRadiiDp = floatArrayOf(
                        4f,
                        0f,
                        0f,
                        4f,
                    ),
                )
                binding.ivSampleRight.loadImage(
                    url = samples.getOrNull(1),
                    cornerRadiiDp = floatArrayOf(
                        0f,
                        4f,
                        4f,
                        0f,
                    ),
                )
            }
        }
    }

    fun addReportBtn() {
        mBind.toolbar.menu.clear()
        mBind.toolbar.setContentInsetEndWithActions(0)

        val item = mBind.toolbar.menu.add("report").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        val reportButton = ActionButton(this).apply {
            text = FlowCopyStore.get(FlowCopyKey.REPORT_ACTION)
            setOnClickListener {
                openActivity<ReportActivity>()
            }
        }

        item.actionView = FrameLayout(this).apply {
            setPadding(0, 0, dp(15), 0)

            addView(
                reportButton,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER,
                ),
            )
        }
    }

    companion object {
        private const val SPAN_COUNT = 2

        const val EXTRA_TOPIC_ID = "topic_id"
        const val EXTRA_TOPIC_NAME = "topic_name"
        const val EXTRA_TOPIC_DESCRIPTION = "topic_description"
        const val EXTRA_TOPIC_IMG = "topic_img"
    }
}
