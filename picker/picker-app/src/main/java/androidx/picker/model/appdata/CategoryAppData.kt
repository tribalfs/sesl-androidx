package androidx.picker.model.appdata

import android.graphics.drawable.Drawable
import androidx.picker.model.AppData
import androidx.picker.model.AppInfo
import androidx.picker.model.AppInfoData

/**
 * Represents a category or group of applications.
 *
 * This class extends [AppData] and holds information specific to a category, including its icon,
 * label, and a list of [AppInfoData] objects representing the applications within that category.
 *
 * @property appInfo The [AppInfo] for the category itself.
 * @property icon The drawable icon for the category.
 * @property label The display name of the category.
 * @property appInfoDataList A list of [AppInfoData] objects, each representing an application
 *                           within this category.
 */
data class CategoryAppData @JvmOverloads constructor(
    override val appInfo: AppInfo,
    var icon: Drawable? = null,
    var label: String = "",
    val appInfoDataList: List<AppInfoData> = emptyList()
) : AppData {

    constructor(
        packageName: String,
        activityName: String,
        user: Int = 0
    ) : this(
        appInfo = AppInfo(packageName, activityName, user)
    )

    val selected: Boolean
        get() = appInfoDataList.all { it.selected }

}