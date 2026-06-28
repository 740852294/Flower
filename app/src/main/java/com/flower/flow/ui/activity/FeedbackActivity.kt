package com.flower.flow.ui.activity

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.FrameLayout
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.widget.ActionButton
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.vm.FeedbackViewModel
import com.flower.flow.databinding.ActivityFeedbackBinding

class FeedbackActivity : BaseActivity<FeedbackViewModel, ActivityFeedbackBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        val isShowBtn = (App.globalConfig?.reportEntranceShow ?: 0) == 1
        if (isShowBtn) {
            addSaveBtn()
        }
        setText()
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
        mBind.tvFeedbackTypeRequired.text =
            String.format("(%s)", FlowCopyStore.get(FlowCopyKey.REQUIRED_MARK))
        mBind.tvFeedbackContentLabel.text = FlowCopyStore.get(FlowCopyKey.FEEDBACK_DESC)
        mBind.etFeedbackContent.hint = FlowCopyStore.get(FlowCopyKey.FEEDBACK_HINT)
        mBind.tvEmailLabel.text = FlowCopyStore.get(FlowCopyKey.CONTACT_FIELD)
        mBind.tvEmailRequired.text =
            String.format("(%s)", FlowCopyStore.get(FlowCopyKey.REQUIRED_MARK))
        mBind.etEmail.hint = FlowCopyStore.get(FlowCopyKey.CONTACT_INPUT_HINT)
    }
}
