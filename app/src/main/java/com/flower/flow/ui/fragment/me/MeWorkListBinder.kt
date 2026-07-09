package com.flower.flow.ui.fragment.me

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.isVisible
import com.drake.brv.BindingAdapter
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.dividerSpace
import com.drake.brv.utils.setup
import com.flower.flow.R
import com.flower.flow.app.core.ext.isVisibleOnScreen
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.data.model.StringResId
import com.flower.flow.data.model.entity.WorkItem
import com.flower.flow.databinding.FragmentMeBinding
import com.flower.flow.databinding.LayoutItemWorkBinding
import com.flower.flow.domain.profile.WorkDownloadJobManager
import com.flower.flow.domain.profile.WorkListSelectionController
import com.flower.flow.ui.fragment.MeFragment
import me.hgj.jetpackmvvm.ext.util.doDebouncedClick
import me.hgj.jetpackmvvm.ext.util.dp2px
import me.hgj.jetpackmvvm.ext.view.grid
import me.hgj.jetpackmvvm.util.BasePage

class MeWorkListBinder(
    private val binding: FragmentMeBinding,
    private val selectionController: WorkListSelectionController,
    private val downloadManager: WorkDownloadJobManager,
    private val callbacks: Callbacks,
) {

    interface Callbacks {
        fun isFragmentVisible(): Boolean
        fun onRequestAgainGenerate(workItem: WorkItem)
        fun syncAnimationPlayback(imageView: android.widget.ImageView)
        fun onOpenWorkPreview(workItem: WorkItem)
        fun onSelectionChanged()
    }

    fun setupRecyclerView() {
        binding.rvList.grid(MeFragment.SPAN_COUNT)
            .dividerSpace(dp2px(8f), DividerOrientation.GRID)
            .setup {
                addType<WorkItem>(R.layout.layout_item_work)

                onBind {
                    getBindingOrNull<LayoutItemWorkBinding>()?.run {
                        WorkItemViewDelegate.bindWorkItemContent(
                            binding = this,
                            model = getModel(),
                            selection = selectionController,
                            downloadState = downloadManager.stateOf(getModel<WorkItem>().baptismdictate),
                            onAnimationSync = callbacks::syncAnimationPlayback,
                        )
                    }
                }

                onPayload { payloads ->
                    getBindingOrNull<LayoutItemWorkBinding>()?.run {
                        val model = getModel<WorkItem>()
                        if (MeFragment.PAYLOAD_SELECTION in payloads) {
                            WorkItemViewDelegate.bindSelectionState(this, model, selectionController)
                        }
                        if (MeFragment.PAYLOAD_DOWNLOAD in payloads) {
                            WorkItemViewDelegate.bindDownloadState(
                                this,
                                model,
                                downloadManager.stateOf(model.baptismdictate),
                            )
                        }
                    }
                }

                onClick(R.id.rootItem) {
                    doDebouncedClick {
                        val model = getModel<WorkItem>()
                        if (selectionController.isSelectionMode) {
                            selectionController.toggleSelection(model.baptismdictate)
                            notifyItemChanged(modelPosition, MeFragment.PAYLOAD_SELECTION)
                            callbacks.onSelectionChanged()
                        } else if (
                            model.afflict == WorkStatusCodes.COMPLETE &&
                            !downloadManager.isActive(model.baptismdictate) &&
                            downloadManager.stateOf(model.baptismdictate) == null
                        ) {
                            callbacks.onOpenWorkPreview(model)
                        }
                    }
                }

                onClick(R.id.btnStatus) {
                    doDebouncedClick {
                        val model = getModel<WorkItem>()
                        if (!selectionController.isSelectionMode) {
                            callbacks.onRequestAgainGenerate(model)
                        }
                    }
                }
            }

        binding.rvList.addOnChildAttachStateChangeListener(
            object : RecyclerView.OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(view: View) {
                    WorkItemViewDelegate.setItemAnimationsRunning(
                        view,
                        callbacks.isFragmentVisible() && view.isVisibleOnScreen(),
                    )
                }

                override fun onChildViewDetachedFromWindow(view: View) {
                    WorkItemViewDelegate.setItemAnimationsRunning(view, false)
                }
            },
        )
    }

    fun bindWorkList(
        baseListNetEntity: BasePage<WorkItem>,
        bindingAdapter: BindingAdapter,
        isRefresh: Boolean? = null,
        onExitSelection: () -> Unit,
    ) {
        val refresh = isRefresh ?: baseListNetEntity.isRefresh()
        if (refresh) {
            onExitSelection()
            bindingAdapter.models = baseListNetEntity.getPageData()
            binding.refreshLayout.finishRefresh()
        } else {
            bindingAdapter.addModels(baseListNetEntity.getPageData())
        }
        if (baseListNetEntity.hasMore()) {
            binding.refreshLayout.finishLoadMore()
            binding.refreshLayout.setNoMoreData(false)
            binding.refreshLayout.setEnableLoadMore(true)
        } else {
            binding.refreshLayout.finishLoadMore()
            binding.refreshLayout.setEnableLoadMore(false)
        }

        binding.rlWorkAdd.isVisible =
            bindingAdapter.models == null || bindingAdapter.models?.isEmpty() == true
        binding.btnDelete.isVisible =
            bindingAdapter.models != null && bindingAdapter.models?.isNotEmpty() == true
        updateWorksMinHeight()
    }

    fun setupMinHeightObserver() {
        binding.nestedScrollView.viewTreeObserver.addOnGlobalLayoutListener {
            updateWorksMinHeight()
        }
    }

    fun updateWorksMinHeight() {
        val viewportHeight = binding.nestedScrollView.height
        if (viewportHeight <= 0) return

        val worksTop = binding.flWorks.top
        val minHeight = viewportHeight - worksTop
        if (minHeight > 0 && binding.flWorks.minimumHeight != minHeight) {
            binding.flWorks.minimumHeight = minHeight
        }
    }

    fun notifyDownloadStateChanged(taskId: String) {
        val adapter = binding.rvList.bindingAdapter
        val position = adapter.models?.indexOfFirst {
            (it as? WorkItem)?.baptismdictate == taskId
        } ?: -1
        if (position >= 0) {
            adapter.notifyItemChanged(position, MeFragment.PAYLOAD_DOWNLOAD)
        }
    }

    fun notifySelectionStateChanged() {
        val adapter = binding.rvList.bindingAdapter
        val modelCount = adapter.models?.size ?: 0
        if (modelCount > 0) {
            adapter.notifyItemRangeChanged(0, modelCount, MeFragment.PAYLOAD_SELECTION)
        }
    }

    fun updateDeleteButtonText() {
        val copyKey = if (selectionController.shouldShowCancelOnDeleteButton()) {
            StringResId.CANCEL_ACTION
        } else {
            StringResId.DELETE_ACTION
        }
        binding.btnDelete.text = AppStrings.get(copyKey)
    }
}
