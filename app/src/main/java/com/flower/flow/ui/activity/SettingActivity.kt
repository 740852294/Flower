package com.flower.flow.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import com.flower.flow.BuildConfig
import com.flower.flow.R
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.util.AesTextCodec
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.model.CacheConfig
import com.flower.flow.data.model.StringResId
import com.flower.flow.data.vm.SettingViewModel
import com.flower.flow.databinding.ActivitySettingBinding
import com.flower.flow.ui.activity.PrivacyActivity.PolicyRoute
import com.flower.flow.ui.dialog.ChangePasswordDialog
import com.flower.flow.ui.dialog.SwitchAccountDialog
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.copyToClipboard
import me.hgj.jetpackmvvm.ext.util.finishAllActivity
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.toast

class SettingActivity : BaseActivity<SettingViewModel, ActivitySettingBinding>() {
    private var passwordPlain: String = ""
    private var isShowPassword = false

    override fun initView(savedInstanceState: Bundle?) {
        setText()
        passwordPlain = decodePassword(UserManager.user?.colonybay.orEmpty())

        UserManager.user?.apply {
            val isShow = this.valleytoward
            mBind.redDot.isVisible = isShow
        }

        mBind.tvAppVersion.text = String.format("V%s", BuildConfig.VERSION_NAME)
        mBind.tvAccount.text = AesTextCodec.decode(CacheConfig.userId)
        mBind.tvPassword.text = "**********"
        mBind.ivPassword.setImageResource(R.mipmap.ic_setting_eyes)

        val isAccountAndPassword = (App.globalConfig?.deistdisturb ?: 0) == 1
        mBind.llAccountAndPassword.isVisible = isAccountAndPassword
        mBind.btnChange.isVisible = isAccountAndPassword
    }

    override fun onBindViewClick() {
        mBind.llShare.clickNoRepeat {
            mViewModel.getShareInfo().obs(this) {
                onSuccess { shareInfo ->
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TITLE, shareInfo.sevenasset)
                        putExtra(Intent.EXTRA_TEXT, shareInfo.imitaterise)
                    }
                    startActivity(Intent.createChooser(intent, shareInfo.sevenasset))
                }
                onError { error ->
                    error.msg.toast()
                }
            }
        }

        mBind.llFeedback.clickNoRepeat {
            openActivity<FeedbackActivity>()
        }

        mBind.llNotify.clickNoRepeat {
            openActivity<SystemNotifyActivity>()
        }

        mBind.llPrivacy.clickNoRepeat {
            openPolicyPage(PolicyRoute.PRIVACY.requestType)
        }

        mBind.llTerms.clickNoRepeat {
            openPolicyPage(PolicyRoute.TERMS.requestType)
        }

        mBind.llLanguage.clickNoRepeat {
            openActivity<LanguageSettingActivity>()
        }

        mBind.llPassword.clickNoRepeat {
            ChangePasswordDialog.Builder(this)
                .setOnChangePassword { password ->
                    mViewModel.updatePassword(password).obs(this) {
                        onSuccess {
                            passwordPlain = password
                            resetPasswordMask()
                            UserManager.saveUserPassword(password)
                        }
                        onError { error ->
                            error.msg.toast()
                        }
                    }
                }
                .show()
        }

        mBind.ivPassword.clickNoRepeat {
            if (isShowPassword) {
                isShowPassword = false
                mBind.tvPassword.text = "**********"
                mBind.ivPassword.setImageResource(R.mipmap.ic_setting_eyes)
                if (passwordPlain.isNotBlank()) {
                    copyToClipboard(passwordPlain, AppStrings.get(StringResId.PASSWORD_LABEL))
                    AppStrings.get(StringResId.COPY_ACTION).toast()
                }
            } else {
                isShowPassword = true
                mBind.tvPassword.text = passwordPlain
                mBind.ivPassword.setImageResource(R.mipmap.ic_setting_copy)
            }
        }

        mBind.ivAccount.clickNoRepeat {
            val uid = mBind.tvAccount.text?.toString().orEmpty()
            if (uid.isNotBlank()) {
                copyToClipboard(uid, AppStrings.get(StringResId.ACCOUNT_LABEL))
                AppStrings.get(StringResId.COPY_ACTION).toast()
            }
        }

        mBind.btnChange.clickNoRepeat {
            SwitchAccountDialog.Builder(this)
                .setOnSwitchAccount { account, password ->
                    mViewModel.switchAccount(account, password).obs(this) {
                        onSuccess { response ->
                            CacheConfig.userId = response.elephantfloat
                            UserManager.clearUser()
                            finishAllActivity()
                            openActivity<MainActivity>()
                        }
                        onError { error ->
                            error.msg.toast()
                        }
                    }
                }
                .show()
        }
    }

    override fun createObserver() {
        EventViewModel.msgRedDotEvent.observe(this) { show ->
            mBind.redDot.isVisible = show
        }

        EventViewModel.languageEvent.observe(this) {
            setText()
        }
    }

    private fun resetPasswordMask() {
        isShowPassword = false
        mBind.tvPassword.text = "**********"
        mBind.ivPassword.setImageResource(R.mipmap.ic_setting_eyes)
    }

    private fun decodePassword(encryptedPassword: String): String {
        if (encryptedPassword.isBlank()) return ""
        return AesTextCodec.decode(encryptedPassword).orEmpty()
    }

    private fun setText() {
        mToolbar.title = AppStrings.get(StringResId.SETTINGS_ENTRY)
        mBind.tvShare.text = AppStrings.get(StringResId.SHARE_ACTION)
        mBind.tvFeedback.text = AppStrings.get(StringResId.FEEDBACK_ENTRY)
        mBind.tvNotify.text = AppStrings.get(StringResId.SYSTEM_NOTICE)
        mBind.tvPrivacy.text = AppStrings.get(StringResId.PRIVACY_LINK)
        mBind.tvTerms.text = AppStrings.get(StringResId.TERMS_LINK)
        mBind.tvLanguage.text = AppStrings.get(StringResId.LANGUAGE_SETTING)
        mBind.tvVersion.text = AppStrings.get(StringResId.VERSION_LABEL)
        mBind.tvAccountLabel.text =
            String.format("%s：", AppStrings.get(StringResId.ACCOUNT_LABEL))
        mBind.tvPasswordLabel.text =
            String.format("%s：", AppStrings.get(StringResId.PASSWORD_LABEL))
        mBind.btnChange.text = AppStrings.get(StringResId.ACCOUNT_SWITCH)
    }

    private fun openPolicyPage(requestType: Int) {
        mViewModel.openPolicyPage(requestType).obs(this) {
            onSuccess { data ->
                if (data.cuspcrow.isEmpty()) {
                    AppStrings.get(StringResId.WEB_EMPTY_HINT).toast()
                } else {
                    val title = when (requestType) {
                        PolicyRoute.PRIVACY.requestType -> {
                            AppStrings.get(StringResId.PRIVACY_LINK)
                        }

                        PolicyRoute.TERMS.requestType -> {
                            AppStrings.get(StringResId.TERMS_LINK)
                        }

                        else -> {
                            ""
                        }
                    }
                    openActivity<WebviewActivity>("url" to data.cuspcrow, "title" to title)
                }
            }
        }
    }
}
