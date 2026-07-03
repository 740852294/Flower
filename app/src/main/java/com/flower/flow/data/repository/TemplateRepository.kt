package com.flower.flow.data.repository

import com.flower.flow.app.core.net.NetUrl
import com.flower.flow.app.core.util.RandomDataUtil
import com.flower.flow.data.model.entity.ApiPagerResponse
import com.flower.flow.data.model.entity.TagItem
import com.flower.flow.data.model.entity.TemplateItem
import com.flower.flow.data.model.entity.TopicListResponse
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toAwaitResponse

object TemplateRepository {

    fun getTagList(): Await<List<TagItem>> {
        return RxHttp.get(NetUrl.Template.TAG_LIST)
            .add("hatebilberry", RandomDataUtil.getRandomData(3))
            .add("actinicgift", RandomDataUtil.getRandomData(5))
            .toAwaitResponse()
    }

    fun getTagTemplateList(tagId: Int, pageNum: Int): Await<ApiPagerResponse<TemplateItem>> {
        return RxHttp.get(NetUrl.Template.TAG_TEMPLATE_LIST)
            .add("benchexcept", tagId)
            .add("blimpcaravan", pageNum)
            .toAwaitResponse()
    }

    fun getTopicList(): Await<TopicListResponse> {
        return RxHttp.get(NetUrl.Template.TOPIC_LIST)
            .add("behavemad", RandomDataUtil.getRandomData(1))
            .add("anxiousamenable", RandomDataUtil.getRandomData(4))
            .toAwaitResponse()
    }

    fun getTopicTemplateList(topicId: Int, pageNum: Int): Await<ApiPagerResponse<TemplateItem>> {
        return RxHttp.get(NetUrl.Template.TOPIC_TEMPLATE_LIST)
            .add("botchdeem", topicId)
            .add("blimpcaravan", pageNum)
            .toAwaitResponse()
    }
}
