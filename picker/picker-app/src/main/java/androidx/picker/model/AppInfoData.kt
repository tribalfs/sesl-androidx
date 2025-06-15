package androidx.picker.model

import android.graphics.drawable.Drawable
import androidx.picker.model.AppData.Companion.TYPE_ITEM_TEXT
import androidx.picker.model.AppData.ItemType

/**
 * Concrete implementation of the [AppInfoData] interface.
 *
 * This class holds the actual data for an application item to be displayed.
 * It provides default values for most properties and overrides `equals` and `hashCode`
 * for proper comparison and use in collections.
 *
 * @param appInfo The core [AppInfo] object containing the application's identity.
 * @param itemType The app itemType representing the type of item.
 * @param icon The main icon [Drawable] for the app.
 * @param subIcon A secondary icon [Drawable], often displayed alongside the main icon.
 * @param label The primary text label for the app.
 * @param subLabel A secondary text label, often displayed below the main label.
 * @param extraLabel An additional text label for supplementary information.
 * @param actionIcon An icon [Drawable] representing an action associated with the app.
 * @param selected A boolean indicating if the app item is currently selected.
 * @param dimmed A boolean indicating if the app item should be displayed in a dimmed state.
 * @param isValueInSubLabel A boolean indicating if the subLabel contains a value that
 *   should be specially handled or formatted.
 */
data class AppInfoDataImpl @JvmOverloads constructor(
    override val appInfo: AppInfo,
    override val itemType: Int = TYPE_ITEM_TEXT,
    override var icon: Drawable? = null,
    override var subIcon: Drawable? = null,
    override var label: String? = null,
    override var subLabel: String? = null,
    override var extraLabel: String? = null,
    override var actionIcon: Drawable? = null,
    override var selected: Boolean = false,
    override var dimmed: Boolean = false,
    override var isValueInSubLabel: Boolean = false
) : AppInfoData

/**
 * Represents the data associated with an application, extending [AppData].
 *
 * This interface defines properties related to the visual representation and state of an app
 * within a picker or list. It includes details like icons, labels, selection state, and dimming.
 *
 * The companion object provides factory methods (`invoke`) for creating instances of [AppInfoData].
 *
 * @property appInfo The core [AppInfo] object inherited from [AppData] containing the application's identity.
 * @property itemType The app itemType representing the type of item, defaults to [TYPE_ITEM_TEXT].
 * @property icon The main icon [Drawable] for the app, defaults to null.
 * @property subIcon A secondary icon [Drawable], often displayed alongside the main icon,
 *   defaults to null.
 * @property label The primary text label for the app, defaults to null.
 * @property subLabel A secondary text label, often displayed below the main label,
 *   defaults to null.
 * @property extraLabel An additional text label for supplementary information, defaults to null.
 * @property actionIcon An icon [Drawable] representing an action associated with the app,
 *   defaults to null.
 * @property selected A boolean indicating if the app item is currently selected,
 *   defaults to false.
 * @property dimmed A boolean indicating if the app item should be displayed in a dimmed state,
 *   defaults to false.
 * @property isValueInSubLabel A boolean indicating if the subLabel contains a value that
 *   should be specially handled or formatted, defaults to false.
 */
interface AppInfoData : AppData {
    var actionIcon: Drawable?
    var dimmed: Boolean
    var extraLabel: String?
    var icon: Drawable?
    @ItemType
    val itemType: Int
    var label: String?
    var selected: Boolean
    var subIcon: Drawable?
    var subLabel: String?
    var isValueInSubLabel: Boolean

    // Convenience properties
    val packageName: String
        get() = appInfo.packageName

    val activityName: String
        get() = appInfo.activityName

    companion object {
        /** Creates an [AppInfoData] instance using the [AppInfoDataImpl] implementation */
        @JvmStatic
        @JvmOverloads
        operator fun invoke(
            appInfo: AppInfo,
            itemType: Int = TYPE_ITEM_TEXT,
            icon: Drawable? = null,
            subIcon: Drawable? = null,
            label: String? = null,
            subLabel: String? = null,
            extraLabel: String? = null,
            actionIcon: Drawable? = null,
            selected: Boolean = false,
            dimmed: Boolean = false,
            isValueInSubLabel: Boolean = false
        ): AppInfoData {
            return AppInfoDataImpl(
                appInfo,
                itemType,
                icon,
                subIcon,
                label,
                subLabel,
                extraLabel,
                actionIcon,
                selected,
                dimmed,
                isValueInSubLabel
            )
        }

    }
}
