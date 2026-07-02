package com.flower.flow.ui.fragment

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.drake.brv.BindingAdapter
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.dividerSpace
import com.drake.brv.utils.setup
import com.flower.flow.R
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseFragment
import com.flower.flow.app.core.ext.loadAvatarFile
import com.flower.flow.app.core.ext.loadImage
import com.flower.flow.app.core.util.FlowCopyStore
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.app.core.util.WorkDownloadStorage
import com.flower.flow.app.core.widget.CenterImageSpan
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.model.FlowCopyKey
import com.flower.flow.data.model.entity.SubmitPageInfo
import com.flower.flow.data.model.entity.UserInfo
import com.flower.flow.data.model.entity.WorkGenerateResult
import com.flower.flow.data.model.entity.WorkItem
import com.flower.flow.data.vm.MeViewModel
import com.flower.flow.databinding.FragmentMeBinding
import com.flower.flow.databinding.LayoutItemWorkBinding
import com.flower.flow.ui.activity.EditUserInfoActivity
import com.flower.flow.ui.activity.IntegralRechargeActivity
import com.flower.flow.ui.activity.MainActivity
import com.flower.flow.ui.activity.SettingActivity
import com.flower.flow.ui.activity.VipJoinActivity
import com.flower.flow.ui.activity.WorkPreviewActivity
import com.flower.flow.ui.adapter.MainAdapter
import com.flower.flow.ui.dialog.CommonMessageDialog
import com.flower.flow.ui.dialog.GenerateResultDialog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.doDebouncedClick
import me.hgj.jetpackmvvm.ext.util.dp2px
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.intent.openActivityForResult
import me.hgj.jetpackmvvm.ext.util.loadListError
import me.hgj.jetpackmvvm.ext.util.loadMore
import me.hgj.jetpackmvvm.ext.util.refresh
import me.hgj.jetpackmvvm.ext.util.statusPadding
import me.hgj.jetpackmvvm.ext.util.toast
import me.hgj.jetpackmvvm.ext.view.grid
import me.hgj.jetpackmvvm.util.BasePage
import rxhttp.toDownloadFlow
import rxhttp.wrapper.param.RxHttp
import kotlin.time.Duration.Companion.milliseconds

class MeFragment : BaseFragment<MeViewModel, FragmentMeBinding>() {

    private var isSelectionMode = false
    private val selectedTaskIds = mutableSetOf<String>()
    private var workListPollingStarted = false
    private val downloadStates = mutableMapOf<String, DownloadUiState>()
    private val downloadJobs = mutableMapOf<String, Job>()

    private data class DownloadUiState(
        val progress: Int?,
    )

    companion object {
        const val SPAN_COUNT = 2

        //状态，0=待解锁，1=待处理，2=处理中，3=已完成，4=处理失败
        const val WORK_STATUS_NONE = 0

        const val WORK_STATUS_WAIT = 1

        const val WORK_STATUS_PROCESSING = 2

        const val WORK_STATUS_COMPLETE = 3

        const val WORK_STATUS_FAIL = 4

        private const val PAYLOAD_SELECTION = "payload_selection"

        private const val PAYLOAD_DOWNLOAD = "payload_download"

        private const val WORK_LIST_POLL_INTERVAL = 2 * 60 * 1000L

        fun newInstance(): MeFragment {
            val args = Bundle()
            val fragment = MeFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBind.scrollContent.statusPadding()

        App.globalConfig?.apply {
            mBind.llMoney.isVisible = (integralAndVipEntranceShow == 1)
        }

        setText()

        UserManager.user?.apply {
            setUserInfo(this)
        }

        mBind.refreshLayout.refresh {
            exitSelectionMode()
            loadData(isLoading = false)
        }

        mBind.refreshLayout.loadMore {
            refreshWorkList(refresh = false, isLoading = false)
        }

        setupWorksMinHeightObserver()

        mBind.rvList.grid(SPAN_COUNT)
            .dividerSpace(dp2px(8f), DividerOrientation.GRID)
            .setup {
                addType<WorkItem>(R.layout.layout_item_work)

                onBind {
                    getBindingOrNull<LayoutItemWorkBinding>()?.run {
                        val model = getModel<WorkItem>()

                        val isVip = UserManager.user?.isVip ?: false

                        bindSelectionState(this, model)

                        btnStatus.isVisible = !model.againGenerateButtonMsg.isNullOrBlank()
                        btnStatus.text = model.againGenerateButtonMsg ?: ""

                        when (model.state) {
                            WORK_STATUS_NONE -> {
                                lockDot.visibility =
                                    if (isSelectionMode) View.GONE else View.VISIBLE
                                llTime.visibility = View.GONE
                                llProgress.visibility = View.GONE
                                ivDownload.visibility = View.GONE
                                ivCover.loadImage(
                                    url = model.aiartImg,
                                    cornerRadiusDp = 15f,
                                )
                                ivCover.applyBlurEffect(true, dp2px(15f).toFloat())
                                bindSampleImages(this, model.inputImgList)
                                llStatus.visibility = View.VISIBLE
                                ivStatus.visibility = View.VISIBLE
                                ivStatus.setImageResource(R.mipmap.ic_work_lock)
                                tvStatus1.visibility = View.GONE
                                tvStatus2.visibility =
                                    if (isVip || model.showMsg.isNullOrBlank()) View.GONE else View.VISIBLE
                                tvStatus2.text = model.showMsg.orEmpty()
                                tvGenerating.visibility = View.GONE
                            }

                            WORK_STATUS_WAIT, WORK_STATUS_PROCESSING -> {
                                lockDot.visibility = View.GONE
                                llTime.visibility = View.GONE
                                llProgress.visibility = View.GONE
                                ivDownload.visibility = View.GONE
                                val url = model.inputImgList?.firstOrNull() ?: ""
                                if (url.isNotBlank()) {
                                    ivCover.loadImage(
                                        url = url,
                                        cornerRadiusDp = 15f,
                                    )
                                }
                                ivCover.applyBlurEffect(true, dp2px(15f).toFloat())
                                ivSampleSingle.isVisible = false
                                llSampleBottom.isVisible = false
                                llStatus.visibility = View.VISIBLE
                                ivStatus.visibility = View.GONE
                                tvStatus1.visibility =
                                    if (model.showMsg.isNullOrBlank()) View.GONE else View.VISIBLE
                                tvStatus1.text = model.showMsg.orEmpty()
                                tvStatus2.visibility = View.GONE
                                tvGenerating.visibility =
                                    if (model.estimatTimeMsg.isNullOrBlank()) View.GONE else View.VISIBLE
                                tvGenerating.text = model.estimatTimeMsg.orEmpty()
                            }

                            WORK_STATUS_COMPLETE -> {
                                lockDot.visibility = View.GONE
                                llTime.visibility =
                                    if (model.aiartType == 2) View.VISIBLE else View.GONE
                                tvTime.text = model.videoDurationMsg.orEmpty()
                                // 下载时，这里显示显示进度，progress是本地添加bean里面的，接口没返回的
                                llProgress.visibility = View.GONE
                                ivDownload.visibility = View.VISIBLE

                                val url = model.inputImgList?.firstOrNull() ?: model.aiartImg ?: ""
                                if (url.isNotBlank()) {
                                    ivCover.loadImage(
                                        url = url,
                                        cornerRadiusDp = 15f,
                                    )
                                }
                                ivCover.applyBlurEffect(false)
                                ivSampleSingle.isVisible = false
                                llSampleBottom.isVisible = false

                                llStatus.visibility = View.GONE
                            }

                            WORK_STATUS_FAIL -> {
                                lockDot.visibility = View.GONE
                                llTime.visibility = View.GONE
                                llProgress.visibility = View.GONE
                                ivDownload.visibility = View.GONE
                                val url = model.inputImgList?.firstOrNull() ?: ""
                                if (url.isNotBlank()) {
                                    ivCover.loadImage(
                                        url = url,
                                        cornerRadiusDp = 15f,
                                    )
                                }
                                ivCover.applyBlurEffect(true, dp2px(15f).toFloat())
                                ivSampleSingle.isVisible = false
                                llSampleBottom.isVisible = false
                                llStatus.visibility = View.VISIBLE
                                ivStatus.visibility = View.VISIBLE
                                ivStatus.setImageResource(R.mipmap.ic_work_failed)
                                tvStatus1.visibility = View.VISIBLE
                                tvStatus1.text = model.showMsg.orEmpty()
                                tvStatus2.visibility = View.GONE
                                tvGenerating.visibility = View.GONE
                            }
                        }

                        bindDownloadState(this, model)
                    }
                }

                onPayload { payloads ->
                    getBindingOrNull<LayoutItemWorkBinding>()?.run {
                        val model = getModel<WorkItem>()
                        if (PAYLOAD_SELECTION in payloads) {
                            bindSelectionState(this, getModel())
                        }
                        if (PAYLOAD_DOWNLOAD in payloads) {
                            bindDownloadState(this, model)
                        }
                    }
                }

                onClick(R.id.rootItem) {
                    doDebouncedClick {
                        val model = getModel<WorkItem>()
                        if (isSelectionMode) {
                            val taskId = model.taskId
                            if (!selectedTaskIds.add(taskId)) {
                                selectedTaskIds.remove(taskId)
                            }
                            notifyItemChanged(modelPosition, PAYLOAD_SELECTION)
                            updateDeleteButtonText()
                        } else if (
                            model.state == WORK_STATUS_COMPLETE &&
                            model.taskId !in downloadStates
                        ) {
                            openActivity<WorkPreviewActivity>(
                                WorkPreviewActivity.EXTRA_WORK_ITEM to model
                            )
                        }
                    }
                }

                onClick(R.id.btnStatus) {
                    doDebouncedClick {
                        val model = getModel<WorkItem>()
                        if (!isSelectionMode) {
                            requestAgainGenerate(model)
                        }
                    }
                }
            }
    }

    override fun onBindViewClick() {
        mBind.llMoney.clickNoRepeat {
            val integral = App.globalConfig?.integralEntranceJumpState ?: 0
            val isVip = UserManager.user?.isVip ?: false
            if (!isVip && integral == 1) {
                openActivity<VipJoinActivity>()
                return@clickNoRepeat
            }
            openActivity<IntegralRechargeActivity>()
        }

        mBind.clVip.clickNoRepeat {
            openActivity<VipJoinActivity>()
        }

        mBind.clInfo.clickNoRepeat {
            openActivityForResult<EditUserInfoActivity> { result ->
                if (result != null) {
                    (activity as? MainActivity)?.getUserInfo(isFirst = false, isLoading = true)
                }
            }
        }

        mBind.rlWorkAdd.clickNoRepeat {
            FlowCopyStore.get(FlowCopyKey.TASK_EMPTY_HINT).toast()
            (activity as? MainActivity)?.switchTab(MainAdapter.PAGE_TOPIC)
        }

        mBind.settingBtn.clickNoRepeat {
            openActivity<SettingActivity>()
        }

        mBind.btnDelete.clickNoRepeat {
            when {
                !isSelectionMode -> enterSelectionMode()
                selectedTaskIds.isEmpty() -> exitSelectionMode()
                else -> deleteSelectedWorks(selectedTaskIds.toList())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        exitSelectionMode()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            exitSelectionMode()
        }
    }

    override fun lazyLoadData() {
        loadData(isLoading = true)
        startWorkListPolling()
    }

    private fun startWorkListPolling() {
        if (workListPollingStarted) return
        workListPollingStarted = true
        viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                delay(WORK_LIST_POLL_INTERVAL.milliseconds)
                refreshWorkList(refresh = true, isLoading = false)
            }
        }
    }

    private fun refreshWorkList(refresh: Boolean, isLoading: Boolean) {
        mViewModel.loadWorkList(refresh = refresh, isLoading = isLoading).obs(viewLifecycleOwner) {
            onSuccess { page ->
                bindWorkList(page, mBind.rvList.bindingAdapter, isRefresh = refresh)
            }
            onError { status ->
                loadListError(status, mBind.refreshLayout)
            }
        }
    }

    private fun loadData(isLoading: Boolean) {
        mViewModel.initData(isLoading).obs(viewLifecycleOwner) {
            onSuccess { page ->
                bindWorkList(
                    page,
                    mBind.rvList.bindingAdapter,
                    isRefresh = true
                )
            }
            onError { status ->
                loadListError(status, mBind.refreshLayout)
                status.msg.toast()
            }
        }
    }

    private fun bindWorkList(
        baseListNetEntity: BasePage<WorkItem>,
        bindingAdapter: BindingAdapter,
        isRefresh: Boolean? = null,
    ) {
        val refresh = isRefresh ?: baseListNetEntity.isRefresh()
        if (refresh) {
            exitSelectionMode(notifyItems = false)
            val pageData = baseListNetEntity.getPageData()
            bindingAdapter.models = pageData
            mBind.refreshLayout.finishRefresh()
        } else {
            bindingAdapter.addModels(baseListNetEntity.getPageData())
        }
        if (baseListNetEntity.hasMore()) {
            mBind.refreshLayout.finishLoadMore()
            mBind.refreshLayout.setNoMoreData(false)
            mBind.refreshLayout.setEnableLoadMore(true)
        } else {
            mBind.refreshLayout.finishLoadMore()
            mBind.refreshLayout.setEnableLoadMore(false)
        }

        mBind.rlWorkAdd.isVisible =
            bindingAdapter.models == null || bindingAdapter.models?.isEmpty() == true
        mBind.btnDelete.isVisible =
            bindingAdapter.models != null && bindingAdapter.models?.isNotEmpty() == true
        updateWorksMinHeight()
    }

    private fun setupWorksMinHeightObserver() {
        mBind.nestedScrollView.viewTreeObserver.addOnGlobalLayoutListener {
            updateWorksMinHeight()
        }
    }

    private fun updateWorksMinHeight() {
        val viewportHeight = mBind.nestedScrollView.height
        if (viewportHeight <= 0) return

        val worksTop = mBind.flWorks.top
        val minHeight = viewportHeight - worksTop
        if (minHeight > 0 && mBind.flWorks.minimumHeight != minHeight) {
            mBind.flWorks.minimumHeight = minHeight
        }
    }

    override fun createObserver() {
        EventViewModel.msgRedDotEvent.observe(this) { show ->
            mBind.redDot.isVisible = show
        }

        UserManager.observeUser().observe(viewLifecycleOwner) { user ->
            user?.apply {
                setUserInfo(user)
            }
        }

        EventViewModel.languageEvent.observe(this) {
            setText()
        }

        EventViewModel.workDataRefreshEvent.observe(this) {
            loadData(false)
        }

        EventViewModel.workDownloadEvent.observe(viewLifecycleOwner) { workItem ->
            startWorkDownload(workItem)
        }
    }

    private fun setUserInfo(user: UserInfo) {
        val isVip = user.isVip
        mBind.clVip.isVisible =
            (!isVip) && ((App.globalConfig?.integralAndVipEntranceShow ?: 0) == 1)
        mBind.ivVipLabel.isVisible = isVip

        val avatar = user.avatar
        if (avatar.isNotEmpty()) {
            mBind.ivAvatar.loadAvatarFile(avatar)
        }

        mBind.tvName.text = user.name
        mBind.tvMoney.text = user.integralBalance.toString()

        mBind.tvOverTip.isVisible = user.clearTaskMsg.isNotEmpty()
        setIconText(mBind.tvOverTip, R.mipmap.ic_me_over_star, user.clearTaskMsg)
        mBind.scrollContent.post { updateWorksMinHeight() }
    }

    private fun setText() {
        mBind.vipTitleText.text = FlowCopyStore.get(FlowCopyKey.VIDEO_PRO_NAME)
        mBind.vipSubtitleText.text = FlowCopyStore.get(FlowCopyKey.VIP_UNLOCK_HINT)
        mBind.btnVip.text = FlowCopyStore.get(FlowCopyKey.VIP_OPEN_HINT)
        mBind.worksLabel.text = FlowCopyStore.get(FlowCopyKey.WORKS_TAB)
        updateDeleteButtonText()
    }

    private fun enterSelectionMode() {
        isSelectionMode = true
        selectedTaskIds.clear()
        updateDeleteButtonText()
        notifySelectionStateChanged()
    }

    private fun exitSelectionMode(notifyItems: Boolean = true) {
        if (!isSelectionMode && selectedTaskIds.isEmpty()) return

        isSelectionMode = false
        selectedTaskIds.clear()
        updateDeleteButtonText()
        if (notifyItems) {
            notifySelectionStateChanged()
        }
    }

    private fun notifySelectionStateChanged() {
        val adapter = mBind.rvList.bindingAdapter
        val modelCount = adapter.models?.size ?: 0
        if (modelCount > 0) {
            adapter.notifyItemRangeChanged(0, modelCount, PAYLOAD_SELECTION)
        }
    }

    private fun bindSelectionState(
        binding: LayoutItemWorkBinding,
        model: WorkItem,
    ) {
        binding.ivSelect.isVisible = isSelectionMode
        binding.ivSelect.setImageResource(
            if (model.taskId in selectedTaskIds) {
                R.mipmap.ic_work_item_selected
            } else {
                R.mipmap.ic_work_item_unselect
            }
        )
        if (model.state == WORK_STATUS_NONE) {
            binding.lockDot.visibility =
                if (isSelectionMode) View.GONE else View.VISIBLE
        }
    }

    private fun bindDownloadState(
        binding: LayoutItemWorkBinding,
        model: WorkItem,
    ) {
        val downloadState = downloadStates[model.taskId]
        binding.llProgress.isVisible = downloadState != null
        if (downloadState != null) {
            binding.tvDownload.text = model.downloadingMsg.orEmpty()
            binding.pbDownload.isIndeterminate = downloadState.progress == null
            downloadState.progress?.let { binding.pbDownload.progress = it }
            binding.ivDownload.visibility = View.GONE
        } else {
            binding.pbDownload.isIndeterminate = false
            binding.pbDownload.progress = 0
            if (model.state == WORK_STATUS_COMPLETE) {
                binding.ivDownload.visibility = View.VISIBLE
            }
        }
    }

    private fun requestAgainGenerate(workItem: WorkItem) {
        if (workItem.taskId.isBlank()) return
        mViewModel.prepareAgainGenerate(workItem.aiartId, workItem.taskId)
            .obs(viewLifecycleOwner) {
                onSuccess { result ->
                    val generateResult = result.generateResult
                    if (generateResult != null) {
                        handleAgainGenerateResult(generateResult)
                    } else {
                        proceedAfterRepeatCheck(result.pageInfo, workItem.taskId)
                    }
                }
                onError { status ->
                    status.msg.toast()
                }
            }
    }

    private fun proceedAfterRepeatCheck(pageInfo: SubmitPageInfo, taskId: String) {
        if (pageInfo.isGenerateFreeEverydayPopup) {
            showGenerateFreeEverydayDialog(pageInfo)
            return
        }
        proceedAfterFreeEverydayCheck(pageInfo, taskId)
    }

    private fun showGenerateFreeEverydayDialog(pageInfo: SubmitPageInfo) {
        val title = pageInfo.generateFreeEverydayPopupTitle
            .ifBlank { pageInfo.generateFreeEverydayPopupMsg }
        val content = pageInfo.generateFreeEverydayPopupMsg
            .ifBlank { pageInfo.generateFreeEverydayPopupTitle }
        if (title.isBlank()) return

        activity?.let { host ->
            CommonMessageDialog.Builder(host)
                .setTitle(title)
                .setContent(content)
                .setConfirmButton(FlowCopyStore.get(FlowCopyKey.ROGER_ACTION))
                .show()
        }
    }

    private fun proceedAfterFreeEverydayCheck(pageInfo: SubmitPageInfo, taskId: String) {
        if (pageInfo.isConsumeIntegralPopup) {
            showConsumeIntegralDialog(pageInfo, taskId)
            return
        }
        submitAgainGenerate(taskId)
    }

    private fun showConsumeIntegralDialog(pageInfo: SubmitPageInfo, taskId: String) {
        val title = pageInfo.consumeIntegralPopupTitle
            .ifBlank { pageInfo.consumeIntegralPopupMsg }
        val content = pageInfo.consumeIntegralPopupMsg
            .ifBlank { pageInfo.consumeIntegralPopupTitle }
        if (title.isBlank()) {
            submitAgainGenerate(taskId)
            return
        }

        activity?.let { host ->
            CommonMessageDialog.Builder(host)
                .setTitle(title)
                .setContent(content)
                .setCancelButton(FlowCopyStore.get(FlowCopyKey.CANCEL_ACTION))
                .setConfirmButton(FlowCopyStore.get(FlowCopyKey.CONFIRM_ACTION)) {
                    submitAgainGenerate(taskId)
                }
                .show()
        }
    }

    private fun submitAgainGenerate(taskId: String) {
        mViewModel.continueAgainGenerate(taskId).obs(viewLifecycleOwner) {
            onSuccess(::handleAgainGenerateResult)
            onError { status ->
                status.msg.toast()
            }
        }
    }

    private fun handleAgainGenerateResult(result: WorkGenerateResult) {
        when (result.state) {
            WorkGenerateResult.STATE_VIP_INTERCEPT -> {
                showAgainGenerateInterceptDialog(result) {
                    openActivity<VipJoinActivity>()
                    loadData(isLoading = false)
                }
            }

            WorkGenerateResult.STATE_RECHARGE_INTERCEPT -> {
                showAgainGenerateInterceptDialog(result) {
                    openActivity<IntegralRechargeActivity>()
                    loadData(isLoading = false)
                }
            }

            else -> loadData(isLoading = true)
        }
    }

    private fun showAgainGenerateInterceptDialog(
        result: WorkGenerateResult,
        onConfirm: () -> Unit,
    ) {
        val host = activity ?: return
        GenerateResultDialog.Builder(host)
            .setResult(result)
            .setOnConfirm(onConfirm)
            .setCancelButtonText(FlowCopyStore.get(FlowCopyKey.CANCEL_ACTION))
            .setOnCancel {
                loadData(isLoading = true)
            }
            .show() ?: loadData(isLoading = true)
    }

    private fun startWorkDownload(workItem: WorkItem) {
        val taskId = workItem.taskId
        if (
            taskId.isBlank() ||
            workItem.outputUrl.isBlank() ||
            downloadJobs[taskId]?.isActive == true
        ) {
            return
        }

        downloadStates[taskId] = DownloadUiState(progress = 0)
        notifyDownloadStateChanged(taskId)

        val appContext = requireContext().applicationContext
        downloadJobs[taskId] = viewLifecycleOwner.lifecycleScope.launch {
            var destination: WorkDownloadStorage.Destination? = null
            var downloadCompleted = false
            try {
                destination = withContext(Dispatchers.IO) {
                    WorkDownloadStorage.createDestination(appContext, workItem)
                }
                val uri = destination.uri
                val tempFile = destination.tempFile
                when {
                    uri != null -> {
                        RxHttp.get(workItem.outputUrl)
                            .toDownloadFlow(appContext, uri)
                            .onProgress { progress ->
                                updateDownloadProgress(
                                    taskId,
                                    progress.progress.takeIf { progress.totalSize > 0 },
                                )
                            }
                            .collect { }
                    }

                    tempFile != null -> {
                        RxHttp.get(workItem.outputUrl)
                            .toDownloadFlow(tempFile.absolutePath)
                            .onProgress { progress ->
                                updateDownloadProgress(
                                    taskId,
                                    progress.progress.takeIf { progress.totalSize > 0 },
                                )
                            }
                            .collect { }
                    }
                }
                withContext(Dispatchers.IO) {
                    WorkDownloadStorage.complete(appContext, destination)
                }
                downloadCompleted = true
            } catch (_: CancellationException) {
            } catch (_: Throwable) {
            } finally {
                if (!downloadCompleted) {
                    withContext(NonCancellable + Dispatchers.IO) {
                        WorkDownloadStorage.cleanup(appContext, destination)
                    }
                }
                downloadStates.remove(taskId)
                downloadJobs.remove(taskId)
                if (view != null) {
                    notifyDownloadStateChanged(taskId)
                }
            }

            if (downloadCompleted) {
                recordWorkDownloaded(taskId)
            }
        }
    }

    private fun updateDownloadProgress(taskId: String, progress: Int?) {
        if (downloadStates[taskId]?.progress == progress) return
        downloadStates[taskId] = DownloadUiState(progress)
        notifyDownloadStateChanged(taskId)
    }

    private fun notifyDownloadStateChanged(taskId: String) {
        val adapter = mBind.rvList.bindingAdapter
        val position = adapter.models?.indexOfFirst {
            (it as? WorkItem)?.taskId == taskId
        } ?: -1
        if (position >= 0) {
            adapter.notifyItemChanged(position, PAYLOAD_DOWNLOAD)
        }
    }

    private fun recordWorkDownloaded(taskId: String) {
        mViewModel.recordWorkDownloaded(taskId).obs(viewLifecycleOwner) {
            onError { status ->
                status.msg.toast()
            }
        }
    }

    private fun updateDeleteButtonText() {
        val copyKey = if (isSelectionMode && selectedTaskIds.isEmpty()) {
            FlowCopyKey.CANCEL_ACTION
        } else {
            FlowCopyKey.DELETE_ACTION
        }
        mBind.btnDelete.text = FlowCopyStore.get(copyKey)
    }

    private fun deleteSelectedWorks(taskIds: List<String>) {
        if (taskIds.isEmpty()) return
        activity?.let {
            CommonMessageDialog.Builder(it)
                .setTitle(FlowCopyStore.get(FlowCopyKey.NOTICE_HEAD))
                .setContent(FlowCopyStore.get(FlowCopyKey.TASK_DELETE_HINT))
                .setConfirmButton(FlowCopyStore.get(FlowCopyKey.CONFIRM_ACTION)) {
                    mViewModel.deleteWorkTasks(taskIds).obs(viewLifecycleOwner) {
                        onSuccess {
                            exitSelectionMode()
                            loadData(isLoading = true)
                        }
                        onError { status ->
                            status.msg.toast()
                        }
                    }
                }
                .setCancelButton(FlowCopyStore.get(FlowCopyKey.CANCEL_ACTION))
                .show()
        }
    }

    fun setIconText(
        textView: TextView,
        iconRes: Int,
        text: String,
        iconSizeDp: Int = 18
    ) {
        val density = textView.resources.displayMetrics.density
        val size = (iconSizeDp * density).toInt()

        val drawable = ContextCompat.getDrawable(textView.context, iconRes)!!.mutate()
        drawable.setBounds(0, 0, size, size)

        // 注意：前面加一个占位符，图片替换占位符，不要替换正文第一个字母
        val finalText = "  $text"
        val span = SpannableString(finalText)

        span.setSpan(
            CenterImageSpan(drawable),
            0,
            1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = span
    }

    private fun bindSampleImages(
        binding: LayoutItemWorkBinding,
        samples: List<String>?,
    ) {
        binding.ivSampleSingle.isVisible = false
        binding.llSampleBottom.isVisible = false
        when {
            samples.isNullOrEmpty() -> return
            samples.size == 1 -> {
                binding.ivSampleSingle.isVisible = true
                binding.ivSampleSingle.loadImage(
                    url = samples.first(),
                    cornerRadiusDp = 10f,
                )
            }

            else -> {
                binding.llSampleBottom.isVisible = true
                binding.ivSampleLeft.loadImage(
                    url = samples[0],
                    cornerRadiiDp = floatArrayOf(
                        10f,
                        0f,
                        0f,
                        10f,
                    ),
                )
                binding.ivSampleRight.loadImage(
                    url = samples.getOrNull(1),
                    cornerRadiiDp = floatArrayOf(
                        0f,
                        10f,
                        10f,
                        0f,
                    ),
                )
            }
        }
    }
}
