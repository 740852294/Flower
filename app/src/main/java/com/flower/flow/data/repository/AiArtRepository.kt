package com.flower.flow.data.repository

import com.flower.flow.app.core.net.NetUrl
import com.flower.flow.data.model.entity.ApiPagerResponse
import com.flower.flow.data.model.entity.SubmitPageInfo
import com.flower.flow.data.model.entity.WorkGenerateResult
import com.flower.flow.data.model.entity.WorkItem
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.toAwaitResponse
import java.io.File

object AiArtRepository {

    fun getCreateSubmitPage(id: Int): Await<SubmitPageInfo> {
        return RxHttp.get(NetUrl.AiArt.UPLOAD_PAGE_INFO)
            .add("aiartId", id)
            .toAwaitResponse()
    }

    fun generateWork(aiartId: Int, files: List<File>): Await<WorkGenerateResult> {
        var request = RxHttp.postForm(NetUrl.AiArt.GENERATE_WORK)
            .add("aiartId", aiartId)
        files.forEachIndexed { index, file ->
            request = request.addFile("file${index + 1}", file)
        }
        return request.toAwaitResponse()
    }

    fun getWorkTaskList(pageNum: Int): Await<ApiPagerResponse<WorkItem>> {
        return RxHttp.get(NetUrl.AiArt.WORK_LIST)
            .add("pageNum", pageNum)
            .toAwaitResponse()
    }

    fun deleteWorkTasks(taskIds: List<String>): Await<Any> {
        return RxHttp.postJson(NetUrl.AiArt.WORK_DELETE)
            .add("taskIdArr", taskIds)
            .toAwaitResponse()
    }
}
