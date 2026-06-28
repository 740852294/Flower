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

    private val backgroundDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = dp(15).toFloat()
    }

    private val enabledColor = "#FFF77417".toColorInt()
    private val disabledColor = "#FF525255".toColorInt()

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
        background = backgroundDrawable
        updateBackgroundColor(isEnabled)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        updateBackgroundColor(enabled)
    }

    private fun updateBackgroundColor(enabled: Boolean) {
        backgroundDrawable.setColor(if (enabled) enabledColor else disabledColor)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density + 0.5f).toInt()
    }
}