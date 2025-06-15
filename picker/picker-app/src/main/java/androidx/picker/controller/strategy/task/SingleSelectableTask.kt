package androidx.picker.controller.strategy.task

import androidx.picker.loader.select.SelectableItem
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData
import kotlinx.coroutines.DisposableHandle

/**
 * A task that ensures only one item can be selected at a time from a list of `ViewData`.
 *
 * This task filters out `AppInfoViewData` instances from the input list, extracts their
 * `SelectableItem` objects, and then manages their selection state.
 *
 * Key behaviors:
 * - If no items are initially selected, the first item in the list will be selected by default.
 * - When an item is selected, all other items are automatically deselected.
 * - Listeners are registered on each `SelectableItem` to enforce the single-selection rule.
 * - Previous listeners are disposed of before new ones are registered to prevent memory leaks.
 * - The task returns the original input list, with the selection states of the `SelectableItem`
 *   objects updated.
 *
 * @see Task
 * @see ViewData
 * @see AppInfoViewData
 * @see SelectableItem
 */
class SingleSelectableTask : Task<List<ViewData>, List<ViewData>> {

    private var disposableHandle: DisposableHandle? = null

    override fun invoke(input: List<ViewData>): List<ViewData> {
        val appInfoViewDataList = input.filterIsInstance<AppInfoViewData>()
        val selectableItems = appInfoViewDataList.mapNotNull { it.selectableItem }

        if (selectableItems.isEmpty()) {
            return input
        }

        disposableHandle?.dispose()

        var selectedItem: SelectableItem? = selectableItems.firstOrNull { it.isSelected }

        // Set only the selected item as selected, others as not selected
        selectableItems.forEach { it.setValueSilence(it == selectedItem) }

        val disposableHandleList = selectableItems.flatMap { selectableItem ->
            val before = selectableItem.registerBeforeChangeUpdateListener { _ ->
            // Only allow selection if this is not the already selected item
            selectableItem != selectedItem
        }
            val after = selectableItem.registerAfterChangeUpdateListener { isSelected ->
            if (isSelected) {
                selectedItem = selectableItem
                selectableItems.filter { it != selectedItem }.forEach { it.setValue(false) }
                selectedItem.setValueSilence(true)
            }
        }
            listOf(before, after)
        }

        selectedItem?.setValue(true)

        disposableHandle = object : DisposableHandle {
            override fun dispose() {
                disposableHandleList.forEach { it.dispose() }
            }
        }

        return input
    }
}