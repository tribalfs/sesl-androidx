package androidx.picker.repository

import android.graphics.drawable.Drawable
import androidx.picker.features.observable.UpdateMutableState
import androidx.picker.loader.AppIconFlow
import androidx.picker.loader.DataLoader
import androidx.picker.loader.select.SelectStateLoader
import androidx.picker.loader.select.SelectableItem
import androidx.picker.model.AppInfoData
import androidx.picker.model.appdata.CategoryAppData
import androidx.picker.model.appdata.GroupAppData
import androidx.picker.model.viewdata.AllAppsViewData
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.CategoryViewData
import androidx.picker.model.viewdata.GroupTitleViewData
import kotlin.reflect.KProperty


/**
 * Repository for creating view data models.
 *
 * This class is responsible for transforming raw data into view-specific data models
 * that can be used by the UI layer. It utilizes a [DataLoader] to fetch application
 * information and icons, and a [SelectStateLoader] to manage the selection state of items.
 *
 * @param dataLoader The loader for accessing application data and icons.
 * @param selectStateLoader The loader for managing the selection state of items.
 */
class ViewDataRepository(
    private val dataLoader: DataLoader,
    private val selectStateLoader: SelectStateLoader
) {

    /**
     * Clears all data related to the select state.
     * This function calls the `clearData` method of the `selectStateLoader` to remove any stored
     * selection information.
     */
    fun clearData() = selectStateLoader.clearData()

    /**
     * Creates an [AllAppsViewData] object based on a list of [AppInfoViewData] objects.
     *
     * @param appInfoViewDataList The list of [AppInfoViewData] objects.
     * @return The created [AllAppsViewData] object.
     */
    fun createAllAppsViewData(appInfoViewDataList: List<AppInfoViewData>): AllAppsViewData {
        val selectableItems = ArrayList<SelectableItem>()
        val iterator = appInfoViewDataList.iterator() as MutableIterator<AppInfoViewData>
        while (iterator.hasNext()) {
            iterator.next().selectableItem?.let { selectableItems.add(it) }
        }
        return AllAppsViewData(selectStateLoader.createAllAppsSelectableItem(selectableItems))

    }

    /**
     * Creates an [AppInfoViewData] object from an [AppInfoData] object.
     *
     * @param appInfoData The [AppInfoData] object to convert.
     * @return The created [AppInfoViewData] object.
     */
    fun createAppInfoViewData(appInfoData: AppInfoData): AppInfoViewData {
        val appInfo = appInfoData.appInfo
        val appIconFlow = AppIconFlow(
            base = object : UpdateMutableState<AppInfoData, Drawable>(appInfoData) {
                override fun getValue(thisRef: Any?, prop: KProperty<*>): Drawable? {
                    return base.icon
                }

                override fun setValue(thisRef: Any?, prop: KProperty<*>, value: Drawable?) {
                    base.icon = value
                }

            },
            defaultIconFlow = dataLoader.loadIcon(appInfo)
        )
        val selectableItem = selectStateLoader.createSelectableItem(appInfoData)
        val appInfoViewData = AppInfoViewData(appInfoData, appIconFlow, selectableItem, 1 , null)
        val label = appInfoData.label ?: dataLoader.getLabel(appInfo)
        appInfoViewData.label = label
        return appInfoViewData
    }

    /**
     * Creates a [CategoryViewData] object.
     *
     * @param appData The [CategoryAppData] to use.
     * @param viewDataList The list of [AppInfoViewData] to use.
     * @return The created [CategoryViewData] object.
     */
    fun createCategoryViewData(
        appData: CategoryAppData,
        viewDataList: List<AppInfoViewData>
    ): CategoryViewData {
        val selectableItems = viewDataList.mapNotNull { it.selectableItem }
        return CategoryViewData(
            appData = appData,
            selectableItem = selectStateLoader.createCategorySelectableItem(
                appData,
                selectableItems
            ),
            invisibleChildren = mutableListOf()
        )
    }

    /**
     * Creates a [GroupTitleViewData] object from a [GroupAppData] object.
     *
     * @param groupAppData The [GroupAppData] object to create the [GroupTitleViewData] object from.
     * @return The created [GroupTitleViewData] object.
     */
    fun createGroupTitleViewData(groupAppData: GroupAppData): GroupTitleViewData =
        GroupTitleViewData(groupAppData)
}