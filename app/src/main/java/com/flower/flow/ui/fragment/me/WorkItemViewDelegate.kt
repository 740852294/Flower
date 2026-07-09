package com.flower.flow.ui.fragment.me

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.flower.flow.R
import com.flower.flow.app.core.ext.clearImage
import com.flower.flow.app.core.ext.loadImage
import com.flower.flow.app.core.ext.setImageAnimationRunning
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.data.model.entity.WorkItem
import com.flower.flow.databinding.LayoutItemWorkBinding
import com.flower.flow.domain.profile.WorkDownloadJobManager
import com.flower.flow.domain.profile.WorkListSelectionController

object WorkItemViewDelegate {

    fun bindSelectionState(
        binding: LayoutItemWorkBinding,
        model: WorkItem,
        selection: WorkListSelectionController,
    ) {
        binding.ivSelect.isVisible = selection.isSelectionMode
        binding.ivSelect.setImageResource(
            if (selection.isSelected(model.baptismdictate)) {
                R.mipmap.ic_work_item_selected
            } else {
                R.mipmap.ic_work_item_unselect
            },
        )
        if (model.afflict == WorkStatusCodes.NONE) {
            binding.lockDot.visibility =
                if (selection.isSelectionMode) View.GONE else View.VISIBLE
        }
    }

    fun bindDownloadState(
        binding: LayoutItemWorkBinding,
        model: WorkItem,
        downloadState: WorkDownloadJobManager.DownloadUiState?,
    ) {
        binding.llProgress.isVisible = downloadState != null
        if (downloadState != null) {
            binding.tvDownload.text = model.attachaway.orEmpty()
            binding.pbDownload.isIndeterminate = downloadState.progress == null
            downloadState.progress?.let { binding.pbDownload.progress = it }
        } else {
            binding.pbDownload.isIndeterminate = false
            binding.pbDownload.progress = 0
        }
    }

    fun bindWorkItemContent(
        binding: LayoutItemWorkBinding,
        model: WorkItem,
        selection: WorkListSelectionController,
        downloadState: WorkDownloadJobManager.DownloadUiState?,
        onAnimationSync: (ImageView) -> Unit,
    ) {
        val isVip = UserManager.user?.shareengage ?: false
        bindSelectionState(binding, model, selection)
        binding.ivCover.setImageAnimationRunning(false)

        binding.btnStatus.isVisible = !model.increaserace.isNullOrBlank()
        binding.btnStatus.text = model.increaserace ?: ""

        when (model.afflict) {
            WorkStatusCodes.NONE -> bindLockedState(
                binding,
                model,
                isVip,
                selection,
                onAnimationSync
            )

            WorkStatusCodes.WAIT, WorkStatusCodes.PROCESSING ->
                bindProcessingState(binding, model, onAnimationSync)

            WorkStatusCodes.COMPLETE ->
                bindCompleteState(binding, model, onAnimationSync)

            WorkStatusCodes.FAIL ->
                bindFailedState(binding, model, onAnimationSync)
        }
        bindDownloadState(binding, model, downloadState)
    }

    private fun bindLockedState(
        binding: LayoutItemWorkBinding,
        model: WorkItem,
        isVip: Boolean,
        selection: WorkListSelectionController,
        onAnimationSync: (ImageView) -> Unit,
    ) {
        binding.lockDot.visibility = if (selection.isSelectionMode) View.GONE else View.VISIBLE
        binding.llTime.visibility = View.INVISIBLE
        binding.llProgress.visibility = View.GONE
        binding.ivDownload.visibility = View.INVISIBLE
        binding.ivCover.loadImage(
            url = model.clogcadre,
            isAutoPlay = false,
            resizeToViewport = true,
            onFinalImageSet = onAnimationSync,
        )
        binding.ivCover.applyBlurEffect(true)
        bindSampleImages(binding, model.aperitifaccost)
        binding.llStatus.visibility = View.VISIBLE
        binding.ivStatus.visibility = View.VISIBLE
        binding.ivStatus.setImageResource(R.mipmap.ic_work_lock)
        binding.tvStatus1.visibility = View.GONE
        binding.tvStatus2.visibility =
            if (isVip || model.acculturatecurd.isNullOrBlank()) View.GONE else View.VISIBLE
        binding.tvStatus2.text = model.acculturatecurd.orEmpty()
        binding.tvGenerating.visibility = View.GONE
    }

    private fun bindProcessingState(
        binding: LayoutItemWorkBinding,
        model: WorkItem,
        onAnimationSync: (ImageView) -> Unit,
    ) {
        binding.lockDot.visibility = View.GONE
        binding.llTime.visibility = View.INVISIBLE
        binding.llProgress.visibility = View.GONE
        binding.ivDownload.visibility = View.INVISIBLE
        val url = model.aperitifaccost?.firstOrNull() ?: ""
        if (url.isNotBlank()) {
            binding.ivCover.loadImage(
                url = url,
                isAutoPlay = false,
                resizeToViewport = true,
                onFinalImageSet = onAnimationSync,
            )
        }
        binding.ivCover.applyBlurEffect(true)
        binding.ivSampleSingle.isVisible = false
        binding.llSampleBottom.isVisible = false
        binding.llStatus.visibility = View.VISIBLE
        binding.ivStatus.visibility = View.GONE
        binding.tvStatus1.visibility =
            if (model.acculturatecurd.isNullOrBlank()) View.GONE else View.VISIBLE
        binding.tvStatus1.text = model.acculturatecurd.orEmpty()
        binding.tvStatus2.visibility = View.GONE
        binding.tvGenerating.visibility =
            if (model.notechildhood.isNullOrBlank()) View.GONE else View.VISIBLE
        binding.tvGenerating.text = model.notechildhood.orEmpty()
    }

    private fun bindCompleteState(
        binding: LayoutItemWorkBinding,
        model: WorkItem,
        onAnimationSync: (ImageView) -> Unit,
    ) {
        binding.lockDot.visibility = View.GONE
        binding.llTime.visibility =
            if (model.behavebanister == 2) View.VISIBLE else View.INVISIBLE
        binding.tvTime.text = model.aggregatechief.orEmpty()
        binding.llProgress.visibility = View.GONE
        binding.ivDownload.visibility = View.VISIBLE
        val url = model.aperitifaccost?.firstOrNull() ?: model.clogcadre ?: ""
        if (url.isNotBlank()) {
            binding.ivCover.loadImage(
                url = url,
                isAutoPlay = false,
                resizeToViewport = true,
                onFinalImageSet = onAnimationSync,
            )
        }
        binding.ivCover.applyBlurEffect(false)
        binding.ivSampleSingle.isVisible = false
        binding.llSampleBottom.isVisible = false
        binding.llStatus.visibility = View.GONE
    }

    private fun bindFailedState(
        binding: LayoutItemWorkBinding,
        model: WorkItem,
        onAnimationSync: (ImageView) -> Unit,
    ) {
        binding.lockDot.visibility = View.GONE
        binding.llTime.visibility = View.INVISIBLE
        binding.llProgress.visibility = View.GONE
        binding.ivDownload.visibility = View.INVISIBLE
        val url = model.aperitifaccost?.firstOrNull() ?: ""
        if (url.isNotBlank()) {
            binding.ivCover.loadImage(
                url = url,
                isAutoPlay = false,
                resizeToViewport = true,
                onFinalImageSet = onAnimationSync,
            )
        }
        binding.ivCover.applyBlurEffect(true)
        binding.ivSampleSingle.isVisible = false
        binding.llSampleBottom.isVisible = false
        binding.llStatus.visibility = View.VISIBLE
        binding.ivStatus.visibility = View.VISIBLE
        binding.ivStatus.setImageResource(R.mipmap.ic_work_failed)
        binding.tvStatus1.visibility = View.VISIBLE
        binding.tvStatus1.text = model.acculturatecurd.orEmpty()
        binding.tvStatus2.visibility = View.GONE
        binding.tvGenerating.visibility = View.GONE
    }

    fun bindSampleImages(
        binding: LayoutItemWorkBinding,
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
                    isAutoPlay = false,
                    resizeToViewport = true,
                )
            }

            else -> {
                binding.llSampleBottom.isVisible = true
                binding.ivSampleLeft.loadImage(
                    url = samples[0],
                    isAutoPlay = false,
                    resizeToViewport = true,
                )
                binding.ivSampleRight.loadImage(
                    url = samples.getOrNull(1),
                    isAutoPlay = false,
                    resizeToViewport = true,
                )
            }
        }
    }

    fun setItemAnimationsRunning(itemView: View, running: Boolean) {
        LayoutItemWorkBinding.bind(itemView).run {
            ivCover.setImageAnimationRunning(running)
            ivSampleSingle.setImageAnimationRunning(false)
            ivSampleLeft.setImageAnimationRunning(false)
            ivSampleRight.setImageAnimationRunning(false)
        }
    }

    fun recycleItem(itemView: View) {
        LayoutItemWorkBinding.bind(itemView).run {
            ivCover.setImageAnimationRunning(false)
            ivCover.applyBlurEffect(false)
            ivCover.clearImage()
            ivSampleSingle.clearImage()
            ivSampleLeft.clearImage()
            ivSampleRight.clearImage()
        }
    }
}
