package com.flower.flow.app.core.ext

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.fresco.vito.options.ImageOptions
import com.facebook.fresco.vito.source.ImageSourceProvider
import com.facebook.fresco.vito.view.VitoView

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
 * 释放 Vito 图片资源并清空 ImageView。
 */
fun ImageView.clearImage() {
    VitoView.release(this)
    setImageDrawable(null)
}
