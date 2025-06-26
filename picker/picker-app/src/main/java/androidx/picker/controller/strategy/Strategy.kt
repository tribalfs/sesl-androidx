package androidx.picker.controller.strategy

import androidx.picker.controller.strategy.task.ConvertAppInfoDataTask
import androidx.picker.controller.strategy.task.ParseAppDataTask
import androidx.picker.controller.strategy.task.SortAppInfoViewDataTask
import androidx.picker.di.AppPickerContext
import androidx.picker.model.AppData
import androidx.picker.model.AppInfoData
import androidx.picker.model.appdata.CategoryAppData
import androidx.picker.model.appdata.GroupAppData
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData

/**
 * The strategy for the creation and management of different UI [ViewData] objects
 * for [AbsAdapter][androidx.picker.adapter.AbsAdapter].
 * This includes data that defines the selection behavior of the items.
 *
 * The concrete strategy subclass is responsible for converting the [AppData] objects
 * into instances of corresponding subclass of UI [ViewData].
 *
 * Concrete strategies:
 * - [AppItemStrategy]
 * - [SingleSelectStrategy]
 * - [AllSelectStrategy]
 * - [LimitedSelectStrategy]
 *
 * @property appPickerContext The context providing access to dependencies like repositories.
 * @see androidx.picker.controller.ViewDataController
 */
abstract class Strategy(private val appPickerContext: AppPickerContext) {

    val viewDataRepository = appPickerContext.viewDataRepository

    /**
     * Clears the data stored in the [androidx.picker.repository.ViewDataRepository].
     *
     * This function is typically called to reset the state of the picker.
     */
    fun clear() = viewDataRepository.clearData()

    /**
     * Converts a list of [AppData] objects into a list of [ViewData] objects.
     *
     * This is an abstract function that must be implemented by concrete strategy classes.
     * The implementation should define how the conversion is performed and, if a
     * [comparator] is provided, how the resulting [ViewData] list is sorted.
     *
     * @param dataList The mutable list of [AppData] objects to be converted.
     *                 The list can contain null elements.
     * @param comparator An optional [Comparator] to sort the resulting list of [ViewData] objects.
     *                   If null, the list will not be sorted by this function.
     * @return A mutable list of [ViewData] objects, potentially sorted according to the
     *         provided [comparator]. The list can contain null elements.
     */
    abstract fun convert(
        dataList: List<AppData>,
        comparator: Comparator<ViewData>?
    ): List<ViewData>

    private var _comparator: Comparator<ViewData>? = null

    private val convertAppInfoDataTask by lazy(LazyThreadSafetyMode.NONE) {
        ConvertAppInfoDataTask(
            createAppInfoViewData = { appInfoData -> viewDataRepository.createAppInfoViewData(appInfoData) }
        )
    }

    private val sortAppInfoViewDataTask by lazy(LazyThreadSafetyMode.NONE) { SortAppInfoViewDataTask() }

    private fun List<AppInfoData>.convertToAppInfoDataSorted() = sortAppInfoViewDataTask(convertAppInfoDataTask(this), _comparator)

    private fun GroupAppData.convertToGroupTitleViewData() = viewDataRepository.createGroupTitleViewData(this)

    private fun CategoryAppData.convertToCategoryViewData(appInfoViewDataList: List<AppInfoViewData>) =
        viewDataRepository.createCategoryViewData(this, appInfoViewDataList)

    /**
     * Transforms a list of different types of [AppData] objects and it into
     * a list of different concrete [ViewData] objects
     */
    private val parseAppData by lazy(LazyThreadSafetyMode.NONE) {
        ParseAppDataTask(
            createAppInfoViewDatas = { appInfoDataList -> appInfoDataList.convertToAppInfoDataSorted() },
            createGroupTitleViewData = { groupAppData -> groupAppData.convertToGroupTitleViewData() },
            createCategoryViewData = { catAppData, appInfoViewDataList ->
                catAppData.convertToCategoryViewData(appInfoViewDataList)
            }
        )
    }

    /**
     * Transforms a list of different types of [AppData] objects and it into
     * a list of different concrete [ViewData] objects sorted based on the provided [comparator].
     */
    internal fun List<AppData>.transformToViewData(comparator: Comparator<ViewData>?): List<ViewData> {
        _comparator = comparator
        return parseAppData(this).also { _comparator = null }
    }
}