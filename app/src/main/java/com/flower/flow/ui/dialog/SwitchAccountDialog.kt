package com.flower.flow.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.flower.flow.R
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.databinding.DialogSwitchAccountBinding
import com.flower.flow.data.model.StringResId
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.toast

class SwitchAccountDialog private constructor(
    private val activity: FragmentActivity,
    private val onSwitchAccount: (account: String, password: String) -> Unit,
) : Dialog(activity, R.style.CommonMessageDialog) {

    private val binding = DialogSwitchAccountBinding.inflate(activity.layoutInflater)

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
        title.text = AppStrings.get(StringResId.ACCOUNT_SWITCH)
        accountEditText.hint = AppStrings.get(StringResId.ACCOUNT_INPUT_HINT)
        passwordEditText.hint = AppStrings.get(StringResId.PASSWORD_INPUT_HINT)
        confirmButton.text = AppStrings.get(StringResId.CONFIRM_ACTION)
        cancelButton.text = AppStrings.get(StringResId.CANCEL_ACTION)

        cancelButton.clickNoRepeat { dismiss() }
        confirmButton.clickNoRepeat {
            val account = accountEditText.text?.toString()?.trim().orEmpty()
            val password = passwordEditText.text?.toString()?.trim().orEmpty()
            if (account.isEmpty()) {
                AppStrings.get(StringResId.ACCOUNT_INPUT_HINT).toast()
                return@clickNoRepeat
            }
            if (password.isEmpty()) {
                AppStrings.get(StringResId.PASSWORD_INPUT_HINT).toast()
                return@clickNoRepeat
            }
            dismiss()
            onSwitchAccount(account, password)
        }
    }

    private fun FragmentActivity.dpToPx(value: Int): Int {
        return (value * resources.displayMetrics.density + 0.5f).toInt()
    }

    class Builder(private val activity: FragmentActivity) {

        private var onSwitchAccount: (account: String, password: String) -> Unit = { _, _ -> }

        fun setOnSwitchAccount(
            onSwitchAccount: (account: String, password: String) -> Unit,
        ) = apply {
            this.onSwitchAccount = onSwitchAccount
        }

        fun build(): SwitchAccountDialog {
            return SwitchAccountDialog(activity, onSwitchAccount)
        }

        fun show(): SwitchAccountDialog = build().also(SwitchAccountDialog::show)
    }

    private companion object {
        const val LARGE_SCREEN_MIN_WIDTH_DP = 600
        const val LARGE_SCREEN_HORIZONTAL_PADDING_DP = 40
        const val SMALL_SCREEN_WIDTH_RATIO = 0.8f
    }
}
