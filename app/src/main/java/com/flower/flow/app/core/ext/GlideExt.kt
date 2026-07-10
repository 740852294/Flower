package com.flower.flow.app.core.ext

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.flower.flow.R
import java.io.File

fun ImageView.loadGlideImage(
    url: String?,
    @DrawableRes placeholderRes: Int = R.mipmap.ic_pic_loading,
    scaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER_CROP,
    onResourceReady: ((ImageView) -> Unit)? = null,
    autoPlay: Boolean = true,
) {
    this.scaleType = scaleType
    setTag(R.id.tag_image_auto_play, autoPlay)
    if (url.isNullOrBlank()) {
        setTag(R.id.tag_image_url, null)
        setTag(R.id.tag_image_auto_play, null)
        Glide.with(this).clear(this)
        setImageResource(placeholderRes)
        return
    }

    val currentUrl = getTag(R.id.tag_image_url) as? String
    val currentDrawable = drawable
    val placeholderState = ContextCompat.getDrawable(context, placeholderRes)?.constantState
    if (currentUrl == url &&
        currentDrawable != null &&
        currentDrawable.constantState != placeholderState
    ) {
        setGlideAnimationRunning(autoPlay)
        onResourceReady?.let { callback ->
            post { callback(this) }
        }
        return
    }

    setTag(R.id.tag_image_url, url)
    val requestBuilder = Glide.with(this)
        .load(url)
        .placeholder(placeholderRes)
        .error(placeholderRes)
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)

    if (onResourceReady != null || !autoPlay) {
        requestBuilder.listener(
            object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean,
                ): Boolean = false

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    post {
                        if (getTag(R.id.tag_image_url) == url) {
                            setGlideAnimationRunning(autoPlay)
                            onResourceReady?.invoke(this@loadGlideImage)
                        }
                    }
                    return false
                }
            },
        )
    }

    requestBuilder
        .into(this)
}

fun ImageView.clearGlideImage() {
    setTag(R.id.tag_image_url, null)
    setTag(R.id.tag_image_auto_play, null)
    Glide.with(this).clear(this)
    setImageDrawable(null)
}

/**
 * 宽度占满父布局，高度按图片比例自适应。
 */
fun ImageView.loadGlideImageFitWidth(
    url: String?,
    @DrawableRes placeholderRes: Int = R.mipmap.ic_pic_loading,
    onAspectRatio: ((width: Int, height: Int) -> Unit)? = null,
    autoPlay: Boolean = true,
) {
    scaleType = ImageView.ScaleType.FIT_CENTER
    setTag(R.id.tag_image_auto_play, autoPlay)
    if (url.isNullOrBlank()) {
        clearGlideImage()
        return
    }

    val currentUrl = getTag(R.id.tag_image_url) as? String
    val currentDrawable = drawable
    val placeholderState = ContextCompat.getDrawable(context, placeholderRes)?.constantState
    if (currentUrl == url &&
        currentDrawable != null &&
        currentDrawable.constantState != placeholderState
    ) {
        setGlideAnimationRunning(autoPlay)
        applyGlideFitWidthDrawable(currentDrawable, onAspectRatio)
        return
    }

    setTag(R.id.tag_image_url, url)
    Glide.with(this)
        .load(url)
        .placeholder(placeholderRes)
        .error(placeholderRes)
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .listener(
            object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean,
                ): Boolean = false

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    if (getTag(R.id.tag_image_url) == url) {
                        applyGlideFitWidthDrawable(resource, onAspectRatio)
                        post {
                            if (getTag(R.id.tag_image_url) == url) {
                                setGlideAnimationRunning(autoPlay)
                            }
                        }
                    }
                    return false
                }
            },
        )
        .into(this)
}

fun ImageView.loadGlideAvatar(
    source: String?,
    @DrawableRes placeholderRes: Int = R.mipmap.ic_default_avatar,
    scaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER_CROP,
    onResourceReady: ((ImageView) -> Unit)? = null,
) {
    val resolvedSource = source
        ?.takeIf { it.isNotBlank() }
        ?.let(::resolveGlideSource)
    loadGlideImage(
        url = resolvedSource,
        placeholderRes = placeholderRes,
        scaleType = scaleType,
        onResourceReady = onResourceReady,
    )
}

fun ImageView.startGlideAnimation() {
    (drawable as? Animatable)?.start()
}

fun ImageView.stopGlideAnimation() {
    (drawable as? Animatable)?.stop()
}

fun ImageView.setGlideAnimationRunning(running: Boolean) {
    val shouldRun = running && (getTag(R.id.tag_image_auto_play) as? Boolean != false)
    if (shouldRun) {
        startGlideAnimation()
    } else {
        stopGlideAnimation()
    }
}

private fun ImageView.applyGlideFitWidthDrawable(
    drawable: Drawable,
    onAspectRatio: ((width: Int, height: Int) -> Unit)?,
) {
    val imageWidth = drawable.intrinsicWidth
    val imageHeight = drawable.intrinsicHeight
    if (imageWidth <= 0 || imageHeight <= 0) return
    if (onAspectRatio != null) {
        onAspectRatio.invoke(imageWidth, imageHeight)
    } else {
        updateGlideHeightByAspectRatio(imageWidth, imageHeight)
    }
}

private fun ImageView.updateGlideHeightByAspectRatio(imageWidth: Int, imageHeight: Int) {
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

private fun resolveGlideSource(source: String): String {
    return when {
        source.startsWith("http://") ||
            source.startsWith("https://") ||
            source.startsWith("file://") ||
            source.startsWith("content://") -> source

        else -> Uri.fromFile(File(source)).toString()
    }
}
