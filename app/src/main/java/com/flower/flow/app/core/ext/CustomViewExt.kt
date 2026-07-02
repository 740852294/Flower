package com.flower.flow.app.core.ext

import android.view.View
import androidx.appcompat.widget.Toolbar
import com.flower.flow.R
import com.google.android.material.appbar.MaterialToolbar
import me.hgj.jetpackmvvm.ext.util.dp2px
import me.hgj.jetpackmvvm.ext.util.toHtml

/**
 * 初始化有返回键的toolbar
 */
fun MaterialToolbar.initClose(
    titleStr: String = "",
    backImg: Int = R.mipmap.ic_back,
    onBack: (toolbar: Toolbar) -> Unit
): MaterialToolbar {
    title = titleStr.toHtml()
    setNavigationIcon(backImg)
    setNavigationOnClickListener { onBack.invoke(this) }
    return this
}

fun View.applyCornerRadius(cornerRadiusDp: Float) {
    if (cornerRadiusDp <= 0f) {
        clipToOutline = false
        outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
        return
    }
    val radiusPx = dp2px(cornerRadiusDp).toFloat()
    clipToOutline = true
    outlineProvider = object : android.view.ViewOutlineProvider() {
        override fun getOutline(view: View, outline: android.graphics.Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, radiusPx)
        }
    }
    if (width > 0 && height > 0) {
        invalidateOutline()
    }
}