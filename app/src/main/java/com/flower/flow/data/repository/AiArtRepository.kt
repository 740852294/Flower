package com.flower.flow.data.repository

import com.flower.flow.app.core.net.NetUrl
import com.flower.flow.data.model.entity.SubmitPageInfo
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toAwaitResponse

object AiArtRepository {

    fun getCreateSubmitPage(id: Int): Await<SubmitPageInfo> {
        return RxHttp.get(NetUrl.AiArt.UPLOAD_PAGE_INFO)
            .add("aiartId", id)
            .toAwaitResponse()
    }
}
