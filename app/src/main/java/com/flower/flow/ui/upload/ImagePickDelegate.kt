package com.flower.flow.ui.upload

import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.flower.flow.app.core.util.LegacyStoragePermission
import com.flower.flow.app.core.util.StoragePermissionUi

class ImagePickDelegate(
    private val activity: AppCompatActivity,
    private val photoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
    private val legacyPickerLauncher: ActivityResultLauncher<Intent>,
) {

    private var readPermissionRequested = false
    private var pendingOpenAfterSettings = false

    private val readPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            readPermissionRequested = false
            launchLegacyPicker()
            return@registerForActivityResult
        }
        if (StoragePermissionUi.isPermanentDenial(
                activity,
                LegacyStoragePermission.READ,
                readPermissionRequested,
            )
        ) {
            showPermanentDenyDialog()
        }
    }

    init {
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                resumeAfterSettingsIfNeeded()
            }
        })
    }

    fun autoOpenIfPossible() {
        openGallery()
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

            LegacyStoragePermission.isReadGranted(activity) -> {
                launchLegacyPicker()
            }

            else -> {
                readPermissionRequested = true
                readPermissionLauncher.launch(LegacyStoragePermission.READ)
            }
        }
    }

    private fun resumeAfterSettingsIfNeeded() {
        if (!pendingOpenAfterSettings) return
        pendingOpenAfterSettings = false
        if (!LegacyStoragePermission.isReadGranted(activity)) return
        readPermissionRequested = false
        launchLegacyPicker()
    }

    private fun showPermanentDenyDialog() {
        StoragePermissionUi.showPermanentDenyDialog(activity) {
            pendingOpenAfterSettings = true
            LegacyStoragePermission.openAppSettings(activity)
        }
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
