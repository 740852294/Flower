package com.flower.flow.ui.activity

import android.os.Bundle
import androidx.core.view.isVisible
import com.flower.flow.BuildConfig
import com.flower.flow.R
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.util.AesTextCodec
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.model.CacheConfig
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.vm.SettingViewModel
import com.flower.flow.databinding.ActivitySettingBinding
import com.flower.flow.ui.activity.PrivacyActivity.PolicyRoute
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.copyToClipboard
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.toast

class SettingActivity : BaseActivity<SettingViewModel, ActivitySettingBinding>() {

    override val title: String
        get() = FlowCopyStore.get(FlowCopyKey.SETTINGS_ENTRY)

    private val passwordOriginal: String by lazy {
        val pwd = UserManager.user?.password ?: ""
        if (pwd.isNotBlank()) {
            AesTextCodec.decode(pwd) ?: ""
        } else {
            ""
        }
    }

    private var isShowPassword = false

    override fun initView(savedInstanceState: Bundle?) {
        setText()

        UserManager.user?.apply {
            val isShow = this.sysNotifyRedDot
            mBind.redDot.isVisible = isShow
        }

        mBind.tvAppVersion.text = String.format("V%s", BuildConfig.VERSION_NAME)
        mBind.tvAccount.text = CacheConfig.userId
        mBind.tvPassword.text = "**********"
        mBind.ivPassword.setImageResource(R.mipmap.ic_setting_eyes)

        val isAccountAndPassword = (App.globalConfig?.userChangeShow ?: 0) == 1
        mBind.llAccountAndPassword.isVisible = isAccountAndPassword
        mBind.btnChange.isVisible = isAccountAndPassword
    }

    override fun onBindViewClick() {
        mBind.llShare.clickNoRepeat {

        }

        mBind.llFeedback.clickNoRepeat {
            openActivity<FeedbackActivity>()
        }

        mBind.llNotify.clickNoRepeat {

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
            "修改密码".toast()
        }

        mBind.ivPassword.clickNoRepeat {
            if (isShowPassword) {
                isShowPassword = false
                mBind.tvPassword.text = "**********"
                mBind.ivPassword.setImageResource(R.mipmap.ic_setting_eyes)
                if (passwordOriginal.isNotBlank()) {
                    copyToClipboard(passwordOriginal, FlowCopyStore.get(FlowCopyKey.PASSWORD_LABEL))
                    FlowCopyStore.get(FlowCopyKey.COPY_ACTION).toast()
                }
            } else {
                isShowPassword = true
                mBind.tvPassword.text = passwordOriginal
                mBind.ivPassword.setImageResource(R.mipmap.ic_setting_copy)
            }
        }

        mBind.ivAccount.clickNoRepeat {
            val uid = mBind.tvAccount.text?.toString().orEmpty()
            if (uid.isNotBlank()) {
                copyToClipboard(uid, FlowCopyStore.get(FlowCopyKey.ACCOUNT_LABEL))
                FlowCopyStore.get(FlowCopyKey.COPY_ACTION).toast()
            }
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

    private fun setText() {
        mBind.tvShare.text = FlowCopyStore.get(FlowCopyKey.SHARE_ACTION)
        mBind.tvFeedback.text = FlowCopyStore.get(FlowCopyKey.FEEDBACK_ENTRY)
        mBind.tvNotify.text = FlowCopyStore.get(FlowCopyKey.SYSTEM_NOTICE)
        mBind.tvPrivacy.text = FlowCopyStore.get(FlowCopyKey.PRIVACY_LINK)
        mBind.tvTerms.text = FlowCopyStore.get(FlowCopyKey.TERMS_LINK)
        mBind.tvLanguage.text = FlowCopyStore.get(FlowCopyKey.LANGUAGE_SETTING)
        mBind.tvVersion.text = FlowCopyStore.get(FlowCopyKey.VERSION_LABEL)
        mBind.tvAccountLabel.text =
            String.format("%s：", FlowCopyStore.get(FlowCopyKey.ACCOUNT_LABEL))
        mBind.tvPasswordLabel.text =
            String.format("%s：", FlowCopyStore.get(FlowCopyKey.PASSWORD_LABEL))
        mBind.btnChange.text = FlowCopyStore.get(FlowCopyKey.ACCOUNT_SWITCH)
    }

    private fun openPolicyPage(requestType: Int) {
        mViewModel.openPolicyPage(requestType).obs(this) {
            onSuccess { data ->
                if (data.url.isEmpty()) {
                    FlowCopyStore.get(FlowCopyKey.WEB_EMPTY_HINT).toast()
                } else {
                    val title = when (requestType) {
                        PolicyRoute.PRIVACY.requestType -> {
                            FlowCopyStore.get(FlowCopyKey.PRIVACY_LINK)
                        }

                        PolicyRoute.TERMS.requestType -> {
                            FlowCopyStore.get(FlowCopyKey.TERMS_LINK)
                        }

                        else -> {
                            ""
                        }
                    }
                    openActivity<WebviewActivity>("url" to data.url, "title" to title)
                }
            }
        }
    }
}
