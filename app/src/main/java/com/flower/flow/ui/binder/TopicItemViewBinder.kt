package com.flower.flow.ui.binder

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.flower.flow.app.App
import com.flower.flow.app.core.ext.loadImage
import com.flower.flow.data.model.entity.TopicItem
import com.flower.flow.databinding.LayoutItemTopicLeftBinding
import com.flower.flow.databinding.LayoutItemTopicRightBinding

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

internal fun LayoutItemTopicLeftBinding.bindTopicItem(model: TopicItem) {
    bindTopicItem(asTopicItemViewRefs(), model)
}

internal fun LayoutItemTopicRightBinding.bindTopicItem(model: TopicItem) {
    bindTopicItem(asTopicItemViewRefs(), model)
}

internal fun bindTopicItem(refs: TopicItemViewRefs, model: TopicItem) {
    refs.tvName.text = model.name
    refs.tvDesc.text = model.description

    val modelList = model.aiartList
    val model1 = modelList?.getOrNull(0)
    if (model1 != null) {
        refs.clTemple1.isVisible = true
        refs.ivCover1.loadImage(
            url = model1.img,
            cornerRadiusDp = 10f,
            borderWidthDp = 1f,
        )
        val showCost = model1.lockIntegral > 0 && model1.lockType != 0
        val showConfig = (App.globalConfig?.templateAbduceIntegralShow ?: 0) in arrayOf(1, 3)
        refs.llLockBadge.isVisible = showCost && showConfig
        refs.tvLockIntegral.text = model1.lockIntegral.toString()
        bindSampleImages(refs, model1.sampleImgList)
    } else {
        refs.clTemple1.isVisible = false
    }

    bindOptionalCover(refs.ivCover2, modelList?.getOrNull(1), cornerRadiusDp = 10f)
    bindOptionalCover(refs.ivCover3, modelList?.getOrNull(2), cornerRadiusDp = 10f)
    bindOptionalCover(
        refs.ivCover4,
        modelList?.getOrNull(3),
        cornerRadiusDp = 16f,
        borderColor = Color.BLACK,
        isAutoPlay = false,
    )
    bindOptionalCover(
        refs.ivCover5,
        modelList?.getOrNull(4),
        cornerRadiusDp = 16f,
        borderColor = Color.BLACK,
        isAutoPlay = false,
    )
    bindOptionalCover(
        refs.ivCover6,
        modelList?.getOrNull(5),
        cornerRadiusDp = 16f,
        borderColor = Color.BLACK,
        isAutoPlay = false,
    )
}

private fun bindOptionalCover(
    imageView: ImageView,
    template: com.flower.flow.data.model.entity.TemplateItem?,
    cornerRadiusDp: Float,
    borderColor: Int = Color.TRANSPARENT,
    isAutoPlay: Boolean = true,
) {
    if (template != null) {
        imageView.isVisible = true
        imageView.loadImage(
            url = template.img,
            cornerRadiusDp = cornerRadiusDp,
            borderWidthDp = 1f,
            borderColor = borderColor,
            isAutoPlay = isAutoPlay,
        )
    } else {
        imageView.isVisible = false
    }
}

private fun bindSampleImages(
    refs: TopicItemViewRefs,
    samples: List<String>?,
) {
    refs.ivSampleSingle.isVisible = false
    refs.llSampleBottom.isVisible = false
    when {
        samples.isNullOrEmpty() -> return
        samples.size == 1 -> {
            refs.ivSampleSingle.isVisible = true
            refs.ivSampleSingle.loadImage(
                url = samples.first(),
                cornerRadiusDp = 4f,
            )
        }

        else -> {
            refs.llSampleBottom.isVisible = true
            refs.ivSampleLeft.loadImage(
                url = samples[0],
                cornerRadiiDp = floatArrayOf(4f, 0f, 0f, 4f),
            )
            refs.ivSampleRight.loadImage(
                url = samples.getOrNull(1),
                cornerRadiiDp = floatArrayOf(0f, 4f, 4f, 0f),
            )
        }
    }
}
