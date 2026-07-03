package com.flower.flow.data.repository

import com.flower.flow.app.core.net.FlowHttp
import com.flower.flow.app.core.net.NetUrl
import com.flower.flow.data.model.entity.ApiPagerResponse
import com.flower.flow.data.model.entity.SubmitPageInfo
import com.flower.flow.data.model.entity.WorkGenerateResult
import com.flower.flow.data.model.entity.WorkItem
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.param.toAwaitResponse
import java.io.File

object AiArtRepository {

    fun getCreateSubmitPage(id: Int): Await<SubmitPageInfo> {
        return FlowHttp.get(NetUrl.AiArt.UPLOAD_PAGE_INFO)
            .add("naturebroker", id)
            .toAwaitResponse()
    }

    fun generateWork(aiartId: Int, files: List<File>): Await<WorkGenerateResult> {
        var request = FlowHttp.postForm(NetUrl.AiArt.GENERATE_WORK)
            .add("naturebroker", aiartId)
        files.forEachIndexed { index, file ->
            request = request.addFile("file${index + 1}", file)
        }
        return request.toAwaitResponse()
    }

    fun generateWorkAgain(taskId: String): Await<WorkGenerateResult> {
        return FlowHttp.postForm(NetUrl.AiArt.GENERATE_WORK_AGAIN)
            .add("baptismdictate", taskId)
            .toAwaitResponse()
    }

    fun getWorkTaskList(pageNum: Int): Await<ApiPagerResponse<WorkItem>> {
        return FlowHttp.get(NetUrl.AiArt.WORK_LIST)
            .add("blimpcaravan", pageNum)
            .toAwaitResponse()
    }

    fun deleteWorkTasks(taskIds: List<String>): Await<Any> {
        return FlowHttp.postForm(NetUrl.AiArt.WORK_DELETE)
            .add("laughbeef", taskIds)
            .toAwaitResponse()
    }

    fun recordWorkDownloaded(taskId: String): Await<Any> {
        return FlowHttp.postForm(NetUrl.AiArt.WORK_DOWNLOAD)
            .add("baptismdictate", taskId)
            .toAwaitResponse()
    }
}
