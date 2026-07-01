package com.flower.flow.ui.activity

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.flower.flow.R
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.ext.initClose
import com.flower.flow.app.core.ext.loadImageFitWidth
import com.flower.flow.data.vm.TopicTemplateListViewModel
import com.flower.flow.databinding.ActivityTopicTemplateListBinding
import me.hgj.jetpackmvvm.ext.util.intent.extraAct
import me.hgj.jetpackmvvm.ext.util.loadMore
import me.hgj.jetpackmvvm.ext.util.statusPadding

class TopicTemplateListActivity :
    BaseActivity<TopicTemplateListViewModel, ActivityTopicTemplateListBinding>() {

    val topicId: Int by extraAct(EXTRA_TOPIC_ID, 0)

    val topicName: String by extraAct(EXTRA_TOPIC_NAME, "")

    val topicDescription: String by extraAct(EXTRA_TOPIC_DESCRIPTION, "")

    val topicImg: String by extraAct(EXTRA_TOPIC_IMG, "")

    override val showTitle: Boolean
        get() = false

    override fun initView(savedInstanceState: Bundle?) {
        mBind.llToolbar.statusPadding()
        mBind.toolbar.initClose(topicName) {
            finish()
        }

        mBind.tvDes.text = topicDescription

        if (topicImg.isNotEmpty()) {
            mBind.ivImg.loadImageFitWidth(url = topicImg)
        }

        mBind.refreshLayout.loadMore {

        }

        mBind.rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                updateToolbarBackground()
            }
        })
        updateToolbarBackground()
    }

    private fun updateToolbarBackground() {
        val atTop = !mBind.rvList.canScrollVertically(-1)
        val colorRes = if (atTop) R.color.transparent else R.color.black
        mBind.llToolbar.setBackgroundColor(ContextCompat.getColor(this, colorRes))
    }

    companion object {
        const val EXTRA_TOPIC_ID = "topic_id"
        const val EXTRA_TOPIC_NAME = "topic_name"
        const val EXTRA_TOPIC_DESCRIPTION = "topic_description"
        const val EXTRA_TOPIC_IMG = "topic_img"
    }
}
