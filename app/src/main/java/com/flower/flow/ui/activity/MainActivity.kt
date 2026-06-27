package com.flower.flow.ui.activity

import android.os.Bundle
import com.flower.flow.R
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.vm.MainViewModel
import com.flower.flow.databinding.ActivityMainBinding
import com.flower.flow.databinding.LayoutMainBottomTabItemBinding
import com.flower.flow.ui.adapter.MainAdapter
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat

class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {

    override val showTitle = false

    private data class TabConfig(
        val binding: LayoutMainBottomTabItemBinding,
        val iconRes: Int,
        val titleKey: String,
        val pageIndex: Int,
    )

    private lateinit var tabs: List<TabConfig>

    override fun initView(savedInstanceState: Bundle?) {
        tabs = listOf(
            TabConfig(
                mBind.tabTopic,
                R.drawable.menu_topic,
                FlowCopyKey.HOME_TAB,
                MainAdapter.PAGE_TOPIC
            ),
            TabConfig(
                mBind.tabTag,
                R.drawable.menu_tag,
                FlowCopyKey.EXPLORE_TAB,
                MainAdapter.PAGE_TAG
            ),
            TabConfig(mBind.tabMe, R.drawable.menu_me, FlowCopyKey.MINE_TAB, MainAdapter.PAGE_USER),
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
    }

    private fun selectTab(index: Int) {
        tabs.forEach { tab ->
            val selected = tab.pageIndex == index
            tab.binding.tabIcon.isSelected = selected
        }
        mBind.mainViewPager.setCurrentItem(index, false)
    }
}
