package com.flower.flow.app.core.widget.loadCallBack

import android.content.Context
import android.view.View
import com.flower.flow.R
import me.hgj.jetpackmvvm.widget.loadsir.callback.Callback

/**
 * 作者　: hegaojian
 * 时间　: 2023/3/28
 * 描述　:
 */
class LoadingCallback : Callback() {

    override fun onCreateView() = R.layout.layout_loading

    override fun onReloadEvent(context: Context?, view: View?) = true
}