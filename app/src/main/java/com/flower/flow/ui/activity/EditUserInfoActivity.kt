package com.flower.flow.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.ext.loadGlideAvatar
import com.flower.flow.app.core.util.AesTextCodec
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.app.core.util.PhotoCompressUtil
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.app.core.widget.ActionButton
import com.flower.flow.data.model.CacheConfig
import com.flower.flow.data.model.StringResId
import com.flower.flow.data.vm.EditUserInfoViewModel
import com.flower.flow.databinding.ActivityEditUserInfoBinding
import com.flower.flow.ui.upload.ImagePickDelegate
import kotlinx.coroutines.launch
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.copyToClipboard
import me.hgj.jetpackmvvm.ext.util.toast
import java.io.File

class EditUserInfoActivity : BaseActivity<EditUserInfoViewModel, ActivityEditUserInfoBinding>() {

    override val title: String
        get() = AppStrings.get(StringResId.PROFILE_EDIT)

    private lateinit var saveButton: ActionButton
    private lateinit var imagePickDelegate: ImagePickDelegate
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
        imagePickDelegate = ImagePickDelegate(this, photoPickerLauncher, legacyPickerLauncher)
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
            text = AppStrings.get(StringResId.SAVE_ACTION)
            isEnabled = false
            clickNoRepeat {
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
        mBind.nameLabel.text = AppStrings.get(StringResId.NICKNAME_LABEL)
        mBind.accountLabel.text = AppStrings.get(StringResId.ACCOUNT_LABEL)
    }

    override fun createObserver() {
        UserManager.user?.apply {
            val avatar = this.excludephone
            if (avatar.isNotEmpty()) {
                mBind.ivAvatar.loadGlideAvatar(avatar)
            }

            val name = this.dazzledeacon
            if (name.isNotEmpty()) {
                mBind.etName.setText(name)
            }
            originalNickname = mBind.etName.text?.toString()?.trim().orEmpty()

            mBind.tvAccount.text = AesTextCodec.decode(CacheConfig.userId)
            updateSaveButtonState()
        }
    }

    private fun updateSaveButtonState() {
        if (!::saveButton.isInitialized) return
        val nickname = mBind.etName.text?.toString()?.trim().orEmpty()
        val hasChanges =
            nickname.isNotEmpty() && (nickname != originalNickname || mViewModel.hasPendingAvatar())
        saveButton.isEnabled = hasChanges
    }

    private fun saveUserInfo() {
        val nickname = mBind.etName.text?.toString()?.trim().orEmpty()
        if (nickname.isBlank()) {
            AppStrings.get(StringResId.PLEASE_ENTER_TEXT).toast()
            return
        }

        val avatarPath = mViewModel.pendingAvatarPath.takeIf { mViewModel.hasPendingAvatar() }
        mViewModel.updateUserInfo(nickname, avatarPath).obs(this) {
            onSuccess {
                setResult(RESULT_OK, Intent())
                finish()
            }
            onError { error ->
                error.msg.toast()
            }
        }
    }

    override fun onBindViewClick() {
        mBind.copyButton.clickNoRepeat {
            val uid = mBind.tvAccount.text?.toString().orEmpty()
            if (uid.isNotBlank()) {
                copyToClipboard(uid, AppStrings.get(StringResId.ACCOUNT_LABEL))
                AppStrings.get(StringResId.COPY_ACTION).toast()
            }
        }

        mBind.ivAvatar.clickNoRepeat {
            imagePickDelegate.openGallery()
        }

        mBind.ivModify.clickNoRepeat {
            imagePickDelegate.openGallery()
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
                AppStrings.get(StringResId.PHOTO_UPLOAD_FAIL).toast()
                return@launch
            }
            mBind.ivAvatar.loadGlideAvatar(compressedFile.absolutePath)
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
