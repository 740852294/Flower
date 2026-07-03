package com.flower.flow.app.core.util

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.flower.flow.data.model.entity.WorkItem
import java.io.File
import java.io.IOException

object WorkDownloadStorage {

    data class Destination(
        val uri: Uri? = null,
        val tempFile: File? = null,
        val finalFile: File? = null,
        val mimeType: String,
    )

    fun createDestination(context: Context, workItem: WorkItem): Destination {
        val isVideo = workItem.behavebanister == VIDEO_ART_TYPE
        val extension = resolveExtension(workItem.wantbirdcage, isVideo)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: if (isVideo) DEFAULT_VIDEO_MIME_TYPE else DEFAULT_IMAGE_MIME_TYPE
        val directory = if (isVideo) {
            Environment.DIRECTORY_MOVIES
        } else {
            Environment.DIRECTORY_PICTURES
        }
        val fileName = buildFileName(workItem.baptismdictate, extension)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val collection = if (isVideo) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "$directory/$ALBUM_NAME")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(collection, values)
                ?: throw IOException("Unable to create MediaStore destination")
            return Destination(uri = uri, mimeType = mimeType)
        }

        @Suppress("DEPRECATION")
        val albumDirectory = File(
            Environment.getExternalStoragePublicDirectory(directory),
            ALBUM_NAME,
        )
        if (!albumDirectory.exists() && !albumDirectory.mkdirs()) {
            throw IOException("Unable to create download directory")
        }
        val finalFile = File(albumDirectory, fileName)
        return Destination(
            tempFile = File(albumDirectory, "$fileName.part"),
            finalFile = finalFile,
            mimeType = mimeType,
        )
    }

    fun complete(context: Context, destination: Destination) {
        destination.uri?.let { uri ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }
                context.contentResolver.update(uri, values, null, null)
            }
            return
        }

        val tempFile = destination.tempFile ?: throw IOException("Missing temporary file")
        val finalFile = destination.finalFile ?: throw IOException("Missing destination file")
        if (!tempFile.renameTo(finalFile)) {
            tempFile.copyTo(finalFile, overwrite = true)
            if (!tempFile.delete()) {
                tempFile.deleteOnExit()
            }
        }
        MediaScannerConnection.scanFile(
            context,
            arrayOf(finalFile.absolutePath),
            arrayOf(destination.mimeType),
            null,
        )
    }

    fun cleanup(context: Context, destination: Destination?) {
        destination ?: return
        destination.uri?.let { uri ->
            context.contentResolver.delete(uri, null, null)
            return
        }
        destination.tempFile?.delete()
    }

    private fun resolveExtension(url: String, isVideo: Boolean): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            ?.lowercase()
            ?.substringBefore('?')
            .orEmpty()
        val supportedExtensions = if (isVideo) VIDEO_EXTENSIONS else IMAGE_EXTENSIONS
        return extension.takeIf { it in supportedExtensions }
            ?: if (isVideo) DEFAULT_VIDEO_EXTENSION else DEFAULT_IMAGE_EXTENSION
    }

    private fun buildFileName(taskId: String, extension: String): String {
        val safeTaskId = taskId
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(MAX_TASK_ID_LENGTH)
            .ifBlank { "work" }
        return "Flower_${safeTaskId}_${System.currentTimeMillis()}.$extension"
    }

    private const val ALBUM_NAME = "Flower"
    private const val VIDEO_ART_TYPE = 2
    private const val DEFAULT_IMAGE_EXTENSION = "jpg"
    private const val DEFAULT_VIDEO_EXTENSION = "mp4"
    private const val DEFAULT_IMAGE_MIME_TYPE = "image/jpeg"
    private const val DEFAULT_VIDEO_MIME_TYPE = "video/mp4"
    private const val MAX_TASK_ID_LENGTH = 64

    private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif")
    private val VIDEO_EXTENSIONS = setOf("mp4", "mov", "m4v", "webm")
}
