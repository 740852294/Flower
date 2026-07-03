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
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.app.core.util.MainNavigator
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.model.StringResId
import com.flower.flow.data.model.MainInitResult
import com.flower.flow.data.vm.MainViewModel
import com.flower.flow.databinding.ActivityMainBinding
import com.flower.flow.databinding.LayoutMainBottomTabItemBinding
import com.flower.flow.ui.adapter.MainAdapter
import com.flower.flow.ui.dialog.CommonMessageDialog
import com.flower.flow.ui.fragment.MeFragment
import com.flower.flow.ui.fragment.TopicFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.toast
import kotlin.time.Duration.Companion.milliseconds

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
    private var pendingTabIndex: Int? = null

    override fun initView(savedInstanceState: Bundle?) {
        pendingTabIndex = readTargetTab(intent)

        setupMainContent()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val tabIndex = readTargetTab(intent) ?: return
        if (::tabs.isInitialized) {
            switchTab(tabIndex)
        } else {
            pendingTabIndex = tabIndex
        }
    }

    private fun readTargetTab(intent: Intent): Int? {
        val tabIndex = intent.getIntExtra(MainNavigator.EXTRA_TARGET_TAB, -1)
        return tabIndex.takeIf { it >= 0 }
    }

    override fun createObserver() {
        mViewModel.initMain().obs(this) {
            onSuccess { result ->
                when (result) {
                    is MainInitResult.ForceUpdate -> showForceUpdateDialog(result)
                    is MainInitResult.Ready -> initData()
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

        EventViewModel.languageEvent.observe(this) {
            refreshTabLabels()
        }
    }

    private fun refreshTabLabels() {
        if (!::tabs.isInitialized) return
        tabs.forEach { tab ->
            tab.binding.tabLabel.text = AppStrings.get(tab.titleKey)
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
                StringResId.HOME_TAB,
                MainAdapter.PAGE_TOPIC,
            ),
            TabConfig(
                mBind.tabTag,
                R.drawable.menu_tag,
                StringResId.EXPLORE_TAB,
                MainAdapter.PAGE_TAG,
            ),
            TabConfig(
                mBind.tabMe,
                R.drawable.menu_me,
                StringResId.MINE_TAB,
                MainAdapter.PAGE_USER,
            ),
        )

        tabs.forEach { tab ->
            tab.binding.tabLabel.text = AppStrings.get(tab.titleKey)
            tab.binding.tabIcon.setImageResource(tab.iconRes)
            tab.binding.root.clickNoRepeat { selectTab(tab.pageIndex) }
        }

        mBind.mainViewPager.adapter = MainAdapter(this)
        mBind.mainViewPager.offscreenPageLimit = mBind.mainViewPager.adapter!!.itemCount
        mBind.mainViewPager.isUserInputEnabled = false
        selectTab(pendingTabIndex ?: MainAdapter.PAGE_TOPIC)
        pendingTabIndex = null
    }

    private fun initData() {
        supportFragmentManager.setFragmentResult(
            TopicFragment.REQUEST_MAIN_DATA_READY,
            Bundle(),
        )
        getUserInfo(isFirst = true, isLoading = true)
        startUserInfoPolling()
    }

    private fun startUserInfoPolling() {
        if (userInfoPollingStarted) return
        userInfoPollingStarted = true
        lifecycleScope.launch {
            while (true) {
                delay(USER_INFO_POLL_INTERVAL.milliseconds)
                getUserInfo(false, isLoading = false)
            }
        }
    }

    fun getUserInfo(isFirst: Boolean, isLoading: Boolean) {
        mViewModel.fetchUserInfo(isLoading).obs(this) {
            onSuccess { userInfo ->
                if (isFirst && !userInfo.shareengage && (App.globalConfig?.exaltabrade
                        ?: 0) == 1
                ) {
                    openActivity<VipJoinActivity>()
                }
            }
            onError { error ->
                if (isFirst) {
                    error.msg.toast()
                }
            }
        }
    }

    fun switchTab(index: Int) {
        if (!::tabs.isInitialized) return
        selectTab(index)
    }

    fun refreshTopicListSilently() {
        if (!::tabs.isInitialized) return
        val fragment = (mBind.mainViewPager.adapter as? MainAdapter)
            ?.getFragment(MainAdapter.PAGE_TOPIC) as? TopicFragment
        fragment?.refreshHomeTopicList()
    }

    fun refreshMeWorkListSilently() {
        if (!::tabs.isInitialized) return
        val fragment = (mBind.mainViewPager.adapter as? MainAdapter)
            ?.getFragment(MainAdapter.PAGE_USER) as? MeFragment
        fragment?.refreshWorkListSilently()
    }

    private fun selectTab(index: Int) {
        val previousTab = currentTabIndex
        if (previousTab != index) {
            if (index == MainAdapter.PAGE_TOPIC && previousTab != -1) {
                getUserInfo(isFirst = false, isLoading = false)
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
