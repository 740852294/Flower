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
import com.flower.flow.R
import com.flower.flow.app.App
import com.flower.flow.app.core.ext.clearGlideImage
import com.flower.flow.app.core.ext.loadGlideImage
import com.flower.flow.app.core.ext.setGlideAnimationRunning
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
) {
    bindTopicItem(asTopicItemViewRefs(), model)
}

internal fun LayoutItemTopicRightBinding.bindTopicItem(
    model: TopicItem,
) {
    bindTopicItem(asTopicItemViewRefs(), model)

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

internal fun bindTopicItem(
    refs: TopicItemViewRefs,
    model: TopicItem,
) {
    refs.tvName.text = model.dazzledeacon
    refs.tvDesc.text = model.bequeathconclave

    val modelList = model.blessameba
    val model1 = modelList?.getOrNull(0)
    if (model1 != null) {
        refs.clTemple1.isVisible = true
        refs.ivCover1.loadGlideImage(model1.bullmind)
        val showCost = model1.peacearrow > 0
        val showConfig = (App.globalConfig?.disposenovel ?: 0) in arrayOf(1, 3)
        refs.llLockBadge.isVisible = showCost && showConfig
        refs.tvLockIntegral.text = model1.peacearrow.toString()
        bindSampleImages(refs, model1.neverchapter)
    } else {
        refs.clTemple1.isVisible = false
        refs.ivCover1.clearGlideImage()
    }

    bindOptionalCover(
        refs.ivCover2,
        modelList?.getOrNull(1),
    )
    bindOptionalCover(
        refs.ivCover3,
        modelList?.getOrNull(2),
    )
    bindOptionalCover(
        refs.ivCover4,
        modelList?.getOrNull(3),
        autoPlay = false,
    )
    bindOptionalCover(
        refs.ivCover5,
        modelList?.getOrNull(4),
        autoPlay = false,
    )
    bindOptionalCover(
        refs.ivCover6,
        modelList?.getOrNull(5),
        autoPlay = false,
    )
}

private fun bindOptionalCover(
    imageView: ImageView,
    template: TemplateItem?,
    autoPlay: Boolean = true,
) {
    if (template != null) {
        imageView.isVisible = true
        imageView.loadGlideImage(template.bullmind, autoPlay = autoPlay)
    } else {
        imageView.isVisible = false
        imageView.clearGlideImage()
    }
}

private fun bindSampleImages(
    refs: TopicItemViewRefs,
    samples: List<String>?,
) {
    refs.ivSampleSingle.isVisible = false
    refs.llSampleBottom.isVisible = false
    when {
        samples.isNullOrEmpty() -> {
            refs.ivSampleSingle.clearGlideImage()
            refs.ivSampleLeft.clearGlideImage()
            refs.ivSampleRight.clearGlideImage()
            return
        }
        samples.size == 1 -> {
            refs.ivSampleSingle.isVisible = true
            refs.ivSampleSingle.loadGlideImage(samples.first())
            refs.ivSampleLeft.clearGlideImage()
            refs.ivSampleRight.clearGlideImage()
        }

        else -> {
            refs.llSampleBottom.isVisible = true
            refs.ivSampleSingle.clearGlideImage()
            refs.ivSampleLeft.loadGlideImage(samples[0])
            refs.ivSampleRight.loadGlideImage(samples.getOrNull(1))
        }
    }
}

internal fun View.clearTopicItemImages() {
    forEachTopicImageView { imageView ->
        imageView.clearGlideImage()
    }
}

internal fun View.setTopicItemAnimationsRunning(running: Boolean) {
    forEachTopicImageView { imageView ->
        imageView.setGlideAnimationRunning(running)
    }
}

private fun View.forEachTopicImageView(action: (ImageView) -> Unit) {
    TOPIC_IMAGE_VIEW_IDS.forEach { id ->
        findViewById<ImageView>(id)?.let(action)
    }
}

private val TOPIC_IMAGE_VIEW_IDS = intArrayOf(
    R.id.ivCover1,
    R.id.ivCover2,
    R.id.ivCover3,
    R.id.ivCover4,
    R.id.ivCover5,
    R.id.ivCover6,
    R.id.ivSampleSingle,
    R.id.ivSampleLeft,
    R.id.ivSampleRight,
)
