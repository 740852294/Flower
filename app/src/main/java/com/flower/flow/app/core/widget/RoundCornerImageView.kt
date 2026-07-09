package com.flower.flow.app.core.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import com.flower.flow.R

class RoundCornerImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val clipPath = Path()
    private val borderPath = Path()
    private val contentRect = RectF()
    private val borderRect = RectF()
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.TRANSPARENT
        strokeJoin = Paint.Join.ROUND
    }

    private var cornerRadiiPx: FloatArray? = null
    private var borderWidthPx = 0f
    private var borderColor = Color.TRANSPARENT
    private var borderGradientStartColor: Int? = null
    private var borderGradientEndColor: Int? = null
    private var borderGradientOrientation = BORDER_GRADIENT_LEFT_TO_RIGHT
    private var defaultCornerRadiiPx: FloatArray? = null
    private var defaultBorderWidthPx = 0f
    private var defaultBorderColor = Color.TRANSPARENT
    private var defaultBorderGradientStartColor: Int? = null
    private var defaultBorderGradientEndColor: Int? = null
    private var defaultBorderGradientOrientation = BORDER_GRADIENT_LEFT_TO_RIGHT

    init {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.RoundCornerImageView,
            defStyleAttr,
            0,
        )
        try {
            val cornerRadius = typedArray.getDimension(
                R.styleable.RoundCornerImageView_rciv_cornerRadius,
                0f,
            )
            val hasCornerStyle =
                typedArray.hasValue(R.styleable.RoundCornerImageView_rciv_cornerRadius) ||
                    typedArray.hasValue(R.styleable.RoundCornerImageView_rciv_cornerTopLeftRadius) ||
                    typedArray.hasValue(R.styleable.RoundCornerImageView_rciv_cornerTopRightRadius) ||
                    typedArray.hasValue(R.styleable.RoundCornerImageView_rciv_cornerBottomRightRadius) ||
                    typedArray.hasValue(R.styleable.RoundCornerImageView_rciv_cornerBottomLeftRadius)

            if (hasCornerStyle) {
                val topLeft = typedArray.getDimension(
                    R.styleable.RoundCornerImageView_rciv_cornerTopLeftRadius,
                    cornerRadius,
                )
                val topRight = typedArray.getDimension(
                    R.styleable.RoundCornerImageView_rciv_cornerTopRightRadius,
                    cornerRadius,
                )
                val bottomRight = typedArray.getDimension(
                    R.styleable.RoundCornerImageView_rciv_cornerBottomRightRadius,
                    cornerRadius,
                )
                val bottomLeft = typedArray.getDimension(
                    R.styleable.RoundCornerImageView_rciv_cornerBottomLeftRadius,
                    cornerRadius,
                )
                defaultCornerRadiiPx = floatArrayOf(
                    topLeft, topLeft,
                    topRight, topRight,
                    bottomRight, bottomRight,
                    bottomLeft, bottomLeft,
                )
            }

            defaultBorderWidthPx = typedArray.getDimension(
                R.styleable.RoundCornerImageView_rciv_borderWidth,
                0f,
            )
            defaultBorderColor = typedArray.getColor(
                R.styleable.RoundCornerImageView_rciv_borderColor,
                Color.TRANSPARENT,
            )
            if (
                typedArray.hasValue(R.styleable.RoundCornerImageView_rciv_borderGradientStartColor) &&
                typedArray.hasValue(R.styleable.RoundCornerImageView_rciv_borderGradientEndColor)
            ) {
                defaultBorderGradientStartColor = typedArray.getColor(
                    R.styleable.RoundCornerImageView_rciv_borderGradientStartColor,
                    Color.TRANSPARENT,
                )
                defaultBorderGradientEndColor = typedArray.getColor(
                    R.styleable.RoundCornerImageView_rciv_borderGradientEndColor,
                    Color.TRANSPARENT,
                )
                defaultBorderGradientOrientation = typedArray.getInt(
                    R.styleable.RoundCornerImageView_rciv_borderGradientOrientation,
                    BORDER_GRADIENT_LEFT_TO_RIGHT,
                )
            }
        } finally {
            typedArray.recycle()
        }
        restoreDefaultRoundStyle()
    }

    fun setCornerRadiiPx(radii: FloatArray?) {
        cornerRadiiPx = radii?.copyOf()
        updateClipPath(width, height)
        invalidate()
    }

    fun setBorderPx(width: Float, @ColorInt color: Int) {
        borderWidthPx = width.coerceAtLeast(0f)
        borderColor = color
        borderGradientStartColor = null
        borderGradientEndColor = null
        updateBorderPaint(this.width, height)
        updateClipPath(this.width, height)
        invalidate()
    }

    fun setBorderGradientPx(
        width: Float,
        @ColorInt startColor: Int,
        @ColorInt endColor: Int,
        orientation: Int = BORDER_GRADIENT_LEFT_TO_RIGHT,
    ) {
        borderWidthPx = width.coerceAtLeast(0f)
        borderGradientStartColor = startColor
        borderGradientEndColor = endColor
        borderGradientOrientation = orientation
        updateBorderPaint(this.width, height)
        updateClipPath(this.width, height)
        invalidate()
    }

    fun clearRoundStyle() {
        restoreDefaultRoundStyle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateBorderPaint(w, h)
        updateClipPath(w, h)
    }

    override fun draw(canvas: Canvas) {
        val hasClipPath = !clipPath.isEmpty
        if (!hasClipPath) {
            super.draw(canvas)
        } else {
            val saveCount = canvas.save()
            canvas.clipPath(clipPath)
            super.draw(canvas)
            canvas.restoreToCount(saveCount)
        }

        if (borderWidthPx > 0f && !borderPath.isEmpty) {
            canvas.drawPath(borderPath, borderPaint)
        }
    }

    private fun updateClipPath(width: Int, height: Int) {
        clipPath.reset()
        borderPath.reset()
        val radii = cornerRadiiPx
        if (width <= 0 || height <= 0) {
            return
        }

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        if (radii != null && radii.size == 8) {
            contentRect.set(0f, 0f, viewWidth, viewHeight)
            clipPath.addRoundRect(contentRect, radii, Path.Direction.CW)
            clipPath.close()
        }

        if (borderWidthPx <= 0f) {
            return
        }

        val halfBorder = borderWidthPx / 2f
        borderRect.set(
            halfBorder,
            halfBorder,
            viewWidth - halfBorder,
            viewHeight - halfBorder,
        )
        if (radii != null && radii.size == 8) {
            borderPath.addRoundRect(
                borderRect,
                radii.copyForInset(halfBorder),
                Path.Direction.CW,
            )
        } else {
            borderPath.addRect(borderRect, Path.Direction.CW)
        }
        borderPath.close()
    }

    private fun restoreDefaultRoundStyle() {
        cornerRadiiPx = defaultCornerRadiiPx?.copyOf()
        borderWidthPx = defaultBorderWidthPx
        borderColor = defaultBorderColor
        borderGradientStartColor = defaultBorderGradientStartColor
        borderGradientEndColor = defaultBorderGradientEndColor
        borderGradientOrientation = defaultBorderGradientOrientation
        updateBorderPaint(width, height)
        updateClipPath(width, height)
        invalidate()
    }

    private fun updateBorderPaint(width: Int, height: Int) {
        borderPaint.strokeWidth = borderWidthPx
        val startColor = borderGradientStartColor
        val endColor = borderGradientEndColor
        if (borderWidthPx <= 0f || width <= 0 || height <= 0 || startColor == null || endColor == null) {
            borderPaint.shader = null
            borderPaint.color = borderColor
            return
        }

        val (endX, endY) = when (borderGradientOrientation) {
            BORDER_GRADIENT_TOP_TO_BOTTOM -> 0f to height.toFloat()
            else -> width.toFloat() to 0f
        }
        borderPaint.color = Color.WHITE
        borderPaint.shader = LinearGradient(
            0f,
            0f,
            endX,
            endY,
            startColor,
            endColor,
            Shader.TileMode.CLAMP,
        )
    }

    companion object {
        const val BORDER_GRADIENT_LEFT_TO_RIGHT = 0
        const val BORDER_GRADIENT_TOP_TO_BOTTOM = 1
    }
}

private fun FloatArray.copyForInset(inset: Float): FloatArray =
    FloatArray(size) { index -> (this[index] - inset).coerceAtLeast(0f) }
