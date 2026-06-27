package com.flower.flow.app.core.ext

import android.app.Dialog
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.flower.flow.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import me.hgj.jetpackmvvm.ext.util.dp2px
import me.hgj.jetpackmvvm.ext.util.hideOffKeyboard
import java.util.WeakHashMap

private val loadingDialogs = WeakHashMap<Any, Dialog>()

private const val LOADING_SIZE_DP = 70

private fun createLoadingDialog(
    activity: AppCompatActivity,
    owner: LifecycleOwner,
    ownerKey: Any,
    coroutineScope: CoroutineScope?,
): Dialog {
    val contentView = LayoutInflater.from(activity).inflate(R.layout.layout_app_loading_dialog, null)
    val size = dp2px(LOADING_SIZE_DP.toFloat())
    return Dialog(activity, R.style.LoadingDialog).apply {
        setContentView(contentView)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            decorView.setPadding(0, 0, 0, 0)
            setLayout(size, size)
            attributes = attributes.apply {
                width = size
                height = size
            }
        }
        setOnDismissListener {
            coroutineScope?.cancel()
            loadingDialogs.remove(ownerKey)
        }
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                dismiss()
            }
        })
    }
}

fun Fragment.showAppLoadingExt(coroutineScope: CoroutineScope? = null) {
    dismissAppLoadingExt()
    val activity = activity as? AppCompatActivity ?: return
    if (activity.isFinishing) return
    val dialog = createLoadingDialog(activity, this, this, coroutineScope)
    loadingDialogs[this] = dialog
    dialog.show()
}

fun AppCompatActivity.showAppLoadingExt(coroutineScope: CoroutineScope? = null) {
    dismissAppLoadingExt()
    if (isFinishing) return
    hideOffKeyboard()
    val dialog = createLoadingDialog(this, this, this, coroutineScope)
    loadingDialogs[this] = dialog
    dialog.show()
}

fun AppCompatActivity.dismissAppLoadingExt() {
    loadingDialogs[this]?.dismiss()
    loadingDialogs.remove(this)
}

fun Fragment.dismissAppLoadingExt() {
    loadingDialogs[this]?.dismiss()
    loadingDialogs.remove(this)
}

fun dismissAppAllLoading() {
    loadingDialogs.values.forEach { it.dismiss() }
    loadingDialogs.clear()
}
