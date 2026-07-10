package com.flower.flow.ui.fragment

import android.os.Bundle
import androidx.core.view.isVisible
import com.drake.brv.BindingAdapter
import com.drake.brv.utils.bindingAdapter
import com.flower.flow.R
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseFragment
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.model.StringResId
import com.flower.flow.data.model.entity.TopicItem
import com.flower.flow.data.vm.TopicViewModel
import com.flower.flow.databinding.FragmentTopicBinding
import com.flower.flow.databinding.LayoutItemTopicLeftBinding
import com.flower.flow.databinding.LayoutItemTopicRightBinding
import com.flower.flow.ui.activity.IntegralRechargeActivity
import com.flower.flow.ui.activity.TopicTemplateListActivity
import com.flower.flow.ui.activity.VipJoinActivity
import com.flower.flow.ui.binder.bindTopicItem
import com.flower.flow.ui.binder.clearTopicItemImages
import com.flower.flow.ui.binder.setTopicItemAnimationsRunning
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.doDebouncedClick
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.loadListError
import me.hgj.jetpackmvvm.ext.util.loadListSuccess
import me.hgj.jetpackmvvm.ext.util.refresh
import me.hgj.jetpackmvvm.ext.util.statusPadding
import me.hgj.jetpackmvvm.ext.util.toast
import me.hgj.jetpackmvvm.ext.view.vertical

class TopicFragment : BaseFragment<TopicViewModel, FragmentTopicBinding>() {

    private var isLazyLoaded = false

    companion object {
        const val REQUEST_MAIN_DATA_READY = "request_main_data_ready"

        fun newInstance(): TopicFragment {
            val args = Bundle()
            val fragment = TopicFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBind.llContent.statusPadding()

        UserManager.user?.apply {
            mBind.tvMoney.text = beastamalgam.toString()
        }

        setText()

        parentFragmentManager.setFragmentResultListener(
            REQUEST_MAIN_DATA_READY,
            viewLifecycleOwner,
        ) { _, _ ->
            loadInitialTopicList()
        }

        mBind.refreshLayout.refresh {
            loadTopicList(showPageLoading = false)
            mViewModel.fetchUserInfo()
        }

        val adapter = object : BindingAdapter() {
            override fun onViewRecycled(holder: BindingViewHolder) {
                super.onViewRecycled(holder)
                holder.itemView.clearTopicItemImages()
            }

            override fun onViewAttachedToWindow(holder: BindingViewHolder) {
                super.onViewAttachedToWindow(holder)
                holder.itemView.setTopicItemAnimationsRunning(true)
            }

            override fun onViewDetachedFromWindow(holder: BindingViewHolder) {
                super.onViewDetachedFromWindow(holder)
                holder.itemView.setTopicItemAnimationsRunning(false)
            }
        }

        mBind.rvList.vertical()
        adapter.apply {
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
                        getBindingOrNull<LayoutItemTopicLeftBinding>()?.bindTopicItem(
                            model,
                        )
                    }

                    R.layout.layout_item_topic_right -> {
                        getBindingOrNull<LayoutItemTopicRightBinding>()?.bindTopicItem(
                            model,
                        )
                    }
                }
            }

            onClick(R.id.rootItem) {
                doDebouncedClick {
                    val model = getModel<TopicItem>()
                    openActivity<TopicTemplateListActivity>(
                        TopicTemplateListActivity.EXTRA_TOPIC_ID to model.acetoneactuate,
                        TopicTemplateListActivity.EXTRA_TOPIC_NAME to model.dazzledeacon,
                        TopicTemplateListActivity.EXTRA_TOPIC_DESCRIPTION to model.bequeathconclave,
                        TopicTemplateListActivity.EXTRA_TOPIC_IMG to model.bullmind,
                    )
                }
            }
        }
        mBind.rvList.adapter = adapter
    }

    override fun lazyLoadData() {
//        if (App.globalConfig != null) {
//            loadInitialTopicList()
//        }
    }

    fun refreshHomeTopicList() {
        if (!isLazyLoaded) return
        loadTopicList(showPageLoading = false)
    }

    override fun onBindViewClick() {
        mBind.llMoney.clickNoRepeat {
            val jump = App.globalConfig?.framepublic ?: 0
            val isVip = UserManager.user?.shareengage ?: false
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
                mBind.tvMoney.text = beastamalgam.toString()

                App.globalConfig?.apply {
                    mBind.llMoney.isVisible = (exaltabrade == 1)
                }
            }
        }

    }

    fun loadTopicList(showPageLoading: Boolean) {
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

    private fun loadInitialTopicList() {
        if (isLazyLoaded || App.globalConfig == null) return
        loadTopicList(showPageLoading = true)
        isLazyLoaded = true
    }

    private fun setText() {
        mBind.tvLabel.text = AppStrings.get(StringResId.VIDEO_APP_DESC)
    }
}
