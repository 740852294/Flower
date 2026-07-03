package com.flower.flow.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.view.isVisible
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.flower.flow.R
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.model.entity.SysNotifyItem
import com.flower.flow.data.vm.SystemNotifyViewModel
import com.flower.flow.databinding.ActivitySystemNotifyBinding
import com.flower.flow.databinding.LayoutItemSystemNotifyBinding
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.doDebouncedClick
import me.hgj.jetpackmvvm.ext.util.intent.openActivityForResult
import me.hgj.jetpackmvvm.ext.util.refresh
import me.hgj.jetpackmvvm.ext.util.toast
import me.hgj.jetpackmvvm.ext.view.vertical

class SystemNotifyActivity :
    BaseActivity<SystemNotifyViewModel, ActivitySystemNotifyBinding>() {

    override val title: String
        get() = FlowCopyStore.get(FlowCopyKey.SYSTEM_NOTICE)

    @SuppressLint("NotifyDataSetChanged")
    override fun initView(savedInstanceState: Bundle?) {
        UserManager.saveUserMsgDot(false)

        mBind.refreshLayout.setEnableLoadMore(false)
        mBind.refreshLayout.refresh {
            loadNotifyList(showLoading = false)
        }

        mBind.rvNotify.vertical()
            .setup {
                addType<SysNotifyItem>(R.layout.layout_item_system_notify)

                onBind {
                    getBindingOrNull<LayoutItemSystemNotifyBinding>()?.run {
                        val model = getModel<SysNotifyItem>()
                        tvContent.text = model.imitaterise
                        btnReply.text = FlowCopyStore.get(FlowCopyKey.REPLY_ACTION)
                    }
                }

                onClick(R.id.btnReply) {
                    doDebouncedClick {
                        openActivityForResult<FeedbackActivity> {
                            loadNotifyList(showLoading = true)
                        }
                    }
                }
            }

        loadNotifyList(showLoading = true)
    }

    override fun createObserver() {

    }

    private fun loadNotifyList(showLoading: Boolean) {
        mViewModel.loadNotifyList(showLoading).obs(this) {
            onSuccess { list ->
                mBind.refreshLayout.finishRefresh()
                updateListState(list)
            }
            onError { error ->
                mBind.refreshLayout.finishRefresh(false)
                error.msg.toast()
            }
        }
    }

    private fun updateListState(list: List<SysNotifyItem>) {
        val hasData = list.isNotEmpty()
        mBind.ivEmpty.isVisible = !hasData
        mBind.rvNotify.isVisible = hasData
        if (hasData) {
            mBind.rvNotify.models = list
        } else {
            mBind.rvNotify.models = emptyList<SysNotifyItem>()
        }
    }
}
