package com.flower.flow.app.core.ext

import androidx.appcompat.widget.Toolbar
import com.flower.flow.R
import me.hgj.jetpackmvvm.ext.util.toHtml

/**
 * 初始化有返回键的toolbar
 */
fun Toolbar.initClose(
    titleStr: String = "",
    backImg: Int = R.mipmap.ic_back,
    onBack: (toolbar: Toolbar) -> Unit
): Toolbar {
    title = titleStr.toHtml()
    setNavigationIcon(backImg)
    setNavigationOnClickListener { onBack.invoke(this) }
    return this
}