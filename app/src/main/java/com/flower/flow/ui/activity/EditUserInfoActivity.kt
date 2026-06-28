package com.flower.flow.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.ext.loadAvatarFile
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.util.PhotoCompressUtil
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.app.core.widget.ActionButton
import com.flower.flow.data.model.CacheConfig
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.vm.EditUserInfoViewModel
import com.flower.flow.databinding.ActivityEditUserInfoBinding
import com.flower.flow.ui.dialog.CommonMessageDialog
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import me.hgj.jetpackmvvm.core.data.obs
import kotlinx.coroutines.launch
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.copyToClipboard
import me.hgj.jetpackmvvm.ext.util.toast
import java.io.File

class EditUserInfoActivity : BaseActivity<EditUserInfoViewModel, ActivityEditUserInfoBinding>() {

    override val title: String
        get() = FlowCopyStore.get(FlowCopyKey.PROFILE_EDIT)

    private lateinit var saveButton: ActionButton
    private var originalNickname: String = ""

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

    override fun initView(savedInstanceState: Bundle?) {
        addSaveBtn()
        setText()
        mBind.etName.doAfterTextChanged {
            updateSaveButtonState()
        }
    }

    fun addSaveBtn() {
        mToolbar.menu.clear()
        mToolbar.setContentInsetEndWithActions(0)

        val item = mToolbar.menu.add("save").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        saveButton = ActionButton(this).apply {
            text = FlowCopyStore.get(FlowCopyKey.SAVE_ACTION)
            isEnabled = false
            setOnClickListener {
                saveUserInfo()
            }
        }

        item.actionView = FrameLayout(this).apply {
            setPadding(0, 0, dp(15), 0)

            addView(
                saveButton,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER,
                ),
            )
        }
    }

    private fun setText() {
        mBind.nameLabel.text = FlowCopyStore.get(FlowCopyKey.NICKNAME_LABEL)
        mBind.accountLabel.text = FlowCopyStore.get(FlowCopyKey.ACCOUNT_LABEL)
    }

    override fun createObserver() {
        UserManager.user?.apply {
            val avatar = this.avatar
            if (avatar.isNotEmpty()) {
                mBind.ivAvatar.loadAvatarFile(avatar)
            }

            val name = this.name
            if (name.isNotEmpty()) {
                mBind.etName.setText(name)
            }
            originalNickname = mBind.etName.text?.toString()?.trim().orEmpty()

            mBind.tvAccount.text = CacheConfig.userId
            updateSaveButtonState()
        }
    }

    private fun updateSaveButtonState() {
        if (!::saveButton.isInitialized) return
        val nickname = mBind.etName.text?.toString()?.trim().orEmpty()
        val hasChanges = nickname != originalNickname || mViewModel.hasPendingAvatar()
        saveButton.isEnabled = hasChanges
    }

    private fun saveUserInfo() {
        val nickname = mBind.etName.text?.toString()?.trim().orEmpty()
        if (nickname.isBlank()) {
            FlowCopyStore.get(FlowCopyKey.PLEASE_ENTER_TEXT).toast()
            return
        }

        val avatarPath = mViewModel.pendingAvatarPath.takeIf { mViewModel.hasPendingAvatar() }
        mViewModel.updateUserInfo(nickname, avatarPath).obs(this) {
            onSuccess {
                setResult(RESULT_OK)
                finish()
            }
            onError { error ->
                error.toast()
            }
        }
    }

    override fun onBindViewClick() {
        mBind.copyButton.clickNoRepeat {
            val uid = mBind.tvAccount.text?.toString().orEmpty()
            if (uid.isNotBlank()) {
                copyToClipboard(uid, FlowCopyStore.get(FlowCopyKey.ACCOUNT_LABEL))
                FlowCopyStore.get(FlowCopyKey.COPY_ACTION).toast()
            }
        }

        mBind.ivAvatar.clickNoRepeat {
            openSystemGallery()
        }

        mBind.ivModify.clickNoRepeat {
            openSystemGallery()
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
            FlowCopyStore.get(FlowCopyKey.PHOTO_UPLOAD_FAIL).toast()
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

            mBind.ivAvatar.loadAvatarFile(compressedFile)
            mViewModel.setPendingAvatarPath(compressedFile.absolutePath)
            updateSaveButtonState()
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
}
