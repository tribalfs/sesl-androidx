package androidx.picker.controller.strategy

import androidx.annotation.Keep
import androidx.picker.controller.strategy.task.SingleSelectableTask
import androidx.picker.di.AppPickerContext
import androidx.picker.model.AppData
import androidx.picker.model.viewdata.ViewData

/**
 * The concrete strategy that does similar to [AppItemStrategy] but additionally executes [SingleSelectableTask]
 * that ensures there only one [item][ViewData] is currently selected.
 *
 * @param appPickerContext The context providing access to necessary repositories and resources.
 */
@Keep
class SingleSelectStrategy(
    appPickerContext: AppPickerContext
) : Strategy(appPickerContext) {

    private val singleSelect by lazy(LazyThreadSafetyMode.NONE) { SingleSelectableTask() }

    override fun convert(
        dataList: List<AppData>,
        comparator: Comparator<ViewData>?
    ): List<ViewData> {
        val viewDataList = dataList.transformToViewData(comparator)
        return singleSelect(viewDataList)
    }
}