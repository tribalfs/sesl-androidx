package androidx.picker.controller.strategy

import androidx.annotation.Keep
import androidx.picker.controller.strategy.task.LimitedSelectableTask
import androidx.picker.di.AppPickerContext
import androidx.picker.model.AppData
import androidx.picker.model.viewdata.ViewData
import androidx.picker.model.AppInfoData as AppInfoData

/**
 * The concrete strategy that does similar to [AppItemStrategy] but additionally executes [LimitedSelectableTask]
 * to ensure that the number of selected items is limited to [getItemLimitedSize].
 *
 * Once the maximum selected items is reached, this will set the [AppInfoData.dimmed] flag of
 * each of the unselected items to `true` making them unavailable for selection until the selected items is reduced.
 *
 * By default, the limit set to 5. To customize this limit, override [getItemLimitedSize] in a subclass.
 *
 * @param appPickerContext The context for the app picker, providing access to repositories and
 * other dependencies.
 */
@Keep
open class LimitedSelectStrategy(
    appPickerContext: AppPickerContext
) : AppItemStrategy(appPickerContext) {

    private val limitSelectable by lazy(LazyThreadSafetyMode.NONE) {
        LimitedSelectableTask(getItemLimitedSize())
    }

    override fun convert(
        dataList: List<AppData>,
        comparator: Comparator<ViewData>?
    ): List<ViewData> {
        val viewDataList = dataList.transformToViewData(comparator)
        return limitSelectable(viewDataList)
    }

    /**
     * Retrieves the maximum number of items that can be selected.
     *
     * @return The limit for item selection.
     */
    open fun getItemLimitedSize(): Int = 5
}