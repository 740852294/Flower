package com.flower.flow.app.init

import android.app.Application
import com.flower.flow.R
import com.flower.flow.app.core.widget.ProgressOnlyFooter
import com.flower.flow.app.core.widget.loadCallBack.EmptyCallback
import com.flower.flow.app.core.widget.loadCallBack.ErrorCallback
import com.flower.flow.app.core.widget.loadCallBack.LoadingCallback
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import me.hgj.jetpackmvvm.core.init.BaseInitTask
import me.hgj.jetpackmvvm.ext.util.getColorExt
import me.hgj.jetpackmvvm.widget.loadsir.callback.SuccessCallback
import me.hgj.jetpackmvvm.widget.loadsir.core.LoadSir


class WidgetTask(
    override val name: String = "WidgetTask",
    override val runOnMainThread: Boolean = false,
    override val isBlocking: Boolean = false
) : BaseInitTask() {
    override suspend fun init(app: Application) {
//        SmartRefreshLayout.setDefaultRefreshInitializer { context, layout ->
//            //设置 SmartRefreshLayout 通用配置
//            layout.setEnableScrollContentWhenLoaded(true)//是否在加载完成时滚动列表显示新的内容
//            layout.setFooterTriggerRate(0.6f)
//        }
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
            MaterialHeader(context).apply {
                setPrimaryColors(getColorExt(R.color.appColor))
            }
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
            ProgressOnlyFooter(context).apply {
                setPrimaryColors(getColorExt(R.color.appColor))
            }
        }

        LoadSir.beginBuilder()
            .setErrorCallBack(ErrorCallback())
            .setEmptyCallBack(EmptyCallback())
            .setLoadingCallBack(LoadingCallback()) //比如我替换了全局loading加载
            .setDefaultCallback(SuccessCallback::class.java)
            .commit()
    }

}