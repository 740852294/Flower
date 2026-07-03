package com.flower.flow.domain.profile

/**
 * 作品列表多选状态，与 UI 解耦。
 */
class WorkListSelectionController {

    var isSelectionMode: Boolean = false
        private set

    private val selectedTaskIds = linkedSetOf<String>()

    fun enterSelectionMode() {
        isSelectionMode = true
        selectedTaskIds.clear()
    }

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedTaskIds.clear()
    }

    fun toggleSelection(taskId: String): Boolean {
        return if (!selectedTaskIds.add(taskId)) {
            selectedTaskIds.remove(taskId)
            false
        } else {
            true
        }
    }

    fun isSelected(taskId: String): Boolean = taskId in selectedTaskIds

    fun selectedIds(): List<String> = selectedTaskIds.toList()

    fun hasSelection(): Boolean = selectedTaskIds.isNotEmpty()

    fun shouldShowCancelOnDeleteButton(): Boolean =
        isSelectionMode && selectedTaskIds.isEmpty()
}
