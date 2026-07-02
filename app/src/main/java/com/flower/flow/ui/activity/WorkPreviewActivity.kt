package com.flower.flow.ui.activity

import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.ext.applyCornerRadius
import com.flower.flow.app.core.ext.loadImageFitWidth
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.model.entity.WorkItem
import com.flower.flow.databinding.ActivityWorkPreviewBinding
import com.flower.flow.ui.dialog.CommonMessageDialog
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.intent.bundle

class WorkPreviewActivity : BaseActivity<BaseViewModel, ActivityWorkPreviewBinding>() {

    private val workItem: WorkItem by bundle(WorkItem(), EXTRA_WORK_ITEM)

    private var player: ExoPlayer? = null

    override val title: String
        get() = ""

    override fun initView(savedInstanceState: Bundle?) {
        if (workItem.taskId.isBlank()) {
            finish()
            return
        }

        val outputUrl = workItem.outputUrl
        if (outputUrl.isBlank()) {
            finish()
            return
        }

        setText()
        mBind.preview.applyCornerRadius(PREVIEW_CORNER_RADIUS_DP)
        mBind.previewVideo.applyCornerRadius(PREVIEW_CORNER_RADIUS_DP)
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
                .setTitle(FlowCopyStore.get(FlowCopyKey.DOWNLOAD_ACTION))
                .setContent(workItem.saveLocalPopupMsg.orEmpty())
                .setConfirmButton(FlowCopyStore.get(FlowCopyKey.DOWNLOAD_ACTION)) {

                }
                .setCancelButton(FlowCopyStore.get(FlowCopyKey.CANCEL_ACTION)) {

                }
                .show()
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

        mBind.previewImage.loadImageFitWidth(
            url = url,
            cornerRadiusDp = PREVIEW_CORNER_RADIUS_DP,
            onAspectRatio = ::updatePreviewAspectRatio,
        )
    }

    private fun setupVideoPreview(url: String) {
        mBind.previewImage.isVisible = false
        mBind.previewVideo.isVisible = true
        mBind.videoLoading.isVisible = true

        val exoPlayer = ExoPlayer.Builder(this).build().also { player = it }
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        exoPlayer.playWhenReady = true
        exoPlayer.setMediaItem(MediaItem.fromUri(url))
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

    private fun isVideoOutput(url: String): Boolean {
        if (workItem.aiartType == VIDEO_ART_TYPE) return true
        val lowerUrl = url.substringBefore('#').substringBefore('?').lowercase()
        return lowerUrl.endsWith(".mp4")
    }

    private fun setText() {
        mBind.btnDownload.text = FlowCopyStore.get(FlowCopyKey.DOWNLOAD_ACTION)
    }

    companion object {
        const val EXTRA_WORK_ITEM = "workItem"

        private const val PREVIEW_CORNER_RADIUS_DP = 15f
        private const val DEFAULT_ASPECT_WIDTH = 325
        private const val DEFAULT_ASPECT_HEIGHT = 488
        private const val VIDEO_ART_TYPE = 2
    }
}
