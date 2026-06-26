package me.hgj.jetpackmvvm.core.init

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.hgj.jetpackmvvm.ext.util.logD

/**
 * 作者　：hegaojian
 * 时间　：2025/9/29
 * 说明　：初始化任务管理器
 * 示例  ：
 * ```kotlin
 * InitTaskManager
 *     .register(NetTask())
 *     .register(UtilTask())
 *     .register(WidgetTask())
 *     .execute(this)
 * ```
 */
object InitTaskManager {

    private val tasks = mutableListOf<InitTask>()

    /**
     * 任务是否都初始化完毕
     */
    @Volatile
    var isInitialized: Boolean = false
        private set

    private var onAllTasksDone: (() -> Unit)? = null

    /**
     * 注册任务
     */
    fun register(task: InitTask): InitTaskManager {
        tasks.add(task)
        return this
    }

    /**
     * 全部任务完成的回调
     */
    fun onComplete(callback: () -> Unit): InitTaskManager {
        onAllTasksDone = callback
        return this
    }

    /**
     * 执行任务
     */
    fun execute(app: Application) {
        if (tasks.isEmpty()) {
            isInitialized = true
            onAllTasksDone?.invoke()
            return
        }

        val asyncTasks = tasks.filter { !it.isBlocking || !it.runOnMainThread }

        // 1️⃣ 主线程阻塞任务（关键任务）
        tasks.filter { it.runOnMainThread && it.isBlocking }.forEach {
            runBlocking {
                runTask(app, it)
            }
        }

        // 2️⃣ 异步任务
        if (asyncTasks.isEmpty()) {
            markAllDone()
            return
        }

        asyncTasks.forEach { task ->
            CoroutineScope(Dispatchers.Default).launch {
                runTask(app, task)
                checkIfAllTasksDone(asyncTasks)
            }
        }
    }

    /**
     * 检查是否全部异步任务完成
     */
    @Synchronized
    private fun checkIfAllTasksDone(asyncTasks: List<InitTask>) {
        val allDone = asyncTasks.all {
            (it as? BaseInitTask)?.isFinished == true
        }
        if (allDone && !isInitialized) {
            markAllDone()
        }
    }

    private fun markAllDone() {
        isInitialized = true
        onAllTasksDone?.invoke()
        "✅ 所有初始化任务完成".logD("InitTaskManager")
    }

    /**
     * 统一执行逻辑（自动记录完成状态）
     */
    private suspend fun runTask(app: Application, task: InitTask) {
        val start = System.currentTimeMillis()
        try {
            task.init(app)
            (task as? BaseInitTask)?.isFinished = true
            val cost = System.currentTimeMillis() - start
            "✅ ${task.name} 初始化完成，耗时 ${cost}ms".logD("InitTaskManager")
        } catch (e: Exception) {
            (task as? BaseInitTask)?.isFinished = true
            "❌ ${task.name} 初始化失败：${e.message}".logD("InitTaskManager")
        }
    }
}
