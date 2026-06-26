package com.flower.flow.ui.activity

import android.os.Bundle
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.databinding.ActivityWebviewBinding
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.ext.util.intent.extraAct

class WebviewActivity : BaseActivity<BaseViewModel, ActivityWebviewBinding>() {

    private val mUrl by extraAct("url", "")
    private val mTitle by extraAct("title", "")

    override val title: String
        get() = mTitle

    override fun initView(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        if (mUrl.isBlank()) {
            finish()
            return
        }
        mBind.webView.settings.javaScriptEnabled = true
        mBind.webView.webViewClient = WebViewClient()
        mBind.webView.loadUrl(mUrl)
    }
}
