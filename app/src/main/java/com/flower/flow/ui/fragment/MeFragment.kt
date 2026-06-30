package com.flower.flow.ui.fragment

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.flower.flow.R
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseFragment
import com.flower.flow.app.core.ext.loadAvatarFile
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.app.core.widget.CenterImageSpan
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.model.entity.UserInfo
import com.flower.flow.data.vm.MeViewModel
import com.flower.flow.databinding.FragmentMeBinding
import com.flower.flow.ui.activity.EditUserInfoActivity
import com.flower.flow.ui.activity.IntegralRechargeActivity
import com.flower.flow.ui.activity.MainActivity
import com.flower.flow.ui.activity.SettingActivity
import com.flower.flow.ui.activity.VipJoinActivity
import com.flower.flow.ui.adapter.MainAdapter
import me.hgj.jetpackmvvm.core.net.interception.logging.util.LogUtils
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.intent.openActivityForResult
import me.hgj.jetpackmvvm.ext.util.logD
import me.hgj.jetpackmvvm.ext.util.statusPadding
import me.hgj.jetpackmvvm.ext.util.toast

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
        LogUtils.debugInfo("MeFragment", "lazyLoadData")

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
}