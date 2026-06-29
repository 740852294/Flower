package com.flower.flow.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.flower.flow.data.model.entity.TagItem
import com.flower.flow.ui.fragment.TagListFragment

class TagPagerAdapter(
    fragment: Fragment,
) : FragmentStateAdapter(fragment) {

    private var tags: List<TagItem> = emptyList()

    fun submitTags(newTags: List<TagItem>) {
        tags = newTags
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = tags.size

    override fun createFragment(position: Int): Fragment {
        return TagListFragment.newInstance(tags[position].id)
    }

    override fun getItemId(position: Int): Long = tags[position].id.toLong()

    override fun containsItem(itemId: Long): Boolean {
        return tags.any { it.id.toLong() == itemId }
    }
}
