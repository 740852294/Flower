package com.flower.flow.app.core.ext

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.facebook.drawee.drawable.ArrayDrawable
import com.facebook.drawee.drawable.DrawableParent
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.fresco.vito.listener.BaseImageListener
import com.facebook.fresco.vito.options.ImageOptions
import com.facebook.fresco.vito.source.ImageSourceProvider
import com.facebook.fresco.vito.view.VitoView
import com.facebook.imagepipeline.image.ImageInfo
import com.flower.flow.R
import java.io.File

/**
 * 通用网络图片加载方法（基于 Fresco Vito）。
 *
 * 圆角/边框通过 View 裁剪实现，静态图与 WebP 动图均有效。
 *
 * @param url 图片地址，为空或空字符串时清空当前图片
 * @param placeholderRes 占位图资源 ID
 * @param resizeToViewport 是否按 ImageView 显示尺寸下采样解码
 */
fun ImageView.loadImage(
    url: String?,
    @DrawableRes placeholderRes: Int = R.mipmap.ic_pic_loading,
    scaleType: ScalingUtils.ScaleType = ScalingUtils.ScaleType.CENTER_CROP,
    isAutoPlay: Boolean = true,
    resizeToViewport: Boolean = false,
    onFinalImageSet: ((ImageView) -> Unit)? = null,
) {
    clearStoredImageAnimationDrawable()
    if (url.isNullOrBlank()) {
        clearImage()
        return
    }

    val imageOptionsBuilder = ImageOptions.create()
        .scale(scaleType)
    if (resizeToViewport) {
        imageOptionsBuilder.resizeToViewport(true)
    }
    placeholderRes.let { imageOptionsBuilder.placeholderRes(it, scaleType) }
    imageOptionsBuilder.autoPlay(isAutoPlay)
    imageOptionsBuilder.autoStop(isAutoPlay)

    val imageSource = ImageSourceProvider.forUri(url)
    if (onFinalImageSet == null) {
        VitoView.show(
            imageSource,
            imageOptionsBuilder.build(),
            this,
        )
        return
    }

    val target = this
    val imageListener = object : BaseImageListener() {
        override fun onFinalImageSet(
            id: Long,
            imageOrigin: Int,
            imageInfo: ImageInfo?,
            drawable: Drawable?,
        ) {
            target.setStoredImageAnimationDrawable(drawable)
            target.post {
                onFinalImageSet(target)
            }
        }
    }

    VitoView.show(
        imageSource,
        imageOptionsBuilder.build(),
        null,
        imageListener,
        this,
    )
}

/** 控制 Fresco Vito 加载的 WebP/GIF 动图播放状态，静态图片会被忽略。 */
fun ImageView.setImageAnimationRunning(running: Boolean) {
    val frescoDrawable = VitoView.getDrawable(this)
    val animatable = findImageAnimatable(
        getStoredImageAnimationDrawable(),
        frescoDrawable?.actualImageDrawable,
        frescoDrawable as? Drawable,
        drawable,
    ) ?: return
    if (running) {
        if (!animatable.isRunning) animatable.start()
    } else if (animatable.isRunning) {
        animatable.stop()
    }
}

/**
 * 宽度占满父布局，高度按图片比例自适应。
 *
 * Fresco Vito 不会触发 ImageView 的 adjustViewBounds，需要在图片加载完成后手动计算高度。
 */
fun ImageView.loadImageFitWidth(
    url: String?,
    @DrawableRes placeholderRes: Int = R.mipmap.ic_pic_loading,
    isAutoPlay: Boolean = true,
    onAspectRatio: ((width: Int, height: Int) -> Unit)? = null,
) {
    if (url.isNullOrBlank()) {
        clearImage()
        return
    }

    scaleType = ImageView.ScaleType.FIT_CENTER

    val imageOptions = ImageOptions.create()
        .scale(ScalingUtils.ScaleType.FIT_CENTER)
        .apply {
            placeholderRes(placeholderRes, ScalingUtils.ScaleType.FIT_CENTER)
            autoPlay(isAutoPlay)
            autoStop(isAutoPlay)
        }
        .build()

    val imageListener = object : BaseImageListener() {
        override fun onFinalImageSet(
            id: Long,
            imageOrigin: Int,
            imageInfo: ImageInfo?,
            drawable: Drawable?,
        ) {
            val imageWidth = imageInfo?.width ?: return
            val imageHeight = imageInfo?.height ?: return
            if (imageWidth <= 0 || imageHeight <= 0) return
            if (onAspectRatio != null) {
                onAspectRatio.invoke(imageWidth, imageHeight)
            } else {
                updateHeightByAspectRatio(imageWidth, imageHeight)
            }
        }
    }

    VitoView.show(
        ImageSourceProvider.forUri(url),
        imageOptions,
        null,
        imageListener,
        this,
    )
}

private fun ImageView.updateHeightByAspectRatio(imageWidth: Int, imageHeight: Int) {
    fun applyHeight() {
        val viewWidth = measuredWidth - paddingLeft - paddingRight
        if (viewWidth <= 0) return
        val targetHeight = (viewWidth.toFloat() * imageHeight / imageWidth).toInt() +
            paddingTop + paddingBottom
        if (layoutParams.height != targetHeight) {
            layoutParams = layoutParams.apply { height = targetHeight }
            requestLayout()
        }
    }

    if (measuredWidth > 0) {
        applyHeight()
        return
    }

    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth <= 0) return
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            applyHeight()
        }
    })
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
    scaleType: ScalingUtils.ScaleType = ScalingUtils.ScaleType.CENTER_CROP,
    isAutoPlay: Boolean = true,
    onFinalImageSet: ((ImageView) -> Unit)? = null,
) {
    clearStoredImageAnimationDrawable()
    if (source.isNullOrBlank()) {
        setImageDrawable(null)
        return
    }

    val uri = resolveAvatarSource(source)

    val imageOptionsBuilder = ImageOptions.create()
        .scale(scaleType)
    imageOptionsBuilder.autoPlay(isAutoPlay)
    imageOptionsBuilder.autoStop(isAutoPlay)

    val imageSource = ImageSourceProvider.forUri(uri)
    if (onFinalImageSet == null) {
        VitoView.show(
            imageSource,
            imageOptionsBuilder.build(),
            this,
        )
        return
    }

    val target = this
    val imageListener = object : BaseImageListener() {
        override fun onFinalImageSet(
            id: Long,
            imageOrigin: Int,
            imageInfo: ImageInfo?,
            drawable: Drawable?,
        ) {
            target.setStoredImageAnimationDrawable(drawable)
            target.post {
                onFinalImageSet(target)
            }
        }
    }

    VitoView.show(
        imageSource,
        imageOptionsBuilder.build(),
        null,
        imageListener,
        this,
    )
}

/**
 * 加载本地头像文件，圆形裁剪显示。
 */
fun ImageView.loadAvatarFile(
    file: File?,
    scaleType: ScalingUtils.ScaleType = ScalingUtils.ScaleType.CENTER_CROP,
    isAutoPlay: Boolean = true,
    onFinalImageSet: ((ImageView) -> Unit)? = null,
) {
    loadAvatarFile(
        source = file?.absolutePath,
        scaleType = scaleType,
        isAutoPlay = isAutoPlay,
        onFinalImageSet = onFinalImageSet,
    )
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
    clearStoredImageAnimationDrawable()
    VitoView.release(this)
    setImageDrawable(null)
}

private fun ImageView.setStoredImageAnimationDrawable(drawable: Drawable?) {
    setTag(R.id.tag_image_animation_drawable, drawable)
}

private fun ImageView.getStoredImageAnimationDrawable(): Drawable? {
    return getTag(R.id.tag_image_animation_drawable) as? Drawable
}

private fun ImageView.clearStoredImageAnimationDrawable() {
    setStoredImageAnimationDrawable(null)
}

private fun findImageAnimatable(vararg drawables: Drawable?): Animatable? {
    drawables.forEach { drawable ->
        val animatable = findAnimatable(drawable)
        if (animatable != null) return animatable
    }
    return null
}

private fun findAnimatable(drawable: Drawable?, depth: Int = 0): Animatable? {
    if (drawable == null || depth > MAX_DRAWABLE_UNWRAP_DEPTH) return null
    if (drawable is Animatable) return drawable

    if (drawable is DrawableParent) {
        findAnimatable(drawable.drawable, depth + 1)?.let { return it }
    }

    if (drawable is ArrayDrawable) {
        for (index in 0 until drawable.numberOfLayers) {
            findAnimatable(drawable.getDrawable(index), depth + 1)?.let { return it }
        }
    }

    return null
}

private const val MAX_DRAWABLE_UNWRAP_DEPTH = 8
