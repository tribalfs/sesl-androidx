package androidx.picker.model.viewdata

import androidx.picker.model.AppData

/**
 * Interface for the (sub) ui model of the side view component of an item view
 * managed by [androidx.picker.adapter.AbsAdapter].
 *
 * This interface extends [ViewData] and provides access to the [AppData.
 *
 * @property appData The [AppData] object which contains
 * information about the application being presented in the picker.
 */
interface AppSideViewData : ViewData {
    val appData: AppData
}