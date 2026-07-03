package com.flower.flow.app

import android.app.Application
import com.facebook.cache.disk.DiskCacheConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.fresco.vito.init.FrescoVito
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.flower.flow.app.core.net.AppExceptionMessageProvider
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.data.model.entity.GlobalConfig
import com.flower.flow.app.init.NetTask
import com.flower.flow.app.init.WidgetTask
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
            FlowCopyStore.loadCache()
            AppExceptionMessageProvider.install()
            initFrescoCache()
            //启动初始化任务
            InitTaskManager
                .register(NetTask())
                .register(WidgetTask())
                .execute(this)
        }
    }

    private fun initFrescoCache() {
        val diskCacheConfig = DiskCacheConfig.newBuilder(this)
            // 缓存目录名
            .setBaseDirectoryName("fresco_cache")
            // 缓存在 app cache 目录下
            .setBaseDirectoryPath(cacheDir)
            // 最大缓存大小，比如 500MB
            .setMaxCacheSize(500L * 1024L * 1024L)
            // 低磁盘空间时缓存大小
            .setMaxCacheSizeOnLowDiskSpace(50L * 1024L * 1024L)
            // 极低磁盘空间时缓存大小
            .setMaxCacheSizeOnVeryLowDiskSpace(20L * 1024L * 1024L)
            .build()

        val imagePipelineConfig = ImagePipelineConfig.newBuilder(this)
            .setMainDiskCacheConfig(diskCacheConfig)
            .build()

        Fresco.initialize(this, imagePipelineConfig)
        FrescoVito.initialize()
    }
}