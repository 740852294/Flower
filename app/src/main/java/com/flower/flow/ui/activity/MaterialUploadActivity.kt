package com.flower.flow.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.ext.initClose
import com.flower.flow.app.core.ext.loadImage
import com.flower.flow.app.core.ext.loadImageFile
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.util.PhotoCompressUtil
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.model.entity.SubmitPageInfo
import com.flower.flow.data.model.entity.TemplateItem
import com.flower.flow.data.vm.MaterialUploadViewModel
import com.flower.flow.databinding.ActivityMaterialUploadBinding
import com.flower.flow.ui.dialog.CommonMessageDialog
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import kotlinx.coroutines.launch
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.intent.bundle
import me.hgj.jetpackmvvm.ext.util.statusPadding
import me.hgj.jetpackmvvm.ext.util.toast
import java.io.File

class MaterialUploadActivity :
    BaseActivity<MaterialUploadViewModel, ActivityMaterialUploadBinding>() {

    val templateItem: TemplateItem? by bundle<TemplateItem>(null, "TagTemple")

    private var submitPageInfo: SubmitPageInfo? = null
    private var pickSlot = UploadSlot.SINGLE

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
        mBind.llContent.statusPadding()
        mBind.toolbar.initClose(templateItem?.name ?: "") {
            finish()
        }
        setText()

        templateItem?.apply {
            mBind.toolbar.title = name
            mBind.ivCover.loadImage(
                url = img,
                cornerRadiusDp = COVER_CORNER_RADIUS_DP,
            )
            bindPhotoUpload(this)
            bindLockBadge(this)
        }

        mBind.root.post {
            pickSlot = getFirstUploadSlot()
            autoOpenSystemGallery()
        }
    }

    override fun onBindViewClick() {
        mBind.llSingleUpload.clickNoRepeat {
            pickSlot = UploadSlot.SINGLE
            openSystemGallery()
        }
        mBind.llUploadSlotLeft.clickNoRepeat {
            pickSlot = UploadSlot.LEFT
            openSystemGallery()
        }
        mBind.llUploadSlotRight.clickNoRepeat {
            pickSlot = UploadSlot.RIGHT
            openSystemGallery()
        }
    }

    override fun createObserver() {
        val id = templateItem?.id ?: return
        mViewModel.getCreateSubmitPage(id).obs(this) {
            onSuccess { info ->
                submitPageInfo = info
            }
        }
    }

    private fun autoOpenSystemGallery() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                openSystemGallery()
            }

            hasStoragePermission() -> {
                openLegacyPicker()
            }
        }
    }

    private fun openSystemGallery() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                openLegacyPicker()
            }

            else -> {
                requestStoragePermissionAndOpen()
            }
        }
    }

    private fun requestStoragePermissionAndOpen() {
        XXPermissions.with(this)
            .permission(PermissionLists.getReadExternalStoragePermission())
            .request { _, deniedList ->
                if (deniedList.isEmpty()) {
                    openLegacyPicker()
                } else if (XXPermissions.isDoNotAskAgainPermissions(this, deniedList)) {
                    showStoragePermanentDenyDialog()
                }
            }
    }

    private fun showStoragePermanentDenyDialog() {
        CommonMessageDialog.Builder(this)
            .setTitle(FlowCopyStore.get(FlowCopyKey.PERM_STORE_HEAD))
            .setContent(FlowCopyStore.get(FlowCopyKey.STORAGE_DESC))
            .setConfirmButton(FlowCopyStore.get(FlowCopyKey.CONFIRM_ACTION)) {
                XXPermissions.startPermissionActivity(this)
            }
            .setCancelButton(FlowCopyStore.get(FlowCopyKey.CANCEL_ACTION))
            .show()
    }

    private fun openLegacyPicker() {
        var intent = Intent(Intent.ACTION_PICK).apply {
            setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (intent.resolveActivity(packageManager) == null) {
            intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        if (intent.resolveActivity(packageManager) != null) {
            legacyPickerLauncher.launch(intent)
        }
    }

    private fun handleSelectedImageUri(uri: Uri?) {
        val cachedFile = uri?.let { saveUriToCache(it) } ?: run {
            return
        }

        lifecycleScope.launch {
            val compressedFiles = PhotoCompressUtil.prepareUploadFiles(
                listOf(cachedFile),
                cacheDir,
            )
            val compressedFile = compressedFiles.firstOrNull() ?: run {
                FlowCopyStore.get(FlowCopyKey.PHOTO_UPLOAD_FAIL).toast()
                return@launch
            }
            showCompressedPreview(compressedFile, pickSlot)
        }
    }

    private fun showCompressedPreview(file: File, slot: UploadSlot) {
        when (slot) {
            UploadSlot.SINGLE -> {
                mViewModel.singleUploadFilePath = file.absolutePath
                mBind.ivUploadPreview.loadImageFile(file)
                mBind.ivUploadPreview.isVisible = true
                mBind.llSingleUploadPlaceholder.isVisible = false
            }

            UploadSlot.LEFT -> {
                mViewModel.leftUploadFilePath = file.absolutePath
                mBind.ivUploadPreviewLeft.loadImageFile(file)
                mBind.ivUploadPreviewLeft.isVisible = true
                mBind.llUploadPlaceholderLeft.isVisible = false
            }

            UploadSlot.RIGHT -> {
                mViewModel.rightUploadFilePath = file.absolutePath
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

    private fun hasStoragePermission(): Boolean {
        return XXPermissions.isGrantedPermissions(
            this,
            listOf(PermissionLists.getReadExternalStoragePermission()),
        )
    }

    private fun getFirstUploadSlot(): UploadSlot {
        return if ((templateItem?.uploadNum ?: 1) >= 2) {
            UploadSlot.LEFT
        } else {
            UploadSlot.SINGLE
        }
    }

    private fun bindPhotoUpload(model: TemplateItem) {
        val isMultiUpload = model.uploadNum >= 2
        mBind.llSingleUpload.isVisible = !isMultiUpload
        mBind.llDoubleUpload.isVisible = isMultiUpload
    }

    private fun bindLockBadge(model: TemplateItem) {
        val showCost = model.lockIntegral > 0 && model.lockType != 0
        mBind.llCost.isVisible = showCost
        if (showCost) {
            mBind.tvLockIntegral.text = model.lockIntegral.toString()
        }
    }

    private fun setText() {
        val uploadLabel = FlowCopyStore.get(FlowCopyKey.PHOTO_UPLOAD_ACTION)
        mBind.tvUploadLabel.text = uploadLabel
        mBind.tvUploadLabelLeft.text = uploadLabel
        mBind.tvUploadLabelRight.text = uploadLabel
        mBind.tvCreate.text = FlowCopyStore.get(FlowCopyKey.CREATE_HINT)
    }

    private enum class UploadSlot {
        SINGLE,
        LEFT,
        RIGHT,
    }

    companion object {
        private const val COVER_CORNER_RADIUS_DP = 15f
    }
}
