package com.flower.flow.app.core.attribution

import android.content.Context
import com.flower.flow.data.model.entity.RegisterResponse
import com.flower.flow.data.repository.UserRepository

object FlowerRegisterCoordinator {

    suspend fun registerByAttribution(context: Context): RegisterResponse {
        val payload = FlowerAttributionResolver.resolve(context)
        return UserRepository.registerLivedata(
            code = payload.code,
            source = payload.source,
            sourceFlag = payload.sourceFlag,
            step = payload.step,
        ).await()
    }
}
