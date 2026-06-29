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
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.app.core.widget.ActionButton
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.model.entity.SysTypeItem
import com.flower.flow.data.vm.FeedbackViewModel
import com.flower.flow.databinding.ActivityFeedbackBinding
import com.flower.flow.databinding.LayoutItemFeedbackTypeBinding
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.toast
import me.hgj.jetpackmvvm.ext.view.vertical

class FeedbackActivity : BaseActivity<FeedbackViewModel, ActivityFeedbackBinding>() {

    private var feedbackTypePosition = -1

    @SuppressLint("NotifyDataSetChanged")
    override fun initView(savedInstanceState: Bundle?) {
        val isShowBtn = (App.globalConfig?.reportEntranceShow ?: 0) == 1
        if (isShowBtn) {
            addSaveBtn()
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
                        tvName.text = model.name
                        if (feedbackTypePosition == modelPosition) {
                            ivSelect.setImageResource(R.mipmap.ic_feedback_type_item_selected)
                        } else {
                            ivSelect.setImageResource(R.mipmap.ic_feedback_type_item_unselect)
                        }
                    }
                }

                onClick(R.id.llItem) {
                    feedbackTypePosition = modelPosition
                    notifyDataSetChanged()
                }
            }
    }

    override fun onBindViewClick() {
        mBind.btnSubmit.clickNoRepeat {
            val selectedType = getSelectedFeedbackType()
            if (selectedType == null) {
                FlowCopyStore.get(FlowCopyKey.FEEDBACK_TYPE_PICK).toast()
                return@clickNoRepeat
            }

            val email = mBind.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                FlowCopyStore.get(FlowCopyKey.CONTACT_INPUT_HINT).toast()
                return@clickNoRepeat
            }

            val content = mBind.etFeedbackContent.text.toString().trim()
            mViewModel.submitFeedback(content, email, selectedType.id).obs(this) {
                onSuccess {
                    UserManager.saveUserMsgDot(true)
                    finish()
                }
                onError { error ->
                    error.toast()
                }
            }
        }
    }

    private fun getSelectedFeedbackType(): SysTypeItem? {
        return (mBind.rvFeedbackType.models as? List<SysTypeItem>)
            ?.getOrNull(feedbackTypePosition)
    }

    fun addSaveBtn() {
        mToolbar.menu.clear()
        mToolbar.setContentInsetEndWithActions(0)

        val item = mToolbar.menu.add("report").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        val saveButton = ActionButton(this).apply {
            text = FlowCopyStore.get(FlowCopyKey.REPORT_ACTION)
            setOnClickListener {
                openActivity<ReportActivity>()
            }
        }

        item.actionView = FrameLayout(this).apply {
            setPadding(0, 0, dp(15), 0)

            addView(
                saveButton,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER,
                ),
            )
        }
    }

    private fun setText() {
        mBind.tvTitle.text = FlowCopyStore.get(FlowCopyKey.FEEDBACK_ENTRY)
        mBind.tvFeedbackTypeLabel.text = FlowCopyStore.get(FlowCopyKey.FEEDBACK_TYPE)
        mBind.tvFeedbackTypeRequired.text = FlowCopyStore.get(FlowCopyKey.REQUIRED_MARK)
        mBind.tvFeedbackContentLabel.text = FlowCopyStore.get(FlowCopyKey.FEEDBACK_DESC)
        mBind.etFeedbackContent.hint = FlowCopyStore.get(FlowCopyKey.FEEDBACK_HINT)
        mBind.tvEmailLabel.text = FlowCopyStore.get(FlowCopyKey.CONTACT_FIELD)
        mBind.tvEmailRequired.text = FlowCopyStore.get(FlowCopyKey.REQUIRED_MARK)
        mBind.etEmail.hint = FlowCopyStore.get(FlowCopyKey.CONTACT_INPUT_HINT)
        mBind.btnSubmit.text = FlowCopyStore.get(FlowCopyKey.SEND_ACTION)
    }

    override fun createObserver() {
        mViewModel.loadFeedbackTypes().obs(this) {
            onSuccess { list ->
                mBind.rvFeedbackType.models = list
            }
            onError { error ->
                error.toast()
            }
        }
    }
}
