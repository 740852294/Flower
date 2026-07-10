package com.flower.flow.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.ext.loadGlideImageFitWidth
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.app.core.util.LegacyStoragePermission
import com.flower.flow.app.core.util.StoragePermissionUi
import com.flower.flow.app.core.util.VideoPreviewCache
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.model.StringResId
import com.flower.flow.data.model.entity.WorkItem
import com.flower.flow.databinding.ActivityWorkPreviewBinding
import com.flower.flow.ui.dialog.CommonMessageDialog
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.intent.bundle
import me.hgj.jetpackmvvm.ext.util.toast

class WorkPreviewActivity : BaseActivity<BaseViewModel, ActivityWorkPreviewBinding>() {

    private val workItem: WorkItem by bundle(WorkItem(), EXTRA_WORK_ITEM)

    private var player: ExoPlayer? = null
    private var pendingDownloadFromSettings = false
    private var downloadDispatched = false
    private var writePermissionRequested = false

    private val writePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            writePermissionRequested = false
            dispatchWorkDownload()
            return@registerForActivityResult
        }
        if (StoragePermissionUi.isPermanentDenial(
                this,
                LegacyStoragePermission.WRITE,
                writePermissionRequested,
            )
        ) {
            showStoragePermanentDenyDialog()
        }
    }

    override val title: String
        get() = ""

    override fun initView(savedInstanceState: Bundle?) {
        if (workItem.baptismdictate.isBlank()) {
            finish()
            return
        }

        val outputUrl = workItem.wantbirdcage
        if (outputUrl.isBlank()) {
            finish()
            return
        }

        setText()
        applyDefaultPreviewHeight()

        if (isVideoOutput(outputUrl)) {
            setupVideoPreview(outputUrl)
        } else {
            setupImagePreview(outputUrl)
        }
    }

    override fun onBindViewClick() {
        mBind.btnDownload.clickNoRepeat {
            CommonMessageDialog.Builder(this)
                .setContent(workItem.demand.orEmpty())
                .setConfirmButton(AppStrings.get(StringResId.CONFIRM_ACTION)) {
                    requestWorkDownload()
                }
                .setCancelButton(AppStrings.get(StringResId.CANCEL_ACTION)) {}
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!pendingDownloadFromSettings) return

        pendingDownloadFromSettings = false
        if (hasLegacyStoragePermission()) {
            dispatchWorkDownload()
        }
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onStart() {
        super.onStart()
        player?.play()
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }

    private fun setupImagePreview(url: String) {
        mBind.previewImage.isVisible = true
        mBind.previewVideo.isVisible = false
        mBind.videoLoading.isVisible = false

        mBind.previewImage.loadGlideImageFitWidth(
            url = url,
            onAspectRatio = ::updatePreviewAspectRatio,
        )
    }

    @SuppressLint("UnsafeOptInUsageError")
    @OptIn(UnstableApi::class)
    private fun setupVideoPreview(url: String) {
        mBind.previewImage.isVisible = false
        mBind.previewVideo.isVisible = true
        mBind.videoLoading.isVisible = true

        val exoPlayer = VideoPreviewCache.createPlayer(this).also { player = it }
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        exoPlayer.playWhenReady = true
        exoPlayer.setMediaItem(
            MediaItem.Builder()
                .setUri(url)
                .setCustomCacheKey(workItem.baptismdictate.ifBlank { url })
                .build()
        )
        exoPlayer.addListener(object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    updatePreviewAspectRatio(videoSize.width, videoSize.height)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> mBind.videoLoading.isVisible = true
                    Player.STATE_READY -> mBind.videoLoading.isVisible = false
                }
            }
        })
        exoPlayer.prepare()
        mBind.previewVideo.player = exoPlayer
    }

    private fun applyDefaultPreviewHeight() {
        updatePreviewAspectRatio(DEFAULT_ASPECT_WIDTH, DEFAULT_ASPECT_HEIGHT)
    }

    private fun updatePreviewAspectRatio(mediaWidth: Int, mediaHeight: Int) {
        if (mediaWidth <= 0 || mediaHeight <= 0) return

        fun applyHeight() {
            val viewWidth = mBind.preview.measuredWidth
            if (viewWidth <= 0) return
            val targetHeight = (viewWidth.toFloat() * mediaHeight / mediaWidth).toInt()
            if (mBind.preview.layoutParams.height != targetHeight) {
                mBind.preview.layoutParams = mBind.preview.layoutParams.apply {
                    height = targetHeight
                }
                mBind.preview.requestLayout()
            }
        }

        if (mBind.preview.measuredWidth > 0) {
            applyHeight()
            return
        }

        mBind.preview.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (mBind.preview.measuredWidth <= 0) return
                mBind.preview.viewTreeObserver.removeOnGlobalLayoutListener(this)
                applyHeight()
            }
        })
    }

    private fun releasePlayer() {
        mBind.previewVideo.player = null
        player?.release()
        player = null
    }

    private fun requestWorkDownload() {
        if (!LegacyStoragePermission.needsLegacyWritePermission() || hasLegacyStoragePermission()) {
            dispatchWorkDownload()
            return
        }

        writePermissionRequested = true
        writePermissionLauncher.launch(LegacyStoragePermission.WRITE)
    }

    private fun showStoragePermanentDenyDialog() {
        StoragePermissionUi.showPermanentDenyDialog(this) {
            pendingDownloadFromSettings = true
            LegacyStoragePermission.openAppSettings(this)
        }
    }

    private fun hasLegacyStoragePermission(): Boolean {
        if (!LegacyStoragePermission.needsLegacyWritePermission()) return true
        return LegacyStoragePermission.isWriteGranted(this)
    }

    private fun dispatchWorkDownload() {
        if (downloadDispatched) return
        downloadDispatched = true
        workItem.expelhotel?.toast()
        EventViewModel.workDownloadEvent.postValue(workItem)
        finish()
    }

    private fun isVideoOutput(url: String): Boolean {
        if (workItem.behavebanister == VIDEO_ART_TYPE) return true
        val lowerUrl = url.substringBefore('#').substringBefore('?').lowercase()
        return lowerUrl.endsWith(".mp4")
    }

    private fun setText() {
        mBind.btnDownload.text = AppStrings.get(StringResId.DOWNLOAD_ACTION)
    }

    companion object {
        const val EXTRA_WORK_ITEM = "workItem"
        private const val DEFAULT_ASPECT_WIDTH = 325
        private const val DEFAULT_ASPECT_HEIGHT = 488
        private const val VIDEO_ART_TYPE = 2
    }
}
