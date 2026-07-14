package com.flower.flow.ui.fragment.me

import androidx.core.view.isVisible
import com.drake.brv.BindingAdapter
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.listener.ItemDifferCallback
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.dividerSpace
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
        val adapter = object : BindingAdapter() {
            override fun onViewRecycled(holder: BindingViewHolder) {
                super.onViewRecycled(holder)
                WorkItemViewDelegate.recycleItem(holder.itemView)
            }

            override fun onViewAttachedToWindow(holder: BindingViewHolder) {
                super.onViewAttachedToWindow(holder)
                WorkItemViewDelegate.setItemAnimationsRunning(
                    holder.itemView,
                    callbacks.isFragmentVisible() && holder.itemView.isVisibleOnScreen(),
                )
            }

            override fun onViewDetachedFromWindow(holder: BindingViewHolder) {
                super.onViewDetachedFromWindow(holder)
                WorkItemViewDelegate.setItemAnimationsRunning(holder.itemView, false)
            }
        }

        binding.rvList.grid(MeFragment.SPAN_COUNT)
            .dividerSpace(dp2px(8f), DividerOrientation.GRID)
        adapter.apply {
            itemDifferCallback = workItemDifferCallback
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
                    val model = currentWorkItem() ?: return@run
                    if (PAYLOAD_WORK_CONTENT in payloads) {
                        WorkItemViewDelegate.bindWorkItemUiContent(
                            binding = this,
                            model = model,
                            selection = selectionController,
                            downloadState = downloadManager.stateOf(model.baptismdictate),
                        )
                    }
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
                    val model = currentWorkItem() ?: return@doDebouncedClick
                    if (selectionController.isSelectionMode) {
                        selectionController.toggleSelection(model.baptismdictate)
                        notifyItemChanged(modelPosition, MeFragment.PAYLOAD_SELECTION)
                        callbacks.onSelectionChanged()
                        return@doDebouncedClick
                    }
                    val downloading = downloadManager.isActive(model.baptismdictate) ||
                        downloadManager.stateOf(model.baptismdictate) != null
                    if (downloading) return@doDebouncedClick
                    when (model.afflict) {
                        WorkStatusCodes.COMPLETE -> callbacks.onOpenWorkPreview(model)
                        WorkStatusCodes.NONE, WorkStatusCodes.FAIL ->
                            callbacks.onRequestAgainGenerate(model)
                    }
                }
            }
        }
        binding.rvList.adapter = adapter
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
            bindingAdapter.setDifferModels(baseListNetEntity.getPageData(), detectMoves = false)
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

    private fun BindingAdapter.BindingViewHolder.currentWorkItem(): WorkItem? {
        return adapter.models?.getOrNull(modelPosition) as? WorkItem
    }

    private val workItemDifferCallback = object : ItemDifferCallback {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            val oldWorkItem = oldItem as? WorkItem ?: return false
            val newWorkItem = newItem as? WorkItem ?: return false
            return oldWorkItem.baptismdictate == newWorkItem.baptismdictate
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Any, newItem: Any): Any? {
            val oldWorkItem = oldItem as? WorkItem ?: return null
            val newWorkItem = newItem as? WorkItem ?: return null
            return if (oldWorkItem.canUpdateWithoutReloadingImages(newWorkItem)) {
                PAYLOAD_WORK_CONTENT
            } else {
                null
            }
        }
    }

    private fun WorkItem.canUpdateWithoutReloadingImages(newItem: WorkItem): Boolean {
        return afflict == newItem.afflict && imageKeys() == newItem.imageKeys()
    }

    private fun WorkItem.imageKeys(): List<String?> {
        return when (afflict) {
            WorkStatusCodes.NONE -> listOf(clogcadre) + aperitifaccost.orEmpty()
            WorkStatusCodes.WAIT, WorkStatusCodes.PROCESSING ->
                listOf(aperitifaccost?.firstOrNull())
            WorkStatusCodes.COMPLETE ->
                listOf(aperitifaccost?.firstOrNull() ?: clogcadre)
            WorkStatusCodes.FAIL ->
                listOf(aperitifaccost?.firstOrNull())
            else -> emptyList()
        }
    }

    private companion object {
        const val PAYLOAD_WORK_CONTENT = "payload_work_content"
    }
}
