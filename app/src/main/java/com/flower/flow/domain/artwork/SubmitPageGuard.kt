package com.flower.flow.domain.artwork

import com.flower.flow.app.core.util.GenerateSubmitCache
import com.flower.flow.data.model.entity.SubmitPageInfo

/**
 * 生成前校验流水线：重复上传 → 每日免费提示 → 积分消耗确认。
 * 仅产出下一步动作，不持有 UI 或网络依赖。
 */
object SubmitPageGuard {

    sealed class PreCheckOutcome {
        data object Proceed : PreCheckOutcome()

        /** 阻断提交，不展示弹窗（如每日免费限制且无文案） */
        data object Block : PreCheckOutcome()

        data class Prompt(
            val title: String,
            val content: String,
            val kind: PromptKind,
        ) : PreCheckOutcome()
    }

    enum class PromptKind {
        /** 重复上传：取消 / 确认后继续后续校验 */
        RepeatUpload,
        /** 每日免费：仅确认，不继续提交 */
        FreeDailyNotice,
        /** 积分消耗：取消 / 确认后提交 */
        IntegralConsume,
    }

    fun evaluateNewUpload(
        pageInfo: SubmitPageInfo,
        templateId: Int,
        sourcePaths: List<String>,
    ): PreCheckOutcome {
        if (
            GenerateSubmitCache.isRepeatCheckEnabled(pageInfo) &&
            GenerateSubmitCache.isDuplicateUpload(templateId, sourcePaths)
        ) {
            buildRepeatPrompt(pageInfo)?.let { return it }
        }
        return evaluatePostRepeat(pageInfo)
    }

    fun evaluateRegenerate(pageInfo: SubmitPageInfo): PreCheckOutcome {
        return evaluatePostRepeat(pageInfo)
    }

    private fun evaluatePostRepeat(pageInfo: SubmitPageInfo): PreCheckOutcome {
        if (pageInfo.valuefunny) {
            return buildFreeDailyPrompt(pageInfo) ?: PreCheckOutcome.Block
        }
        return evaluateIntegral(pageInfo)
    }

    private fun evaluateIntegral(pageInfo: SubmitPageInfo): PreCheckOutcome {
        if (!pageInfo.consider) {
            return PreCheckOutcome.Proceed
        }
        return buildIntegralPrompt(pageInfo) ?: PreCheckOutcome.Proceed
    }

    private fun buildRepeatPrompt(pageInfo: SubmitPageInfo): PreCheckOutcome.Prompt? {
        val title = pageInfo.hatchampion
        val content = pageInfo.tryamount
        if (title.isBlank() && content.isBlank()) return null
        val (resolvedTitle, resolvedContent) = when {
            title.isNotBlank() && content.isNotBlank() -> title to content
            title.isNotBlank() -> title to title
            else -> content to content
        }
        return PreCheckOutcome.Prompt(
            title = resolvedTitle,
            content = resolvedContent,
            kind = PromptKind.RepeatUpload,
        )
    }

    private fun buildFreeDailyPrompt(pageInfo: SubmitPageInfo): PreCheckOutcome.Prompt? {
        val title = pageInfo.foolcyst.ifBlank { pageInfo.apieceasteroid }
        val content = pageInfo.apieceasteroid.ifBlank { pageInfo.foolcyst }
        if (title.isBlank()) return null
        return PreCheckOutcome.Prompt(
            title = title,
            content = content,
            kind = PromptKind.FreeDailyNotice,
        )
    }

    private fun buildIntegralPrompt(pageInfo: SubmitPageInfo): PreCheckOutcome.Prompt? {
        val title = pageInfo.ecologybestial.ifBlank { pageInfo.hirenoun }
        val content = pageInfo.hirenoun.ifBlank { pageInfo.ecologybestial }
        if (title.isBlank()) return null
        return PreCheckOutcome.Prompt(
            title = title,
            content = content,
            kind = PromptKind.IntegralConsume,
        )
    }
}
