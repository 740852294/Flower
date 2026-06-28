package com.flower.flow.ui.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.flower.flow.R
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.ext.dismissAppLoadingExt
import com.flower.flow.app.core.ext.showAppLoadingExt
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.model.MainInitResult
import com.flower.flow.data.vm.MainViewModel
import com.flower.flow.databinding.ActivityMainBinding
import com.flower.flow.databinding.LayoutMainBottomTabItemBinding
import com.flower.flow.ui.adapter.MainAdapter
import com.flower.flow.ui.dialog.CommonMessageDialog
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {

    override val showTitle = false

    private var msgRedDot = false
    private var workRedDot = false
    private var userInfoPollingStarted = false

    private data class TabConfig(
        val binding: LayoutMainBottomTabItemBinding,
        val iconRes: Int,
        val titleKey: String,
        val pageIndex: Int,
    )

    private lateinit var tabs: List<TabConfig>
    private var currentTabIndex = -1

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun createObserver() {
        mViewModel.initMain().obs(this) {
            onSuccess { result ->
                when (result) {
                    is MainInitResult.ForceUpdate -> showForceUpdateDialog(result)
                    is MainInitResult.Ready -> setupMainContent()
                }
            }
        }

        EventViewModel.msgRedDotEvent.observe(this) { show ->
            msgRedDot = show == true
            updateMeTabRedDot()
        }

        EventViewModel.workRedDotEvent.observe(this) { show ->
            workRedDot = show == true
            updateMeTabRedDot()
        }
    }

    private fun updateMeTabRedDot() {
        mBind.tabMe.redDot.isVisible = msgRedDot || workRedDot
    }

    private fun showForceUpdateDialog(result: MainInitResult.ForceUpdate) {
        CommonMessageDialog.Builder(this)
            .setTitle(result.title)
            .setContent(result.content)
            .setConfirmButton(result.buttonMsg) {
                openMarket()
            }
            .show()
    }

    private fun setupMainContent() {

        tabs = listOf(
            TabConfig(
                mBind.tabTopic,
                R.drawable.menu_topic,
                FlowCopyKey.HOME_TAB,
                MainAdapter.PAGE_TOPIC,
            ),
            TabConfig(
                mBind.tabTag,
                R.drawable.menu_tag,
                FlowCopyKey.EXPLORE_TAB,
                MainAdapter.PAGE_TAG,
            ),
            TabConfig(
                mBind.tabMe,
                R.drawable.menu_me,
                FlowCopyKey.MINE_TAB,
                MainAdapter.PAGE_USER,
            ),
        )

        tabs.forEach { tab ->
            tab.binding.tabLabel.text = FlowCopyStore.get(tab.titleKey)
            tab.binding.tabIcon.setImageResource(tab.iconRes)
            tab.binding.root.clickNoRepeat { selectTab(tab.pageIndex) }
        }

        mBind.mainViewPager.adapter = MainAdapter(this)
        mBind.mainViewPager.offscreenPageLimit = mBind.mainViewPager.adapter!!.itemCount
        mBind.mainViewPager.isUserInputEnabled = false
        selectTab(MainAdapter.PAGE_TOPIC)

        getUserInfo(true)
        startUserInfoPolling()
    }

    private fun startUserInfoPolling() {
        if (userInfoPollingStarted) return
        userInfoPollingStarted = true
        lifecycleScope.launch {
            while (true) {
                delay(USER_INFO_POLL_INTERVAL)
                getUserInfo(false)
            }
        }
    }

    fun getUserInfo(isFirst: Boolean) {
        mViewModel.fetchUserInfo(isFirst).obs(this){
            onSuccess { userInfo ->
                if (isFirst && !userInfo.isVip && (App.globalConfig?.integralAndVipEntranceShow ?: 0) == 1) {
                    openActivity<VipJoinActivity>()
                }
            }
            onError { error ->
                if (isFirst) {
                    error.toast()
                }
            }
        }
    }

    fun switchTab(index: Int) {
        if (!::tabs.isInitialized) return
        selectTab(index)
    }

    private fun selectTab(index: Int) {
        val previousTab = currentTabIndex
        if (previousTab != index) {
            if (index == MainAdapter.PAGE_TOPIC && previousTab != -1) {
                getUserInfo(false)
            }
            currentTabIndex = index
        }
        tabs.forEach { tab ->
            tab.binding.tabIcon.isSelected = tab.pageIndex == index
        }
        mBind.mainViewPager.setCurrentItem(index, false)
    }

    private fun openMarket() {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                "market://details?id=$packageName".toUri(),
            ).apply {
                setPackage("com.android.vending")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$packageName".toUri(),
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
    }

    private companion object {
        private const val USER_INFO_POLL_INTERVAL = 2 * 60 * 1000L
    }
}
