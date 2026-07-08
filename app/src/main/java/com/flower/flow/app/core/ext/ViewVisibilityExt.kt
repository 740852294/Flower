package com.flower.flow.app.core.ext

import android.graphics.Rect
import android.view.View

/** Returns true only when part of this view is currently visible within all parent clips. */
fun View.isVisibleOnScreen(): Boolean {
    if (!isShown || width <= 0 || height <= 0) return false
    val visibleRect = Rect()
    return getGlobalVisibleRect(visibleRect) &&
        visibleRect.width() > 0 &&
        visibleRect.height() > 0
}
