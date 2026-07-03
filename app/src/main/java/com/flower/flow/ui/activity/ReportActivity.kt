package com.flower.flow.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.widget.doAfterTextChanged
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.flower.flow.R
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.data.model.StringResId
import com.flower.flow.data.model.entity.SysTypeItem
import com.flower.flow.data.vm.ReportViewModel
import com.flower.flow.databinding.ActivityReportBinding
import com.flower.flow.databinding.LayoutItemFeedbackTypeBinding
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.doDebouncedClick
import me.hgj.jetpackmvvm.ext.util.toast
import me.hgj.jetpackmvvm.ext.view.vertical
import java.util.Locale

class ReportActivity : BaseActivity<ReportViewModel, ActivityReportBinding>() {

    private var reportTypePosition = -1

    @SuppressLint("NotifyDataSetChanged")
    override fun initView(savedInstanceState: Bundle?) {
        setText()
        setupCharCount()

        mBind.rvReportType.vertical()
            .setup {
                addType<SysTypeItem>(R.layout.layout_item_feedback_type)

                onBind {
                    getBindingOrNull<LayoutItemFeedbackTypeBinding>()?.run {
                        val model = getModel<SysTypeItem>()
                        tvName.text = model.dazzledeacon
                        if (reportTypePosition == modelPosition) {
                            ivSelect.setImageResource(R.mipmap.ic_feedback_type_item_selected)
                        } else {
                            ivSelect.setImageResource(R.mipmap.ic_feedback_type_item_unselect)
                        }
                    }
                }

                onClick(R.id.llItem) {
                    doDebouncedClick {
                        reportTypePosition = modelPosition
                        notifyDataSetChanged()
                    }
                }
            }
    }

    override fun onBindViewClick() {
        mBind.btnSubmit.clickNoRepeat {
            val selectedType = getSelectedReportType()
            if (selectedType == null) {
                AppStrings.get(StringResId.REPORT_TYPE_PICK).toast()
                return@clickNoRepeat
            }

            val content = mBind.etReportContent.text.toString().trim()
            mViewModel.submitReport(content, selectedType.acetoneactuate).obs(this) {
                onSuccess { msg ->
                    msg.toast()
                    finish()
                }
                onError { error ->
                    error.msg.toast()
                }
            }
        }
    }

    override fun createObserver() {
        mViewModel.loadReportTypes().obs(this) {
            onSuccess { list ->
                mBind.rvReportType.models = list
            }
            onError { error ->
                error.msg.toast()
            }
        }
    }

    private fun getSelectedReportType(): SysTypeItem? {
        return (mBind.rvReportType.models as? List<SysTypeItem>)
            ?.getOrNull(reportTypePosition)
    }

    private fun setupCharCount() {
        mBind.tvCharCount.text = String.format(Locale.getDefault(), "0/%d", MAX_CONTENT_LENGTH)
        mBind.etReportContent.doAfterTextChanged { text ->
            val length = text?.length ?: 0
            mBind.tvCharCount.text =
                String.format(Locale.getDefault(), "%d/%d", length, MAX_CONTENT_LENGTH)
        }
    }

    private fun setText() {
        mBind.tvTitle.text = AppStrings.get(StringResId.REPORT_ACTION)
        mBind.tvReportTypeLabel.text = AppStrings.get(StringResId.REPORT_TYPE)
        mBind.tvReportTypeRequired.text = AppStrings.get(StringResId.REQUIRED_MARK)
        mBind.tvReportContentLabel.text = AppStrings.get(StringResId.REPORT_DESC)
        mBind.tvReportContentOptional.text = AppStrings.get(StringResId.OPTIONAL_MARK)
        mBind.etReportContent.hint = AppStrings.get(StringResId.REPORT_HINT)
        mBind.btnSubmit.text = AppStrings.get(StringResId.SEND_ACTION)
    }

    private companion object {
        private const val MAX_CONTENT_LENGTH = 200
    }
}
