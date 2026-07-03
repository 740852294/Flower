package com.flower.flow.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import com.flower.flow.R
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.ext.loadImage
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.data.model.CacheConfig
import com.flower.flow.data.model.StringResId
import com.flower.flow.data.vm.PrivacyViewModel
import com.flower.flow.databinding.ActivityPrivacyBinding
import com.flower.flow.domain.startup.StartUserGate
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.doDebouncedClick
import me.hgj.jetpackmvvm.ext.util.finishActivityByClass
import me.hgj.jetpackmvvm.ext.util.finishAllActivity
import me.hgj.jetpackmvvm.ext.util.getColorExt
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.toast

class PrivacyActivity : BaseActivity<PrivacyViewModel, ActivityPrivacyBinding>() {

    override val showTitle = false

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun onBindViewClick() {
        mBind.btnDisagree.clickNoRepeat {
            finishAllActivity()
        }

        mBind.btnAgree.clickNoRepeat {
            CacheConfig.isAgree = true
            checkUser()
        }
    }

    private fun checkUser() {
        StartUserGate.checkUser(
            lifecycleOwner = this,
            context = applicationContext,
            registerUser = mViewModel::registerUserByAttribution,
            onGoMain = ::goMain,
        )
    }

    private fun goMain() {
        openActivity<MainActivity>()
        finish()
    }

    override fun createObserver() {
        mViewModel.initLanguageConfig().obs(this) {
            onSuccess {
                mBind.clContent.visibility = View.VISIBLE
                setLanguage()
            }
            onError { error ->
                error.msg.toast()
                finishAllActivity()
            }
        }

        mViewModel.getBackgroundVideo().obs(this) {
            onSuccess {
                if (it.cuspcrow.isNotEmpty()) {
                    mBind.bgVideo.loadImage(it.cuspcrow)
                    mBind.ivBg.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun setLanguage() {
        mBind.title.text = AppStrings.get(StringResId.AGREEMENT_HEAD)
        mBind.content.apply {
            text = composeAgreementCopy()
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = Color.TRANSPARENT
        }
        mBind.btnAgree.text = AppStrings.get(StringResId.ACCEPT_ACTION)
        mBind.btnDisagree.text = AppStrings.get(StringResId.REJECT_HINT)
    }

    private fun composeAgreementCopy(): CharSequence {
        val termsLabel = AppStrings.get(StringResId.TERMS_LINK)
        val privacyLabel = AppStrings.get(StringResId.PRIVACY_LINK)
        val fullText = buildString {
            append(AppStrings.get(StringResId.PRIVACY_DESC))
            append(' ')
            append(termsLabel)
            append(' ')
            append(AppStrings.get(StringResId.AND_WORD))
            append(' ')
            append(privacyLabel)
        }
        val builder = SpannableStringBuilder(fullText)
        val accentColor = getColorExt(R.color.colorAccent)
        attachPolicySpan(builder, fullText, termsLabel, accentColor, PolicyRoute.TERMS)
        attachPolicySpan(builder, fullText, privacyLabel, accentColor, PolicyRoute.PRIVACY)
        return builder
    }

    private fun attachPolicySpan(
        builder: SpannableStringBuilder,
        fullText: CharSequence,
        label: String,
        accentColor: Int,
        route: PolicyRoute,
    ) {
        if (label.isEmpty()) return
        val start = fullText.indexOf(label)
        if (start < 0) return

        val end = start + label.length
        builder.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    doDebouncedClick {
                        openPolicyPage(route.requestType)
                    }
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = accentColor
                    ds.isUnderlineText = true
                }
            },
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
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

    enum class PolicyRoute(val requestType: Int) {
        PRIVACY(1),
        TERMS(2),
    }
}
