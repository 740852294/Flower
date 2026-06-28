package com.flower.flow.app.core.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import com.hjq.shape.view.ShapeButton
import androidx.core.graphics.toColorInt

class ActionButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ShapeButton(context, attrs) {

    init {
        setTextColor(Color.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)

        gravity = Gravity.CENTER
        isAllCaps = false
        includeFontPadding = false

        minWidth = 0
        minHeight = 0
        minimumWidth = 0
        minimumHeight = 0

        setPadding(dp(13), dp(5), dp(13), dp(5))

        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(15).toFloat()
            setColor("#FFF77417".toColorInt())
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density + 0.5f).toInt()
    }
}