package com.flower.flow.app.core.net

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentActivity
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.data.model.CacheConfig
import com.flower.flow.data.model.StringResId
import com.flower.flow.ui.activity.StartActivity
import com.flower.flow.ui.dialog.CommonMessageDialog
import kotlinx.coroutines.CancellationException
import me.hgj.jetpackmvvm.core.JetpackMvvm
import me.hgj.jetpackmvvm.ext.util.currentActivity
import me.hgj.jetpackmvvm.ext.util.finishAllActivity

class AccountCancelledException : CancellationException()

object AccountCancelledHandler {

    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var isShowing = false

    @Volatile
    private var pendingMessage: String? = null

    fun handle(message: String): Nothing {
        if (pendingMessage == null) {
            pendingMessage = message
            mainHandler.post { showDialogIfNeeded() }
        }
        throw AccountCancelledException()
    }

    private fun showDialogIfNeeded(retryCount: Int = 0) {
        if (isShowing) return
        val message = pendingMessage ?: return
        val activity = currentActivity as? FragmentActivity
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            if (retryCount < 10) {
                mainHandler.postDelayed({ showDialogIfNeeded(retryCount + 1) }, 100)
            }
            return
        }
        isShowing = true
        CommonMessageDialog.Builder(activity)
            .setTitle(AppStrings.get(StringResId.USER_CANCELLED_HEAD))
            .setContent(message.ifBlank { " " })
            .setConfirmButton(AppStrings.get(StringResId.ROGER_ACTION)) {
                restartApp()
            }
            .show()
    }

    private fun restartApp() {
        CacheConfig.userId = ""
        CacheConfig.isAgree = false
        UserManager.clearUser()
        pendingMessage = null
        finishAllActivity()
        JetpackMvvm.app.startActivity(
            Intent(JetpackMvvm.app, StartActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            },
        )
        isShowing = false
    }
}
