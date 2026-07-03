package com.flower.flow.app.core.util

import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.flower.flow.data.model.StringResId
import com.flower.flow.ui.dialog.CommonMessageDialog

object StoragePermissionUi {

    fun isPermanentDenial(
        activity: FragmentActivity,
        permission: String,
        hasRequestedPermission: Boolean,
    ): Boolean {
        if (!hasRequestedPermission) return false
        val granted = when (permission) {
            LegacyStoragePermission.READ -> LegacyStoragePermission.isReadGranted(activity)
            LegacyStoragePermission.WRITE -> LegacyStoragePermission.isWriteGranted(activity)
            else -> false
        }
        if (granted) return false
        return !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    fun showPermanentDenyDialog(
        activity: FragmentActivity,
        onConfirmOpenSettings: () -> Unit,
    ) {
        CommonMessageDialog.Builder(activity)
            .setTitle(AppStrings.get(StringResId.PERM_STORE_HEAD))
            .setContent(AppStrings.get(StringResId.STORAGE_DESC))
            .setConfirmButton(AppStrings.get(StringResId.CONFIRM_ACTION)) { _ ->
                onConfirmOpenSettings()
            }
            .setCancelButton(AppStrings.get(StringResId.CANCEL_ACTION))
            .show()
    }
}
