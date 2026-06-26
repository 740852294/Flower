package com.flower.flow.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.flower.flow.ui.fragment.MeFragment
import com.flower.flow.ui.fragment.TagFragment
import com.flower.flow.ui.fragment.TopicFragment

class MainAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    companion object {
        const val PAGE_TOPIC = 0
        const val PAGE_TAG = 1
        const val PAGE_USER = 2
    }

    // 缓存已创建的 Fragment
    private val fragmentCache = mutableMapOf<Int, Fragment>()

    override fun createFragment(position: Int): Fragment {
        // 如果缓存里有，直接用缓存
        fragmentCache[position]?.let { return it }
        // 没有则创建新的
        val fragment = when (position) {
            PAGE_TOPIC -> TopicFragment.newInstance()
            PAGE_TAG -> TagFragment.newInstance()
            PAGE_USER -> MeFragment.newInstance()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
        // 放进缓存
        fragmentCache[position] = fragment
        return fragment
    }

    override fun getItemCount(): Int = 3

    /**
     * 提供一个方法外部能拿到已缓存的 Fragment
     */
    fun getFragment(position: Int): Fragment? {
        return fragmentCache[position]
    }
}