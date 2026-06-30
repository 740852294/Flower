package com.flower.flow.app.core.util

import android.app.Activity
import android.content.Intent
import com.flower.flow.ui.activity.MainActivity
import me.hgj.jetpackmvvm.ext.util.containsActivity

object MainNavigator {

    const val EXTRA_TARGET_TAB = "extra_target_tab"

    fun openMainTab(activity: Activity, tabIndex: Int, finishCaller: Boolean = true) {
        val intent = Intent(activity, MainActivity::class.java).apply {
            putExtra(EXTRA_TARGET_TAB, tabIndex)
            flags = if (containsActivity(MainActivity::class.java)) {
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            } else {
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
        activity.startActivity(intent)
        if (finishCaller && activity !is MainActivity) {
            activity.finish()
        }
    }
}
