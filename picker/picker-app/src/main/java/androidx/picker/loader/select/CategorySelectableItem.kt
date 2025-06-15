package androidx.picker.loader.select

import androidx.annotation.Keep
import androidx.picker.features.observable.BooleanState
import kotlinx.coroutines.DisposableHandle

/**
 * Represents a selectable item that acts as a category for a list of other selectable items.
 *
 * This class extends [SelectableItem] and implements [DisposableHandle].
 * Its selection state is determined by the selection state of all its child [SelectableItem]s.
 * If all child items are selected, this category item is selected. Otherwise, it's not.
 *
 * When this category item's selection state changes (either by direct user interaction or
 * programmatically), it propagates that change to all its child items.
 *
 * Conversely, if any of the child items' selection state changes, this category item
 * updates its own selection state accordingly.
 *
 * It's important to call [dispose] when this item is no longer needed to release
 * listeners and prevent memory leaks.
 *
 * @param selectableItemList The list of [SelectableItem]s that this category manages.
 * @param onUpdated A lambda function that is invoked when the selection state of this category item changes.
 *                  It receives a boolean indicating the new selection state (true if selected, false otherwise).
 *                  Defaults to an empty lambda.
 */
@Keep
class CategorySelectableItem @JvmOverloads constructor(
    selectableItemList: List<SelectableItem>,
    onUpdated: (isSelected: Boolean) -> Unit = { }
) : SelectableItem(
    BooleanState(selectableItemList.all { it.isSelected }),
    onUpdated
), DisposableHandle {

    private var disposableHandle: DisposableHandle? = null
    private val selectableItemList: MutableList<SelectableItem> = selectableItemList.toMutableList()

    init {
        bindSelectableItemList()
    }

    private fun bindSelectableItemList() {
        disposableHandle?.dispose()

        val disposableListener = registerAfterChangeUpdateListener { isSelected ->
            selectableItemList.forEach { it.setValueSilence(isSelected) }
        }

        val disposableHandleList = selectableItemList.map { item ->
            item.registerAfterChangeUpdateListener{ updateAllAppsStatus() }
        }

        disposableHandle = object: DisposableHandle {
            override fun dispose() {
                disposableListener.dispose()
                val disposableHandles = disposableHandleList.iterator() as MutableIterator<DisposableHandle>
                while (disposableHandles.hasNext()) {
                    disposableHandles.next().dispose()
                }
            }
        }
    }

    private fun updateAllAppsStatus() {
        if (selectableItemList.isEmpty()) return
        val allSelected = selectableItemList.all { it.isSelected }
        setValueSilence(allSelected)
    }

    override fun dispose() {
        disposableHandle?.dispose()
    }

}