package androidx.picker.controller.strategy.task

import androidx.picker.model.AppInfo
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData
import kotlinx.coroutines.DisposableHandle

/**
 * A task that limits the number of selectable items.
 *
 * This task operates on a list of [ViewData] and ensures that the number of selected items
 * does not exceed a specified limit. It achieves this by:
 * - Filtering [AppInfoViewData] instances that have a [androidx.picker.loader.select.SelectableItem].
 * - Tracking the currently selected [AppInfo] instances in a [HashSet].
 * - Registering listeners on each [androidx.picker.loader.select.SelectableItem]:
 *     - A `beforeChangeUpdateListener` to prevent selection if the limit is reached.
 *     - An `afterChangeUpdateListener` to update the `selectedSet` and the dimmed state
 *       of other items.
 * - Dimming items that are not selected when the selection limit is reached.
 *
 * The task also manages [DisposableHandle] instances for the registered listeners, ensuring
 * they are disposed of when the task is re-executed or no longer needed.
 *
 * @param limited The maximum number of items that can be selected.
 */
class LimitedSelectableTask(
    private val limited: Int
) : Task<List<ViewData>, List<ViewData>> {

    private var disposableHandle: DisposableHandle? = null
    private var selectedSet: HashSet<AppInfo>? = null

    /**
     * Checks if the selection is dimmed.
     *
     * The selection is dimmed if the number of selected items is greater than or equal to the limit.
     *
     * @return True if the selection is dimmed, false otherwise.
     * @throws UninitializedPropertyAccessException If selectedSet is not initialized.
     */
    private fun isDimmed(): Boolean {
        if (selectedSet == null) {
            throw UninitializedPropertyAccessException("selectedSet")
        }
        return selectedSet!!.size >= limited
    }

    override fun invoke(input: List<ViewData>): List<ViewData> {
        // Filter AppInfoViewData
        val appInfoViewDataList = input.filterIsInstance<AppInfoViewData>()
        // Only those with selectableItem
        val selectablePairs = appInfoViewDataList
            .filter { it.selectableItem != null }
            .map { it to it.selectableItem!! }

        if (selectablePairs.isEmpty()) return input

        // Find selected and not dimmed
        val selectedPairs = selectablePairs.filter { (viewData, selectable) ->
            !viewData.dimmed && selectable.isSelected
        }
        val selectedAppInfos = selectedPairs.map { it.first.appInfo }
        selectedSet = HashSet(selectedAppInfos)

        disposableHandle?.dispose()

        val disposableHandleList = selectablePairs.flatMap { (appInfoViewData, selectableItem) ->
            val before = selectableItem.registerBeforeChangeUpdateListener{ isSelectable ->
                if (isSelectable) !isDimmed()  else true
            }
            val after = selectableItem.registerAfterChangeUpdateListener { isSelectable ->
                if (isSelectable) {
                    selectedSet?.add(appInfoViewData.appInfo)
                } else {
                    selectedSet?.remove(appInfoViewData.appInfo)
                }
                selectableItem.setValue(isSelectable)
                for ((viewData, item) in selectablePairs) {
                    if (!viewData.dimmed || !viewData.selected) {
                        val shouldDim = isDimmed() && !item.isSelected
                        viewData.dimmedItem.setValueSilence(shouldDim)
                    }
                }
            }
            listOf(before, after)
        }

        disposableHandle = object : DisposableHandle {
            override fun dispose() {
                disposableHandleList.forEach { it.dispose() }
            }
        }

        return input
    }
}