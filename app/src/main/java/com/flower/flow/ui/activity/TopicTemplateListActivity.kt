package com.flower.flow.ui.activity

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.dividerSpace
import com.drake.brv.utils.setup
import com.drake.brv.utils.staggered
import com.flower.flow.R
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.ext.initClose
import com.flower.flow.app.core.ext.loadImage
import com.flower.flow.app.core.ext.loadImageFitWidth
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.widget.ActionButton
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.app.event.TopicTemplateListSyncEvent
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.model.entity.TemplateItem
import com.flower.flow.data.vm.TopicTemplateListViewModel
import com.flower.flow.databinding.ActivityTopicTemplateListBinding
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.intent.extraAct
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.doDebouncedClick
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.loadListError
import me.hgj.jetpackmvvm.ext.util.loadListSuccess
import me.hgj.jetpackmvvm.ext.util.loadMore
import me.hgj.jetpackmvvm.ext.util.statusPadding
import me.hgj.jetpackmvvm.ext.util.toast

class TopicTemplateListActivity :
    BaseActivity<TopicTemplateListViewModel, ActivityTopicTemplateListBinding>() {

    val topicId: Int by extraAct(EXTRA_TOPIC_ID, 0)

    val topicName: String by extraAct(EXTRA_TOPIC_NAME, "")

    val topicDescription: String by extraAct(EXTRA_TOPIC_DESCRIPTION, "")

    val topicImg: String by extraAct(EXTRA_TOPIC_IMG, "")

    private var hasNext = false

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

        val isShowBtn = (App.globalConfig?.athleteacanthus ?: 0) == 1
        if (isShowBtn) {
            addReportBtn()
        }
    }

    override fun createObserver() {
        EventViewModel.topicTemplateListSyncEvent.observe(this) { event ->
            syncTemplates(event)
        }
    }

    private fun setupList() {
        mBind.rvList.staggered(SPAN_COUNT)
            .dividerSpace(dp(4),DividerOrientation.HORIZONTAL)
            .dividerSpace(dp(8),DividerOrientation.VERTICAL)
            .setup {
                addType<TemplateItem> { position ->
                    if (position == 1) {
                        R.layout.layout_item_topic_template_min
                    } else {
                        R.layout.layout_item_topic_template
                    }
                }
                onBind {
                    bindTemplateItem(itemView, getModel())
                }

                onClick(R.id.rootItem) {
                    doDebouncedClick {
                        openTopicUseTemplate(modelPosition)
                    }
                }
            }
    }

    private fun loadTemplates(refresh: Boolean) {
        mViewModel.loadTemplates(topicId, refresh).obs(this) {
            onSuccess { page ->
                hasNext = page.hasNext
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

    private fun openTopicUseTemplate(position: Int) {
        val templateList = mBind.rvList.bindingAdapter.models
            ?.filterIsInstance<TemplateItem>()
            ?.toTypedArray()
            ?: emptyArray()
        openActivity<TopicUseTemplateActivity>(
            TopicUseTemplateActivity.EXTRA_TOPIC_ID to topicId,
            TopicUseTemplateActivity.EXTRA_TOPIC_NAME to topicName,
            TopicUseTemplateActivity.EXTRA_TEMPLATE_LIST to templateList,
            TopicUseTemplateActivity.EXTRA_HAS_NEXT to hasNext,
            TopicUseTemplateActivity.EXTRA_CURRENT_PAGE to mViewModel.currentPage,
            TopicUseTemplateActivity.EXTRA_POSITION to position,
        )
    }

    private fun syncTemplates(event: TopicTemplateListSyncEvent) {
        if (event.topicId != topicId) return

        val existingIds = mBind.rvList.bindingAdapter.models
            ?.filterIsInstance<TemplateItem>()
            ?.mapTo(mutableSetOf()) { it.id }
            ?: mutableSetOf()
        val newItems = event.appendedTemplates.filter { existingIds.add(it.id) }
        mBind.rvList.bindingAdapter.addModels(newItems)

        hasNext = event.hasNext
        mViewModel.updateCurrentPage(event.currentPage)
        mBind.refreshLayout.setNoMoreData(!hasNext)
        mBind.refreshLayout.setEnableLoadMore(hasNext)
    }

    private fun updateToolbarBackground(scrollY: Int = mBind.nestedScrollView.scrollY) {
        val atTop = scrollY <= 0
        val colorRes = if (atTop) R.color.transparent else R.color.black
        mBind.llToolbar.setBackgroundColor(ContextCompat.getColor(this, colorRes))
    }

    private fun bindTemplateItem(itemView: View, model: TemplateItem) {
        itemView.findViewById<TextView>(R.id.tvTitle).text = model.name
        itemView.findViewById<ImageView>(R.id.ivCover).loadImage(
            url = model.img,
            cornerRadiusDp = COVER_CORNER_RADIUS_DP,
        )
        val showCost = model.lockIntegral > 0
        val showConfig = (App.globalConfig?.disposenovel ?: 0) in arrayOf(2, 3)
        val showLock = showCost && showConfig
        itemView.findViewById<View>(R.id.llLockBadge).isVisible = showLock
        if (showLock) {
            itemView.findViewById<TextView>(R.id.tvLockIntegral).text =
                model.lockIntegral.toString()
        }
        bindSampleImages(itemView, model.sampleImgList)
    }

    private fun bindSampleImages(itemView: View, samples: List<String>?) {
        val ivSampleSingle = itemView.findViewById<ImageView>(R.id.ivSampleSingle)
        val llSampleBottom = itemView.findViewById<View>(R.id.llSampleBottom)
        ivSampleSingle.isVisible = false
        llSampleBottom.isVisible = false
        when {
            samples.isNullOrEmpty() -> return
            samples.size == 1 -> {
                ivSampleSingle.isVisible = true
                ivSampleSingle.loadImage(
                    url = samples.first(),
                    cornerRadiusDp = SAMPLE_CORNER_RADIUS_DP,
                )
            }

            else -> {
                llSampleBottom.isVisible = true
                itemView.findViewById<ImageView>(R.id.ivSampleLeft).loadImage(
                    url = samples[0],
                    cornerRadiiDp = floatArrayOf(
                        SAMPLE_CORNER_RADIUS_DP,
                        0f,
                        0f,
                        SAMPLE_CORNER_RADIUS_DP,
                    ),
                )
                itemView.findViewById<ImageView>(R.id.ivSampleRight).loadImage(
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

    fun addReportBtn() {
        mBind.toolbar.menu.clear()
        mBind.toolbar.setContentInsetEndWithActions(0)

        val item = mBind.toolbar.menu.add("report").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        val reportButton = ActionButton(this).apply {
            text = FlowCopyStore.get(FlowCopyKey.REPORT_ACTION)
            clickNoRepeat {
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
        private const val COVER_CORNER_RADIUS_DP = 10f
        private const val SAMPLE_CORNER_RADIUS_DP = 4f

        const val EXTRA_TOPIC_ID = "topic_id"
        const val EXTRA_TOPIC_NAME = "topic_name"
        const val EXTRA_TOPIC_DESCRIPTION = "topic_description"
        const val EXTRA_TOPIC_IMG = "topic_img"
    }
}
