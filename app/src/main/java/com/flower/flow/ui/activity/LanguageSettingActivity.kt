package com.flower.flow.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.FrameLayout
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.flower.flow.R
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.widget.ActionButton
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.model.CacheConfig
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.model.entity.LanguageItem
import com.flower.flow.data.vm.LanguageSettingViewModel
import com.flower.flow.databinding.ActivityLanguageSettingBinding
import com.flower.flow.databinding.LayoutItemLanguageBinding
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.doDebouncedClick
import me.hgj.jetpackmvvm.ext.util.toast

class LanguageSettingActivity :
    BaseActivity<LanguageSettingViewModel, ActivityLanguageSettingBinding>() {

    private var selectedPosition = -1
    private var currentLanguageId = 0
    private lateinit var saveButton: ActionButton

    override val title: String
        get() = FlowCopyStore.get(FlowCopyKey.LANGUAGE_SETTING)

    @SuppressLint("NotifyDataSetChanged")
    override fun initView(savedInstanceState: Bundle?) {
        currentLanguageId = resolveCurrentLanguageId()
        addSaveBtn()

        mBind.rvLanguage.linear().divider(R.drawable.line_rv_divider)
            .setup {
                addType<LanguageItem>(R.layout.layout_item_language)

                onBind {
                    getBindingOrNull<LayoutItemLanguageBinding>()?.run {
                        val model = getModel<LanguageItem>()
                        tvName.text = model.descendcoffer
                        if (selectedPosition == modelPosition) {
                            ivSelect.setImageResource(R.mipmap.ic_feedback_type_item_selected)
                        } else {
                            ivSelect.setImageResource(R.mipmap.ic_feedback_type_item_unselect)
                        }
                    }
                }

                onClick(R.id.llItem) {
                    doDebouncedClick {
                        selectedPosition = modelPosition
                        notifyDataSetChanged()
                        updateSaveButtonState()
                    }
                }
            }
    }

    override fun createObserver() {
        mViewModel.loadLanguages().obs(this) {
            onSuccess { list ->
                mBind.rvLanguage.models = list
                selectedPosition = list.indexOfFirst { it.acetoneactuate == currentLanguageId }
                    .takeIf { it >= 0 } ?: 0
                updateSaveButtonState()
            }
            onError { error ->
                error.msg.toast()
            }
        }
    }

    private fun addSaveBtn() {
        mToolbar.menu.clear()
        mToolbar.setContentInsetEndWithActions(0)

        val item = mToolbar.menu.add("save").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        saveButton = ActionButton(this).apply {
            text = FlowCopyStore.get(FlowCopyKey.SAVE_ACTION)
            isEnabled = false
            clickNoRepeat {
                applySelectedLanguage()
            }
        }

        item.actionView = FrameLayout(this).apply {
            setPadding(0, 0, dp(15), 0)

            addView(
                saveButton,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER,
                ),
            )
        }
    }

    private fun applySelectedLanguage() {
        val selected = (mBind.rvLanguage.models as? List<LanguageItem>)
            ?.getOrNull(selectedPosition)
        if (selected == null) {
            FlowCopyStore.get(FlowCopyKey.PICK_HINT).toast()
            return
        }
        if (selected.acetoneactuate == currentLanguageId) {
            finish()
            return
        }
        mViewModel.applyLanguage(selected, currentLanguageId).obs(this) {
            onSuccess {
                EventViewModel.languageEvent.value = selected
                finish()
            }
            onError { error ->
                error.msg.toast()
            }
        }
    }

    private fun updateSaveButtonState() {
        val selected = (mBind.rvLanguage.models as? List<LanguageItem>)
            ?.getOrNull(selectedPosition)
        saveButton.isEnabled = selected != null && selected.acetoneactuate != currentLanguageId
    }

    private fun resolveCurrentLanguageId(): Int {
        return when {
            App.currentLanguageId > 0 -> App.currentLanguageId
            CacheConfig.selectedLanguageId > 0 -> CacheConfig.selectedLanguageId
            else -> 0
        }
    }
}
