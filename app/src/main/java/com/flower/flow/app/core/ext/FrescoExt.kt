package com.flower.flow.app.core.ext

import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.fresco.vito.options.ImageOptions
import com.facebook.fresco.vito.options.RoundingOptions
import com.facebook.fresco.vito.source.ImageSourceProvider
import com.facebook.fresco.vito.view.VitoView
import java.io.File

/**
 * 通用网络图片加载方法（基于 Fresco Vito）。
 * @param url 图片地址，为空或空字符串时清空当前图片
 * @param placeholderRes 占位图资源 ID
 */
fun ImageView.loadImage(
    url: String?,
    @DrawableRes placeholderRes: Int? = null,
    scaleType: ScalingUtils.ScaleType = ScalingUtils.ScaleType.CENTER_CROP
) {
    if (url.isNullOrBlank()) {
        setImageDrawable(null)
        return
    }

    val imageOptionsBuilder = ImageOptions.create()
    placeholderRes?.let { imageOptionsBuilder.placeholderRes(it, scaleType) }
    imageOptionsBuilder.autoPlay(true)
    imageOptionsBuilder.autoStop(true)

    VitoView.show(
        ImageSourceProvider.forUri(url),
        imageOptionsBuilder.build(),
        this,
    )
}

/**
 * 加载头像（本地文件或网络 URL），圆形裁剪显示。
 * @param source 网络 URL（http/https）、content://、file://，或本地文件绝对路径
 */
fun ImageView.loadAvatarFile(
    source: String?,
    @DrawableRes placeholderRes: Int? = null,
    scaleType: ScalingUtils.ScaleType = ScalingUtils.ScaleType.CENTER_CROP,
) {
    if (source.isNullOrBlank()) {
        setImageDrawable(null)
        return
    }

    val uri = resolveAvatarSource(source)

    val imageOptionsBuilder = ImageOptions.create()
        .round(RoundingOptions.asCircle())
    placeholderRes?.let { imageOptionsBuilder.placeholderRes(it, scaleType) }
    imageOptionsBuilder.autoPlay(true)
    imageOptionsBuilder.autoStop(true)

    VitoView.show(
        ImageSourceProvider.forUri(uri),
        imageOptionsBuilder.build(),
        this,
    )
}

/**
 * 加载本地头像文件，圆形裁剪显示。
 */
fun ImageView.loadAvatarFile(
    file: File?,
    @DrawableRes placeholderRes: Int? = null,
    scaleType: ScalingUtils.ScaleType = ScalingUtils.ScaleType.CENTER_CROP,
) {
    loadAvatarFile(file?.absolutePath, placeholderRes, scaleType)
}

private fun resolveAvatarSource(source: String): String {
    return when {
        source.startsWith("http://") ||
            source.startsWith("https://") ||
            source.startsWith("file://") ||
            source.startsWith("content://") -> source

        else -> Uri.fromFile(File(source)).toString()
    }
}

/**
 * 释放 Vito 图片资源并清空 ImageView。
 */
fun ImageView.clearImage() {
    VitoView.release(this)
    setImageDrawable(null)
}
