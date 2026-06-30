package com.flower.flow.app.core.widget.loadCallBack

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.flower.flow.R
import com.flower.flow.databinding.LayoutEmptyBinding
import com.flower.flow.databinding.LayoutErrorBinding
import me.hgj.jetpackmvvm.widget.loadsir.callback.Callback

class ErrorCallback : Callback() {

    override fun onCreateView() = R.layout.layout_error

}
