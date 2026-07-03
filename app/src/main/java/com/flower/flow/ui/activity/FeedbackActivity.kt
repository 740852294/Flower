package com.flower.flow.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.FrameLayout
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.flower.flow.R
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.app.core.widget.ActionButton
import com.flower.flow.data.model.StringResId
import com.flower.flow.data.model.entity.SysTypeItem
import com.flower.flow.data.vm.FeedbackViewModel
import com.flower.flow.databinding.ActivityFeedbackBinding
import com.flower.flow.databinding.LayoutItemFeedbackTypeBinding
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.doDebouncedClick
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.toast
import me.hgj.jetpackmvvm.ext.view.vertical

class FeedbackActivity : BaseActivity<FeedbackViewModel, ActivityFeedbackBinding>() {

    private var feedbackTypePosition = -1

    @SuppressLint("NotifyDataSetChanged")
    override fun initView(savedInstanceState: Bundle?) {
        val isShowBtn = (App.globalConfig?.athleteacanthus ?: 0) == 1
        if (isShowBtn) {
            addReportBtn()
        }
        setText()

        mBind.rvFeedbackType.vertical()
            .setup {

                addType<SysTypeItem>(R.layout.layout_item_feedback_type)

                onCreate {
                    getBindingOrNull<LayoutItemFeedbackTypeBinding>()?.run {

                    }
                }

                onBind {
                    getBindingOrNull<LayoutItemFeedbackTypeBinding>()?.run {
                        val model = getModel<SysTypeItem>()
                        tvName.text = model.dazzledeacon
                        if (feedbackTypePosition == modelPosition) {
                            ivSelect.setImageResource(R.mipmap.ic_feedback_type_item_selected)
                        } else {
                            ivSelect.setImageResource(R.mipmap.ic_feedback_type_item_unselect)
                        }
                    }
                }

                onClick(R.id.llItem) {
                    doDebouncedClick {
                        feedbackTypePosition = modelPosition
                        notifyDataSetChanged()
                    }
                }
            }
    }

    override fun onBindViewClick() {
        mBind.btnSubmit.clickNoRepeat {
            val selectedType = getSelectedFeedbackType()
            if (selectedType == null) {
                AppStrings.get(StringResId.FEEDBACK_TYPE_PICK).toast()
                return@clickNoRepeat
            }

            val email = mBind.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                AppStrings.get(StringResId.CONTACT_INPUT_HINT).toast()
                return@clickNoRepeat
            }

            val content = mBind.etFeedbackContent.text.toString().trim()
            mViewModel.submitFeedback(content, email, selectedType.acetoneactuate).obs(this) {
                onSuccess {
                    UserManager.saveUserMsgDot(true)
                    finish()
                }
                onError { error ->
                    error.msg.toast()
                }
            }
        }
    }

    private fun getSelectedFeedbackType(): SysTypeItem? {
        return (mBind.rvFeedbackType.models as? List<SysTypeItem>)
            ?.getOrNull(feedbackTypePosition)
    }

    fun addReportBtn() {
        mToolbar.menu.clear()
        mToolbar.setContentInsetEndWithActions(0)

        val item = mToolbar.menu.add("report").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        val reportButton = ActionButton(this).apply {
            text = AppStrings.get(StringResId.REPORT_ACTION)
            clickNoRepeat {
                openActivity<ReportActivity>()
            }
        }

        item.actionView = FrameLayout(this).apply {
            setPadding(0, 0, dp(15), 0)

            addView(
                reportButton,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER,
                ),
            )
        }
    }

    private fun setText() {
        mBind.tvTitle.text = AppStrings.get(StringResId.FEEDBACK_ENTRY)
        mBind.tvFeedbackTypeLabel.text = AppStrings.get(StringResId.FEEDBACK_TYPE)
        mBind.tvFeedbackTypeRequired.text = AppStrings.get(StringResId.REQUIRED_MARK)
        mBind.tvFeedbackContentLabel.text = AppStrings.get(StringResId.FEEDBACK_DESC)
        mBind.etFeedbackContent.hint = AppStrings.get(StringResId.FEEDBACK_HINT)
        mBind.tvEmailLabel.text = AppStrings.get(StringResId.CONTACT_FIELD)
        mBind.tvEmailRequired.text = AppStrings.get(StringResId.REQUIRED_MARK)
        mBind.etEmail.hint = AppStrings.get(StringResId.CONTACT_INPUT_HINT)
        mBind.btnSubmit.text = AppStrings.get(StringResId.SEND_ACTION)
    }

    override fun createObserver() {
        mViewModel.loadFeedbackTypes().obs(this) {
            onSuccess { list ->
                mBind.rvFeedbackType.models = list
            }
            onError { error ->
                error.msg.toast()
            }
        }
    }
}
