package com.flower.flow.app.core.ext

import android.graphics.Color
import android.graphics.Outline
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.fresco.vito.options.ImageOptions
import com.facebook.fresco.vito.options.RoundingOptions
import com.facebook.fresco.vito.source.ImageSourceProvider
import com.facebook.fresco.vito.view.VitoView
import com.flower.flow.R
import me.hgj.jetpackmvvm.ext.util.dp2px
import java.io.File

/**
 * 通用网络图片加载方法（基于 Fresco Vito）。
 *
 * 圆角/边框通过 View 裁剪实现，静态图与 WebP 动图均有效。
 * Fresco 默认 BITMAP_ONLY 圆角不支持动图，因此不再依赖 Fresco 的 RoundingOptions。
 *
 * @param url 图片地址，为空或空字符串时清空当前图片
 * @param placeholderRes 占位图资源 ID
 * @param cornerRadiusDp 统一圆角半径，单位 dp
 * @param cornerRadiiDp 四角圆角半径 [topLeft, topRight, bottomRight, bottomLeft]，单位 dp
 * @param borderWidthDp 边框宽度，单位 dp
 * @param borderColor 边框颜色
 */
fun ImageView.loadImage(
    url: String?,
    @DrawableRes placeholderRes: Int = R.mipmap.ic_pic_loading,
    scaleType: ScalingUtils.ScaleType = ScalingUtils.ScaleType.CENTER_CROP,
    cornerRadiusDp: Float? = null,
    cornerRadiiDp: FloatArray? = null,
    borderWidthDp: Float? = null,
    @ColorInt borderColor: Int = Color.WHITE,
) {
    if (url.isNullOrBlank()) {
        clearImage()
        return
    }

    val roundStyle = buildRoundStyle(cornerRadiusDp, cornerRadiiDp)
    if (roundStyle != null) {
        applyRoundStyle(roundStyle, borderWidthDp, borderColor)
    } else {
        clearRoundStyle()
    }

    val imageOptionsBuilder = ImageOptions.create()
        .scale(scaleType)
    placeholderRes.let { imageOptionsBuilder.placeholderRes(it, scaleType) }
    imageOptionsBuilder.autoPlay(true)
    imageOptionsBuilder.autoStop(true)

    VitoView.show(
        ImageSourceProvider.forUri(url),
        imageOptionsBuilder.build(),
        this,
    )
}

private data class RoundStyle(
    val radiiPx: FloatArray,
    val uniformRadiusPx: Float?,
)

private fun buildRoundStyle(
    cornerRadiusDp: Float?,
    cornerRadiiDp: FloatArray?,
): RoundStyle? {
    if (cornerRadiiDp != null && cornerRadiiDp.size == 4) {
        val topLeft = dp2px(cornerRadiiDp[0]).toFloat()
        val topRight = dp2px(cornerRadiiDp[1]).toFloat()
        val bottomRight = dp2px(cornerRadiiDp[2]).toFloat()
        val bottomLeft = dp2px(cornerRadiiDp[3]).toFloat()
        val radiiPx = floatArrayOf(
            topLeft, topLeft,
            topRight, topRight,
            bottomRight, bottomRight,
            bottomLeft, bottomLeft,
        )
        val isUniform = topLeft == topRight && topRight == bottomRight && bottomRight == bottomLeft
        return RoundStyle(radiiPx, if (isUniform) topLeft else null)
    }
    if (cornerRadiusDp != null && cornerRadiusDp > 0f) {
        val radiusPx = dp2px(cornerRadiusDp).toFloat()
        return RoundStyle(
            radiiPx = FloatArray(8) { radiusPx },
            uniformRadiusPx = radiusPx,
        )
    }
    return null
}

private fun ImageView.applyRoundStyle(
    roundStyle: RoundStyle,
    borderWidthDp: Float?,
    @ColorInt borderColor: Int,
) {
    clipToOutline = true
    outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            if (view.width <= 0 || view.height <= 0) return
            val uniformRadius = roundStyle.uniformRadiusPx
            if (uniformRadius != null) {
                outline.setRoundRect(0, 0, view.width, view.height, uniformRadius)
                return
            }
            val path = Path()
            path.addRoundRect(
                RectF(0f, 0f, view.width.toFloat(), view.height.toFloat()),
                roundStyle.radiiPx,
                Path.Direction.CW,
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                outline.setPath(path)
            } else {
                @Suppress("DEPRECATION")
                outline.setConvexPath(path)
            }
        }
    }

    if (borderWidthDp != null && borderWidthDp > 0f) {
        foreground = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.TRANSPARENT)
            cornerRadii = roundStyle.radiiPx
            setStroke(dp2px(borderWidthDp), borderColor)
        }
    } else {
        foreground = null
    }

    if (width > 0 && height > 0) {
        invalidateOutline()
    }
}

private fun ImageView.clearRoundStyle() {
    clipToOutline = false
    outlineProvider = ViewOutlineProvider.BACKGROUND
    foreground = null
}

/**
 * 加载本地图片文件，等比例居中显示。
 */
fun ImageView.loadImageFile(
    file: File?,
    @DrawableRes placeholderRes: Int = R.mipmap.ic_pic_loading,
    scaleType: ScalingUtils.ScaleType = ScalingUtils.ScaleType.FIT_CENTER,
) {
    this.scaleType = ImageView.ScaleType.FIT_CENTER
    loadImage(
        url = file?.let { Uri.fromFile(it).toString() },
        placeholderRes = placeholderRes,
        scaleType = scaleType,
    )
}

/**
 * 加载头像（本地文件或网络 URL），圆形裁剪显示。
 * @param source 网络 URL（http/https）、content://、file://，或本地文件绝对路径
 */
fun ImageView.loadAvatarFile(
    source: String?,
    @DrawableRes placeholderRes: Int = R.mipmap.ic_pic_loading,
    scaleType: ScalingUtils.ScaleType = ScalingUtils.ScaleType.CENTER_CROP,
) {
    if (source.isNullOrBlank()) {
        setImageDrawable(null)
        return
    }

    val uri = resolveAvatarSource(source)

    val imageOptionsBuilder = ImageOptions.create()
        .round(RoundingOptions.asCircle())
        .scale(scaleType)
    placeholderRes.let { imageOptionsBuilder.placeholderRes(it, scaleType) }
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
    @DrawableRes placeholderRes: Int = R.mipmap.ic_pic_loading,
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
