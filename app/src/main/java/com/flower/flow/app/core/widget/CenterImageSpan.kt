package com.flower.flow.app.core.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan
import androidx.core.graphics.withSave

class CenterImageSpan(drawable: Drawable) : ImageSpan(drawable) {

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val drawable = drawable
        canvas.withSave {

            val fm = paint.fontMetricsInt
            val drawableHeight = drawable.bounds.height()

            // 图片按当前文字行垂直居中
            val transY = y + (fm.ascent + fm.descent - drawableHeight) / 2

            translate(x, transY.toFloat())
            drawable.draw(this)
        }
    }
}