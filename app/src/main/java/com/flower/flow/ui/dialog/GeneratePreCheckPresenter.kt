package com.flower.flow.ui.dialog

import androidx.fragment.app.FragmentActivity
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.data.model.StringResId
import com.flower.flow.domain.artwork.SubmitPageGuard

/**
 * 将 [SubmitPageGuard] 的校验结果映射为弹窗交互。
 */
class GeneratePreCheckPresenter(
    private val host: FragmentActivity,
) {

    fun runUploadPipeline(
        pageInfo: com.flower.flow.data.model.entity.SubmitPageInfo,
        templateId: Int,
        sourcePaths: List<String>,
        onProceed: () -> Unit,
    ) {
        dispatch(
            outcome = SubmitPageGuard.evaluateNewUpload(pageInfo, templateId, sourcePaths),
            onProceed = onProceed,
            onRepeatConfirmed = {
                runPostRepeat(pageInfo, onProceed)
            },
        )
    }

    fun runRegeneratePipeline(
        pageInfo: com.flower.flow.data.model.entity.SubmitPageInfo,
        onProceed: () -> Unit,
    ) {
        dispatch(
            outcome = SubmitPageGuard.evaluateRegenerate(pageInfo),
            onProceed = onProceed,
            onRepeatConfirmed = onProceed,
        )
    }

    private fun runPostRepeat(
        pageInfo: com.flower.flow.data.model.entity.SubmitPageInfo,
        onProceed: () -> Unit,
    ) {
        dispatch(
            outcome = SubmitPageGuard.evaluateRegenerate(pageInfo),
            onProceed = onProceed,
            onRepeatConfirmed = onProceed,
        )
    }

    private fun dispatch(
        outcome: SubmitPageGuard.PreCheckOutcome,
        onProceed: () -> Unit,
        onRepeatConfirmed: () -> Unit,
    ) {
        when (outcome) {
            SubmitPageGuard.PreCheckOutcome.Proceed -> onProceed()
            SubmitPageGuard.PreCheckOutcome.Block -> Unit
            is SubmitPageGuard.PreCheckOutcome.Prompt -> showPrompt(outcome, onProceed, onRepeatConfirmed)
        }
    }

    private fun showPrompt(
        prompt: SubmitPageGuard.PreCheckOutcome.Prompt,
        onProceed: () -> Unit,
        onRepeatConfirmed: () -> Unit,
    ) {
        val builder = CommonMessageDialog.Builder(host)
            .setTitle(prompt.title)
            .setContent(prompt.content)

        when (prompt.kind) {
            SubmitPageGuard.PromptKind.RepeatUpload -> {
                builder
                    .setCancelButton(AppStrings.get(StringResId.CANCEL_ACTION))
                    .setConfirmButton(AppStrings.get(StringResId.CONFIRM_ACTION)) {
                        onRepeatConfirmed()
                    }
            }

            SubmitPageGuard.PromptKind.FreeDailyNotice -> {
                builder.setConfirmButton(AppStrings.get(StringResId.ROGER_ACTION))
            }

            SubmitPageGuard.PromptKind.IntegralConsume -> {
                builder
                    .setCancelButton(AppStrings.get(StringResId.CANCEL_ACTION))
                    .setConfirmButton(AppStrings.get(StringResId.CONFIRM_ACTION)) {
                        onProceed()
                    }
            }
        }
        builder.show()
    }
}
