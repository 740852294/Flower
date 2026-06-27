package com.flower.flow.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.flower.flow.ui.fragment.MeFragment
import com.flower.flow.ui.fragment.TagFragment
import com.flower.flow.ui.fragment.TopicFragment

class MainAdapter(
    activity: FragmentActivity
) : FragmentStateAdapter(activity) {

    companion object {
        const val PAGE_TOPIC = 0
        const val PAGE_TAG = 1
        const val PAGE_USER = 2
    }

    private val fragmentCache = mutableMapOf<Int, Fragment>()

    override fun createFragment(position: Int): Fragment {
        fragmentCache[position]?.let { return it }
        val fragment = when (position) {
            PAGE_TOPIC -> TopicFragment.newInstance()
            PAGE_TAG -> TagFragment.newInstance()
            PAGE_USER -> MeFragment.newInstance()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
        fragmentCache[position] = fragment
        return fragment
    }

    override fun getItemCount(): Int = 3

    fun getFragment(position: Int): Fragment? = fragmentCache[position]
}
