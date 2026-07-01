package com.flower.flow.ui.activity

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.FrameLayout
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.ext.initClose
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.widget.ActionButton
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.model.entity.TemplateItem
import com.flower.flow.data.vm.TopicUseTemplateViewModel
import com.flower.flow.databinding.ActivityTopicUseTemplateBinding
import me.hgj.jetpackmvvm.ext.util.intent.extraAct
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.statusPadding

class TopicUseTemplateActivity :
    BaseActivity<TopicUseTemplateViewModel, ActivityTopicUseTemplateBinding>() {

    val topicId: Int by extraAct(EXTRA_TOPIC_ID, 0)

    val topicName: String by extraAct(EXTRA_TOPIC_NAME, "")

    val hasNext: Boolean by extraAct(EXTRA_HAS_NEXT, false)

    val currentPage: Int by extraAct(EXTRA_CURRENT_PAGE, 1)

    val position: Int by extraAct(EXTRA_POSITION, 0)

    val templateList: List<TemplateItem> by lazy { readTemplateList() }

    val currentTemplate: TemplateItem?
        get() = templateList.getOrNull(position)

    override val showTitle: Boolean
        get() = false

    override fun initView(savedInstanceState: Bundle?) {
        mBind.llContent.statusPadding()
        mBind.toolbar.initClose(topicName) {
            finish()
        }
        val isShowBtn = (App.globalConfig?.reportEntranceShow ?: 0) == 1
        if (isShowBtn) {
            addReportBtn()
        }
    }

    private fun readTemplateList(): List<TemplateItem> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayExtra(EXTRA_TEMPLATE_LIST, TemplateItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayExtra(EXTRA_TEMPLATE_LIST)
        }?.filterIsInstance<TemplateItem>() ?: emptyList()
    }

    override fun onBindViewClick() {

    }

    override fun createObserver() {

    }

    fun addReportBtn() {
        mBind.toolbar.menu.clear()
        mBind.toolbar.setContentInsetEndWithActions(0)

        val item = mBind.toolbar.menu.add("report").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        val reportButton = ActionButton(this).apply {
            text = FlowCopyStore.get(FlowCopyKey.REPORT_ACTION)
            setOnClickListener {
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

    companion object {
        const val EXTRA_TOPIC_ID = "topic_id"
        const val EXTRA_TOPIC_NAME = "topic_name"
        const val EXTRA_TEMPLATE_LIST = "template_list"
        const val EXTRA_HAS_NEXT = "has_next"
        const val EXTRA_CURRENT_PAGE = "current_page"
        const val EXTRA_POSITION = "position"
    }
}
