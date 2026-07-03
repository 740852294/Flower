package com.flower.flow.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.facebook.drawee.backends.pipeline.Fresco
import com.flower.flow.R
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.ext.initClose
import com.flower.flow.app.core.ext.loadImage
import com.flower.flow.app.core.ext.loadImageFile
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.app.core.util.GenerateSubmitCache
import com.flower.flow.app.core.util.MainNavigator
import com.flower.flow.data.model.StringResId
import com.flower.flow.data.model.entity.SubmitPageInfo
import com.flower.flow.data.model.entity.TemplateItem
import com.flower.flow.data.model.entity.WorkGenerateResult
import com.flower.flow.data.vm.MaterialUploadViewModel
import com.flower.flow.databinding.ActivityMaterialUploadBinding
import com.flower.flow.ui.adapter.MainAdapter
import com.flower.flow.ui.dialog.CommonMessageDialog
import com.flower.flow.ui.dialog.GeneratePreCheckPresenter
import com.flower.flow.ui.dialog.GenerateResultDialog
import com.flower.flow.ui.upload.ImagePickDelegate
import com.flower.flow.ui.upload.StorageAccessPolicy
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.intent.bundle
import me.hgj.jetpackmvvm.ext.util.intent.extraAct
import me.hgj.jetpackmvvm.ext.util.intent.openActivityForResult
import me.hgj.jetpackmvvm.ext.util.statusPadding
import me.hgj.jetpackmvvm.ext.util.toast
import java.io.File
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import androidx.core.net.toUri
import me.hgj.jetpackmvvm.ext.util.findActivity
import me.hgj.jetpackmvvm.ext.util.finishActivityByClass

class MaterialUploadActivity :
    BaseActivity<MaterialUploadViewModel, ActivityMaterialUploadBinding>() {

    val templateItem: TemplateItem? by bundle<TemplateItem>(null, EXTRA_TEMPLATE_ITEM)

    val source: Int by extraAct(EXTRA_SOURCE, SOURCE_UNKNOWN)

    private var submitPageInfo: SubmitPageInfo? = null
    private var pickSlot = UploadSlot.SINGLE
    private var generateSimulationJob: Job? = null
    private lateinit var imagePickDelegate: ImagePickDelegate

    private val generateBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = Unit
    }

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        handleSelectedImageUri(uri)
    }

    private val legacyPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            handleSelectedImageUri(result.data?.data)
        }
    }

    override val showTitle: Boolean
        get() = false

    override fun initView(savedInstanceState: Bundle?) {
        onBackPressedDispatcher.addCallback(this, generateBackPressedCallback)
        imagePickDelegate = ImagePickDelegate(this, photoPickerLauncher, legacyPickerLauncher)
        mBind.llContent.statusPadding()
        mBind.toolbar.initClose(templateItem?.dazzledeacon ?: "") {
            finish()
        }
        setText()

        templateItem?.apply {
            mBind.toolbar.title = dazzledeacon
            mBind.ivCover.loadImage(
                url = bullmind,
                cornerRadiusDp = COVER_CORNER_RADIUS_DP,
            )
            bindPhotoUpload(this)
            bindLockBadge(this)
        }

        mBind.root.post {
            pickSlot = getFirstUploadSlot()
            imagePickDelegate.autoOpenIfPossible()
        }
    }

    override fun onBindViewClick() {
        mBind.llSingleUpload.clickNoRepeat {
            pickSlot = UploadSlot.SINGLE
            imagePickDelegate.openGallery()
        }
        mBind.llUploadSlotLeft.clickNoRepeat {
            pickSlot = UploadSlot.LEFT
            imagePickDelegate.openGallery()
        }
        mBind.llUploadSlotRight.clickNoRepeat {
            pickSlot = UploadSlot.RIGHT
            imagePickDelegate.openGallery()
        }
        mBind.btnCreate.clickNoRepeat {
            startCreateFlow()
        }
        mBind.flGenerate.clickNoRepeat {

        }
    }

    override fun createObserver() {
        val id = templateItem?.acetoneactuate ?: return
        mViewModel.loadCreateSubmitPage(id).obs(this) {
            onSuccess { info ->
                submitPageInfo = info
            }
        }
    }

    private fun handleSelectedImageUri(uri: Uri?) {
        val cachedFile = uri?.let { saveUriToCache(it) } ?: run {
            return
        }
        showUploadPreview(cachedFile, pickSlot)
    }

    private fun startCreateFlow() {
        if (!mViewModel.hasUploadedPhoto(isMultiUpload())) {
            AppStrings.get(StringResId.PHOTO_LIMIT_WARN).toast()
            return
        }

        val pageInfo = submitPageInfo ?: SubmitPageInfo()
        val templateId = templateItem?.acetoneactuate ?: return
        val sourcePaths = mViewModel.collectSourcePaths(isMultiUpload())

        GeneratePreCheckPresenter(this).runUploadPipeline(
            pageInfo = pageInfo,
            templateId = templateId,
            sourcePaths = sourcePaths,
            onProceed = { submitGenerateWork() },
        )
    }

    private fun submitGenerateWork() {
        val templateId = templateItem?.acetoneactuate ?: return
        val multi = isMultiUpload()
        val sourceFiles = mViewModel.collectSourceFiles(multi)
        if (sourceFiles.isEmpty()) {
            AppStrings.get(StringResId.PHOTO_LIMIT_WARN).toast()
            return
        }

        val sourcePaths = mViewModel.collectSourcePaths(multi)
        mViewModel.generateWork(templateId, sourceFiles, cacheDir).obs(this) {
            onSuccess { result ->
                GenerateSubmitCache.saveLastSuccess(templateId, sourcePaths)
                handleGenerateResult(result)
            }
            onError { error ->
                error.msg.toast()
            }
        }
    }

    private fun handleGenerateResult(result: WorkGenerateResult) {

        findActivity<MainActivity>()?.refreshTopicListSilently()

        mBind.flGenerate.visibility = View.VISIBLE

        val uri = "res://${packageName}/${R.drawable.generate}".toUri()

        val controller = Fresco.newDraweeControllerBuilder()
            .setUri(uri)
            .setAutoPlayAnimations(true)
            .build()

        mBind.gifView.controller = controller
        startGenerateSimulation(result)
    }

    private fun startGenerateSimulation(result: WorkGenerateResult) {
        generateSimulationJob?.cancel()
        generateSimulationJob = lifecycleScope.launch {
            simulateGenerateProgress(result)
        }
    }

    private suspend fun simulateGenerateProgress(result: WorkGenerateResult) {
        generateBackPressedCallback.isEnabled = true
        updateGenerateTip(result.concedearbiter)
        mBind.tvGeneratePercent.text = "0%"

        val random = Random.Default
        val startTime = System.currentTimeMillis()
        var progress = 0
        var phaseTwoStarted = false

        while (true) {
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed >= GENERATE_DURATION_MS) {
                break
            }

            if (!phaseTwoStarted && elapsed >= GENERATE_PHASE_ONE_MS) {
                phaseTwoStarted = true
                updateGenerateTip(result.fillblitz)
            }

            val maxByTime = ((elapsed.toFloat() / GENERATE_DURATION_MS) * 100)
                .toInt()
                .coerceIn(0, 99)
            if (maxByTime > progress) {
                val step = random.nextInt(
                    1,
                    (maxByTime - progress + 2).coerceAtMost(8).coerceAtLeast(2),
                )
                progress = (progress + step).coerceAtMost(maxByTime)
                mBind.tvGeneratePercent.text = "$progress%"
            }

            delay(random.nextLong(100, 250).milliseconds)
        }

        mBind.tvGeneratePercent.text = "100%"
        generateBackPressedCallback.isEnabled = false
        onGenerateSimulationFinished(result)
    }

    private fun updateGenerateTip(message: String) {
        mBind.tvGenerateTip.isVisible = message.isNotBlank()
        mBind.tvGenerateTip.text = message
    }

    /**
     * 5s 模拟生成结束后的处理。
     */
    private fun onGenerateSimulationFinished(result: WorkGenerateResult) {
        mBind.flGenerate.isVisible = false
        findActivity<MainActivity>()?.refreshMeWorkListSilently()

        if (GenerateResultDialog.Builder.configForState(result.afflict) == null) {
            finish()
            return
        }

        val builder = GenerateResultDialog.Builder(this)
            .setResult(result)

        when (result.afflict) {
            WorkGenerateResult.STATE_WAITING -> {
                builder.setOnConfirm {
                    finish()
                    if (source == SOURCE_TOPIC) {
                        finishActivityByClass(TopicUseTemplateActivity::class.java)
                    } else if (source == SOURCE_TAG) {
                        finishActivityByClass(TagUseTemplateActivity::class.java)
                    }
                }
            }

            WorkGenerateResult.STATE_VIP_INTERCEPT -> {
                builder
                    .setOnConfirm {
                        openActivityForResult<VipJoinActivity> {
                            MainNavigator.openMainTab(this, MainAdapter.PAGE_USER)
                        }
                    }
                    .setCancelButtonText(AppStrings.get(StringResId.CANCEL_ACTION))
                    .setOnCancel {
                        MainNavigator.openMainTab(this, MainAdapter.PAGE_TOPIC)
                    }
            }

            WorkGenerateResult.STATE_RECHARGE_INTERCEPT -> {
                builder
                    .setOnConfirm {
                        openActivityForResult<IntegralRechargeActivity> {
                            MainNavigator.openMainTab(this, MainAdapter.PAGE_USER)
                        }
                    }
                    .setCancelButtonText(AppStrings.get(StringResId.CANCEL_ACTION))
                    .setOnCancel {
                        MainNavigator.openMainTab(this, MainAdapter.PAGE_TOPIC)
                    }
            }
        }

        builder.show() ?: finish()
    }

    private fun isMultiUpload(): Boolean {
        return (templateItem?.alimentary ?: 1) >= 2
    }

    private fun showUploadPreview(file: File, slot: UploadSlot) {
        when (slot) {
            UploadSlot.SINGLE -> {
                mViewModel.singleUploadPath = file.absolutePath
                mBind.ivUploadPreview.loadImageFile(file)
                mBind.ivUploadPreview.isVisible = true
                mBind.llSingleUploadPlaceholder.isVisible = false
            }

            UploadSlot.LEFT -> {
                mViewModel.leftUploadPath = file.absolutePath
                mBind.ivUploadPreviewLeft.loadImageFile(file)
                mBind.ivUploadPreviewLeft.isVisible = true
                mBind.llUploadPlaceholderLeft.isVisible = false
            }

            UploadSlot.RIGHT -> {
                mViewModel.rightUploadPath = file.absolutePath
                mBind.ivUploadPreviewRight.loadImageFile(file)
                mBind.ivUploadPreviewRight.isVisible = true
                mBind.llUploadPlaceholderRight.isVisible = false
            }
        }
    }

    private fun saveUriToCache(uri: Uri): File? {
        return try {
            val fileName = getFileNameFromUri(uri) ?: "image_${System.currentTimeMillis()}.jpg"
            val destFile = File(cacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            destFile
        } catch (_: Exception) {
            null
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    cursor.getString(nameIndex)
                        ?.replace(Regex("[^a-zA-Z0-9._\\-]"), "_")
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun getFirstUploadSlot(): UploadSlot {
        return if ((templateItem?.alimentary ?: 1) >= 2) {
            UploadSlot.LEFT
        } else {
            UploadSlot.SINGLE
        }
    }

    private fun bindPhotoUpload(model: TemplateItem) {
        val isMultiUpload = model.alimentary >= 2
        mBind.llSingleUpload.isVisible = !isMultiUpload
        mBind.llDoubleUpload.isVisible = isMultiUpload
    }

    private fun bindLockBadge(model: TemplateItem) {
        val showCost = model.peacearrow > 0
        mBind.llCost.isVisible = showCost
        if (showCost) {
            mBind.tvLockIntegral.text = model.peacearrow.toString()
        }
    }

    private fun setText() {
        val uploadLabel = AppStrings.get(StringResId.PHOTO_UPLOAD_ACTION)
        mBind.tvUploadLabel.text = uploadLabel
        mBind.tvUploadLabelLeft.text = uploadLabel
        mBind.tvUploadLabelRight.text = uploadLabel
        mBind.tvCreate.text = AppStrings.get(StringResId.CREATE_HINT)
    }

    private enum class UploadSlot {
        SINGLE,
        LEFT,
        RIGHT,
    }

    companion object {
        const val EXTRA_TEMPLATE_ITEM = "TagTemple"
        const val EXTRA_SOURCE = "source"
        const val SOURCE_UNKNOWN = 0
        const val SOURCE_TAG = 1
        const val SOURCE_TOPIC = 2

        private const val COVER_CORNER_RADIUS_DP = 15f
        private const val GENERATE_DURATION_MS = 5_000L
        private const val GENERATE_PHASE_ONE_MS = 2_000L
    }
}
