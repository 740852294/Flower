package com.flower.flow.ui.binder

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.flower.flow.app.App
import com.flower.flow.app.core.ext.loadImage
import com.flower.flow.app.core.ext.setImageAnimationRunning
import com.flower.flow.data.model.entity.TemplateItem
import com.flower.flow.data.model.entity.TopicItem
import com.flower.flow.databinding.LayoutItemTopicLeftBinding
import com.flower.flow.databinding.LayoutItemTopicRightBinding
import kotlin.math.roundToInt

internal data class TopicItemViewRefs(
    val tvName: TextView,
    val tvDesc: TextView,
    val clTemple1: ConstraintLayout,
    val ivCover1: ImageView,
    val llLockBadge: View,
    val tvLockIntegral: TextView,
    val ivCover2: ImageView,
    val ivCover3: ImageView,
    val ivCover4: ImageView,
    val ivCover5: ImageView,
    val ivCover6: ImageView,
    val ivSampleSingle: ImageView,
    val llSampleBottom: LinearLayout,
    val ivSampleLeft: ImageView,
    val ivSampleRight: ImageView,
)

internal fun LayoutItemTopicLeftBinding.asTopicItemViewRefs() = TopicItemViewRefs(
    tvName = tvName,
    tvDesc = tvDesc,
    clTemple1 = clTemple1,
    ivCover1 = ivCover1,
    llLockBadge = llLockBadge,
    tvLockIntegral = tvLockIntegral,
    ivCover2 = ivCover2,
    ivCover3 = ivCover3,
    ivCover4 = ivCover4,
    ivCover5 = ivCover5,
    ivCover6 = ivCover6,
    ivSampleSingle = ivSampleSingle,
    llSampleBottom = llSampleBottom,
    ivSampleLeft = ivSampleLeft,
    ivSampleRight = ivSampleRight,
)

internal fun LayoutItemTopicRightBinding.asTopicItemViewRefs() = TopicItemViewRefs(
    tvName = tvName,
    tvDesc = tvDesc,
    clTemple1 = clTemple1,
    ivCover1 = ivCover1,
    llLockBadge = llLockBadge,
    tvLockIntegral = tvLockIntegral,
    ivCover2 = ivCover2,
    ivCover3 = ivCover3,
    ivCover4 = ivCover4,
    ivCover5 = ivCover5,
    ivCover6 = ivCover6,
    ivSampleSingle = ivSampleSingle,
    llSampleBottom = llSampleBottom,
    ivSampleLeft = ivSampleLeft,
    ivSampleRight = ivSampleRight,
)

internal fun LayoutItemTopicLeftBinding.bindTopicItem(
    model: TopicItem,
    onFinalImageSet: (ImageView) -> Unit,
) {
    bindTopicItem(asTopicItemViewRefs(), model, onFinalImageSet)
}

internal fun LayoutItemTopicRightBinding.bindTopicItem(
    model: TopicItem,
    onFinalImageSet: (ImageView) -> Unit,
) {
    bindTopicItem(asTopicItemViewRefs(), model, onFinalImageSet)

    fun Int.dpToPx(): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        toFloat(),
        root.resources.displayMetrics,
    ).roundToInt()

    ivCover2.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        marginEnd = if (ivCover3.isVisible) 16.dpToPx() else 0
    }
    clTemple1.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        marginEnd = when {
            ivCover3.isVisible -> 30.dpToPx()
            ivCover2.isVisible -> 16.dpToPx()
            else -> 0
        }
    }
}

internal fun LayoutItemTopicLeftBinding.setTopicItemAnimationsRunning(running: Boolean) {
    asTopicItemViewRefs().setAnimationsRunning(running)
}

internal fun LayoutItemTopicRightBinding.setTopicItemAnimationsRunning(running: Boolean) {
    asTopicItemViewRefs().setAnimationsRunning(running)
}

internal fun bindTopicItem(
    refs: TopicItemViewRefs,
    model: TopicItem,
    onFinalImageSet: (ImageView) -> Unit,
) {
    refs.tvName.text = model.dazzledeacon
    refs.tvDesc.text = model.bequeathconclave

    val modelList = model.blessameba
    val model1 = modelList?.getOrNull(0)
    if (model1 != null) {
        refs.clTemple1.isVisible = true
        refs.ivCover1.loadImage(
            url = model1.bullmind,
            isAutoPlay = true,
            onFinalImageSet = onFinalImageSet,
        )
        val showCost = model1.peacearrow > 0
        val showConfig = (App.globalConfig?.disposenovel ?: 0) in arrayOf(1, 3)
        refs.llLockBadge.isVisible = showCost && showConfig
        refs.tvLockIntegral.text = model1.peacearrow.toString()
        bindSampleImages(refs, model1.neverchapter, onFinalImageSet)
    } else {
        refs.clTemple1.isVisible = false
    }

    bindOptionalCover(
        refs.ivCover2,
        modelList?.getOrNull(1),
        onFinalImageSet = onFinalImageSet,
    )
    bindOptionalCover(
        refs.ivCover3,
        modelList?.getOrNull(2),
        onFinalImageSet = onFinalImageSet,
    )
    bindOptionalCover(
        refs.ivCover4,
        modelList?.getOrNull(3),
        isAutoPlay = false,
    )
    bindOptionalCover(
        refs.ivCover5,
        modelList?.getOrNull(4),
        isAutoPlay = false,
    )
    bindOptionalCover(
        refs.ivCover6,
        modelList?.getOrNull(5),
        isAutoPlay = false,
    )
}

private fun bindOptionalCover(
    imageView: ImageView,
    template: TemplateItem?,
    onFinalImageSet: ((ImageView) -> Unit)? = null,
    isAutoPlay: Boolean = true,
) {
    if (template != null) {
        imageView.isVisible = true
        imageView.loadImage(
            url = template.bullmind,
            isAutoPlay = isAutoPlay,
            onFinalImageSet = onFinalImageSet,
        )
    } else {
        imageView.isVisible = false
    }
}

private fun bindSampleImages(
    refs: TopicItemViewRefs,
    samples: List<String>?,
    onFinalImageSet: (ImageView) -> Unit,
) {
    refs.ivSampleSingle.isVisible = false
    refs.llSampleBottom.isVisible = false
    when {
        samples.isNullOrEmpty() -> return
        samples.size == 1 -> {
            refs.ivSampleSingle.isVisible = true
            refs.ivSampleSingle.loadImage(
                url = samples.first(),
                isAutoPlay = true,
                onFinalImageSet = onFinalImageSet,
            )
        }

        else -> {
            refs.llSampleBottom.isVisible = true
            refs.ivSampleLeft.loadImage(
                url = samples[0],
                isAutoPlay = true,
                onFinalImageSet = onFinalImageSet,
            )
            refs.ivSampleRight.loadImage(
                url = samples.getOrNull(1),
                isAutoPlay = true,
                onFinalImageSet = onFinalImageSet,
            )
        }
    }
}

private fun TopicItemViewRefs.setAnimationsRunning(running: Boolean) {
    ivCover1.setImageAnimationRunning(running)
    ivCover2.setImageAnimationRunning(running)
    ivCover3.setImageAnimationRunning(running)
    if (!running) {
        ivCover4.setImageAnimationRunning(false)
        ivCover5.setImageAnimationRunning(false)
        ivCover6.setImageAnimationRunning(false)
        ivSampleSingle.setImageAnimationRunning(false)
        ivSampleLeft.setImageAnimationRunning(false)
        ivSampleRight.setImageAnimationRunning(false)
    }
}
