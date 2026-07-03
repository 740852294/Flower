package com.flower.flow.app.core.net

import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.data.model.StringResId
import me.hgj.jetpackmvvm.core.net.Error
import me.hgj.jetpackmvvm.core.net.ExceptionHandle

object AppExceptionMessageProvider {

    fun install() {
        ExceptionHandle.errorMessageProvider = { error ->
            val copyKey = when (error) {
                Error.PARSE_ERROR -> StringResId.DATA_PARSE_FAIL
                Error.NETWORK_ERROR -> StringResId.NET_FAIL
                Error.TIMEOUT_ERROR -> StringResId.CONNECT_TIMEOUT
                Error.SSL_ERROR, Error.UNKNOWN -> StringResId.REQUEST_FAIL
            }
            AppStrings.get(copyKey)
        }
    }
}
