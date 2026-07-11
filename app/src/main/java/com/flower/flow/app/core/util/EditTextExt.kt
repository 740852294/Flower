package com.flower.flow.app.core.util

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import kotlin.math.max

/** Lets a scrollable multiline EditText handle vertical gestures inside a scrolling parent. */
@SuppressLint("ClickableViewAccessibility")
fun EditText.enableNestedVerticalScrolling() {
    setOnTouchListener { view, event ->
        val editText = view as EditText
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE
            -> {
                val contentCanScroll = editText.canScrollVertically(-1) ||
                    editText.canScrollVertically(1)
                editText.parent.requestDisallowInterceptTouchEvent(contentCanScroll)
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL
            -> editText.parent.requestDisallowInterceptTouchEvent(false)
        }
        false
    }
}

fun AppCompatActivity.enableKeyboardAvoidance(
    root: View,
    scrollView: NestedScrollView,
    vararg inputViews: View,
) {
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    root.post {
        val initialBottomPadding = root.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            view.updatePadding(bottom = initialBottomPadding + max(systemBars.bottom, ime.bottom))
            if (imeVisible) {
                scrollView.post { scrollView.scrollFocusedInputIntoView() }
            }
            WindowInsetsCompat.CONSUMED
        }

        inputViews.forEach { inputView ->
            inputView.setOnFocusChangeListener { focusedView, hasFocus ->
                if (hasFocus) {
                    scrollView.postDelayed(
                        { scrollView.scrollChildFullyIntoView(focusedView) },
                        KEYBOARD_SCROLL_DELAY_MS,
                    )
                }
            }
        }
        ViewCompat.requestApplyInsets(root)
    }
}

private fun NestedScrollView.scrollFocusedInputIntoView() {
    val focusedView = findFocus() ?: return
    if (focusedView.isDescendantOf(this)) {
        scrollChildFullyIntoView(focusedView)
    }
}

private fun NestedScrollView.scrollChildFullyIntoView(child: View) {
    val childRect = Rect()
    child.getDrawingRect(childRect)
    offsetDescendantRectToMyCoords(child, childRect)

    val margin = (FOCUSED_INPUT_MARGIN_DP * resources.displayMetrics.density + 0.5f).toInt()
    val viewportTop = scrollY
    val viewportBottom = scrollY + height - paddingBottom
    val targetTop = childRect.top - margin
    val targetBottom = childRect.bottom + margin
    val dy = when {
        targetBottom > viewportBottom -> targetBottom - viewportBottom
        targetTop < viewportTop -> targetTop - viewportTop
        else -> 0
    }
    if (dy != 0) {
        smoothScrollBy(0, dy)
    }
}

private fun View.isDescendantOf(parentView: View): Boolean {
    var currentView: View? = this
    while (currentView != null) {
        if (currentView == parentView) {
            return true
        }
        currentView = currentView.parent as? View
    }
    return false
}

private const val KEYBOARD_SCROLL_DELAY_MS = 250L
private const val FOCUSED_INPUT_MARGIN_DP = 16
