package androidx.picker.model.appdata

import androidx.picker.model.AppData
import androidx.picker.model.AppInfo
import androidx.picker.model.Groupable

/**
 * Represents app data that can be grouped.
 *
 * This class extends [AppData] and implements [Groupable], allowing it to be categorized
 * and potentially contain child [AppData] items.
 *
 * @property appInfo The [AppInfo] for this app data, providing essential application details.
 * @property group The group this app data belongs to. Defaults to an empty string, indicating
 * no specific group.
 * @property subLabel A secondary label for this app data, which can provide additional context or
 * information. Defaults to an empty string.
 * @property appDataList A list of [AppData] objects that are considered children or members of
 * this group. Defaults to an empty list.
 *
 */
data class GroupAppData @JvmOverloads constructor(
    override val appInfo: AppInfo,
    override var group: String = "",
    var subLabel: String = "",
    val appDataList: List<AppData> = emptyList()
) : AppData, Groupable {

    /**
     * Alternative constructor for [GroupAppData] that allows initializing the group.
     *
     * @param packageName The packageName to construct the [AppInfo] with.
     * @param activityName The activityName to construct the [AppInfo] with.
     * @param user  The optional user ID associated with this app data. Defaults to 0.
     * @param subLabel A secondary label for this app data, which can provide additional context or
     * @param appDataList A list of [AppData] objects that are considered children or members of
     */
    @JvmOverloads
    constructor(
        packageName: String,
        activityName: String,
        user: Int = 0,
        subLabel: String = "",
        appDataList: List<AppData> = emptyList()
    ) : this(
        appInfo = AppInfo(packageName, activityName, user),
        subLabel = subLabel,
        appDataList = appDataList
    )
}