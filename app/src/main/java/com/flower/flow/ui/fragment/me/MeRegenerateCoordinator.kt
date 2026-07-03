package com.flower.flow.ui.fragment.me

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.data.model.StringResId
import com.flower.flow.data.model.entity.WorkGenerateResult
import com.flower.flow.data.model.entity.WorkItem
import com.flower.flow.data.vm.MeViewModel
import com.flower.flow.ui.activity.IntegralRechargeActivity
import com.flower.flow.ui.activity.VipJoinActivity
import com.flower.flow.ui.dialog.CommonMessageDialog
import com.flower.flow.ui.dialog.GeneratePreCheckPresenter
import com.flower.flow.ui.dialog.GenerateResultDialog
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.toast

class MeRegenerateCoordinator(
    private val fragment: Fragment,
    private val viewModel: MeViewModel,
    private val onReload: (showLoading: Boolean) -> Unit,
) {

    fun requestAgainGenerate(workItem: WorkItem) {
        if (workItem.baptismdictate.isBlank()) return
        viewModel.prepareAgainGenerate(workItem.naturebroker, workItem.baptismdictate)
            .obs(fragment.viewLifecycleOwner) {
                onSuccess { result ->
                    val generateResult = result.generateResult
                    if (generateResult != null) {
                        handleAgainGenerateResult(generateResult)
                    } else {
                        fragment.activity?.let { host ->
                            GeneratePreCheckPresenter(host).runRegeneratePipeline(
                                pageInfo = result.pageInfo,
                                onProceed = { submitAgainGenerate(workItem.baptismdictate) },
                            )
                        }
                    }
                }
                onError { status ->
                    status.msg.toast()
                }
            }
    }

    private fun submitAgainGenerate(taskId: String) {
        viewModel.continueAgainGenerate(taskId).obs(fragment.viewLifecycleOwner) {
            onSuccess(::handleAgainGenerateResult)
            onError { status ->
                status.msg.toast()
            }
        }
    }

    private fun handleAgainGenerateResult(result: WorkGenerateResult) {
        when (result.afflict) {
            WorkGenerateResult.STATE_VIP_INTERCEPT -> {
                showAgainGenerateInterceptDialog(result) {
                    fragment.openActivity<VipJoinActivity>()
                    onReload(false)
                }
            }

            WorkGenerateResult.STATE_RECHARGE_INTERCEPT -> {
                showAgainGenerateInterceptDialog(result) {
                    fragment.openActivity<IntegralRechargeActivity>()
                    onReload(false)
                }
            }

            else -> onReload(true)
        }
    }

    private fun showAgainGenerateInterceptDialog(
        result: WorkGenerateResult,
        onConfirm: () -> Unit,
    ) {
        val host = fragment.activity ?: return
        GenerateResultDialog.Builder(host)
            .setResult(result)
            .setOnConfirm(onConfirm)
            .setCancelButtonText(AppStrings.get(StringResId.CANCEL_ACTION))
            .setOnCancel {
                onReload(true)
            }
            .show() ?: onReload(true)
    }

    fun deleteSelectedWorks(
        owner: LifecycleOwner,
        taskIds: List<String>,
        onDeleted: () -> Unit,
    ) {
        if (taskIds.isEmpty()) return
        fragment.activity?.let {
            CommonMessageDialog.Builder(it)
                .setTitle(AppStrings.get(StringResId.NOTICE_HEAD))
                .setContent(AppStrings.get(StringResId.TASK_DELETE_HINT))
                .setConfirmButton(AppStrings.get(StringResId.CONFIRM_ACTION)) {
                    viewModel.deleteWorkTasks(taskIds).obs(owner) {
                        onSuccess {
                            onDeleted()
                        }
                        onError { status ->
                            status.msg.toast()
                        }
                    }
                }
                .setCancelButton(AppStrings.get(StringResId.CANCEL_ACTION))
                .show()
        }
    }
}
