package com.flower.flow.app.core.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PhotoCompressUtil {

    private const val MAX_OUTPUT_BYTES = 400 * 1024L
    private const val JPEG_START_QUALITY = 85
    private const val JPEG_QUALITY_STEP = 10
    private const val JPEG_FLOOR_QUALITY = 30
    private const val BYPASS_THRESHOLD_KB = 300
    private const val MAX_SAMPLE_RATIO = 3

    suspend fun shrinkFile(source: File, destination: File): File? = withContext(Dispatchers.IO) {
        if (!source.exists()) return@withContext null

        var sampleRatio = 1
        var latestOutput: File? = null

        while (sampleRatio <= MAX_SAMPLE_RATIO) {
            val bitmap = readBitmapScaled(source, sampleRatio) ?: return@withContext null
            latestOutput = writeJpegUntilFit(bitmap, destination)
            bitmap.recycle()

            if (latestOutput != null && latestOutput.length() <= MAX_OUTPUT_BYTES) {
                return@withContext latestOutput
            }
            sampleRatio++
        }

        latestOutput
    }

    suspend fun prepareUploadFiles(inputs: List<File>, workDir: File): List<File> =
        withContext(Dispatchers.IO) {
            inputs.map { input ->
                if (input.length() / 1024 < BYPASS_THRESHOLD_KB) {
                    input
                } else {
                    val output = File(workDir, "upload_${System.currentTimeMillis()}_${input.name}")
                    shrinkFile(input, output) ?: input
                }
            }
        }

    private fun readBitmapScaled(source: File, sampleRatio: Int): Bitmap? {
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleRatio
        }
        return runCatching {
            BitmapFactory.decodeFile(source.absolutePath, decodeOptions)
        }.getOrNull()
    }

    private fun writeJpegUntilFit(bitmap: Bitmap, destination: File): File? {
        var quality = JPEG_START_QUALITY

        while (quality >= JPEG_FLOOR_QUALITY) {
            val written = runCatching {
                FileOutputStream(destination).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
                }
            }.isSuccess

            if (!written) return null
            if (destination.length() <= MAX_OUTPUT_BYTES) {
                return destination
            }
            quality -= JPEG_QUALITY_STEP
        }

        return destination
    }
}
