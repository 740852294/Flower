package com.flower.flow.app.core.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.Outline
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.graphics.createBitmap
import com.flower.flow.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 自定义 ImageView：对 Drawable 实时模糊
 *
 * - Android 12+ 使用 RenderEffect (GPU)
 * - Android 12 以下使用 StackBlur (CPU, 降采样优化)
 */
class DrawableBlurImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ImageFilterView(context, attrs, defStyleAttr) {

    private var blurActive = false
    private var effectRadius = 20f
    private val downsampleRatio = 0.15f

    private var sourceBitmap: Bitmap? = null
    private var blurredBitmap: Bitmap? = null
    private var offscreenCanvas: Canvas? = null

    private val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sourceRect = Rect()
    private val destRect = Rect()
    private val clipPath = Path()
    private val clipRect = RectF()

    private var cornerRadiusPx = 0f

    init {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.DrawableBlurImageView,
            defStyleAttr,
            0,
        )
        try {
            cornerRadiusPx = typedArray.getDimension(
                R.styleable.DrawableBlurImageView_dbiv_cornerRadius,
                0f,
            )
        } finally {
            typedArray.recycle()
        }
        restoreDefaultRoundStyle()
    }

    /**
     * @param enabled 是否启用模糊
     * @param radius 模糊半径（建议 0-25）
     */
    fun applyBlurEffect(enabled: Boolean, radius: Float = 20f) {
        if (blurActive == enabled && effectRadius == radius) return

        effectRadius = radius.coerceIn(0f, 25f)
        blurActive = enabled

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val gpuRadius = 105f
            setRenderEffect(
                if (blurActive) {
                    RenderEffect.createBlurEffect(gpuRadius, gpuRadius, Shader.TileMode.CLAMP)
                } else {
                    null
                },
            )
        } else {
            if (!enabled) {
                clearOffscreenBitmaps()
            }
            invalidate()
        }
    }

    override fun draw(canvas: Canvas) {
        if (clipPath.isEmpty) {
            super.draw(canvas)
            return
        }

        val saveCount = canvas.save()
        canvas.clipPath(clipPath)
        super.draw(canvas)
        canvas.restoreToCount(saveCount)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateClipPath(w, h)
        if (cornerRadiusPx > 0f) {
            invalidateOutline()
        }
    }

    fun restoreDefaultRoundStyle() {
        if (cornerRadiusPx <= 0f) {
            clipToOutline = false
            outlineProvider = ViewOutlineProvider.BACKGROUND
            return
        }

        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                if (view.width <= 0 || view.height <= 0) return
                outline.setRoundRect(0, 0, view.width, view.height, cornerRadiusPx)
            }
        }
        updateClipPath(width, height)
        if (width > 0 && height > 0) {
            invalidateOutline()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            super.onDraw(canvas)
            return
        }

        if (!blurActive || drawable == null) {
            super.onDraw(canvas)
            return
        }

        try {
            val viewWidth = width
            val viewHeight = height
            if (viewWidth <= 0 || viewHeight <= 0) {
                super.onDraw(canvas)
                return
            }

            prepareOffscreenBitmaps(viewWidth, viewHeight)

            val currentDrawable = drawable ?: run {
                super.onDraw(canvas)
                return
            }

            offscreenCanvas?.let { offscreen ->
                offscreen.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                val saveCount = offscreen.save()
                offscreen.scale(downsampleRatio, downsampleRatio)
                currentDrawable.setBounds(0, 0, viewWidth, viewHeight)
                currentDrawable.draw(offscreen)
                offscreen.restoreToCount(saveCount)
            }

            // Blurred covers are intentionally static. Recompute only when Android asks
            // this view to draw instead of scheduling a permanent animation loop.
            processBlurFrame()

            val blurred = blurredBitmap ?: run {
                super.onDraw(canvas)
                return
            }

            canvas.drawBitmap(blurred, sourceRect, destRect, drawPaint)
        } catch (e: Exception) {
            e.printStackTrace()
            super.onDraw(canvas)
        }
    }

    private fun updateClipPath(width: Int, height: Int) {
        clipPath.reset()
        if (width <= 0 || height <= 0 || cornerRadiusPx <= 0f) {
            return
        }
        clipRect.set(0f, 0f, width.toFloat(), height.toFloat())
        clipPath.addRoundRect(clipRect, cornerRadiusPx, cornerRadiusPx, Path.Direction.CW)
        clipPath.close()
    }

    private fun prepareOffscreenBitmaps(viewWidth: Int, viewHeight: Int) {
        val scaledWidth = (viewWidth * downsampleRatio).toInt().coerceAtLeast(1)
        val scaledHeight = (viewHeight * downsampleRatio).toInt().coerceAtLeast(1)

        val needsRecreate = sourceBitmap == null ||
            sourceBitmap?.width != scaledWidth ||
            sourceBitmap?.height != scaledHeight

        if (!needsRecreate) return

        clearOffscreenBitmaps()

        sourceBitmap = createBitmap(scaledWidth, scaledHeight)
        blurredBitmap = createBitmap(scaledWidth, scaledHeight)
        offscreenCanvas = Canvas(sourceBitmap!!)

        sourceRect.set(0, 0, scaledWidth, scaledHeight)
        destRect.set(0, 0, viewWidth, viewHeight)
    }

    private fun processBlurFrame() {
        try {
            val source = sourceBitmap ?: return
            val output = blurredBitmap ?: return

            val w = source.width
            val h = source.height
            val pixels = IntArray(w * h)
            source.getPixels(pixels, 0, w, 0, 0, w, h)
            output.setPixels(pixels, 0, w, 0, 0, w, h)

            runStackBlur(output, effectRadius.roundToInt())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clearOffscreenBitmaps() {
        sourceBitmap?.recycle()
        blurredBitmap?.recycle()
        sourceBitmap = null
        blurredBitmap = null
        offscreenCanvas = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearOffscreenBitmaps()
    }

    private fun runStackBlur(bitmap: Bitmap, radius: Int) {
        if (radius < 1) return

        val w = bitmap.width
        val h = bitmap.height
        val pix = IntArray(w * h)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)

        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rSum: Int
        var gSum: Int
        var bSum: Int
        var p: Int
        var yp: Int
        var yi: Int
        val vMin = IntArray(max(w, h))

        var divSum = (div + 1) shr 1
        divSum *= divSum
        val dv = IntArray(256 * divSum)
        for (i in 0 until 256 * divSum) {
            dv[i] = (i / divSum)
        }

        var yw = 0
        yi = 0

        val stack = Array(div) { IntArray(3) }
        var stackPointer: Int
        var stackStart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routSum: Int
        var goutSum: Int
        var boutSum: Int
        var rinSum: Int
        var ginSum: Int
        var binSum: Int

        for (y in 0 until h) {
            rinSum = 0
            ginSum = 0
            binSum = 0
            routSum = 0
            goutSum = 0
            boutSum = 0
            rSum = 0
            gSum = 0
            bSum = 0

            for (i in -radius..radius) {
                p = pix[yi + min(wm, max(i, 0))]
                sir = stack[i + radius]
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = (p and 0x0000ff)
                rbs = r1 - abs(i)
                rSum += sir[0] * rbs
                gSum += sir[1] * rbs
                bSum += sir[2] * rbs
                if (i > 0) {
                    rinSum += sir[0]
                    ginSum += sir[1]
                    binSum += sir[2]
                } else {
                    routSum += sir[0]
                    goutSum += sir[1]
                    boutSum += sir[2]
                }
            }
            stackPointer = radius

            for (x in 0 until w) {
                r[yi] = dv[rSum]
                g[yi] = dv[gSum]
                b[yi] = dv[bSum]

                rSum -= routSum
                gSum -= goutSum
                bSum -= boutSum

                stackStart = stackPointer - radius + div
                sir = stack[stackStart % div]

                routSum -= sir[0]
                goutSum -= sir[1]
                boutSum -= sir[2]

                if (y == 0) {
                    vMin[x] = min(x + radius + 1, wm)
                }
                p = pix[yw + vMin[x]]

                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = (p and 0x0000ff)

                rinSum += sir[0]
                ginSum += sir[1]
                binSum += sir[2]

                rSum += rinSum
                gSum += ginSum
                bSum += binSum

                stackPointer = (stackPointer + 1) % div
                sir = stack[stackPointer % div]

                routSum += sir[0]
                goutSum += sir[1]
                boutSum += sir[2]

                rinSum -= sir[0]
                ginSum -= sir[1]
                binSum -= sir[2]

                yi++
            }
            yw += w
        }

        for (x in 0 until w) {
            rinSum = 0
            ginSum = 0
            binSum = 0
            routSum = 0
            goutSum = 0
            boutSum = 0
            rSum = 0
            gSum = 0
            bSum = 0
            yp = -radius * w
            for (i in -radius..radius) {
                yi = max(0, yp) + x
                sir = stack[i + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = r1 - abs(i)
                rSum += r[yi] * rbs
                gSum += g[yi] * rbs
                bSum += b[yi] * rbs
                if (i > 0) {
                    rinSum += sir[0]
                    ginSum += sir[1]
                    binSum += sir[2]
                } else {
                    routSum += sir[0]
                    goutSum += sir[1]
                    boutSum += sir[2]
                }
                if (i < hm) {
                    yp += w
                }
            }
            yi = x
            stackPointer = radius
            for (y in 0 until h) {
                pix[yi] = (0xff000000.toInt() or (dv[rSum] shl 16) or (dv[gSum] shl 8) or dv[bSum])

                rSum -= routSum
                gSum -= goutSum
                bSum -= boutSum

                stackStart = stackPointer - radius + div
                sir = stack[stackStart % div]

                routSum -= sir[0]
                goutSum -= sir[1]
                boutSum -= sir[2]

                if (x == 0) {
                    vMin[y] = min(y + r1, hm) * w
                }
                p = x + vMin[y]

                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]

                rinSum += sir[0]
                ginSum += sir[1]
                binSum += sir[2]

                rSum += rinSum
                gSum += ginSum
                bSum += binSum

                stackPointer = (stackPointer + 1) % div
                sir = stack[stackPointer]

                routSum += sir[0]
                goutSum += sir[1]
                boutSum += sir[2]

                rinSum -= sir[0]
                ginSum -= sir[1]
                binSum -= sir[2]

                yi += w
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
    }
}
