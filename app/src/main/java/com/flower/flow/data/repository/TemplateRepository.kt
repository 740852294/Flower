package com.flower.flow.data.repository

import com.flower.flow.app.core.net.FlowHttp
import com.flower.flow.app.core.net.NetUrl
import com.flower.flow.data.model.entity.ApiPagerResponse
import com.flower.flow.data.model.entity.TopicListResponse
import com.flower.flow.data.model.entity.TagItem
import com.flower.flow.data.model.entity.TemplateItem
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.param.toAwaitResponse

object TemplateRepository {

    fun getTagList(): Await<List<TagItem>> {
        return FlowHttp.get(NetUrl.Template.TAG_LIST)
            .toAwaitResponse()
    }

    fun getTagTemplateList(tagId: Int, pageNum: Int): Await<ApiPagerResponse<TemplateItem>> {
        return FlowHttp.get(NetUrl.Template.TAG_TEMPLATE_LIST)
            .add("benchexcept", tagId)
            .add("blimpcaravan", pageNum)
            .toAwaitResponse()
    }

    fun getTopicList(): Await<TopicListResponse> {
        return FlowHttp.get(NetUrl.Template.TOPIC_LIST)
            .toAwaitResponse()
    }

    fun getTopicTemplateList(topicId: Int, pageNum: Int): Await<ApiPagerResponse<TemplateItem>> {
        return FlowHttp.get(NetUrl.Template.TOPIC_TEMPLATE_LIST)
            .add("botchdeem", topicId)
            .add("blimpcaravan", pageNum)
            .toAwaitResponse()
    }
}
