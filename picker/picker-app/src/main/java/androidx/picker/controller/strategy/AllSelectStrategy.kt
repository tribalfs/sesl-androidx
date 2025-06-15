package androidx.picker.controller.strategy

import androidx.annotation.Keep
import androidx.picker.controller.strategy.task.AddAllAppsTask
import androidx.picker.di.AppPickerContext
import androidx.picker.model.AppData
import androidx.picker.model.viewdata.AllAppsViewData
import androidx.picker.model.viewdata.ViewData

/**
 * The concrete strategy that does similar to [AppItemStrategy] but additionally executes [AddAllAppsTask]
 * to prepend an [AllAppsViewData] to the generated list of [ViewData].
 *
 * @param appPickerContext The context providing access to repositories and other dependencies.
 */
@Keep
class AllSelectStrategy(
    appPickerContext: AppPickerContext
) : Strategy(appPickerContext) {

    /** Prepends an [AllAppsViewData] instance into the provided input list. */
    private val prependAllApps by lazy(LazyThreadSafetyMode.NONE) {
        AddAllAppsTask { appInfoViewDataList ->
            viewDataRepository.createAllAppsViewData(appInfoViewDataList)
        }
    }

    override fun convert(
        dataList: List<AppData>,
        comparator: Comparator<ViewData>?
    ): List<ViewData> {
        val viewDataList = dataList.transformToViewData(comparator)
        return prependAllApps(viewDataList)
    }
}