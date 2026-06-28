package com.flower.flow.ui.activity

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.FrameLayout
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.ext.loadImage
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.app.core.widget.ActionButton
import com.flower.flow.data.model.CacheConfig
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.vm.EditUserInfoViewModel
import com.flower.flow.databinding.ActivityEditUserInfoBinding
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.copyToClipboard
import me.hgj.jetpackmvvm.ext.util.toast

class EditUserInfoActivity : BaseActivity<EditUserInfoViewModel, ActivityEditUserInfoBinding>() {

    override val title: String
        get() = FlowCopyStore.get(FlowCopyKey.PROFILE_EDIT)

    override fun initView(savedInstanceState: Bundle?) {
        addSaveBtn()
        setText()
    }

    fun addSaveBtn() {
        mToolbar.menu.clear()
        mToolbar.setContentInsetEndWithActions(0)

        val item = mToolbar.menu.add("save").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        val rightButton = ActionButton(this).apply {
            text = FlowCopyStore.get(FlowCopyKey.SAVE_ACTION)

            setOnClickListener {
                "保存".toast()
            }
        }

        item.actionView = FrameLayout(this).apply {
            setPadding(0, 0, dp(15), 0)

            addView(
                rightButton,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            )
        }
    }

    private fun setText() {
        mBind.nameLabel.text = FlowCopyStore.get(FlowCopyKey.NICKNAME_LABEL)
        mBind.accountLabel.text = FlowCopyStore.get(FlowCopyKey.ACCOUNT_LABEL)
    }

    override fun createObserver() {
        UserManager.user?.apply {
            val avatar = this.avatar
            if (avatar.isNotEmpty()) {
                mBind.ivAvatar.loadImage(avatar)
            }

            val name = this.name
            if (name.isNotEmpty()) {
                mBind.etName.setText(name)
            }

            mBind.tvAccount.text = CacheConfig.userId
        }
    }

    override fun onBindViewClick() {
        mBind.copyButton.clickNoRepeat {
            val uid = mBind.tvAccount.text?.toString().orEmpty()
            if (uid.isNotBlank()) {
                copyToClipboard(uid)
                FlowCopyStore.get(FlowCopyKey.COPY_ACTION).toast()
            }
        }

        mBind.ivAvatar.clickNoRepeat {
            openSystemGallery()
        }

        mBind.ivModify.clickNoRepeat {
            openSystemGallery()
        }
    }

    private fun openSystemGallery() {

    }
}
