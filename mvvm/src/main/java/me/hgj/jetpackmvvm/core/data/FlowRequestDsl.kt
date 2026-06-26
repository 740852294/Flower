package me.hgj.jetpackmvvm.core.data

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import me.hgj.jetpackmvvm.R
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.net.LoadStatusEntity
import me.hgj.jetpackmvvm.core.net.LoadingEntity
import me.hgj.jetpackmvvm.core.net.LoadingType
import me.hgj.jetpackmvvm.ext.util.code
import me.hgj.jetpackmvvm.ext.util.getStringExt
import me.hgj.jetpackmvvm.ext.util.logE
import me.hgj.jetpackmvvm.ext.util.msg

/**
 * 基于 Flow 的网络或本地请求 DSL
 * 支持在 onRequest 中自由组合 Flow 操作符，支持多次数据发射
 *
 * 使用示例：
 * ```
 * ViewModel中：
 * fun observeUserData() = requestFlow {
 *     onRequest {
 *         // 可以自由组合多个 Flow
 *         userRepository.getUserFlow()
 *             .combine(settingsRepository.getSettingsFlow()) { user, settings ->
 *                 UserWithSettings(user, settings)
 *             }
 *             .map { it.toUiModel() }
 *     }
 *     loadingType = LoadingType.LOADING_DIALOG
 *     loadingMessage = "正在加载用户数据..."
 * }
 *
 * UI层中：
 * mViewModel.observeUserData().obs(this) {
 *     onSuccess { userData ->
 *         // 支持多次触发，每次数据更新都会调用
 *         updateUI(userData)
 *     }
 *     onError { status ->
 *         showError(status.msg)
 *     }
 * }
 * ```
 */
fun <T> BaseViewModel.requestFlow(
    requestParameterDslClass: FlowRequestParameterDsl<T>.() -> Unit
): SharedFlow<ApiResult<T>> {
    val dsl = FlowRequestParameterDsl<T>().apply(requestParameterDslClass)
    return executeFlowRequestWithResult(dsl)
}

/**
 * 执行 Flow 版本的请求，支持多次数据发射
 */
private fun <T> BaseViewModel.executeFlowRequestWithResult(
    requestParameterDsl: FlowRequestParameterDsl<T>
): SharedFlow<ApiResult<T>> {
    val resultFlow = MutableSharedFlow<ApiResult<T>>(
        replay = requestParameterDsl.replay,
        extraBufferCapacity = requestParameterDsl.extraBufferCapacity
    )
    viewModelScope.launch(Dispatchers.IO) {
        supervisorScope {
            try {
                // 显示 loading（只在开始时显示一次）
                if (requestParameterDsl.loadingType != LoadingType.LOADING_NULL) {
                    loadingChange.loading.postValue = LoadingEntity(
                        loadingType = requestParameterDsl.loadingType,
                        loadingMessage = requestParameterDsl.loadingMessage,
                        isShow = true,
                        coroutineScope = this
                    )
                }
                // 执行 Flow 并收集结果
                requestParameterDsl.onRequest.invoke(this)
                    .onCompletion { cause ->
                        // 请求完成时隐藏 loading
                        if (requestParameterDsl.loadingType != LoadingType.LOADING_NULL) {
                            loadingChange.loading.postValue = LoadingEntity(
                                loadingType = requestParameterDsl.loadingType,
                                loadingMessage = requestParameterDsl.loadingMessage,
                                isShow = false
                            )
                        }
                        // 成功完成
                        if (cause == null && requestParameterDsl.loadingType == LoadingType.LOADING_XML) {
                            loadingChange.showSuccess.postValue = true
                        }
                    }
                    .catch { e ->
                        // Flow 内部的异常处理
                        if (e is CancellationException) return@catch
                        e.printStackTrace()
                        "抱歉！出错了----> : ${e.message}".logE()
                        val loadStatus = LoadStatusEntity(
                            code = e.code,
                            msg = e.msg,
                            throwable = e,
                            loadingType = requestParameterDsl.loadingType
                        )
                        if (loadStatus.loadingType == LoadingType.LOADING_XML) {
                            loadingChange.showError.postValue = loadStatus
                        }
                        resultFlow.emit(ApiResult.Error(loadStatus))
                    }
                    .collect { data ->
                        // 每次发射数据都发送成功结果
                        resultFlow.emit(ApiResult.Success(data))
                    }

            } catch (e: CancellationException) {
                // 请求被取消
                return@supervisorScope
            } catch (e: Exception) {
                // 外层异常处理
                e.printStackTrace()
                "抱歉！出错了----> : ${e.message}".logE()
                if (requestParameterDsl.loadingType != LoadingType.LOADING_NULL) {
                    loadingChange.loading.postValue = LoadingEntity(
                        loadingType = requestParameterDsl.loadingType,
                        loadingMessage = requestParameterDsl.loadingMessage,
                        isShow = false
                    )
                }
                val loadStatus = LoadStatusEntity(
                    code = e.code,
                    msg = e.msg,
                    throwable = e,
                    loadingType = requestParameterDsl.loadingType
                )
                if (loadStatus.loadingType == LoadingType.LOADING_XML) {
                    loadingChange.showError.postValue = loadStatus
                }
                resultFlow.emit(ApiResult.Error(loadStatus))
            }
        }
    }
    return resultFlow.asSharedFlow()
}

/**
 * 增强版 Flow 请求参数封装类
 */
class FlowRequestParameterDsl<T> {
    /**
     * 协程请求方法体，返回一个 Flow<T>，支持多次数据发射
     * 可以自由使用 Flow 的各种操作符组合
     */
    private var _onRequest: (suspend CoroutineScope.() -> Flow<T>)? = null
    var onRequest: suspend CoroutineScope.() -> Flow<T>
        get() = _onRequest ?: { throw IllegalStateException("onRequest 必须实现哦") }
        set(value) {
            _onRequest = value
        }

    /**
     * 执行请求封装
     */
    fun onRequest(block: suspend CoroutineScope.() -> Flow<T>) {
        _onRequest = block
    }

    /** 加载提示内容 */
    var loadingMessage: String = getStringExt(R.string.helper_loading_tip)

    /** 请求时loading类型 默认请求时不显示loading,
     * 注意：如果有多次发送值的应用场景建议传LoadingType.LOADING_NULL ，比如轮询请求，实时数据流等
     * */
    @LoadingType
    var loadingType = LoadingType.LOADING_NULL

    /** SharedFlow 的重放数量
     * ```
     *1.这个参数指定了将最近发射过的多少个值重放给新的订阅者。例如，如果设置为1，那么每个新的订阅者都会立即收到最近发射的一个值。如果设置为0，那么新的订阅者不会收到任何之前发射的值，只会收到订阅后新发射的值。
     *
     *2.应用场景：当你希望新订阅者能够立即获得最新状态时，可以设置replay>0。比如，在状态管理中，你希望UI组件在配置变化后重建时能够立即获得最新的状态，就可以设置replay=1。
     * ```
     * */
    var replay: Int = 0

    /** SharedFlow 的额外缓冲容量
     * ```
     * 1.这个参数用于配置除了replay之外还可以缓冲多少个数。SharedFlow 的总缓冲容量 = replay + extraBufferCapacity。当有发射值而订阅者尚未消费时，这些值会被缓冲起来，直到达到缓冲容量上限。默认情况下，SharedFlow 的发射操作会挂起，直到有缓冲空间可用（除非使用tryEmit）。
     *
     * 2.应用场景：当你希望处理背压（backpressure）时，可以通过调整缓冲容量来控制。较大的缓冲容量可以允许发射者在不挂起的情况下发射更多值，但可能会消耗更多内存。注意，即使设置了额外的缓冲容量，新的订阅者仍然只能收到replay个最近的值。
     * ```
     * */
    var extraBufferCapacity: Int = 0
}