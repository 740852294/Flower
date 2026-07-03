package com.flower.flow.domain.profile

import android.content.Context
import com.flower.flow.app.core.util.WorkDownloadStorage
import com.flower.flow.data.model.entity.WorkItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rxhttp.toDownloadFlow
import rxhttp.wrapper.param.RxHttp

class WorkDownloadJobManager(
    private val scope: CoroutineScope,
    private val appContext: Context,
    private val onProgress: (taskId: String, progress: Int?) -> Unit,
    private val onFinished: (taskId: String, success: Boolean) -> Unit,
) {

    data class DownloadUiState(val progress: Int?)

    private val downloadStates = mutableMapOf<String, DownloadUiState>()
    private val downloadJobs = mutableMapOf<String, Job>()

    fun stateOf(taskId: String): DownloadUiState? = downloadStates[taskId]

    fun isActive(taskId: String): Boolean = downloadJobs[taskId]?.isActive == true

    fun start(workItem: WorkItem) {
        val taskId = workItem.baptismdictate
        if (
            taskId.isBlank() ||
            workItem.wantbirdcage.isBlank() ||
            isActive(taskId)
        ) {
            return
        }

        downloadStates[taskId] = DownloadUiState(progress = 0)
        onProgress(taskId, 0)

        downloadJobs[taskId] = scope.launch {
            var destination: WorkDownloadStorage.Destination? = null
            var downloadCompleted = false
            try {
                destination = withContext(Dispatchers.IO) {
                    WorkDownloadStorage.createDestination(appContext, workItem)
                }
                val uri = destination.uri
                val tempFile = destination.tempFile
                when {
                    uri != null -> {
                        RxHttp.get(workItem.wantbirdcage)
                            .toDownloadFlow(appContext, uri)
                            .onProgress { progress ->
                                val value = progress.progress.takeIf { progress.totalSize > 0 }
                                downloadStates[taskId] = DownloadUiState(value)
                                onProgress(taskId, value)
                            }
                            .collect { }
                    }

                    tempFile != null -> {
                        RxHttp.get(workItem.wantbirdcage)
                            .toDownloadFlow(tempFile.absolutePath)
                            .onProgress { progress ->
                                val value = progress.progress.takeIf { progress.totalSize > 0 }
                                downloadStates[taskId] = DownloadUiState(value)
                                onProgress(taskId, value)
                            }
                            .collect { }
                    }
                }
                withContext(Dispatchers.IO) {
                    WorkDownloadStorage.complete(appContext, destination)
                }
                downloadCompleted = true
            } catch (_: CancellationException) {
            } catch (_: Throwable) {
            } finally {
                if (!downloadCompleted) {
                    withContext(NonCancellable + Dispatchers.IO) {
                        WorkDownloadStorage.cleanup(appContext, destination)
                    }
                }
                downloadStates.remove(taskId)
                downloadJobs.remove(taskId)
                onFinished(taskId, downloadCompleted)
            }
        }
    }
}
