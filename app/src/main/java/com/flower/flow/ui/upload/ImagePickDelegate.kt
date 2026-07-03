package com.flower.flow.ui.upload

import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.data.model.StringResId
import com.flower.flow.ui.dialog.CommonMessageDialog
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists

object StorageAccessPolicy {

    fun canAutoOpenGallery(activity: FragmentActivity): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> true
            else -> XXPermissions.isGrantedPermission(
                activity,
                PermissionLists.getReadExternalStoragePermission(),
            )
        }
    }

    fun hasReadPermission(activity: FragmentActivity): Boolean {
        return XXPermissions.isGrantedPermission(
            activity,
            PermissionLists.getReadExternalStoragePermission(),
        )
    }
}

class ImagePickDelegate(
    private val activity: AppCompatActivity,
    private val photoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    private val legacyPickerLauncher: ActivityResultLauncher<Intent>,
) {

    fun autoOpenIfPossible() {
        if (StorageAccessPolicy.canAutoOpenGallery(activity)) {
            openGallery()
        }
    }

    fun openGallery() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                launchLegacyPicker()
            }

            else -> requestLegacyPermissionAndOpen()
        }
    }

    private fun requestLegacyPermissionAndOpen() {
        XXPermissions.with(activity)
            .permission(PermissionLists.getReadExternalStoragePermission())
            .request { _, deniedList ->
                when {
                    deniedList.isEmpty() -> launchLegacyPicker()
                    XXPermissions.isDoNotAskAgainPermissions(activity, deniedList) ->
                        showPermanentDenyDialog()
                }
            }
    }

    private fun showPermanentDenyDialog() {
        CommonMessageDialog.Builder(activity)
            .setTitle(AppStrings.get(StringResId.PERM_STORE_HEAD))
            .setContent(AppStrings.get(StringResId.STORAGE_DESC))
            .setConfirmButton(AppStrings.get(StringResId.CONFIRM_ACTION)) {
                XXPermissions.startPermissionActivity(activity)
            }
            .setCancelButton(AppStrings.get(StringResId.CANCEL_ACTION))
            .show()
    }

    private fun launchLegacyPicker() {
        var intent = Intent(Intent.ACTION_PICK).apply {
            setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (intent.resolveActivity(activity.packageManager) == null) {
            intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        if (intent.resolveActivity(activity.packageManager) != null) {
            legacyPickerLauncher.launch(intent)
        }
    }
}
