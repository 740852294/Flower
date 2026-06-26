package me.hgj.jetpackmvvm.ext.util

import android.graphics.Rect
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import kotlin.collections.forEach

/**
 * 作者　: hegaojian
 * 时间　: 2020/11/18
 * 描述　:
 */


fun View.click(action: (view: View) -> Unit) {
    setOnClickListener {
        action.invoke(it)
    }
}

/**
 * 扩大view的点击范围
 */
fun View.expandTouchArea(expandDp: Int) : View{
    val parent = parent as? ViewGroup ?: return this
    parent.post {
        val rect = Rect()
        getHitRect(rect)
        val extend = expandDp.dp
        rect.left -= extend
        rect.top -= extend
        rect.right += extend
        rect.bottom += extend
        parent.touchDelegate = TouchDelegate(rect, this)
    }
    return this
}

/**
 * 设置防止重复点击事件
 * @param views 需要设置点击事件的view
 * @param interval 时间间隔 默认0.5秒
 * @param onClick 点击触发的方法
 */
fun setOnclickNoRepeat(vararg views: View?, interval: Long = 500, onClick: (View) -> Unit) {
    views.forEach {
        it?.clickNoRepeat(interval = interval) { view ->
            onClick.invoke(view)
        }
    }
}


/**
 * 防止重复点击事件 默认0.5秒内不可重复点击
 * @param interval 时间间隔 默认0.5秒
 * @param action 执行方法
 */
var lastClickTime = 0L
fun View.clickNoRepeat(interval: Long = 500, action: (view: View) -> Unit) {
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (lastClickTime != 0L && (currentTime - lastClickTime < interval)) {
            return@setOnClickListener
        }
        lastClickTime = currentTime
        action.invoke(it)
    }
}

/**
 * 设置点击事件
 * @param views 需要设置点击事件的view
 * @param onClick 点击触发的方法
 */
fun setOnclick(vararg views: View?, onClick: (View) -> Unit) {
    views.forEach {
        it?.setOnClickListener { view ->
            onClick.invoke(view)
        }
    }
}