package com.flower.flow.app.core.ext

import androidx.appcompat.widget.Toolbar
import com.flower.flow.R
import com.google.android.material.appbar.MaterialToolbar
import me.hgj.jetpackmvvm.ext.util.doDebouncedClick
import me.hgj.jetpackmvvm.ext.util.toHtml

/**
 * 初始化有返回键的toolbar
 */
fun MaterialToolbar.initClose(
    titleStr: String = "",
    backImg: Int = R.mipmap.ic_back,
    onBack: (toolbar: Toolbar) -> Unit
): MaterialToolbar {
    title = titleStr.toHtml()
    setNavigationIcon(backImg)
    setNavigationOnClickListener {
        doDebouncedClick { onBack.invoke(this) }
    }
    return this
}