package com.flower.flow.app.core.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.scwang.smart.refresh.layout.api.RefreshFooter
import com.scwang.smart.refresh.layout.api.RefreshKernel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.constant.SpinnerStyle

@SuppressLint("RestrictedApi")
class ProgressOnlyFooter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs), RefreshFooter {

    private val progressBar: ProgressBar

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        minimumHeight = dp(60)

        progressBar = ProgressBar(context).apply {
            layoutParams = LayoutParams(dp(28), dp(28))
            visibility = GONE
        }

        addView(progressBar)
    }

    override fun getView(): View = this

    override fun getSpinnerStyle(): SpinnerStyle {
        return SpinnerStyle.Translate
    }

    override fun setPrimaryColors(vararg colors: Int) {
        progressBar.indeterminateTintList = colors.firstOrNull()?.let {
            ColorStateList.valueOf(it)
        }
    }

    override fun onInitialized(
        kernel: RefreshKernel,
        height: Int,
        maxDragHeight: Int
    ) {
    }

    override fun onMoving(
        isDragging: Boolean,
        percent: Float,
        offset: Int,
        height: Int,
        maxDragHeight: Int
    ) {
        if (offset > 0) {
            visibility = VISIBLE
            progressBar.visibility = VISIBLE
        }
    }

    override fun onReleased(
        refreshLayout: RefreshLayout,
        height: Int,
        maxDragHeight: Int
    ) {
    }

    override fun onStartAnimator(
        refreshLayout: RefreshLayout,
        height: Int,
        maxDragHeight: Int
    ) {
        visibility = VISIBLE
        progressBar.visibility = VISIBLE
    }

    override fun onFinish(refreshLayout: RefreshLayout, success: Boolean): Int {
        progressBar.visibility = GONE
        return 0
    }

    override fun onStateChanged(
        refreshLayout: RefreshLayout,
        oldState: RefreshState,
        newState: RefreshState
    ) {
        when (newState) {
            RefreshState.PullUpToLoad,
            RefreshState.LoadReleased,
            RefreshState.Loading -> {
                visibility = VISIBLE
                progressBar.visibility = VISIBLE
            }

            else -> {
                progressBar.visibility = GONE
            }
        }
    }

    override fun setNoMoreData(noMoreData: Boolean): Boolean {
        progressBar.visibility = GONE
        visibility = if (noMoreData) GONE else VISIBLE
        return true
    }

    override fun autoOpen(
        duration: Int,
        dragRate: Float,
        animationOnly: Boolean
    ): Boolean {
        return false
    }

    override fun isSupportHorizontalDrag(): Boolean {
        return false
    }

    override fun onHorizontalDrag(
        percentX: Float,
        offsetX: Int,
        offsetMax: Int
    ) {
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density + 0.5f).toInt()
    }
}