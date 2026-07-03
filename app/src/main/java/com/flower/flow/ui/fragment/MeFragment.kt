package com.flower.flow.ui.fragment

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.drake.brv.utils.bindingAdapter
import com.flower.flow.R
import com.flower.flow.app.App
import com.flower.flow.app.core.base.BaseFragment
import com.flower.flow.app.core.ext.loadAvatarFile
import com.flower.flow.app.core.ext.setImageAnimationRunning
import com.flower.flow.app.core.util.AppStrings
import com.flower.flow.app.core.util.UserManager
import com.flower.flow.app.core.widget.CenterImageSpan
import com.flower.flow.app.event.EventViewModel
import com.flower.flow.data.model.StringResId
import com.flower.flow.data.model.entity.UserInfo
import com.flower.flow.data.model.entity.WorkItem
import com.flower.flow.data.vm.MeViewModel
import com.flower.flow.databinding.FragmentMeBinding
import com.flower.flow.domain.profile.WorkDownloadJobManager
import com.flower.flow.domain.profile.WorkListSelectionController
import com.flower.flow.ui.activity.EditUserInfoActivity
import com.flower.flow.ui.activity.IntegralRechargeActivity
import com.flower.flow.ui.activity.MainActivity
import com.flower.flow.ui.activity.SettingActivity
import com.flower.flow.ui.activity.VipJoinActivity
import com.flower.flow.ui.adapter.MainAdapter
import com.flower.flow.ui.activity.WorkPreviewActivity
import com.flower.flow.ui.fragment.me.MeRegenerateCoordinator
import com.flower.flow.ui.fragment.me.MeWorkListBinder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.hgj.jetpackmvvm.core.data.obs
import me.hgj.jetpackmvvm.ext.util.clickNoRepeat
import me.hgj.jetpackmvvm.ext.util.intent.openActivity
import me.hgj.jetpackmvvm.ext.util.intent.openActivityForResult
import me.hgj.jetpackmvvm.ext.util.loadListError
import me.hgj.jetpackmvvm.ext.util.loadMore
import me.hgj.jetpackmvvm.ext.util.refresh
import me.hgj.jetpackmvvm.ext.util.statusPadding
import me.hgj.jetpackmvvm.ext.util.toast
import kotlin.time.Duration.Companion.milliseconds

class MeFragment : BaseFragment<MeViewModel, FragmentMeBinding>() {

    private val selectionController = WorkListSelectionController()
    private lateinit var downloadManager: WorkDownloadJobManager
    private lateinit var workListBinder: MeWorkListBinder
    private lateinit var regenerateCoordinator: MeRegenerateCoordinator
    private var workListPollingStarted = false
    private var isLazyLoaded = false

    companion object {
        const val SPAN_COUNT = 2
        internal const val PAYLOAD_SELECTION = "payload_selection"
        internal const val PAYLOAD_DOWNLOAD = "payload_download"
        private const val WORK_LIST_POLL_INTERVAL = 2 * 60 * 1000L

        fun newInstance(): MeFragment = MeFragment()
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBind.scrollContent.statusPadding()
        downloadManager = WorkDownloadJobManager(
            scope = viewLifecycleOwner.lifecycleScope,
            appContext = requireContext().applicationContext,
            onProgress = { taskId, _ -> workListBinder.notifyDownloadStateChanged(taskId) },
            onFinished = { taskId, success ->
                workListBinder.notifyDownloadStateChanged(taskId)
                if (success) recordWorkDownloaded(taskId)
            },
        )
        workListBinder = MeWorkListBinder(
            binding = mBind,
            selectionController = selectionController,
            downloadManager = downloadManager,
            callbacks = workListCallbacks,
        )
        regenerateCoordinator = MeRegenerateCoordinator(
            fragment = this,
            viewModel = mViewModel,
            onReload = ::loadData,
        )

        setText()
        UserManager.user?.apply { setUserInfo(this) }

        mBind.refreshLayout.refresh {
            exitSelectionMode()
            loadData(isLoading = false)
        }
        mBind.refreshLayout.loadMore {
            refreshWorkList(refresh = false, isLoading = false)
        }

        workListBinder.setupMinHeightObserver()
        workListBinder.setupRecyclerView()
    }

    private val workListCallbacks = object : MeWorkListBinder.Callbacks {
        override fun isFragmentVisible(): Boolean = isResumed && !isHidden

        override fun onRequestAgainGenerate(workItem: WorkItem) {
            regenerateCoordinator.requestAgainGenerate(workItem)
        }

        override fun syncAnimationPlayback(imageView: ImageView) {
            imageView.setImageAnimationRunning(isResumed && !isHidden)
        }

        override fun onOpenWorkPreview(workItem: WorkItem) {
            openActivity<WorkPreviewActivity>(
                WorkPreviewActivity.EXTRA_WORK_ITEM to workItem,
            )
        }

        override fun onSelectionChanged() {
            workListBinder.updateDeleteButtonText()
        }
    }

    override fun onBindViewClick() {
        mBind.llMoney.clickNoRepeat {
            val integral = App.globalConfig?.framepublic ?: 0
            val isVip = UserManager.user?.shareengage ?: false
            if (!isVip && integral == 1) {
                openActivity<VipJoinActivity>()
                return@clickNoRepeat
            }
            openActivity<IntegralRechargeActivity>()
        }

        mBind.clVip.clickNoRepeat { openActivity<VipJoinActivity>() }

        mBind.clInfo.clickNoRepeat {
            openActivityForResult<EditUserInfoActivity> { result ->
                if (result != null) {
                    (activity as? MainActivity)?.getUserInfo(isFirst = false, isLoading = true)
                }
            }
        }

        mBind.rlWorkAdd.clickNoRepeat {
            AppStrings.get(StringResId.TASK_EMPTY_HINT).toast()
            (activity as? MainActivity)?.switchTab(MainAdapter.PAGE_TOPIC)
        }

        mBind.settingBtn.clickNoRepeat { openActivity<SettingActivity>() }

        mBind.btnDelete.clickNoRepeat {
            when {
                !selectionController.isSelectionMode -> enterSelectionMode()
                !selectionController.hasSelection() -> exitSelectionMode()
                else -> regenerateCoordinator.deleteSelectedWorks(
                    owner = viewLifecycleOwner,
                    taskIds = selectionController.selectedIds(),
                    onDeleted = {
                        exitSelectionMode()
                        loadData(isLoading = true)
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resumeVisibleAnimations()
    }

    override fun onPause() {
        setVisibleAnimationsRunning(false)
        exitSelectionMode()
        super.onPause()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            setVisibleAnimationsRunning(false)
            exitSelectionMode()
        } else {
            resumeVisibleAnimations()
        }
    }

    override fun lazyLoadData() {
        loadData(isLoading = true)
        startWorkListPolling()
        isLazyLoaded = true
    }

    fun refreshWorkListSilently() {
        if (!isLazyLoaded) return
        loadData(isLoading = false)
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
                workListBinder.bindWorkList(
                    page,
                    mBind.rvList.bindingAdapter,
                    isRefresh = refresh,
                    onExitSelection = { exitSelectionMode(notifyItems = false) },
                )
            }
            onError { status ->
                loadListError(status, mBind.refreshLayout)
            }
        }
    }

    private fun loadData(isLoading: Boolean) {
        mViewModel.initData(isLoading).obs(viewLifecycleOwner) {
            onSuccess { page ->
                workListBinder.bindWorkList(
                    page,
                    mBind.rvList.bindingAdapter,
                    isRefresh = true,
                    onExitSelection = { exitSelectionMode(notifyItems = false) },
                )
            }
            onError { status ->
                loadListError(status, mBind.refreshLayout)
                status.msg.toast()
            }
        }
    }

    override fun createObserver() {
        EventViewModel.msgRedDotEvent.observe(this) { show ->
            mBind.redDot.isVisible = show
        }

        UserManager.observeUser().observe(viewLifecycleOwner) { user ->
            user?.apply { setUserInfo(user) }
        }

        EventViewModel.languageEvent.observe(this) {
            setText()
        }

        EventViewModel.workDownloadEvent.observe(viewLifecycleOwner) { workItem ->
            downloadManager.start(workItem)
        }
    }

    private fun setUserInfo(user: UserInfo) {
        App.globalConfig?.apply {
            mBind.llMoney.isVisible = (exaltabrade == 1)
        }

        val isVip = user.shareengage
        mBind.clVip.isVisible = (!isVip) && ((App.globalConfig?.exaltabrade ?: 0) == 1)
        mBind.ivVipLabel.isVisible = isVip

        val avatar = user.excludephone
        if (avatar.isNotEmpty()) {
            mBind.ivAvatar.loadAvatarFile(
                source = avatar,
                isAutoPlay = true,
                onFinalImageSet = workListCallbacks::syncAnimationPlayback,
            )
        }

        mBind.tvName.text = user.dazzledeacon
        mBind.tvMoney.text = user.beastamalgam.toString()
        mBind.tvOverTip.isVisible = user.hardthough.isNotEmpty()
        setIconText(mBind.tvOverTip, R.mipmap.ic_me_over_star, user.hardthough)
        mBind.scrollContent.post { workListBinder.updateWorksMinHeight() }
    }

    private fun setText() {
        mBind.vipTitleText.text = AppStrings.get(StringResId.VIDEO_PRO_NAME)
        mBind.vipSubtitleText.text = AppStrings.get(StringResId.VIP_UNLOCK_HINT)
        mBind.btnVip.text = AppStrings.get(StringResId.VIP_OPEN_HINT)
        mBind.worksLabel.text = AppStrings.get(StringResId.WORKS_TAB)
        workListBinder.updateDeleteButtonText()
    }

    private fun enterSelectionMode() {
        selectionController.enterSelectionMode()
        workListBinder.updateDeleteButtonText()
        workListBinder.notifySelectionStateChanged()
    }

    private fun exitSelectionMode(notifyItems: Boolean = true) {
        if (!selectionController.isSelectionMode && !selectionController.hasSelection()) return
        selectionController.exitSelectionMode()
        workListBinder.updateDeleteButtonText()
        if (notifyItems) workListBinder.notifySelectionStateChanged()
    }

    private fun recordWorkDownloaded(taskId: String) {
        mViewModel.recordWorkDownloaded(taskId).obs(viewLifecycleOwner) {
            onError { status ->
                status.msg.toast()
            }
        }
    }

    fun setIconText(
        textView: TextView,
        iconRes: Int,
        text: String,
        iconSizeDp: Int = 18,
    ) {
        val density = textView.resources.displayMetrics.density
        val size = (iconSizeDp * density).toInt()
        val drawable = ContextCompat.getDrawable(textView.context, iconRes)!!.mutate()
        drawable.setBounds(0, 0, size, size)
        val finalText = "  $text"
        val span = SpannableString(finalText)
        span.setSpan(CenterImageSpan(drawable), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = span
    }

    private fun resumeVisibleAnimations() {
        mBind.rvList.post {
            if (isResumed && !isHidden) {
                setVisibleAnimationsRunning(true)
            }
        }
    }

    private fun setVisibleAnimationsRunning(running: Boolean) {
        mBind.ivAvatar.setImageAnimationRunning(running)
        mBind.rvList.children.forEach { itemView ->
            com.flower.flow.ui.fragment.me.WorkItemViewDelegate.setItemAnimationsRunning(
                itemView,
                running
            )
        }
    }
}
