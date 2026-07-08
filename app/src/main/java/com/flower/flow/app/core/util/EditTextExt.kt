package com.flower.flow.app.core.util

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.widget.EditText

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
