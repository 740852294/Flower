package com.flower.flow.data.model

import com.flower.flow.data.model.entity.GlobalConfig
import com.flower.flow.data.model.entity.UserInfo

sealed class MainInitResult {
    data class ForceUpdate(
        val title: String,
        val content: String,
        val buttonMsg: String,
    ) : MainInitResult()

    data class Ready(
        val globalConfig: GlobalConfig,
        val userInfo: UserInfo,
    ) : MainInitResult()
}
