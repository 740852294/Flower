package com.flower.flow.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.flower.flow.R
import com.flower.flow.data.model.entity.WorkGenerateResult
import com.flower.flow.databinding.DialogGenerateResultBinding
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat

class GenerateResultDialog private constructor(
    private val activity: FragmentActivity,
    private val result: WorkGenerateResult,
    private val config: DialogConfig,
    private val onConfirm: () -> Unit,
    private val onCancel: (() -> Unit)?,
    private val cancelButtonText: CharSequence?,
) : Dialog(activity, R.style.CommonMessageDialog) {

    private val binding = DialogGenerateResultBinding.inflate(activity.layoutInflater)

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        bindContent()
    }

    override fun onStart() {
        super.onStart()
        val screenWidth = activity.resources.displayMetrics.widthPixels
        val dialogWidth = if (
            activity.resources.configuration.smallestScreenWidthDp >= LARGE_SCREEN_MIN_WIDTH_DP
        ) {
            screenWidth - activity.dpToPx(LARGE_SCREEN_HORIZONTAL_PADDING_DP) * 2
        } else {
            (screenWidth * SMALL_SCREEN_WIDTH_RATIO).toInt()
        }
        window?.apply {
            setGravity(Gravity.CENTER)
            setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun show() {
        if (activity.isFinishing || activity.isDestroyed) return
        activity.lifecycle.addObserver(lifecycleObserver)
        super.show()
    }

    override fun dismiss() {
        activity.lifecycle.removeObserver(lifecycleObserver)
        super.dismiss()
    }

    private fun bindContent() = with(binding) {
        title.text = result.popupTitle
        title.isVisible = result.popupTitle.isNotBlank()

        icon.setImageResource(config.iconRes)
        icon.updateLayoutParams {
            width = activity.dpToPx(config.iconWidthDp)
            height = activity.dpToPx(config.iconHeightDp)
        }

        timeContainer.isVisible = config.showTimeMsg && result.popupTimeMsg.isNotBlank()
        timeMsg.text = result.popupTimeMsg

        desc.text = result.popupDescMsg
        desc.isVisible = result.popupDescMsg.isNotBlank()

        confirmButton.text = result.popupButtonMsg
        confirmButton.isVisible = result.popupButtonMsg.isNotBlank()
        confirmButton.clickNoRepeat {
            dismiss()
            onConfirm()
        }

        cancelButton.isVisible = config.showCancelButton && onCancel != null
        if (cancelButton.isVisible) {
            cancelButton.text = cancelButtonText ?: ""
            cancelButton.clickNoRepeat {
                dismiss()
                onCancel?.invoke()
            }
        }
    }

    private fun FragmentActivity.dpToPx(value: Int): Int {
        return (value * resources.displayMetrics.density + 0.5f).toInt()
    }

    data class DialogConfig(
        @DrawableRes val iconRes: Int,
        val iconWidthDp: Int,
        val iconHeightDp: Int,
        val showTimeMsg: Boolean,
        val showCancelButton: Boolean,
    )

    class Builder(private val activity: FragmentActivity) {
        private var result: WorkGenerateResult? = null
        private var onConfirm: (() -> Unit)? = null
        private var onCancel: (() -> Unit)? = null
        private var cancelButtonText: CharSequence? = null

        fun setResult(result: WorkGenerateResult) = apply {
            this.result = result
        }

        fun setOnConfirm(listener: () -> Unit) = apply {
            onConfirm = listener
        }

        fun setOnCancel(listener: () -> Unit) = apply {
            onCancel = listener
        }

        fun setCancelButtonText(text: CharSequence) = apply {
            cancelButtonText = text
        }

        fun show(): GenerateResultDialog? {
            val data = result ?: return null
            val config = configForState(data.state) ?: return null
            val confirm = onConfirm ?: return null
            return GenerateResultDialog(
                activity = activity,
                result = data,
                config = config,
                onConfirm = confirm,
                onCancel = if (config.showCancelButton) onCancel else null,
                cancelButtonText = cancelButtonText,
            ).also { it.show() }
        }

        companion object {
            fun configForState(state: Int): DialogConfig? {
                return when (state) {
                    WorkGenerateResult.STATE_WAITING -> DialogConfig(
                        iconRes = R.mipmap.ic_generate_result_status0,
                        iconWidthDp = 91,
                        iconHeightDp = 64,
                        showTimeMsg = true,
                        showCancelButton = false,
                    )

                    WorkGenerateResult.STATE_VIP_INTERCEPT -> DialogConfig(
                        iconRes = R.mipmap.ic_generate_result_status1,
                        iconWidthDp = 101,
                        iconHeightDp = 84,
                        showTimeMsg = false,
                        showCancelButton = true,
                    )

                    WorkGenerateResult.STATE_RECHARGE_INTERCEPT -> DialogConfig(
                        iconRes = R.mipmap.ic_generate_result_status2,
                        iconWidthDp = 74,
                        iconHeightDp = 74,
                        showTimeMsg = false,
                        showCancelButton = true,
                    )

                    else -> null
                }
            }
        }
    }

    private companion object {
        const val LARGE_SCREEN_MIN_WIDTH_DP = 600
        const val LARGE_SCREEN_HORIZONTAL_PADDING_DP = 40
        const val SMALL_SCREEN_WIDTH_RATIO = 0.8f
    }
}
