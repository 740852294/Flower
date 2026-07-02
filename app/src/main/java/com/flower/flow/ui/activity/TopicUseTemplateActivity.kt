package com.flower.flow.ui.activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.flower.flow.R
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.ext.initClose
import com.flower.flow.app.core.ext.loadImage
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.widget.ActionButton
import com.flower.flow.app.core.widget.CoverFlowScrollListener
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.app.event.TopicTemplateListSyncEvent
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.model.entity.TemplateItem
import com.flower.flow.data.vm.TopicUseTemplateViewModel
import com.flower.flow.databinding.ActivityTopicUseTemplateBinding
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.intent.extraAct
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.statusPadding
import me.hgj.jetpackmvvm.ext.util.toast

class TopicUseTemplateActivity :
    BaseActivity<TopicUseTemplateViewModel, ActivityTopicUseTemplateBinding>() {

    val topicId: Int by extraAct(EXTRA_TOPIC_ID, 0)

    val topicName: String by extraAct(EXTRA_TOPIC_NAME, "")

    val hasNext: Boolean by extraAct(EXTRA_HAS_NEXT, false)

    val currentPage: Int by extraAct(EXTRA_CURRENT_PAGE, 1)

    val position: Int by extraAct(EXTRA_POSITION, 0)

    val templateList: ArrayList<TemplateItem> by lazy { ArrayList(readTemplateList()) }

    val currentTemplate: TemplateItem?
        get() = templateList.getOrNull(selectedPosition)

    private var selectedPosition = 0
    private var previewPosition = RecyclerView.NO_POSITION
    private var itemWidth = 0
    private var itemOverlap = 0
    private var canLoadMore = false
    private var isLoadingMore = false
    private var nextCarouselGestureTime = 0L
    private var isCarouselGestureBlocked = false
    private val appendedTemplates = arrayListOf<TemplateItem>()
    private lateinit var carouselLayoutManager: SinglePageLinearLayoutManager

    private val snapHelper = object : PagerSnapHelper() {
        override fun calculateDistanceToFinalSnap(
            layoutManager: RecyclerView.LayoutManager,
            targetView: View,
        ): IntArray {
            val childCenter = targetView.left + targetView.width / 2
            val containerCenter = (layoutManager.paddingLeft +
                layoutManager.width - layoutManager.paddingRight) / 2
            return intArrayOf(childCenter - containerCenter, 0)
        }

        override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
            if (layoutManager.childCount == 0) return null

            val containerCenter = (layoutManager.paddingLeft +
                layoutManager.width - layoutManager.paddingRight) / 2
            var closestView: View? = null
            var closestDistance = Int.MAX_VALUE
            for (index in 0 until layoutManager.childCount) {
                val child = layoutManager.getChildAt(index) ?: continue
                val childCenter = child.left + child.width / 2
                val distance = kotlin.math.abs(childCenter - containerCenter)
                if (distance < closestDistance) {
                    closestDistance = distance
                    closestView = child
                }
            }
            return closestView
        }

        override fun findTargetSnapPosition(
            layoutManager: RecyclerView.LayoutManager,
            velocityX: Int,
            velocityY: Int,
        ): Int {
            if (layoutManager.itemCount == 0) return RecyclerView.NO_POSITION

            val direction = when {
                velocityX > 0 -> 1
                velocityX < 0 -> -1
                else -> 0
            }
            return (selectedPosition + direction)
                .coerceIn(0, layoutManager.itemCount - 1)
        }
    }
    private val coverFlowListener = CoverFlowScrollListener()

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            updatePreviewPosition(recyclerView)
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            when (newState) {
                RecyclerView.SCROLL_STATE_DRAGGING -> {
                    carouselLayoutManager.beginSinglePageScroll()
                }

                RecyclerView.SCROLL_STATE_IDLE -> {
                    carouselLayoutManager.endSinglePageScroll()
                    updateSelectedPosition(recyclerView)
                }
            }
        }
    }

    private val carouselTouchListener = object : RecyclerView.SimpleOnItemTouchListener() {
        override fun onInterceptTouchEvent(recyclerView: RecyclerView, event: MotionEvent): Boolean {
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                isCarouselGestureBlocked =
                    recyclerView.scrollState != RecyclerView.SCROLL_STATE_IDLE ||
                        SystemClock.elapsedRealtime() < nextCarouselGestureTime
            }
            return isCarouselGestureBlocked
        }

        override fun onTouchEvent(recyclerView: RecyclerView, event: MotionEvent) {
            if (event.actionMasked == MotionEvent.ACTION_UP ||
                event.actionMasked == MotionEvent.ACTION_CANCEL
            ) {
                isCarouselGestureBlocked = false
            }
        }
    }

    override val showTitle: Boolean
        get() = false

    override fun initView(savedInstanceState: Bundle?) {
        mBind.llContent.statusPadding()
        mBind.toolbar.initClose(topicName) {
            finish()
        }
        val isShowBtn = (App.globalConfig?.reportEntranceShow ?: 0) == 1
        if (isShowBtn) {
            addReportBtn()
        }
        selectedPosition = if (templateList.isEmpty()) {
            0
        } else {
            position.coerceIn(templateList.indices)
        }
        canLoadMore = hasNext
        mViewModel.initializeCurrentPage(currentPage)
        mBind.btnUse.text = FlowCopyStore.get(FlowCopyKey.TEMPLATE_ACTION)
        setupTemplateCarousel()
    }

    private fun readTemplateList(): List<TemplateItem> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayExtra(EXTRA_TEMPLATE_LIST, TemplateItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayExtra(EXTRA_TEMPLATE_LIST)
        }?.filterIsInstance<TemplateItem>() ?: emptyList()
    }

    private fun setupTemplateCarousel() {
        val rv = mBind.rvTemplate
        rv.clipChildren = false
        rv.setHasFixedSize(true)
        CoverFlowScrollListener.setupDrawingOrder(rv)
        carouselLayoutManager = SinglePageLinearLayoutManager(this)
        rv.layoutManager = carouselLayoutManager
        snapHelper.attachToRecyclerView(rv)
        rv.addOnScrollListener(coverFlowListener)
        rv.addOnScrollListener(scrollListener)
        rv.addOnItemTouchListener(carouselTouchListener)
        rv.setup {
            addType<TemplateItem>(R.layout.layout_item_topic_template_use)
            onBind {
                if (itemWidth > 0) {
                    val isLast = modelPosition == templateList.lastIndex
                    itemView.layoutParams = RecyclerView.LayoutParams(
                        itemWidth,
                        RecyclerView.LayoutParams.MATCH_PARENT,
                    ).apply {
                        marginEnd = if (isLast) 0 else -itemOverlap
                    }
                }
                bindTemplateUseItem(itemView, getModel())
                coverFlowListener.applyTransform(rv)
            }
        }

        mBind.llRv.post {
            val containerHeight = mBind.llRv.height
            val containerWidth = mBind.llRv.width
            if (containerHeight <= 0 || containerWidth <= 0 || templateList.isEmpty()) {
                return@post
            }
            itemWidth = (containerHeight * ASPECT_WIDTH / ASPECT_HEIGHT).toInt()
            itemOverlap = (itemWidth * ITEM_OVERLAP_RATIO).toInt()
            val itemStep = itemWidth - itemOverlap
            coverFlowListener.itemStep = itemStep.toFloat()
            carouselLayoutManager.pageDistancePx = itemStep
            val sidePadding = (containerWidth - itemWidth).coerceAtLeast(0) / 2
            rv.setPadding(sidePadding, 0, sidePadding, 0)
            rv.models = templateList
            previewPosition = selectedPosition
            updateTemplateName(templateList.getOrNull(selectedPosition))
            (rv.layoutManager as? LinearLayoutManager)
                ?.scrollToPositionWithOffset(selectedPosition, 0)
            coverFlowListener.applyTransform(rv)
            loadMoreIfNeeded(selectedPosition)
        }
    }

    private fun updatePreviewPosition(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager ?: return
        val centerView = snapHelper.findSnapView(layoutManager) ?: return
        val position = layoutManager.getPosition(centerView)
        if (position == RecyclerView.NO_POSITION) return

        if (position != previewPosition) {
            previewPosition = position
            updateTemplateName(templateList.getOrNull(position))
        }
        loadMoreIfNeeded(position)
    }

    private fun updateSelectedPosition(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager ?: return
        val snapView = snapHelper.findSnapView(layoutManager) ?: return
        val newPosition = layoutManager.getPosition(snapView)
        if (newPosition == RecyclerView.NO_POSITION) return
        if (newPosition != selectedPosition) {
            nextCarouselGestureTime =
                SystemClock.elapsedRealtime() + CAROUSEL_DEBOUNCE_DURATION_MS
        }
        selectedPosition = newPosition
        previewPosition = newPosition
        updateTemplateName(templateList.getOrNull(newPosition))
        loadMoreIfNeeded(newPosition)
    }

    private fun loadMoreIfNeeded(position: Int) {
        val preloadPosition = (templateList.lastIndex - 1).coerceAtLeast(0)
        if (!canLoadMore || isLoadingMore || position < preloadPosition) return

        isLoadingMore = true
        mViewModel.loadNextPage(topicId).obs(this) {
            onSuccess { page ->
                isLoadingMore = false
                canLoadMore = page.hasNext

                val knownIds = templateList.mapTo(mutableSetOf()) { it.id }
                val newItems = page.datas.filter { knownIds.add(it.id) }
                if (newItems.isNotEmpty()) {
                    val previousLastIndex = templateList.lastIndex
                    mBind.rvTemplate.bindingAdapter.addModels(newItems)
                    if (previousLastIndex >= 0) {
                        mBind.rvTemplate.bindingAdapter.notifyItemChanged(previousLastIndex)
                    }
                    appendedTemplates.addAll(newItems)
                }
                syncTopicTemplateList()
            }
            onError { error ->
                isLoadingMore = false
                error.msg.toast()
            }
        }
    }

    private fun syncTopicTemplateList() {
        EventViewModel.topicTemplateListSyncEvent.value = TopicTemplateListSyncEvent(
            topicId = topicId,
            appendedTemplates = appendedTemplates.toList(),
            currentPage = mViewModel.currentPage,
            hasNext = canLoadMore,
        )
    }

    private fun updateTemplateName(item: TemplateItem?) {
        mBind.tvName.text = item?.name.orEmpty()
    }

    private fun bindTemplateUseItem(itemView: View, model: TemplateItem) {
        itemView.findViewById<ImageView>(R.id.ivCover).loadImage(
            url = model.img,
            cornerRadiusDp = COVER_CORNER_RADIUS_DP,
        )
        val showCost = model.lockIntegral > 0
        val showConfig = (App.globalConfig?.templateAbduceIntegralShow ?: 0) in arrayOf(2, 3)
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

    override fun onBindViewClick() {
        mBind.btnUse.clickNoRepeat {
            currentTemplate?.let {
                openActivity<MaterialUploadActivity>(
                    MaterialUploadActivity.EXTRA_TEMPLATE_ITEM to it,
                    MaterialUploadActivity.EXTRA_SOURCE to MaterialUploadActivity.SOURCE_TOPIC,
                )
            }
        }
    }

    override fun createObserver() {

    }

    override fun onDestroy() {
        mBind.rvTemplate.removeOnItemTouchListener(carouselTouchListener)
        mBind.rvTemplate.removeOnScrollListener(coverFlowListener)
        mBind.rvTemplate.removeOnScrollListener(scrollListener)
        super.onDestroy()
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
        private const val ASPECT_WIDTH = 247f
        private const val ASPECT_HEIGHT = 358f
        private const val ITEM_OVERLAP_RATIO = 0.84f
        private const val CAROUSEL_DEBOUNCE_DURATION_MS = 250L
        private const val COVER_CORNER_RADIUS_DP = 15f
        private const val SAMPLE_CORNER_RADIUS_DP = 5f

        const val EXTRA_TOPIC_ID = "topic_id"
        const val EXTRA_TOPIC_NAME = "topic_name"
        const val EXTRA_TEMPLATE_LIST = "template_list"
        const val EXTRA_HAS_NEXT = "has_next"
        const val EXTRA_CURRENT_PAGE = "current_page"
        const val EXTRA_POSITION = "position"
    }
}

private class SinglePageLinearLayoutManager(
    context: Context,
) : LinearLayoutManager(context, RecyclerView.HORIZONTAL, false) {

    var pageDistancePx: Int = 0

    private var limitSinglePageScroll = false
    private var gestureScrollDistance = 0

    fun beginSinglePageScroll() {
        if (pageDistancePx <= 0) return
        gestureScrollDistance = 0
        limitSinglePageScroll = true
    }

    fun endSinglePageScroll() {
        limitSinglePageScroll = false
        gestureScrollDistance = 0
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
    ): Int {
        val allowedDx = if (limitSinglePageScroll && pageDistancePx > 0) {
            val targetDistance = (gestureScrollDistance + dx)
                .coerceIn(-pageDistancePx, pageDistancePx)
            targetDistance - gestureScrollDistance
        } else {
            dx
        }
        val consumed = super.scrollHorizontallyBy(allowedDx, recycler, state)
        if (limitSinglePageScroll) {
            gestureScrollDistance += consumed
        }
        return consumed
    }
}
