package com.flower.flow.app.core.net

import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.data.model.FlowCopyKey
import me.hgj.jetpackmvvm.core.net.Error
import me.hgj.jetpackmvvm.core.net.ExceptionHandle

object AppExceptionMessageProvider {

    fun install() {
        ExceptionHandle.errorMessageProvider = { error ->
            val copyKey = when (error) {
                Error.PARSE_ERROR -> FlowCopyKey.DATA_PARSE_FAIL
                Error.NETWORK_ERROR -> FlowCopyKey.NET_FAIL
                Error.TIMEOUT_ERROR -> FlowCopyKey.CONNECT_TIMEOUT
                Error.SSL_ERROR, Error.UNKNOWN -> FlowCopyKey.REQUEST_FAIL
            }
            FlowCopyStore.get(copyKey)
        }
    }
}
