package com.flower.flow.ui.activity

import android.os.Bundle
import com.flower.flow.R
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.vm.MainViewModel
import com.flower.flow.databinding.ActivityMainBinding
import com.flower.flow.ui.adapter.MainAdapter

class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        mBind.mainNavigation.itemIconTintList = null

        mBind.mainNavigation.menu.findItem(R.id.menu_topic).title =
            FlowCopyStore.get(FlowCopyKey.HOME_TAB)
        mBind.mainNavigation.menu.findItem(R.id.menu_tag).title =
            FlowCopyStore.get(FlowCopyKey.EXPLORE_TAB)
        mBind.mainNavigation.menu.findItem(R.id.menu_me).title =
            FlowCopyStore.get(FlowCopyKey.MINE_TAB)

        mBind.mainViewPager.adapter = MainAdapter(this)
        mBind.mainViewPager.offscreenPageLimit = mBind.mainViewPager.adapter!!.itemCount
        mBind.mainViewPager.isUserInputEnabled = false

        mBind.mainNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_topic -> mBind.mainViewPager.setCurrentItem(0, false)
                R.id.menu_tag -> mBind.mainViewPager.setCurrentItem(1, false)
                R.id.menu_me -> mBind.mainViewPager.setCurrentItem(2, false)
            }
            true
        }
        mBind.mainNavigation.selectedItemId = R.id.menu_topic
    }
}
