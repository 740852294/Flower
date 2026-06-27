package com.flower.flow.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseFragment
import com.flower.flow.app.core.ext.loadImage
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.model.entity.UserInfo
import com.flower.flow.data.vm.MeViewModel
import com.flower.flow.databinding.FragmentMeBinding
import com.flower.flow.ui.activity.IntegralRechargeActivity
import com.flower.flow.ui.activity.VipJoinActivity
import me.hgj.jetpackmvvm.core.net.interception.logging.util.LogUtils
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.statusPadding

class MeFragment : BaseFragment<MeViewModel, FragmentMeBinding>() {

    companion object {
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
            //编辑
        }
    }

    override fun lazyLoadData() {
        LogUtils.debugInfo("MeFragment", "lazyLoadData")
    }

    override fun createObserver() {
        EventViewModel.mainFragmentDataEvent.observe(viewLifecycleOwner) {
            LogUtils.debugInfo("MeFragment", "收到通知")
        }

        EventViewModel.msgRedDotEvent.observe(this) { show ->
            mBind.redDot.isVisible = show
        }

        UserManager.observeUser().observe(viewLifecycleOwner) { user ->
            user?.apply {
                setUserInfo(user)
            }
        }
    }

    private fun setUserInfo(user: UserInfo) {
        val isVip = user.isVip
        mBind.clVip.isVisible =
            (!isVip) && ((App.globalConfig?.integralAndVipEntranceShow ?: 0) == 1)
        mBind.ivVipLabel.isVisible = isVip

        val avatar = user.avatar
        if (avatar.isNotEmpty()) {
            mBind.ivAvatar.loadImage(avatar)
        }

        mBind.tvName.text = user.name
        mBind.tvMoney.text = user.integralBalance.toString()
    }

    private fun setText() {
        mBind.vipTitleText.text = FlowCopyStore.get(FlowCopyKey.VIDEO_PRO_NAME)
        mBind.vipSubtitleText.text = FlowCopyStore.get(FlowCopyKey.VIP_UNLOCK_HINT)
        mBind.btnVip.text = FlowCopyStore.get(FlowCopyKey.VIP_OPEN_HINT)
    }
}