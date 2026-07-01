package com.flower.flow.ui.fragment

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.drake.brv.BindingAdapter
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.dividerSpace
import com.drake.brv.utils.setup
import com.flower.flow.R
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseFragment
import com.flower.flow.app.core.ext.loadAvatarFile
import com.flower.flow.app.core.ext.loadImage
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.app.core.widget.CenterImageSpan
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.model.entity.UserInfo
import com.flower.flow.data.model.entity.WorkItem
import com.flower.flow.data.vm.MeViewModel
import com.flower.flow.databinding.FragmentMeBinding
import com.flower.flow.databinding.LayoutItemWorkBinding
import com.flower.flow.ui.activity.EditUserInfoActivity
import com.flower.flow.ui.activity.IntegralRechargeActivity
import com.flower.flow.ui.activity.MainActivity
import com.flower.flow.ui.activity.SettingActivity
import com.flower.flow.ui.activity.VipJoinActivity
import com.flower.flow.ui.adapter.MainAdapter
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.dp2px
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.intent.openActivityForResult
import me.hgj.jetpackmvvm.ext.util.loadListError
import me.hgj.jetpackmvvm.ext.util.loadMore
import me.hgj.jetpackmvvm.ext.util.refresh
import me.hgj.jetpackmvvm.ext.util.statusPadding
import me.hgj.jetpackmvvm.ext.util.toast
import me.hgj.jetpackmvvm.ext.view.grid
import me.hgj.jetpackmvvm.util.BasePage

class MeFragment : BaseFragment<MeViewModel, FragmentMeBinding>() {

    companion object {
        const val SPAN_COUNT = 2

        //状态，0=待解锁，1=待处理，2=处理中，3=已完成，4=处理失败
        const val WORK_STATUS_NONE = 0

        const val WORK_STATUS_WAIT = 1

        const val WORK_STATUS_PROCESSING = 2

        const val WORK_STATUS_COMPLETE = 3

        const val WORK_STATUS_FAIL = 4

        fun newInstance(): MeFragment {
            val args = Bundle()
            val fragment = MeFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBind.llContent.statusPadding()

        App.globalConfig?.apply {
            mBind.llMoney.isVisible = (integralAndVipEntranceShow == 1)
        }

        setText()

        UserManager.user?.apply {
            setUserInfo(this)
        }

        mBind.refreshLayout.refresh {
            mViewModel.initData().obs(viewLifecycleOwner) {
                onSuccess { page ->
                    bindWorkList(
                        page,
                        mBind.rvList.bindingAdapter,
                        mBind.refreshLayout,
                        isRefresh = true
                    )
                }
                onError { status ->
                    loadListError(status, mBind.refreshLayout)
                    status.msg.toast()
                }
            }
        }

        mBind.refreshLayout.loadMore {
            mViewModel.loadWorkList(false).obs(viewLifecycleOwner) {
                onSuccess { page ->
                    bindWorkList(
                        page,
                        mBind.rvList.bindingAdapter,
                        mBind.refreshLayout,
                        isRefresh = false
                    )
                }
                onError { status ->
                    loadListError(status, mBind.refreshLayout)
                    status.msg.toast()
                }
            }
        }

        mBind.rvList.grid(SPAN_COUNT)
            .dividerSpace(dp2px(8f), DividerOrientation.GRID)
            .setup {
                addType<WorkItem>(R.layout.layout_item_work)

                onBind {
                    getBindingOrNull<LayoutItemWorkBinding>()?.run {
                        val model = getModel<WorkItem>()

                        val isVip = UserManager.user?.isVip ?: false

                        btnStatus.isVisible = !model.againGenerateButtonMsg.isNullOrBlank()
                        btnStatus.text = model.againGenerateButtonMsg ?: ""

                        when (model.state) {
                            WORK_STATUS_NONE -> {
                                lockDot.visibility = View.VISIBLE
                                llTime.visibility = View.GONE
                                llProgress.visibility = View.GONE
                                ivDownload.visibility = View.GONE
                                ivCover.loadImage(
                                    url = model.aiartImg,
                                    cornerRadiusDp = 15f,
                                )
                                ivCover.applyBlurEffect(true, dp2px(15f).toFloat())
                                bindSampleImages(this, model.inputImgList)
                                llStatus.visibility = View.VISIBLE
                                ivStatus.visibility = View.VISIBLE
                                ivStatus.setImageResource(R.mipmap.ic_work_lock)
                                tvStatus1.visibility = View.GONE
                                tvStatus2.visibility =
                                    if (isVip || model.showMsg.isNullOrBlank()) View.GONE else View.VISIBLE
                                tvStatus2.text = model.showMsg.orEmpty()
                                tvGenerating.visibility = View.GONE
                            }

                            WORK_STATUS_WAIT, WORK_STATUS_PROCESSING -> {
                                lockDot.visibility = View.GONE
                                llTime.visibility = View.GONE
                                llProgress.visibility = View.GONE
                                ivDownload.visibility = View.GONE
                                val url = model.inputImgList?.firstOrNull() ?: ""
                                if (url.isNotBlank()) {
                                    ivCover.loadImage(
                                        url = url,
                                        cornerRadiusDp = 15f,
                                    )
                                }
                                ivCover.applyBlurEffect(true, dp2px(15f).toFloat())
                                ivSampleSingle.isVisible = false
                                llSampleBottom.isVisible = false
                                llStatus.visibility = View.VISIBLE
                                ivStatus.visibility = View.GONE
                                tvStatus1.visibility =
                                    if (model.showMsg.isNullOrBlank()) View.GONE else View.VISIBLE
                                tvStatus1.text = model.showMsg.orEmpty()
                                tvStatus2.visibility = View.GONE
                                tvGenerating.visibility =
                                    if (model.estimatTimeMsg.isNullOrBlank()) View.GONE else View.VISIBLE
                                tvGenerating.text = model.estimatTimeMsg.orEmpty()
                            }

                            WORK_STATUS_COMPLETE -> {
                                lockDot.visibility = View.GONE
                                llTime.visibility =
                                    if (model.aiartType == 2) View.VISIBLE else View.GONE
                                tvTime.text = model.videoDurationMsg.orEmpty()
                                // 下载时，这里显示显示进度，progress是本地添加bean里面的，接口没返回的
                                llProgress.visibility = View.GONE
                                ivDownload.visibility = View.VISIBLE

                                val url = model.inputImgList?.firstOrNull() ?: model.aiartImg ?: ""
                                if (url.isNotBlank()) {
                                    ivCover.loadImage(
                                        url = url,
                                        cornerRadiusDp = 15f,
                                    )
                                }
                                ivCover.applyBlurEffect(false)
                                ivSampleSingle.isVisible = false
                                llSampleBottom.isVisible = false

                                llStatus.visibility = View.GONE
                            }

                            WORK_STATUS_FAIL -> {
                                lockDot.visibility = View.GONE
                                llTime.visibility = View.GONE
                                llProgress.visibility = View.GONE
                                ivDownload.visibility = View.GONE
                                val url = model.inputImgList?.firstOrNull() ?: ""
                                if (url.isNotBlank()) {
                                    ivCover.loadImage(
                                        url = url,
                                        cornerRadiusDp = 15f,
                                    )
                                }
                                ivCover.applyBlurEffect(true, dp2px(15f).toFloat())
                                ivSampleSingle.isVisible = false
                                llSampleBottom.isVisible = false
                                llStatus.visibility = View.VISIBLE
                                ivStatus.visibility = View.VISIBLE
                                ivStatus.setImageResource(R.mipmap.ic_work_failed)
                                tvStatus1.visibility = View.VISIBLE
                                tvStatus1.text = model.showMsg.orEmpty()
                                tvStatus2.visibility = View.GONE
                                tvGenerating.visibility = View.GONE
                            }
                        }
                    }
                }

                onClick(R.id.rootItem) {

                }
            }
    }

    override fun onBindViewClick() {
        mBind.llMoney.clickNoRepeat {
            val integral = App.globalConfig?.integralEntranceJumpState ?: 0
            val isVip = UserManager.user?.isVip ?: false
            if (!isVip && integral == 1) {
                openActivity<VipJoinActivity>()
                return@clickNoRepeat
            }
            openActivity<IntegralRechargeActivity>()
        }

        mBind.clVip.clickNoRepeat {
            openActivity<VipJoinActivity>()
        }

        mBind.clInfo.clickNoRepeat {
            openActivityForResult<EditUserInfoActivity> { _ ->
                (activity as? MainActivity)?.getUserInfo(isFirst = false, isLoading = true)
            }
        }

        mBind.rlWorkAdd.clickNoRepeat {
            FlowCopyStore.get(FlowCopyKey.TASK_EMPTY_HINT).toast()
            (activity as? MainActivity)?.switchTab(MainAdapter.PAGE_TOPIC)
        }

        mBind.settingBtn.clickNoRepeat {
            openActivity<SettingActivity>()
        }
    }

    override fun lazyLoadData() {
        mViewModel.initData().obs(viewLifecycleOwner) {
            onSuccess { page ->
                bindWorkList(
                    page,
                    mBind.rvList.bindingAdapter,
                    mBind.refreshLayout,
                    isRefresh = true
                )
            }
            onError { status ->
                loadListError(status, mBind.refreshLayout)
                status.msg.toast()
            }
        }
    }

    private fun bindWorkList(
        baseListNetEntity: BasePage<WorkItem>,
        bindingAdapter: BindingAdapter,
        smartRefreshLayout: SmartRefreshLayout,
        isRefresh: Boolean? = null,
    ) {
        val refresh = isRefresh ?: baseListNetEntity.isRefresh()
        if (refresh) {
            bindingAdapter.models = baseListNetEntity.getPageData()
            smartRefreshLayout.finishRefresh()
        } else {
            bindingAdapter.addModels(baseListNetEntity.getPageData())
        }
        if (baseListNetEntity.hasMore()) {
            smartRefreshLayout.finishLoadMore()
            smartRefreshLayout.setNoMoreData(false)
            smartRefreshLayout.setEnableLoadMore(true)
        } else {
            smartRefreshLayout.finishLoadMore()
            smartRefreshLayout.setEnableLoadMore(false)
        }

        mBind.rlWorkAdd.isVisible =
            bindingAdapter.models == null || bindingAdapter.models?.isEmpty() == true
        mBind.btnDelete.isVisible =
            bindingAdapter.models != null && bindingAdapter.models?.isNotEmpty() == true
    }

    override fun createObserver() {
        EventViewModel.msgRedDotEvent.observe(this) { show ->
            mBind.redDot.isVisible = show
        }

        UserManager.observeUser().observe(viewLifecycleOwner) { user ->
            user?.apply {
                setUserInfo(user)
            }
        }

        EventViewModel.languageEvent.observe(this) {
            setText()
        }
    }

    private fun setUserInfo(user: UserInfo) {
        val isVip = user.isVip
        mBind.clVip.isVisible =
            (!isVip) && ((App.globalConfig?.integralAndVipEntranceShow ?: 0) == 1)
        mBind.ivVipLabel.isVisible = isVip

        val avatar = user.avatar
        if (avatar.isNotEmpty()) {
            mBind.ivAvatar.loadAvatarFile(avatar)
        }

        mBind.tvName.text = user.name
        mBind.tvMoney.text = user.integralBalance.toString()

        mBind.tvOverTip.isVisible = user.clearTaskMsg.isNotEmpty()
        setIconText(mBind.tvOverTip, R.mipmap.ic_me_over_star, user.clearTaskMsg)
    }

    private fun setText() {
        mBind.vipTitleText.text = FlowCopyStore.get(FlowCopyKey.VIDEO_PRO_NAME)
        mBind.vipSubtitleText.text = FlowCopyStore.get(FlowCopyKey.VIP_UNLOCK_HINT)
        mBind.btnVip.text = FlowCopyStore.get(FlowCopyKey.VIP_OPEN_HINT)
        mBind.worksLabel.text = FlowCopyStore.get(FlowCopyKey.WORKS_TAB)
        mBind.btnDelete.text = FlowCopyStore.get(FlowCopyKey.DELETE_ACTION)
    }

    fun setIconText(
        textView: TextView,
        iconRes: Int,
        text: String,
        iconSizeDp: Int = 18
    ) {
        val density = textView.resources.displayMetrics.density
        val size = (iconSizeDp * density).toInt()

        val drawable = ContextCompat.getDrawable(textView.context, iconRes)!!.mutate()
        drawable.setBounds(0, 0, size, size)

        // 注意：前面加一个占位符，图片替换占位符，不要替换正文第一个字母
        val finalText = "  $text"
        val span = SpannableString(finalText)

        span.setSpan(
            CenterImageSpan(drawable),
            0,
            1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = span
    }

    private fun bindSampleImages(
        binding: LayoutItemWorkBinding,
        samples: List<String>?,
    ) {
        binding.ivSampleSingle.isVisible = false
        binding.llSampleBottom.isVisible = false
        when {
            samples.isNullOrEmpty() -> return
            samples.size == 1 -> {
                binding.ivSampleSingle.isVisible = true
                binding.ivSampleSingle.loadImage(
                    url = samples.first(),
                    cornerRadiusDp = 10f,
                )
            }

            else -> {
                binding.llSampleBottom.isVisible = true
                binding.ivSampleLeft.loadImage(
                    url = samples[0],
                    cornerRadiiDp = floatArrayOf(
                        10f,
                        0f,
                        0f,
                        10f,
                    ),
                )
                binding.ivSampleRight.loadImage(
                    url = samples.getOrNull(1),
                    cornerRadiiDp = floatArrayOf(
                        0f,
                        10f,
                        10f,
                        0f,
                    ),
                )
            }
        }
    }
}