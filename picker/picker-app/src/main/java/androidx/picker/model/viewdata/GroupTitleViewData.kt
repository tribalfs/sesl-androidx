package androidx.picker.model.viewdata

import androidx.picker.model.appdata.GroupAppData

/**
 * The ui model for the group title item managed by [androidx.picker.adapter.AbsAdapter].
 *
 * This data is used by UI components to display information related to a group of applications.
 * It encapsulates the [GroupAppData] which contains the actual application data and group
 * information.
 *
 * @property appData The underlying application data for this group title.
 */
data class GroupTitleViewData(
    override val appData: GroupAppData
) : AppSideViewData, ViewData {

    override val key: Any = appData.appInfo

    /** The sub-label associated with the group title, used for display purposes. */
    var label: String
        get() = appData.subLabel
        set(value) { appData.subLabel = value }

    /**
     * The title of the group, derived from the [GroupAppData].
     * Setting this property will update the group name in the underlying [GroupAppData].
     */
    var title: String
        get() = appData.group
        set(value) { appData.group = value }

}