package androidx.picker.loader.select

import androidx.annotation.Keep
import androidx.picker.common.log.LogTag
import androidx.picker.common.log.debug
import androidx.picker.features.observable.BooleanState
import kotlinx.coroutines.DisposableHandle

/**
 * Represents a selectable item that groups multiple [SelectableItem] instances.
 * Its selection state reflects whether all underlying items are selected.
 *
 * This class allows for a "select all" functionality. When this item is selected, all its
 * constituent items are also selected. Conversely, if any of the constituent items are deselected,
 * this "select all" item will also become deselected.
 *
 * It implements [DisposableHandle] to manage the lifecycle of listeners attached to the
 * underlying [SelectableItem] instances, ensuring they are properly cleaned up when this
 * item is no longer needed.
 *
 * @property list The initial list of [SelectableItem] instances to be managed.
 * @property onUpdated A callback function that is invoked when the selection state of this
 * "select all" item changes. It receives a boolean indicating the new selection state.
 */
@Keep
class AllAppsSelectableItem @JvmOverloads constructor(
    list: List<SelectableItem>,
    onUpdated: (isSelected: Boolean) -> Unit = { }
) : SelectableItem(
    BooleanState(list.all { it.isSelected }),
    onUpdated
), DisposableHandle, LogTag {


    override val logTag: String
        get() = "AllAppsSelectableItem"

    private var disposableHandle: DisposableHandle? = null

    private val selectableItemList= list.toMutableList()

    private val updateAllAppsStatus: () -> Unit = {
        debug("updateAllAppsStatus")
        if (!list.isEmpty()) {
            val isAllSelected = list.all { it.isSelected }
            debug("setValueSilence: $isAllSelected")
            setValueSilence(isAllSelected)
        }
    }

    init {
        bindSelectableItemList()
    }

    override fun dispose() {
        debug("dispose")
        disposableHandle?.dispose()
    }

    fun reset(dataList: List<SelectableItem>) {
        debug("reset, dataList: ${dataList.size}")
        selectableItemList.clear()
        selectableItemList.addAll(dataList)
        bindSelectableItemList()
    }

    private fun bindSelectableItemList() {
        debug("bindSelectableItemList")
        disposableHandle?.dispose()
        disposableHandle = object : DisposableHandle {
            override fun dispose() {
                val disposableHandleList = selectableItemList.map { item ->
                    item.registerAfterChangeUpdateListener { updateAllAppsStatus() }
                }
                val iterator = disposableHandleList.iterator() as MutableIterator<DisposableHandle>
                while (iterator.hasNext()) { iterator.next().dispose() }
            }
        }
    }

}