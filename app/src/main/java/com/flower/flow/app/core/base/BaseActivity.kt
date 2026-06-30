package com.flower.flow.app.core.base

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.viewbinding.ViewBinding
import com.flower.flow.R
import com.flower.flow.app.core.ext.dismissAppLoadingExt
import com.flower.flow.app.core.ext.initClose
import com.flower.flow.app.core.ext.showAppLoadingExt
import com.flower.flow.databinding.IncludeToolbarBinding
import com.google.android.material.appbar.MaterialToolbar
import me.hgj.jetpackmvvm.base.ui.BaseVbActivity
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.net.LoadingEntity
import me.hgj.jetpackmvvm.ext.util.getColorExt


abstract class BaseActivity<VM : BaseViewModel, VB : ViewBinding> : BaseVbActivity<VM, VB>() {

    lateinit var mToolbar: MaterialToolbar

    open val title = ""

    override val statusDark = false

    override fun onCreate(savedInstanceState: Bundle?) {
        //全面屏
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        //设置状态栏字体颜色为浅色
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        //设置导航栏图标颜色为深色
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars =
            false
        //设置导航栏背景为白色
        window.navigationBarColor = getColorExt(R.color.windowBackground)
        ViewCompat.setOnApplyWindowInsetsListener(mBind.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                bottom = bars.bottom,     // 解决导航栏盖住底部内容
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    /**
     * 定义了自己的头部 ，仅供参考
     */
    override fun getTitleBarView(): View? {
        mToolbar = IncludeToolbarBinding.inflate(layoutInflater).toolbar
        // 初始化 Toolbar（默认返回按钮）
        mToolbar.initClose(title) {
            finish()
        }
        return mToolbar
    }

    override fun showLoading(setting: LoadingEntity) {
        showAppLoadingExt(setting.coroutineScope)
    }

    override fun dismissLoading(setting: LoadingEntity) {
        dismissAppLoadingExt()
    }

    fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density + 0.5f).toInt()
    }
}