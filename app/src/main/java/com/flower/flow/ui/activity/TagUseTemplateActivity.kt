package com.flower.flow.ui.activity

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isVisible
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.ext.clearGlideImage
import com.flower.flow.app.core.ext.initClose
import com.flower.flow.app.core.ext.isVisibleOnScreen
import com.flower.flow.app.core.ext.loadGlideImage
import com.flower.flow.app.core.ext.setGlideAnimationRunning
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.app.core.widget.ActionButton
import com.flower.flow.data.model.StringResId
import com.flower.flow.data.model.entity.TemplateItem
import com.flower.flow.databinding.ActivityTagUseTemplateBinding
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.intent.bundle
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.statusPadding

class TagUseTemplateActivity : BaseActivity<BaseViewModel, ActivityTagUseTemplateBinding>() {

    val templateItem: TemplateItem? by bundle<TemplateItem>(
        null,
        MaterialUploadActivity.EXTRA_TEMPLATE_ITEM,
    )

    private var isResumed = false

    override val showTitle: Boolean
        get() = false

    override fun initView(savedInstanceState: Bundle?) {

        mBind.llContent.statusPadding()
        mBind.toolbar.initClose("") {
            finish()
        }

        val isShowBtn = (App.globalConfig?.athleteacanthus ?: 0) == 1
        if (isShowBtn) {
            addReportBtn()
        }
        setText()

        templateItem?.apply {
            mBind.tvName.text = this.dazzledeacon

            mBind.ivCover.loadGlideImage(
                url = this.bullmind,
                onResourceReady = ::syncAnimationPlayback,
            )

            bindLockBadge(mBind, this)
            bindSampleImages(mBind, this.neverchapter)
        }
    }

    override fun onBindViewClick() {
        mBind.btnUse.clickNoRepeat {
            templateItem?.let {
                openActivity<MaterialUploadActivity>(
                    MaterialUploadActivity.EXTRA_TEMPLATE_ITEM to it,
                    MaterialUploadActivity.EXTRA_SOURCE to MaterialUploadActivity.SOURCE_TAG,
                )
            }
        }
    }

    override fun createObserver() {

    }

    override fun onResume() {
        super.onResume()
        isResumed = true
        setTemplateImagesAnimationRunning(true)
    }

    override fun onPause() {
        isResumed = false
        setTemplateImagesAnimationRunning(false)
        super.onPause()
    }

    fun addReportBtn() {
        mBind.toolbar.menu.clear()
        mBind.toolbar.setContentInsetEndWithActions(0)

        val item = mBind.toolbar.menu.add("report").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        val reportButton = ActionButton(this).apply {
            text = AppStrings.get(StringResId.REPORT_ACTION)
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

    private fun setText() {
        mBind.btnUse.text = AppStrings.get(StringResId.TEMPLATE_ACTION)
    }

    private fun bindLockBadge(
        binding: ActivityTagUseTemplateBinding,
        model: TemplateItem,
    ) {
        val showCost = model.peacearrow > 0
        val showConfig = (App.globalConfig?.disposenovel ?: 0) in arrayOf(3, 4)
        val showLock = showCost && showConfig
        binding.llLockBadge.isVisible = showLock
        if (showLock) {
            binding.tvLockIntegral.text = model.peacearrow.toString()
        }
    }

    private fun bindSampleImages(
        binding: ActivityTagUseTemplateBinding,
        samples: List<String>?,
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
                    onResourceReady = ::syncAnimationPlayback,
                )
                binding.ivSampleLeft.clearGlideImage()
                binding.ivSampleRight.clearGlideImage()
            }

            else -> {
                binding.llSampleBottom.isVisible = true
                binding.ivSampleSingle.clearGlideImage()
                binding.ivSampleLeft.loadGlideImage(
                    url = samples[0],
                    onResourceReady = ::syncAnimationPlayback,
                )
                binding.ivSampleRight.loadGlideImage(
                    url = samples.getOrNull(1),
                    onResourceReady = ::syncAnimationPlayback,
                )
            }
        }
    }

    private fun syncAnimationPlayback(imageView: ImageView) {
        imageView.setGlideAnimationRunning(
            isResumed && imageView.isVisibleOnScreen(),
        )
    }

    private fun setTemplateImagesAnimationRunning(running: Boolean) {
        val imageViews = arrayOf(
            mBind.ivCover,
            mBind.ivSampleSingle,
            mBind.ivSampleLeft,
            mBind.ivSampleRight,
        )
        imageViews.forEach { imageView ->
            imageView.setGlideAnimationRunning(running && imageView.isVisibleOnScreen())
        }
    }
}
