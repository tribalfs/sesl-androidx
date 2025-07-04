package androidx.picker.controller.strategy

import androidx.annotation.Keep
import androidx.picker.di.AppPickerContext
import androidx.picker.model.AppData
import androidx.picker.model.viewdata.ViewData

/**
 * The concrete strategy of [Strategy] that creates the list of different UI [ViewData] objects
 * sorted based on the provided `comparator`, from a list of [AppData] objects.
 *
 * @param appPickerContext The [AppPickerContext] providing access to dependencies.
 */
@Keep
open class AppItemStrategy(
    appPickerContext: AppPickerContext
) : Strategy(appPickerContext) {

    override fun convert(
        dataList: List<AppData>,
        comparator: Comparator<ViewData>?
    ): List<ViewData> = dataList.transformToViewData(comparator)
}