package com.flower.flow.data.repository

import com.flower.flow.app.core.net.NetUrl
import com.flower.flow.data.model.entity.ApiPagerResponse
import com.flower.flow.data.model.entity.TopicListResponse
import com.flower.flow.data.model.entity.TagItem
import com.flower.flow.data.model.entity.TemplateItem
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toAwaitResponse

object TemplateRepository {

    fun getTagList(): Await<List<TagItem>> {
        return RxHttp.get(NetUrl.Template.TAG_LIST)
            .toAwaitResponse()
    }

    fun getTagTemplateList(tagId: Int, pageNum: Int): Await<ApiPagerResponse<TemplateItem>> {
        return RxHttp.get(NetUrl.Template.TAG_TEMPLATE_LIST)
            .add("tagId", tagId)
            .add("pageNum", pageNum)
            .toAwaitResponse()
    }

    fun getTopicList(): Await<TopicListResponse> {
        return RxHttp.get(NetUrl.Template.TOPIC_LIST)
            .toAwaitResponse()
    }

    fun getTopicTemplateList(topicId: Int, pageNum: Int): Await<ApiPagerResponse<TemplateItem>> {
        return RxHttp.get(NetUrl.Template.TOPIC_TEMPLATE_LIST)
            .add("topicId", topicId)
            .add("pageNum", pageNum)
            .toAwaitResponse()
    }
}
