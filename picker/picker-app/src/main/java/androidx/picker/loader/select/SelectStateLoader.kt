package androidx.picker.loader.select

import androidx.annotation.RestrictTo
import androidx.picker.common.log.LogTag
import androidx.picker.model.AppInfo
import androidx.picker.model.AppInfoData
import androidx.picker.model.Selectable
import androidx.picker.model.appdata.CategoryAppData
import androidx.picker.model.viewdata.AllAppsViewData
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData

/**
 * Manages the state of selectable items in a list, including individual items,
 * category-level selections, and an "all apps" selection. It provides methods
 * to create and manage these selectable items, handle selection events, and
 * update their states.
 *
 * This class is intended for internal library use only.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class SelectStateLoader: LogTag {

    private var allAppsSelectableItem: AllAppsSelectableItem? = null
    private val categorySelectableItemMap: MutableMap<AppInfo, CategorySelectableItem> = LinkedHashMap()
    private var onSelectListener: OnSelectListener? = null

    /**
     * Interface for listening to selection state changes.
     */
    interface OnSelectListener {
        fun onAllAppsSelected(isAllSelected: Boolean)
        fun onItemSelected(appInfo: AppInfo, isSelected: Boolean)
    }

    /**
     * Clears all selectable items and their associated data.
     *
     * This function disposes of the "all apps" selectable item and clears the map of
     * category-specific selectable items, disposing of each one.
     */
    fun clearData() {
        allAppsSelectableItem?.dispose()
        allAppsSelectableItem = null
        val iterator = categorySelectableItemMap.iterator()
        while (iterator.hasNext()) {
            iterator.next().value.dispose()
            iterator.remove()
        }
    }

    /**
     * Creates an [AllAppsSelectableItem] from the given list of [SelectableItem] and sets the
     * selection listener.
     *
     * <p>If an [AllAppsSelectableItem] already exists, it will be disposed before creating a new
     * one.
     *
     * @param selectableItemList The list of [SelectableItem] to create the
     *   [AllAppsSelectableItem] from.
     * @return The created [AllAppsSelectableItem].
     */
    fun createAllAppsSelectableItem(selectableItemList: List<SelectableItem>): AllAppsSelectableItem {
        allAppsSelectableItem?.dispose()
        val item = AllAppsSelectableItem(selectableItemList) { selected ->
            onSelectListener?.onAllAppsSelected(selected)
        }
        allAppsSelectableItem = item
        return item
    }

    /**
     * Creates a [CategorySelectableItem] for the given [appData] and [selectableItemList].
     *
     * If a [CategorySelectableItem] already exists for the given [appData], it will be disposed
     * and a new one will be created.
     * The created [CategorySelectableItem] will be stored in the [categorySelectableItemMap]
     * and returned.
     *
     * When the created [CategorySelectableItem] is selected or deselected, the
     * [OnSelectListener.onItemSelected] method will be called with the [appData]'s
     * [AppInfo] and the new selected state.
     *
     * @param appData The [CategoryAppData] to create the [CategorySelectableItem] for.
     * @param selectableItemList The list of [SelectableItem]s that belong to this category.
     * @return The created [CategorySelectableItem].
     */
    fun createCategorySelectableItem(appData: CategoryAppData, selectableItemList: List<SelectableItem>): CategorySelectableItem {
        val appInfo = appData.appInfo
        val newItem = CategorySelectableItem(selectableItemList) { selected ->
            onSelectListener?.onItemSelected(appInfo, selected)
        }
        categorySelectableItemMap.put(appInfo, newItem)?.dispose() // Dispose the old item if there was one
        return newItem
    }


    /**
     * Creates a {@link SelectableItem} for the given {@link AppInfoData}.
     *
     * @param appInfoData The data to create the selectable item for.
     * @return The created {@link SelectableItem}.
     */
    fun createSelectableItem(appInfoData: AppInfoData): SelectableItem {
        val appInfo = appInfoData.appInfo
        return AppDataSelectableItem(appInfoData) { selected ->
            onSelectListener?.onItemSelected(appInfo, selected)
        }
    }

    /**
     * Sets the listener to be notified when an item's selection state changes.
     *
     * @param onListener The listener to set, or null to remove the current listener.
     */
    fun setOnSelectListener(onListener: OnSelectListener?) {
        this.onSelectListener = onListener
    }

    /**
     * Sets the selection state for all items in the given list of ViewData.
     *
     * If an AllAppsViewData is present in the list, it handles the selection state for all
     * AppInfoViewData items. Otherwise, it iterates through all Selectable items and sets their
     * selection state.
     *
     * @param viewDataList The list of ViewData items.
     * @param state The desired selection state (true for selected, false for deselected).
     */
    fun setStateAll(viewDataList: List<ViewData>, state: Boolean) {
        var allAppsViewData: AllAppsViewData? = null
        val appInfoViewDataList  = mutableListOf<AppInfoViewData>()
        val otherSelectables = mutableListOf<Selectable>() // For the 'else' case

        for (viewData in viewDataList) {
            if (viewData is AllAppsViewData) {
                allAppsViewData = viewData
            }
            if (viewData is AppInfoViewData) {
                appInfoViewDataList.add(viewData)
            }

            if (viewData is Selectable) {
                otherSelectables.add(viewData)
            }
        }

        if (allAppsViewData != null) {
            for (appInfoViewData in appInfoViewDataList) {
                if (!appInfoViewData.dimmed) {
                    appInfoViewData.selectableItem?.setValueSilence(state)
                }
            }
            allAppsViewData.selectableItem.setValue(state)
        } else {
            for (selectable in otherSelectables) {
                // Ensure we only process AppInfoViewData that are not dimmed,
                // or other types of Selectable directly.
                if (selectable is AppInfoViewData) {
                    if (!selectable.dimmed) {
                        selectable.selectableItem?.setValueSilence(state)
                    }
                } else {
                    selectable.selectableItem?.setValueSilence(state)
                }
            }
            onSelectListener?.onAllAppsSelected(state)
        }
    }


    /**
     * Updates the list of [SelectableItem]s associated with the "all apps" selectable item.
     *
     * This function effectively resets the [AllAppsSelectableItem] with a new list of
     * underlying selectable items. This is useful when the set of individual items that
     * contribute to the "all apps" selection state changes.
     *
     * @param selectableItemList The new list of [SelectableItem]s.
     */
    fun updateSelectableItemList(selectableItemList: List<SelectableItem>) {
        allAppsSelectableItem?.reset(selectableItemList)
    }

    override val logTag: String
        get() = "SelectStateLoader"
}