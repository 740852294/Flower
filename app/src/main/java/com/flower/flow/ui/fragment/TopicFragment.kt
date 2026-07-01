package com.flower.flow.ui.fragment

import android.os.Bundle
import androidx.core.view.isVisible
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.setup
import com.flower.flow.R
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseFragment
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.model.entity.TopicItem
import com.flower.flow.data.vm.TopicViewModel
import com.flower.flow.databinding.FragmentTopicBinding
import com.flower.flow.databinding.LayoutItemTopicLeftBinding
import com.flower.flow.databinding.LayoutItemTopicRightBinding
import com.flower.flow.ui.activity.IntegralRechargeActivity
import com.flower.flow.ui.activity.TopicTemplateListActivity
import com.flower.flow.ui.activity.VipJoinActivity
import com.flower.flow.ui.binder.bindTopicItem
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.loadListError
import me.hgj.jetpackmvvm.ext.util.loadListSuccess
import me.hgj.jetpackmvvm.ext.util.refresh
import me.hgj.jetpackmvvm.ext.util.statusPadding
import me.hgj.jetpackmvvm.ext.util.toast
import me.hgj.jetpackmvvm.ext.view.vertical

class TopicFragment : BaseFragment<TopicViewModel, FragmentTopicBinding>() {

    companion object {
        fun newInstance(): TopicFragment {
            val args = Bundle()
            val fragment = TopicFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBind.llContent.statusPadding()

        App.globalConfig?.apply {
            mBind.llMoney.isVisible = (integralAndVipEntranceShow == 1)
        }

        UserManager.user?.apply {
            mBind.tvMoney.text = integralBalance.toString()
        }

        setText()

        mBind.refreshLayout.refresh {
            loadTopicList(showPageLoading = false)
        }

        mBind.rvList.vertical()
            .setup {
                addType<TopicItem> { position ->
                    if (position % 2 == 0) {
                        R.layout.layout_item_topic_left
                    } else {
                        R.layout.layout_item_topic_right
                    }
                }

                onBind {
                    val model = getModel<TopicItem>()
                    when (itemViewType) {
                        R.layout.layout_item_topic_left -> {
                            getBindingOrNull<LayoutItemTopicLeftBinding>()?.bindTopicItem(model)
                        }

                        R.layout.layout_item_topic_right -> {
                            getBindingOrNull<LayoutItemTopicRightBinding>()?.bindTopicItem(model)
                        }
                    }
                }

                onClick(R.id.rootItem) {
                    val model = getModel<TopicItem>()
                    openActivity<TopicTemplateListActivity>(
                        TopicTemplateListActivity.EXTRA_TOPIC_ID to model.id,
                        TopicTemplateListActivity.EXTRA_TOPIC_NAME to model.name,
                        TopicTemplateListActivity.EXTRA_TOPIC_DESCRIPTION to model.description,
                        TopicTemplateListActivity.EXTRA_TOPIC_IMG to model.img,
                    )
                }
            }
    }

    override fun lazyLoadData() {
        loadTopicList(showPageLoading = true)
    }

    override fun onBindViewClick() {
        mBind.llMoney.clickNoRepeat {
            val jump = App.globalConfig?.integralEntranceJumpState ?: 0
            val isVip = UserManager.user?.isVip ?: false
            if (!isVip && jump == 1) {
                openActivity<VipJoinActivity>()
                return@clickNoRepeat
            }
            openActivity<IntegralRechargeActivity>()
        }
    }

    override fun createObserver() {
        EventViewModel.languageEvent.observe(this) {
            setText()
            loadTopicList(showPageLoading = true)
        }

        UserManager.observeUser().observe(viewLifecycleOwner) { user ->
            user?.apply {
                mBind.tvMoney.text = integralBalance.toString()
            }
        }

        EventViewModel.homeDataRefreshEvent.observe(this) { result ->
            if (result) {
                loadTopicList(showPageLoading = true)
            }
        }
    }

    private fun loadTopicList(showPageLoading: Boolean) {
        mViewModel.loadTopicList(showPageLoading).obs(this) {
            onSuccess { list ->
                loadListSuccess(
                    ArrayList(list),
                    mBind.rvList.bindingAdapter,
                    mBind.refreshLayout,
                    this@TopicFragment,
                )
            }
            onError { status ->
                loadListError(status, mBind.refreshLayout)
                status.msg.toast()
            }
        }
    }

    private fun setText() {
        mBind.tvLabel.text = FlowCopyStore.get(FlowCopyKey.VIDEO_APP_DESC)
    }
}
