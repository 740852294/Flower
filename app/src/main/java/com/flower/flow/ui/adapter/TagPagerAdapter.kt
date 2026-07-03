package com.flower.flow.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.flower.flow.data.model.entity.TagItem
import com.flower.flow.ui.fragment.TagListFragment

class TagPagerAdapter(
    fragment: Fragment,
) : FragmentStateAdapter(fragment) {

    private var tags: List<TagItem> = emptyList()
    private var generation = 0

    fun submitTags(newTags: List<TagItem>, recreateFragments: Boolean = false) {
        if (recreateFragments) {
            generation++
        }
        tags = newTags
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = tags.size

    override fun createFragment(position: Int): Fragment {
        return TagListFragment.newInstance(tags[position].acetoneactuate)
    }

    override fun getItemId(position: Int): Long {
        return tags[position].acetoneactuate.toLong() + generation * GENERATION_ID_OFFSET
    }

    override fun containsItem(itemId: Long): Boolean {
        return tags.any { tag ->
            tag.acetoneactuate.toLong() + generation * GENERATION_ID_OFFSET == itemId
        }
    }

    companion object {
        private const val GENERATION_ID_OFFSET = 1_000_000L
    }
}
