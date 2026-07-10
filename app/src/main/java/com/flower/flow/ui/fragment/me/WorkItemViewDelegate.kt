package com.flower.flow.ui.fragment.me

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.flower.flow.R
import com.flower.flow.app.core.ext.clearGlideImage
import com.flower.flow.app.core.ext.loadGlideImage
import com.flower.flow.app.core.ext.setGlideAnimationRunning
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.app.core.widget.DrawableBlurImageView
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

    fun bindWorkItemUiContent(
        binding: LayoutItemWorkBinding,
        model: WorkItem,
        selection: WorkListSelectionController,
        downloadState: WorkDownloadJobManager.DownloadUiState?,
    ) {
        val isVip = UserManager.user?.shareengage ?: false
        bindSelectionState(binding, model, selection)

        binding.btnStatus.isVisible = !model.increaserace.isNullOrBlank()
        binding.btnStatus.text = model.increaserace ?: ""

        when (model.afflict) {
            WorkStatusCodes.NONE -> bindLockedUi(binding, model, isVip, selection)
            WorkStatusCodes.WAIT, WorkStatusCodes.PROCESSING -> bindProcessingUi(binding, model)
            WorkStatusCodes.COMPLETE -> bindCompleteUi(binding, model)
            WorkStatusCodes.FAIL -> bindFailedUi(binding, model)
        }
        bindDownloadState(binding, model, downloadState)
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
        binding.ivCover.setGlideAnimationRunning(false)

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
        binding.ivCover.loadGlideImageWithBlur(
            url = model.clogcadre,
            onAnimationSync = onAnimationSync,
        )
        bindSampleImages(binding, model.aperitifaccost, onAnimationSync)
        bindLockedUi(binding, model, isVip, selection)
    }

    private fun bindLockedUi(
        binding: LayoutItemWorkBinding,
        model: WorkItem,
        isVip: Boolean,
        selection: WorkListSelectionController,
    ) {
        binding.llStatus.visibility = View.VISIBLE
        binding.lockDot.visibility = if (selection.isSelectionMode) View.GONE else View.VISIBLE
        binding.llTime.visibility = View.INVISIBLE
        binding.llProgress.visibility = View.GONE
        binding.ivDownload.visibility = View.INVISIBLE
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
            binding.ivCover.loadGlideImageWithBlur(
                url = url,
                onAnimationSync = onAnimationSync,
            )
        } else {
            binding.ivCover.applyBlurEffect(false)
            binding.ivCover.clearGlideImage()
        }
        binding.ivSampleSingle.isVisible = false
        binding.llSampleBottom.isVisible = false
        binding.ivSampleSingle.clearGlideImage()
        binding.ivSampleLeft.clearGlideImage()
        binding.ivSampleRight.clearGlideImage()
        bindProcessingUi(binding, model)
    }

    private fun bindProcessingUi(
        binding: LayoutItemWorkBinding,
        model: WorkItem,
    ) {
        binding.lockDot.visibility = View.GONE
        binding.llTime.visibility = View.INVISIBLE
        binding.llProgress.visibility = View.GONE
        binding.ivDownload.visibility = View.INVISIBLE
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
            binding.ivCover.loadGlideImage(
                url = url,
                onResourceReady = onAnimationSync,
            )
        } else {
            binding.ivCover.clearGlideImage()
        }
        binding.ivCover.applyBlurEffect(false)
        binding.ivSampleSingle.isVisible = false
        binding.llSampleBottom.isVisible = false
        binding.ivSampleSingle.clearGlideImage()
        binding.ivSampleLeft.clearGlideImage()
        binding.ivSampleRight.clearGlideImage()
        bindCompleteUi(binding, model)
    }

    private fun bindCompleteUi(
        binding: LayoutItemWorkBinding,
        model: WorkItem,
    ) {
        binding.lockDot.visibility = View.GONE
        binding.llTime.visibility =
            if (model.behavebanister == 2) View.VISIBLE else View.INVISIBLE
        binding.tvTime.text = model.aggregatechief.orEmpty()
        binding.llProgress.visibility = View.GONE
        binding.ivDownload.visibility = View.VISIBLE
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
            binding.ivCover.loadGlideImageWithBlur(
                url = url,
                onAnimationSync = onAnimationSync,
            )
        } else {
            binding.ivCover.applyBlurEffect(false)
            binding.ivCover.clearGlideImage()
        }
        binding.ivSampleSingle.isVisible = false
        binding.llSampleBottom.isVisible = false
        binding.ivSampleSingle.clearGlideImage()
        binding.ivSampleLeft.clearGlideImage()
        binding.ivSampleRight.clearGlideImage()
        bindFailedUi(binding, model)
    }

    private fun bindFailedUi(
        binding: LayoutItemWorkBinding,
        model: WorkItem,
    ) {
        binding.lockDot.visibility = View.GONE
        binding.llTime.visibility = View.INVISIBLE
        binding.llProgress.visibility = View.GONE
        binding.ivDownload.visibility = View.INVISIBLE
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
        onAnimationSync: (ImageView) -> Unit,
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
                binding.ivSampleSingle.loadGlideImage(
                    url = samples.first(),
                    onResourceReady = onAnimationSync,
                )
                binding.ivSampleLeft.clearGlideImage()
                binding.ivSampleRight.clearGlideImage()
            }

            else -> {
                binding.llSampleBottom.isVisible = true
                binding.ivSampleSingle.clearGlideImage()
                binding.ivSampleLeft.loadGlideImage(
                    url = samples[0],
                    onResourceReady = onAnimationSync,
                )
                binding.ivSampleRight.loadGlideImage(
                    url = samples.getOrNull(1),
                    onResourceReady = onAnimationSync,
                )
            }
        }
    }

    private fun DrawableBlurImageView.loadGlideImageWithBlur(
        url: String?,
        onAnimationSync: (ImageView) -> Unit,
    ) {
        val currentUrl = getTag(R.id.tag_image_url) as? String
        if (url.isNullOrBlank() || currentUrl != url) {
            applyBlurEffect(false)
        }
        loadGlideImage(
            url = url,
            onResourceReady = { imageView ->
                applyBlurEffect(true)
                onAnimationSync(imageView)
            },
        )
    }

    fun setItemAnimationsRunning(itemView: View, running: Boolean) {
        LayoutItemWorkBinding.bind(itemView).run {
            ivCover.setGlideAnimationRunning(running)
            ivSampleSingle.setGlideAnimationRunning(running)
            ivSampleLeft.setGlideAnimationRunning(running)
            ivSampleRight.setGlideAnimationRunning(running)
        }
    }

    fun recycleItem(itemView: View) {
        LayoutItemWorkBinding.bind(itemView).run {
            ivCover.applyBlurEffect(false)
            ivCover.clearGlideImage()
            ivSampleSingle.clearGlideImage()
            ivSampleLeft.clearGlideImage()
            ivSampleRight.clearGlideImage()
        }
    }
}
