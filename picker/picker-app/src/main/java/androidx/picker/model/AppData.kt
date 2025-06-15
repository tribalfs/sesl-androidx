package androidx.picker.model

import android.graphics.drawable.Drawable
import androidx.annotation.IntDef
import androidx.annotation.Keep
import androidx.picker.model.appdata.CategoryAppData
import androidx.picker.model.appdata.GroupAppData
import kotlin.annotation.AnnotationRetention
import kotlin.annotation.Retention

/**
 * Represents the data associated with an application item in a picker.
 *
 * This interface defines the basic structure for application data, including information about the
 * application itself (via [appInfo]) and provides various builder classes for creating different
 * types of application data items.
 *
 * The companion object defines constants for different item types, which are used by the
 * [AppDataBuilderInfo] annotation to specify the type of item a builder creates.
 *
 * Available Builders:
 * - [ListAppDataBuilder]
 * - [ListCheckBoxAppDataBuilder]
 * - [ListRadioButtonAppDataBuilder]
 * - [ListSwitchAppDataBuilder]
 * - [CategoryAppDataBuilder]
 * - [GridAppDataBuilder]
 * - [GridCheckBoxAppDataBuilder]
 * - [GridRemoveAppDataBuilder]
 * - [GroupAppDataBuilder]
 */
interface AppData {
    val appInfo: AppInfo

    companion object {
        const val TYPE_ITEM_TEXT = 0
        const val TYPE_ITEM_ACTION_BUTTON = 1
        const val TYPE_ITEM_CHECKBOX = 2
        const val TYPE_ITEM_CHECKBOX_REMOVE = 3
        const val TYPE_ITEM_RADIOBUTTON = 4
        const val TYPE_ITEM_SWITCH = 5
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(TYPE_ITEM_TEXT, TYPE_ITEM_ACTION_BUTTON, TYPE_ITEM_CHECKBOX, TYPE_ITEM_CHECKBOX_REMOVE, TYPE_ITEM_RADIOBUTTON, TYPE_ITEM_SWITCH)
    annotation class ItemType

    // --- Builder Info Annotation ---
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class AppDataBuilderInfo(@ItemType val itemType: Int)

    /**
     * A builder interface to help build app-specific data.
     * @param <T> The app-specific data type extending [AppData] which is being built.
     *
     * Available Builders:
     * @see ListAppDataBuilder
     * @see ListCheckBoxAppDataBuilder
     * @see ListRadioButtonAppDataBuilder
     * @see ListSwitchAppDataBuilder
     * @see CategoryAppDataBuilder
     * @see GridAppDataBuilder
     * @see GridCheckBoxAppDataBuilder
     * @see GridRemoveAppDataBuilder
     * @see GroupAppDataBuilder
     */
    interface AppDataBuilder<T : AppData> {
        fun build(): T
    }

    // --- CategoryAppDataBuilder ---
    /**
     * Builder class for creating [CategoryAppData] instances.
     *
     * This builder allows for the construction of category-level application data,
     * which typically groups multiple individual application items ([AppInfoData]).*
     *
     * @property key A unique string identifier for this category. This will also be used as the
     * default label if no explicit label is set.
     * @property appInfoDataList List of [AppInfoData] instances representing the items in this category.
     * @property icon Primary icon displayed for the item.
     * @property label Main text label for the item.
     * @property selected Boolean indicating if the checkbox is currently selected.
     */
    class CategoryAppDataBuilder(val key: String) : AppDataBuilder<CategoryAppData> {
        private var appInfoDataList: List<AppInfoData> = emptyList()
        private var icon: Drawable? = null
        private var label: String? = null
        private var selected: Boolean? = null

        fun setAppDatas(datas: List<AppInfoData>) = apply { this.appInfoDataList = datas }
        fun setIcon(icon: Drawable?) = apply { this.icon = icon }
        fun setLabel(label: String?) = apply { this.label = label }
        fun setSelected(selected: Boolean) = apply { this.selected = selected }

        override fun build(): CategoryAppData {
            val appInfo = AppInfo(key, "")
            val label = this.label ?: key
            selected?.let { sel ->
                appInfoDataList.forEach { it.selected = sel }
            }
            return CategoryAppData(appInfo, icon, label, appInfoDataList)
        }

        constructor(categoryAppData: CategoryAppData) : this(categoryAppData.appInfo.activityName) {
            setIcon(categoryAppData.icon)
            setLabel(categoryAppData.label)
            setAppDatas(categoryAppData.appInfoDataList)
        }
    }

    // --- GridAppDataBuilder ---
    /**
     * Builder for creating [AppInfoData] instances which sets its [itemType][AppInfoData.itemType] to [TYPE_ITEM_TEXT]
     * and allows for customization of the following properties:
     * @property appInfo The core [AppInfo] object associated with this item.
     * @property icon Primary icon displayed for the item.
     * @property label Main text label for the item.
     * @property subIcon Secondary icon, typically smaller and displayed alongside the sub-label.
     * @property subLabel Additional text displayed below or beside the main label.
     * @constructor Creates a new `GridAppDataBuilder` for the given `appInfo`.
     * @constructor Creates a new `GridAppDataBuilder` initialized with data from an existing
     * `appInfoData` object.
     */
    @AppDataBuilderInfo(itemType = TYPE_ITEM_TEXT)
    @Keep
    class GridAppDataBuilder(val appInfo: AppInfo) : AppDataBuilder<AppInfoData> {
        private var icon: Drawable? = null
        private var label: String? = null
        private var subIcon: Drawable? = null
        private var subLabel: String? = null

        fun setIcon(icon: Drawable?) = apply { this.icon = icon }
        fun setLabel(label: String?) = apply { this.label = label }
        fun setSubIcon(subIcon: Drawable?) = apply { this.subIcon = subIcon }
        fun setSubLabel(subLabel: String?) = apply { this.subLabel = subLabel }

        override fun build(): AppInfoData =
            AppInfoDataImpl(appInfo, TYPE_ITEM_TEXT, icon, subIcon, label, subLabel, null, null, false, false, false)

        constructor(appInfoData: AppInfoData) : this(appInfoData.appInfo) {
            setIcon(appInfoData.icon)
            setSubIcon(appInfoData.subIcon)
            setLabel(appInfoData.label)
            setSubLabel(appInfoData.subLabel)
        }
    }

    // --- GridCheckBoxAppDataBuilder ---
    /**
     * Builder for creating [AppInfoData] instances which sets its [itemType][AppInfoData.itemType] to [TYPE_ITEM_CHECKBOX]
     * and allows for customization of the following properties:
     * @property appInfo The core [AppInfo] object associated with this item.
     * @property icon Primary icon displayed for the item.
     * @property label Main text label for the item.
     * @property subIcon Secondary icon, typically smaller and displayed alongside the sub-label.
     * @property subLabel Additional text displayed below or beside the main label.
     * @property selected Boolean indicating if the checkbox is currently selected.
     * @property dimmed Boolean indicating if the item should appear dimmed or disabled.
     * @constructor Creates a new [GridCheckBoxAppDataBuilder] for the given [appInfo].
     * @constructor Creates a new [GridCheckBoxAppDataBuilder] by copying properties from an existing [AppInfoData].
     */
    @AppDataBuilderInfo(itemType = TYPE_ITEM_CHECKBOX)
    @Keep
    class GridCheckBoxAppDataBuilder(val appInfo: AppInfo) : AppDataBuilder<AppInfoData> {
        private var icon: Drawable? = null
        private var label: String? = null
        private var subIcon: Drawable? = null
        private var subLabel: String? = null
        private var selected: Boolean = false
        private var dimmed: Boolean = false

        fun setIcon(icon: Drawable?) = apply { this.icon = icon }
        fun setLabel(label: String?) = apply { this.label = label }
        fun setSubIcon(subIcon: Drawable?) = apply { this.subIcon = subIcon }
        fun setSubLabel(subLabel: String?) = apply { this.subLabel = subLabel }
        fun setSelected(selected: Boolean) = apply { this.selected = selected }
        fun setDimmed(dimmed: Boolean) = apply { this.dimmed = dimmed }

        override fun build(): AppInfoData =
            AppInfoDataImpl(appInfo, TYPE_ITEM_CHECKBOX, icon, subIcon, label, subLabel, null, null, selected, dimmed, false)

        constructor(appInfoData: AppInfoData) : this(appInfoData.appInfo) {
            setIcon(appInfoData.icon)
            setSubIcon(appInfoData.subIcon)
            setLabel(appInfoData.label)
            setSubLabel(appInfoData.subLabel)
            setSelected(appInfoData.selected)
            setDimmed(appInfoData.dimmed)
        }
    }

    // --- GridRemoveAppDataBuilder ---
    /**
     * Builder for creating [AppInfoData] instances which sets its [itemType][AppInfoData.itemType] to [TYPE_ITEM_CHECKBOX_REMOVE]
     * and allows for customization of the following properties:
     * @property appInfo The core [AppInfo] object associated with this item.
     * @property icon Primary icon displayed for the item.
     * @property label Main text label for the item.
     * @property subIcon Secondary icon, typically smaller and displayed alongside the sub-label.
     * @property subLabel Additional text displayed below or beside the main label.
     * @property dimmed Boolean indicating if the item should appear dimmed or disabled.
     *
     * @constructor Creates a new builder for the given [appInfo].
     * @constructor Creates a new builder initialized with the data from an existing [AppInfoData] instance.
     */
    @AppDataBuilderInfo(itemType = TYPE_ITEM_CHECKBOX_REMOVE)
    @Keep
    class GridRemoveAppDataBuilder(val appInfo: AppInfo) : AppDataBuilder<AppInfoData> {
        private var icon: Drawable? = null
        private var label: String? = null
        private var subIcon: Drawable? = null
        private var subLabel: String? = null
        private var selected: Boolean = false
        private var dimmed: Boolean = false

        fun setIcon(icon: Drawable?) = apply { this.icon = icon }
        fun setLabel(label: String?) = apply { this.label = label }
        fun setSubIcon(subIcon: Drawable?) = apply { this.subIcon = subIcon }
        fun setSubLabel(subLabel: String?) = apply { this.subLabel = subLabel }
        fun setSelected(selected: Boolean) = apply { this.selected = selected }
        fun setDimmed(dimmed: Boolean) = apply { this.dimmed = dimmed }

        override fun build(): AppInfoData =
            AppInfoDataImpl(appInfo, TYPE_ITEM_CHECKBOX_REMOVE, icon, subIcon, label, subLabel, null, null, selected, dimmed, false)

        constructor(appInfoData: AppInfoData) : this(appInfoData.appInfo) {
            setIcon(appInfoData.icon)
            setSubIcon(appInfoData.subIcon)
            setLabel(appInfoData.label)
            setSubLabel(appInfoData.subLabel)
            setSelected(appInfoData.selected)
            setDimmed(appInfoData.dimmed)
        }
    }

    // --- GroupAppDataBuilder ---
    /**
     * A builder class for creating [GroupAppData] instances.
     *
     * This builder facilitates the construction of group-level application data,
     * which can contain a list of other [AppData] items.
     *
     * @property key A unique string identifier for this group.
     *               This will be used as the default label if no label is explicitly set.
     * @constructor Creates a new `GroupAppDataBuilder` with the given key.
     * @constructor Creates a new `GroupAppDataBuilder` initialized with the data from an existing [GroupAppData] object.
     */
    class GroupAppDataBuilder(val key: String) : AppDataBuilder<GroupAppData> {
        private var label: String? = null
        private var subLabel: String? = null
        private var appInfoDataList: List<AppData> = emptyList()

        fun setLabel(label: String?) = apply { this.label = label }
        fun setSubLabel(subLabel: String?) = apply { this.subLabel = subLabel }
        fun setAppDatas(datas: List<AppData>) = apply { this.appInfoDataList = datas }

        override fun build(): GroupAppData {
            val appInfo = AppInfo(key, "")
            val label = this.label ?: key
            val subLabel = this.subLabel ?: ""
            return GroupAppData(appInfo, label, subLabel, appInfoDataList)
        }

        constructor(groupAppData: GroupAppData) : this(groupAppData.appInfo.activityName) {
            setLabel(groupAppData.group)
            setAppDatas(groupAppData.appDataList)
        }
    }

    // --- ListAppDataBuilder ---
    /**
     * Builder for creating [AppInfoData] instances which sets its [itemType][AppInfoData.itemType] to [TYPE_ITEM_TEXT]
     * and allows for customization of the following properties:
     * @property appInfo The core [AppInfo] object associated with this item.
     * @property icon Primary icon displayed for the item.
     * @property label Main text label for the item.
     * @property subIcon Secondary icon, typically smaller and displayed alongside the sub-label.
     * @property subLabel Additional text displayed below or beside the main label.
     * @property extraLabel  An optional third label for supplementary information typically
     * displayed at the right side of the view
     * @property isValueInSubLabel Flog to indicate if the sub-label represents a value or not
     *
     * Example usage:
     * ```kotlin
     * val appInfo = AppInfo("com.example.app", "Example App")
     * val listAppData = AppData.ListAppDataBuilder(appInfo)
     *     .setIcon(ContextCompat.getDrawable(context, R.drawable.ic_app_icon))
     *     .setLabel("App Name")
     *     .setSubLabel("Version 1.0", isValue = true)
     *     .setExtraLabel("Details")
     *     .build()
     * ```
     */
    @AppDataBuilderInfo(itemType = TYPE_ITEM_TEXT)
    @Keep
    class ListAppDataBuilder(val appInfo: AppInfo) : AppDataBuilder<AppInfoData> {
        private var icon: Drawable? = null
        private var label: String? = null
        private var subIcon: Drawable? = null
        private var subLabel: String? = null
        private var extraLabel: String? = null
        private var isValueInSubLabel: Boolean = false

        fun setIcon(icon: Drawable?) = apply { this.icon = icon }
        fun setLabel(label: String?) = apply { this.label = label }
        fun setSubIcon(subIcon: Drawable?) = apply { this.subIcon = subIcon }
        fun setSubLabel(subLabel: String?, isValue: Boolean = false) = apply {
            this.subLabel = subLabel
            this.isValueInSubLabel = isValue
        }
        fun setExtraLabel(extraLabel: String?) = apply { this.extraLabel = extraLabel }

        override fun build(): AppInfoData =
            AppInfoDataImpl(appInfo, TYPE_ITEM_TEXT, icon, subIcon, label, subLabel, extraLabel, null, false, false, isValueInSubLabel)

        constructor(appInfoData: AppInfoData) : this(appInfoData.appInfo) {
            setIcon(appInfoData.icon)
            setSubIcon(appInfoData.subIcon)
            setLabel(appInfoData.label)
            setSubLabel(appInfoData.subLabel, appInfoData.isValueInSubLabel)
            setExtraLabel(appInfoData.extraLabel)
        }
    }

    // --- ListCheckBoxAppDataBuilder ---
    /**
     * Builder for creating [AppInfoData] instances which sets its [itemType][AppInfoData.itemType] to [TYPE_ITEM_CHECKBOX]
     * and allows for customization of the following properties:
     * @property appInfo The core [AppInfo] object associated with this item.
     * @property icon Primary icon displayed for the item.
     * @property label Main text label for the item.
     * @property subIcon Secondary icon, typically smaller and displayed alongside the sub-label.
     * @property subLabel Additional text displayed below or beside the main label.
     * @property extraLabel  An optional third label for supplementary information.
     * @property actionIcon Icon for an action associated with the item (e.g., a settings gear).
     * @property selected Boolean indicating if the checkbox is currently selected.
     * @property dimmed Boolean indicating if the item should appear dimmed or disabled.
     */
    @AppDataBuilderInfo(itemType = TYPE_ITEM_CHECKBOX)
    @Keep
    class ListCheckBoxAppDataBuilder(val appInfo: AppInfo) : AppDataBuilder<AppInfoData> {
        private var icon: Drawable? = null
        private var label: String? = null
        private var subIcon: Drawable? = null
        private var subLabel: String? = null
        private var extraLabel: String? = null
        private var actionIcon: Drawable? = null
        private var selected: Boolean = false
        private var dimmed: Boolean = false

        fun setIcon(icon: Drawable?) = apply { this.icon = icon }
        fun setLabel(label: String?) = apply { this.label = label }
        fun setSubIcon(subIcon: Drawable?) = apply { this.subIcon = subIcon }
        fun setSubLabel(subLabel: String?) = apply { this.subLabel = subLabel }
        fun setExtraLabel(extraLabel: String?) = apply { this.extraLabel = extraLabel }
        fun setActionIcon(actionIcon: Drawable?) = apply { this.actionIcon = actionIcon }
        fun setSelected(selected: Boolean) = apply { this.selected = selected }
        fun setDimmed(dimmed: Boolean) = apply { this.dimmed = dimmed }

        override fun build(): AppInfoData =
            AppInfoDataImpl(appInfo, TYPE_ITEM_CHECKBOX, icon, subIcon, label, subLabel, extraLabel, actionIcon, selected, dimmed, false)

        constructor(appInfoData: AppInfoData) : this(appInfoData.appInfo) {
            setIcon(appInfoData.icon)
            setSubIcon(appInfoData.subIcon)
            setLabel(appInfoData.label)
            setSubLabel(appInfoData.subLabel)
            setExtraLabel(appInfoData.extraLabel)
            setActionIcon(appInfoData.actionIcon)
            setSelected(appInfoData.selected)
            setDimmed(appInfoData.dimmed)
        }
    }

    // --- ListRadioButtonAppDataBuilder ---
    /**
     * Builder for creating [AppInfoData] instances which sets its [itemType][AppInfoData.itemType] to [TYPE_ITEM_RADIOBUTTON]
     * and allows for customization of the following properties:
     * @property appInfo The core [AppInfo] object associated with this item.
     * @property icon Primary icon displayed for the item.
     * @property label Main text label for the item.
     * @property subIcon Secondary icon, typically smaller and displayed alongside the sub-label.
     * @property subLabel Additional text displayed below or beside the main label.
     * @property actionIcon Icon for an action associated with the item (e.g., a settings gear).
     * @property selected Boolean indicating if the checkbox is currently selected.
     * @property dimmed Boolean indicating if the item should appear dimmed or disabled.
     *
     * This builder allows for the creation of [AppInfoData] instances representing list items
     * that include a radio button for selection. It provides methods to set various visual
     * elements and the selection state of the item.
     *
     * @constructor Creates a new [ListRadioButtonAppDataBuilder] with the given [AppInfo].
     * @constructor Creates a new [ListRadioButtonAppDataBuilder] initialized with the properties
     * of an existing [AppInfoData] object. This is useful for modifying an existing item.
     */
    @AppDataBuilderInfo(itemType = TYPE_ITEM_RADIOBUTTON)
    @Keep
    class ListRadioButtonAppDataBuilder(val appInfo: AppInfo) : AppDataBuilder<AppInfoData> {
        private var icon: Drawable? = null
        private var label: String? = null
        private var subIcon: Drawable? = null
        private var subLabel: String? = null
        private var actionIcon: Drawable? = null
        private var selected: Boolean = false
        private var dimmed: Boolean = false

        fun setIcon(icon: Drawable?) = apply { this.icon = icon }
        fun setLabel(label: String?) = apply { this.label = label }
        fun setSubIcon(subIcon: Drawable?) = apply { this.subIcon = subIcon }
        fun setSubLabel(subLabel: String?) = apply { this.subLabel = subLabel }
        fun setActionIcon(actionIcon: Drawable?) = apply { this.actionIcon = actionIcon }
        fun setSelected(selected: Boolean) = apply { this.selected = selected }
        fun setDimmed(dimmed: Boolean) = apply { this.dimmed = dimmed }

        override fun build(): AppInfoData =
            AppInfoDataImpl(appInfo, TYPE_ITEM_RADIOBUTTON, icon, subIcon, label, subLabel, null, actionIcon, selected, dimmed, false)

        constructor(appInfoData: AppInfoData) : this(appInfoData.appInfo) {
            setIcon(appInfoData.icon)
            setSubIcon(appInfoData.subIcon)
            setLabel(appInfoData.label)
            setSubLabel(appInfoData.subLabel)
            setActionIcon(appInfoData.actionIcon)
            setSelected(appInfoData.selected)
            setDimmed(appInfoData.dimmed)
        }
    }

    // --- ListSwitchAppDataBuilder ---
    /**
     * Builder for creating [AppInfoData] instances which sets its [itemType][AppInfoData.itemType] to [TYPE_ITEM_SWITCH].
     * and allows for customization of the following properties:
     * @property appInfo The core [AppInfo] object associated with this item.
     * @property icon Primary icon displayed for the item.
     * @property label Main text label for the item.
     * @property subIcon Secondary icon, typically smaller and displayed alongside the sub-label.
     * @property subLabel Additional text displayed below or beside the main label.
     * @property selected Boolean indicating if the checkbox is currently selected.
     * @property dimmed Boolean indicating if the item should appear dimmed or disabled.
     * @property isValueInSubLabel Flog to indicate if the sub-label represents a value or not
     *
     * @constructor Creates a new [ListSwitchAppDataBuilder] from an existing [AppInfoData] instance.
     * This is useful for modifying an existing item.
     * @constructor Creates a new [ListSwitchAppDataBuilder] for the given [AppInfo].
     */
    @AppDataBuilderInfo(itemType = TYPE_ITEM_SWITCH)
    @Keep
    class ListSwitchAppDataBuilder(val appInfo: AppInfo) : AppDataBuilder<AppInfoData> {
        private var icon: Drawable? = null
        private var label: String? = null
        private var subIcon: Drawable? = null
        private var subLabel: String? = null
        private var selected: Boolean = false
        private var dimmed: Boolean = false
        private var isValueInSubLabel: Boolean = false

        fun setIcon(icon: Drawable?) = apply { this.icon = icon }
        fun setLabel(label: String?) = apply { this.label = label }
        fun setSubIcon(subIcon: Drawable?) = apply { this.subIcon = subIcon }
        fun setSubLabel(subLabel: String?, isValue: Boolean = false) = apply {
            this.subLabel = subLabel
            this.isValueInSubLabel = isValue
        }
        fun setSelected(selected: Boolean) = apply { this.selected = selected }
        fun setDimmed(dimmed: Boolean) = apply { this.dimmed = dimmed }

        override fun build(): AppInfoData =
            AppInfoDataImpl(appInfo, TYPE_ITEM_SWITCH, icon, subIcon, label, subLabel, null, null, selected, dimmed, isValueInSubLabel)

        constructor(appInfoData: AppInfoData) : this(appInfoData.appInfo) {
            setIcon(appInfoData.icon)
            setSubIcon(appInfoData.subIcon)
            setLabel(appInfoData.label)
            setSubLabel(appInfoData.subLabel, appInfoData.isValueInSubLabel)
            setSelected(appInfoData.selected)
            setDimmed(appInfoData.dimmed)
        }
    }
}