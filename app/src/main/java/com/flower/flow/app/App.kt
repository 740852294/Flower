package com.flower.flow.app

import android.app.Application
import com.flower.flow.app.core.net.AppExceptionMessageProvider
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.app.init.NetTask
import com.flower.flow.app.init.WidgetTask
import com.flower.flow.data.model.entity.GlobalConfig
import me.hgj.jetpackmvvm.core.JetpackMvvm
import me.hgj.jetpackmvvm.core.init.InitTaskManager
import me.hgj.jetpackmvvm.ext.util.isMainProcess

class App : Application(){

    companion object {
        var currentLanguageId: Int = 0
        var globalConfig: GlobalConfig? = null
    }

    override fun onCreate() {
        super.onCreate()
        if (isMainProcess()){
            // 只在主进程初始化 SDK
            JetpackMvvm.init(this)
            AppStrings.loadCache()
            AppExceptionMessageProvider.install()
            //启动初始化任务
            InitTaskManager
                .register(NetTask())
                .register(WidgetTask())
                .execute(this)
        }
    }
}
