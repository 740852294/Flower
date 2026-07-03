package com.flower.flow.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.flower.flow.R
import com.flower.flow.databinding.DialogCommonMessageBinding
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat

class CommonMessageDialog private constructor(
    private val activity: FragmentActivity,
    private val titleText: CharSequence,
    private val contentText: CharSequence,
    private val cancelAction: Action?,
    private val confirmAction: Action?,
) : Dialog(activity, R.style.CommonMessageDialog) {

    private val binding = DialogCommonMessageBinding.inflate(activity.layoutInflater)

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
        title.text = titleText
        content.text = contentText

        title.isVisible = titleText.isNotBlank()
        content.isVisible = contentText.isNotBlank()
        viewSpace.isVisible = !titleText.isNotBlank()
        cancelButton.isVisible = cancelAction != null
        confirmButton.isVisible = confirmAction != null
        buttonSpace.isVisible = cancelAction != null && confirmAction != null

        cancelAction?.let { action ->
            cancelButton.text = action.text
            cancelButton.clickNoRepeat { performAction(action) }
        }
        confirmAction?.let { action ->
            confirmButton.text = action.text
            confirmButton.clickNoRepeat { performAction(action) }
        }
    }

    private fun performAction(action: Action) {
        if (action.dismissOnClick) dismiss()
        action.onClick(this)
    }

    private fun FragmentActivity.dpToPx(value: Int): Int {
        return (value * resources.displayMetrics.density + 0.5f).toInt()
    }

    private data class Action(
        val text: CharSequence,
        val dismissOnClick: Boolean,
        val onClick: (CommonMessageDialog) -> Unit,
    )

    class Builder(private val activity: FragmentActivity) {

        private var title: CharSequence = ""
        private var content: CharSequence = ""
        private var cancelAction: Action? = null
        private var confirmAction: Action? = null

        fun setTitle(title: CharSequence) = apply {
            this.title = title
        }

        fun setTitle(@StringRes titleRes: Int) = apply {
            title = activity.getText(titleRes)
        }

        fun setContent(content: CharSequence) = apply {
            this.content = content
        }

        fun setContent(@StringRes contentRes: Int) = apply {
            content = activity.getText(contentRes)
        }

        fun setCancelButton(
            text: CharSequence,
            dismissOnClick: Boolean = true,
            onClick: (CommonMessageDialog) -> Unit = {},
        ) = apply {
            cancelAction = Action(text, dismissOnClick, onClick)
        }

        fun setCancelButton(
            @StringRes textRes: Int,
            dismissOnClick: Boolean = true,
            onClick: (CommonMessageDialog) -> Unit = {},
        ) = setCancelButton(activity.getText(textRes), dismissOnClick, onClick)

        fun setConfirmButton(
            text: CharSequence,
            dismissOnClick: Boolean = true,
            onClick: (CommonMessageDialog) -> Unit = {},
        ) = apply {
            confirmAction = Action(text, dismissOnClick, onClick)
        }

        fun setConfirmButton(
            @StringRes textRes: Int,
            dismissOnClick: Boolean = true,
            onClick: (CommonMessageDialog) -> Unit = {},
        ) = setConfirmButton(activity.getText(textRes), dismissOnClick, onClick)

        fun build(): CommonMessageDialog {
            return CommonMessageDialog(
                activity = activity,
                titleText = title,
                contentText = content,
                cancelAction = cancelAction,
                confirmAction = confirmAction,
            )
        }

        fun show(): CommonMessageDialog = build().also(CommonMessageDialog::show)
    }

    private companion object {
        const val LARGE_SCREEN_MIN_WIDTH_DP = 600
        const val LARGE_SCREEN_HORIZONTAL_PADDING_DP = 40
        const val SMALL_SCREEN_WIDTH_RATIO = 0.8f
    }
}
