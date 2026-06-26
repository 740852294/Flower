package com.flower.flow.app.init

import android.app.Application
import com.flower.flow.R
import com.flower.flow.app.core.widget.loadCallBack.LoadingCallback
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import me.hgj.jetpackmvvm.core.init.BaseInitTask
import me.hgj.jetpackmvvm.ext.util.getColorExt
import me.hgj.jetpackmvvm.widget.loadsir.callback.SuccessCallback
import me.hgj.jetpackmvvm.widget.loadsir.core.LoadSir
import me.hgj.jetpackmvvm.widget.state.BaseEmptyCallback
import me.hgj.jetpackmvvm.widget.state.BaseErrorCallback


class WidgetTask(
    override val name: String = "WidgetTask",
    override val runOnMainThread: Boolean = false,
    override val isBlocking: Boolean = false
) : BaseInitTask() {
    override suspend fun init(app: Application) {
        SmartRefreshLayout.setDefaultRefreshInitializer { context, layout ->
            //设置 SmartRefreshLayout 通用配置
            layout.setEnableScrollContentWhenLoaded(true)//是否在加载完成时滚动列表显示新的内容
            layout.setFooterTriggerRate(0.6f)
        }
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
            //设置 Head
            ClassicsHeader(context).apply {
                setAccentColor(getColorExt(R.color.black))
            }
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
            //设置 Footer
            ClassicsFooter(context).apply {
                setAccentColor(getColorExt(R.color.black))
            }
        }

        //这里写的原因是 框架的 状态布局页面可能不适用于你的项目，你可以添加自己的 错误 空 加载中全局配置
        LoadSir.beginBuilder()
            .setErrorCallBack(BaseErrorCallback())
            .setEmptyCallBack(BaseEmptyCallback())
            .setLoadingCallBack(LoadingCallback()) //比如我替换了全局loading加载
            .setDefaultCallback(SuccessCallback::class.java)
            .commit()
    }

}