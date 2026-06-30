package com.flower.flow.app.core.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.flower.flow.R

class DashedLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    enum class Orientation {
        HORIZONTAL,
        VERTICAL,
    }

    var orientation: Orientation = Orientation.VERTICAL
        set(value) {
            field = value
            invalidate()
        }

    var dashLength: Float = dp(4f)
        set(value) {
            field = value
            updatePathEffect()
            invalidate()
        }

    var dashGap: Float = dp(4f)
        set(value) {
            field = value
            updatePathEffect()
            invalidate()
        }

    var lineWidth: Float = dp(1f)
        set(value) {
            field = value
            linePaint.strokeWidth = value
            invalidate()
        }

    var lineColor: Int = 0x66FFFFFF
        set(value) {
            field = value
            linePaint.color = value
            invalidate()
        }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
        strokeWidth = lineWidth
        color = lineColor
    }

    private val linePath = Path()

    init {
        if (attrs != null) {
            context.withStyledAttributes(attrs, R.styleable.DashedLineView) {
                orientation = when (getInt(R.styleable.DashedLineView_dlv_orientation, 1)) {
                    0 -> Orientation.HORIZONTAL
                    else -> Orientation.VERTICAL
                }
                dashLength = getDimension(R.styleable.DashedLineView_dlv_dashLength, dashLength)
                dashGap = getDimension(R.styleable.DashedLineView_dlv_dashGap, dashGap)
                lineWidth = getDimension(R.styleable.DashedLineView_dlv_lineWidth, lineWidth)
                lineColor = getColor(R.styleable.DashedLineView_dlv_lineColor, lineColor)
            }
        }
        linePaint.strokeWidth = lineWidth
        linePaint.color = lineColor
        updatePathEffect()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        linePath.reset()
        when (orientation) {
            Orientation.VERTICAL -> {
                val centerX = width / 2f
                linePath.moveTo(centerX, 0f)
                linePath.lineTo(centerX, height.toFloat())
            }

            Orientation.HORIZONTAL -> {
                val centerY = height / 2f
                linePath.moveTo(0f, centerY)
                linePath.lineTo(width.toFloat(), centerY)
            }
        }
        canvas.drawPath(linePath, linePaint)
    }

    private fun updatePathEffect() {
        linePaint.pathEffect = DashPathEffect(floatArrayOf(dashLength, dashGap), 0f)
    }

    private fun dp(value: Float): Float {
        return value * resources.displayMetrics.density
    }
}
