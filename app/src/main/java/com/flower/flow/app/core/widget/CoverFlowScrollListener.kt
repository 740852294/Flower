package com.flower.flow.app.core.widget

import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class CoverFlowScrollListener(
    private val maxRotation: Float = MAX_ROTATION,
    private val minScale: Float = MIN_SCALE,
) : RecyclerView.OnScrollListener() {

    var itemStep: Float = 0f

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        applyTransform(recyclerView)
    }

    fun applyTransform(recyclerView: RecyclerView) {
        if (recyclerView.width <= 0) return

        val rvCenter = recyclerView.width / 2f
        val density = recyclerView.resources.displayMetrics.density
        val step = itemStep.takeIf { it > 0f }
            ?: recyclerView.getChildAt(0)?.width?.times(DEFAULT_STEP_RATIO)
            ?: return

        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            val childCenter = (child.left + child.right) / 2f
            val distance = childCenter - rvCenter
            val offset = (distance / step).coerceIn(-2f, 2f)
            val absOffset = abs(offset).coerceIn(0f, 1f)

            val scale = minScale + (1f - minScale) * (1f - absOffset)
            child.scaleX = scale
            child.scaleY = scale

            // The side cards in the design lean outwards on the screen plane.
            // A Y-axis rotation makes them look like narrow vertical slivers instead.
            child.pivotX = child.width / 2f
            child.pivotY = child.height / 2f
            child.rotationY = 0f
            child.rotation = offset.coerceIn(-1f, 1f) * maxRotation

            child.elevation = (1f - absOffset) * MAX_ELEVATION_DP * density
            child.alpha = when {
                abs(offset) > 1.35f -> 0f
                abs(offset) > 1f -> 1f - (abs(offset) - 1f) / 0.35f
                else -> 1f
            }
        }
    }

    companion object {
        private const val MAX_ROTATION = 3f
        private const val MIN_SCALE = 0.86f
        private const val MAX_ELEVATION_DP = 16f
        private const val DEFAULT_STEP_RATIO = 0.18f

        fun setupDrawingOrder(recyclerView: RecyclerView) {
            recyclerView.setChildDrawingOrderCallback { childCount, drawingPosition ->
                val centerX = recyclerView.width / 2
                val order = (0 until childCount).sortedByDescending { index ->
                    val child = recyclerView.getChildAt(index)
                    abs((child.left + child.right) / 2 - centerX)
                }
                order[drawingPosition]
            }
        }
    }
}
