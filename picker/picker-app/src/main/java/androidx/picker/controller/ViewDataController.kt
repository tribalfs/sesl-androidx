package androidx.picker.controller

import androidx.picker.controller.strategy.Strategy
import androidx.picker.helper.loadIconSync
import androidx.picker.model.AppData
import androidx.picker.model.AppInfo
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.AppSideViewData
import androidx.picker.model.viewdata.ViewData

/**
 * ViewDataController is responsible for managing the data related to views.
 * It extends DataController and uses a specific strategy to handle data operations.
 *
 * This controller maintains both the list of [AppData] objects and the list of [ViewData],
 * which represent the underlying data for the views. It provides methods to add, remove, and update these list.
 * Additionally, it supports ordering of the ViewData items.
 *
 * This also provides method to listen to changes of the [ViewData] list.
 *
 * @param strategy The concrete [Strategy] used by this controller for the creation and management
 * of different UI [ViewData] objects.
 *
 * @see addOnDataEventListener
 * @see removeOnDataEventListener
 */
class ViewDataController(
    private val strategy: Strategy
) : DataController<ViewData>() {

    /**
     * The internal mutable list of [AppData] objects managed by this controller.
     *
     * This list serves as the source data which is then converted into [ViewData] objects.
     * Access to this list should be done via the public [appDataList] property or
     * through the provided methods like `addAppDataList`, `removeAppData`, etc.
     *
     * @see appDataList
     */
    private var _appDataList = mutableListOf<AppData>()

    /**
     * The immutable copy of list of [AppData] objects backed by [_appDataList]
     *
     * @see addAppDataList
     * @see removeAppData
     * @see setAppDataList
     * @see currentList
     */
    val appDataList: List<AppData> get() = _appDataList.toList()

    /**
     * The order of the ViewData list.
     * When set, the list will be re-submitted with the new order.
     */
    var order: Comparator<ViewData>? = null
        set(value) {
            if (field == value) return
            field = value
            submit()
        }

    /**
     * Adds a list of [AppData] to the current list, filtering out duplicates.
     *
     * This function checks for existing [AppData] objects with the same [AppInfo]
     * before adding new items to prevent duplicates. If new items are added,
     * the internal list is updated and [setAppDataList] is called to reflect the changes.
     *
     * @param list The list of [AppData] objects to add.
     */
    fun addAppDataList(list: List<AppData>) {
        val newItems = list.filter { appData ->
            _appDataList.none { isSame(it, appData) }
        }
        if (newItems.isNotEmpty()) {
            _appDataList.addAll(newItems)
            submit()
        }
    }

    /**
     * Retrieves the [AppData] associated with the given [AppInfo].
     *
     * This function first gets the [ViewData] for the provided [appInfo].
     * If the [ViewData] is not an instance of [AppSideViewData], it returns null.
     * If the [ViewData] is an instance of [AppInfoViewData] and its icon is null,
     * it synchronously loads the icon.
     * Finally, it returns the [AppData] from the [AppSideViewData].
     *
     * @param appInfo The [AppInfo] for which to retrieve the [AppData].
     * @return The [AppData] associated with the [appInfo], or null if not found or if the
     *   [ViewData] is not of the correct type.
     */
    fun getAppData(appInfo: AppInfo): AppData? {
        val viewData = getViewData(appInfo)
        if (viewData !is AppSideViewData) return null
        if (viewData is AppInfoViewData && viewData.appInfoData.icon == null) {
            viewData.appInfoData.icon = viewData.iconFlow.loadIconSync()
        }
        return viewData.appData
    }

    /**
     * Returns the [ViewData] associated with the given [AppInfo].
     *
     * @param appInfo The [AppInfo] to search for.
     * @return The [ViewData] associated with the given [AppInfo], or null if not found.
     */
    fun getViewData(appInfo: AppInfo) = currentList.associateBy { it.key }[appInfo]

    /**
     * Removes a single [AppData] element from the current list.
     *
     * This function searches for an [AppData] element in the `appDataList` that
     * is considered the "same" as the provided `element` (based on the `isSame` function,
     * which compares their `appInfo`). If a match is found, it is removed from
     * `appDataList`, and then `setAppDataList` is called to update the underlying
     * data and trigger a UI refresh if necessary.
     *
     * @param element The [AppData] object to remove.
     */
    fun removeAppData(element: AppData) {
        val toRemove = _appDataList.find { isSame(it, element) }
        if (toRemove != null) {
            _appDataList.remove(toRemove)
            submit()
        }
    }

    /**
     * Removes a list of [AppData] from the controller.
     *
     * This function filters the current `appDataList` to find items whose `appInfo` matches any
     * `appInfo` in the provided [list]. If any matching items are found, they are removed from
     * `appDataList`, and the updated list is set using [setAppDataList].
     *
     * @param list The list of [AppData] objects to remove.
     */
    fun removeAppDataList(list: List<AppData>) {
        val appInfoSet = list.map { it.appInfo }.toSet()
        val toRemove = _appDataList.filter { it.appInfo in appInfoSet }
        if (toRemove.isNotEmpty()) {
            _appDataList.removeAll(toRemove)
            submit()
        }
    }

    /**
     * Sets the list of app data.
     *
     * This function replaces the existing list of app data with the new list provided.
     * It then updates the view by submitting the new list and the current sort order.
     *
     * @param mutableAppDataList The new list of [AppData] to be set.
     */
    fun setAppDataList(mutableAppDataList: MutableList<AppData>) {
        _appDataList = mutableAppDataList
        submit()
    }

    private fun isSame(element1: AppData, element2: AppData): Boolean {
        return element1.appInfo == element2.appInfo
    }

    private fun submit() {
        strategy.clear()
        reset(strategy.convert(_appDataList, order))
    }
}

