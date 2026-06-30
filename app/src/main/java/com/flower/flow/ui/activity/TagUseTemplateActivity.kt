package com.flower.flow.ui.activity

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.ext.initClose
import com.flower.flow.app.core.ext.loadImage
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.widget.ActionButton
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.model.entity.TemplateItem
import com.flower.flow.databinding.ActivityTagUseTemplateBinding
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.intent.bundle
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.statusPadding

class TagUseTemplateActivity : BaseActivity<BaseViewModel, ActivityTagUseTemplateBinding>() {

    val templateItem: TemplateItem? by bundle<TemplateItem>(null, "TagTemple")

    override val showTitle: Boolean
        get() = false

    override fun initView(savedInstanceState: Bundle?) {

        mBind.llContent.statusPadding()
        mBind.toolbar.initClose("") {
            finish()
        }

        val isShowBtn = (App.globalConfig?.reportEntranceShow ?: 0) == 1
        if (isShowBtn) {
            addReportBtn()
        }
        setText()

        templateItem?.apply {
            mBind.tvName.text = this.name

            mBind.ivCover.loadImage(
                url = this.img,
                cornerRadiusDp = COVER_CORNER_RADIUS_DP,
                borderWidthDp = COVER_BORDER_WIDTH_DP,
            )

            bindLockBadge(mBind, this)
            bindSampleImages(mBind, this.sampleImgList)
        }
    }

    override fun onBindViewClick() {
        mBind.btnUse.clickNoRepeat {
            templateItem?.let {
                openActivity<MaterialUploadActivity>("TagTemple" to it)
            }
        }
    }

    override fun createObserver() {

    }

    fun addReportBtn() {
        mToolbar.menu.clear()
        mToolbar.setContentInsetEndWithActions(0)

        val item = mToolbar.menu.add("report").apply {
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

    private fun setText() {
        mBind.btnUse.text = FlowCopyStore.get(FlowCopyKey.TEMPLATE_ACTION)
    }

    private fun bindLockBadge(
        binding: ActivityTagUseTemplateBinding,
        model: TemplateItem,
    ) {
        val showCost = model.lockIntegral > 0 && model.lockType != 0
        val showConfig = (App.globalConfig?.templateAbduceIntegralShow ?: 0) in arrayOf(3, 4)
        val showLock = showCost && showConfig
        binding.llLockBadge.isVisible = showLock
        if (showLock) {
            binding.tvLockIntegral.text = model.lockIntegral.toString()
        }
    }

    private fun bindSampleImages(
        binding: ActivityTagUseTemplateBinding,
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
                    cornerRadiusDp = SAMPLE_CORNER_RADIUS_DP,
                )
            }

            else -> {
                binding.llSampleBottom.isVisible = true
                binding.ivSampleLeft.loadImage(
                    url = samples[0],
                    cornerRadiiDp = floatArrayOf(
                        SAMPLE_CORNER_RADIUS_DP,
                        0f,
                        0f,
                        SAMPLE_CORNER_RADIUS_DP,
                    ),
                )
                binding.ivSampleRight.loadImage(
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

    companion object {
        private const val COVER_CORNER_RADIUS_DP = 15f
        private const val COVER_BORDER_WIDTH_DP = 1f
        private const val SAMPLE_CORNER_RADIUS_DP = 5f
    }
}
