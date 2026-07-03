package com.flower.flow.app.core.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Android 9 及以下存储权限辅助。
 *
 * Manifest 中 READ/WRITE_EXTERNAL_STORAGE 带 maxSdkVersion="28" 时，
 */
object LegacyStoragePermission {

    const val READ = Manifest.permission.READ_EXTERNAL_STORAGE
    const val WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE

    fun needsLegacyReadPermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    fun needsLegacyWritePermission(): Boolean = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P

    fun isReadGranted(context: Context): Boolean = isGranted(context, READ)

    fun isWriteGranted(context: Context): Boolean = isGranted(context, WRITE)

    fun openAppSettings(activity: Activity) {
        activity.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
            },
        )
    }

    private fun isGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
    }
}
