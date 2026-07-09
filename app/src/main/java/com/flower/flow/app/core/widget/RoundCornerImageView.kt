package com.flower.flow.app.core.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
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
    private val contentRect = RectF()
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.TRANSPARENT
    }

    private var cornerRadiiPx: FloatArray? = null
    private var borderWidthPx = 0f
    private var defaultCornerRadiiPx: FloatArray? = null
    private var defaultBorderWidthPx = 0f
    private var defaultBorderColor = Color.TRANSPARENT

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
        borderPaint.strokeWidth = borderWidthPx
        borderPaint.color = color
        updateClipPath(this.width, height)
        invalidate()
    }

    fun clearRoundStyle() {
        restoreDefaultRoundStyle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateClipPath(w, h)
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

        if (borderWidthPx > 0f) {
            canvas.drawPath(clipPath, borderPaint)
        }
    }

    private fun updateClipPath(width: Int, height: Int) {
        clipPath.reset()
        val radii = cornerRadiiPx
        if (width <= 0 || height <= 0 || radii == null || radii.size != 8) {
            return
        }

        val halfBorder = borderWidthPx / 2f
        contentRect.set(
            halfBorder,
            halfBorder,
            width.toFloat() - halfBorder,
            height.toFloat() - halfBorder,
        )
        clipPath.addRoundRect(contentRect, radii, Path.Direction.CW)
        clipPath.close()
    }

    private fun restoreDefaultRoundStyle() {
        cornerRadiiPx = defaultCornerRadiiPx?.copyOf()
        borderWidthPx = defaultBorderWidthPx
        borderPaint.strokeWidth = defaultBorderWidthPx
        borderPaint.color = defaultBorderColor
        updateClipPath(width, height)
        invalidate()
    }
}
