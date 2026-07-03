package com.flower.flow.data.repository

import com.flower.flow.app.core.net.NetUrl
import com.flower.flow.app.core.util.RandomDataUtil
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
            .add("naturebroker", id)
            .toAwaitResponse()
    }

    fun generateWork(aiartId: Int, files: List<File>): Await<WorkGenerateResult> {
        var request = RxHttp.postForm(NetUrl.AiArt.GENERATE_WORK)
            .add("naturebroker", aiartId)
            .add("acknowledgedevote", RandomDataUtil.getRandomData(2))
            .add("crinkleaquarium", RandomDataUtil.getRandomData(3))
        files.forEachIndexed { index, file ->
            request = request.addFile("file${index + 1}", file)
        }
        return request.toAwaitResponse()
    }

    fun generateWorkAgain(taskId: String): Await<WorkGenerateResult> {
        return RxHttp.postForm(NetUrl.AiArt.GENERATE_WORK_AGAIN)
            .add("baptismdictate", taskId)
            .add("fightbackstage", RandomDataUtil.getRandomData(5))
            .toAwaitResponse()
    }

    fun getWorkTaskList(pageNum: Int): Await<ApiPagerResponse<WorkItem>> {
        return RxHttp.get(NetUrl.AiArt.WORK_LIST)
            .add("blimpcaravan", pageNum)
            .add("cartel", RandomDataUtil.getRandomData(5))
            .toAwaitResponse()
    }

    fun deleteWorkTasks(taskIds: List<String>): Await<Any> {
        val request = RxHttp.postForm(NetUrl.AiArt.WORK_DELETE)
            .add("dazeamiable", RandomDataUtil.getRandomData(1))
            .add("abstractought", RandomDataUtil.getRandomData(1))
        taskIds.forEach { taskId -> request.add("laughbeef", taskId) }
        return request.toAwaitResponse()
    }

    fun recordWorkDownloaded(taskId: String): Await<Any> {
        return RxHttp.postForm(NetUrl.AiArt.WORK_DOWNLOAD)
            .add("baptismdictate", taskId)
            .add("drivebride", RandomDataUtil.getRandomData(3))
            .toAwaitResponse()
    }
}
