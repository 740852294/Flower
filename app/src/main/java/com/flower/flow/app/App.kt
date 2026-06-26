package com.flower.flow.app

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.fresco.vito.init.FrescoVito
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.init.NetTask
import com.flower.flow.app.init.WidgetTask
import me.hgj.jetpackmvvm.core.JetpackMvvm
import me.hgj.jetpackmvvm.core.init.InitTaskManager
import me.hgj.jetpackmvvm.ext.util.isMainProcess

class App : Application(){

    companion object {
        var currentLanguageId: Int = 0
    }

    override fun onCreate() {
        super.onCreate()
        if (isMainProcess()){
            // 只在主进程初始化 SDK
            JetpackMvvm.init(this)
            FlowCopyStore.loadCache()
            Fresco.initialize(this)
            FrescoVito.initialize()
            //启动初始化任务
            InitTaskManager
                .register(NetTask())
                .register(WidgetTask())
                .execute(this)
        }
    }
}