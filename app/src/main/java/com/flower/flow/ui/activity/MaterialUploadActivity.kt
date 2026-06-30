package com.flower.flow.ui.activity

import android.os.Bundle
import androidx.core.view.isVisible
import com.flower.flow.app.core.base.BaseActivity
import com.flower.flow.app.core.ext.initClose
import com.flower.flow.app.core.ext.loadImage
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.model.entity.SubmitPageInfo
import com.flower.flow.data.model.entity.TemplateItem
import com.flower.flow.data.vm.MaterialUploadViewModel
import com.flower.flow.databinding.ActivityMaterialUploadBinding
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.intent.bundle
import me.hgj.jetpackmvvm.ext.util.statusPadding

class MaterialUploadActivity :
    BaseActivity<MaterialUploadViewModel, ActivityMaterialUploadBinding>() {

    val templateItem: TemplateItem? by bundle<TemplateItem>(null, "TagTemple")

    private var submitPageInfo: SubmitPageInfo? = null

    override val showTitle: Boolean
        get() = false

    override fun initView(savedInstanceState: Bundle?) {
        mBind.llContent.statusPadding()
        mBind.toolbar.initClose(templateItem?.name ?: "") {
            finish()
        }
        setText()

        templateItem?.apply {
            mBind.toolbar.title = name
            mBind.ivCover.loadImage(
                url = img,
                cornerRadiusDp = COVER_CORNER_RADIUS_DP,
            )
            bindPhotoUpload(this)
            bindLockBadge(this)
        }
    }

    override fun onBindViewClick() {
    }

    override fun createObserver() {
        val id = templateItem?.id ?: return
        mViewModel.getCreateSubmitPage(id).obs(this) {
            onSuccess { info ->
                submitPageInfo = info
            }
        }
    }

    private fun bindPhotoUpload(model: TemplateItem) {
        val isMultiUpload = model.uploadNum >= 2
        mBind.llSingleUpload.isVisible = !isMultiUpload
        mBind.llDoubleUpload.isVisible = isMultiUpload
    }

    private fun bindLockBadge(model: TemplateItem) {
        val showCost = model.lockIntegral > 0 && model.lockType != 0
        mBind.llCost.isVisible = showCost
        if (showCost) {
            mBind.tvLockIntegral.text = model.lockIntegral.toString()
        }
    }

    private fun setText() {
        val uploadLabel = FlowCopyStore.get(FlowCopyKey.PHOTO_UPLOAD_ACTION)
        mBind.tvUploadLabel.text = uploadLabel
        mBind.tvUploadLabelLeft.text = uploadLabel
        mBind.tvUploadLabelRight.text = uploadLabel
        mBind.tvCreate.text = FlowCopyStore.get(FlowCopyKey.CREATE_HINT)
    }

    companion object {
        private const val COVER_CORNER_RADIUS_DP = 15f
    }
}
