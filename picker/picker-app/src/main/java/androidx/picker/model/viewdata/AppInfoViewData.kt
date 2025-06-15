/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.picker.model.viewdata

import android.graphics.drawable.Drawable
import androidx.picker.features.observable.ObservableProperty
import androidx.picker.features.observable.StringState
import androidx.picker.features.observable.UpdateMutableState
import androidx.picker.features.observable.UpdateObservableProperty
import androidx.picker.loader.AppIconFlow
import androidx.picker.loader.select.AppDataSelectableItem
import androidx.picker.loader.select.SelectableItem
import androidx.picker.model.AppData
import androidx.picker.model.AppData.ItemType
import androidx.picker.model.AppInfo
import androidx.picker.model.AppInfoData
import androidx.picker.model.Highlightable
import androidx.picker.model.Selectable
import androidx.picker.model.SpanData
import kotlin.reflect.KProperty

/**
 * The UI model for an application item managed by [AbsAdapter][androidx.picker.adapter.AbsAdapter].
 *
 * This class combines application information ([AppInfoData]) with UI-specific properties like
 * icon loading ([AppIconFlow]), selection state ([SelectableItem]), and layout information
 * ([SpanData]). It also implements interfaces for searchability, highlighting, and handling
 * action clicks.
 *
 * @property appInfoData The core application data.
 * @property iconFlow The flow responsible for loading the application icon.
 * @property selectableItem The item representing the selectable state of this application.
 * @property spanCount The number of columns this item should span in a grid layout.
 * @property onActionClick A callback function triggered when an action associated with this item is
 *   clicked.
 */
data class AppInfoViewData(
    val appInfoData: AppInfoData,
    val iconFlow: AppIconFlow,
    override val selectableItem: SelectableItem? = null,
    override val spanCount: Int = 1,
    var onActionClick: ((AppInfoViewData) -> Unit)? = null,
) : SearchableViewData,
    AppInfoData by appInfoData,
    AppSideViewData,
    SpanData,
    Selectable,
    Highlightable {

    override val appData: AppData = this.appInfoData

    private val highlightText = ObservableProperty(StringState(""))

    /**
     * An observable property indicating whether the item should be dimmed in the UI.
     * This typically signifies that the item is not currently selectable or available.
     * It is backed by the [AppInfoData.dimmed] property and allows for observing changes to it.
     */
    val dimmedItem: UpdateObservableProperty<AppInfoData, Boolean> =
        UpdateObservableProperty(object : UpdateMutableState<AppInfoData, Boolean>(appInfoData) {
            override fun getValue(thisRef: Any?, prop: KProperty<*>): Boolean = base.dimmed
            override fun setValue(thisRef: Any?, prop: KProperty<*>, value: Boolean?) { base.dimmed = value == true }
        })

    override fun getHighlightText(): ObservableProperty<String> = highlightText

    override val key: Any
        get() = appInfoData.appInfo

    override val searchable: List<String>
        get() = listOfNotNull(appInfoData.label)

    override var icon: Drawable?
        get() = appInfoData.icon
        set(value) { appInfoData.icon = value }

    override var label: String?
        get() = appInfoData.label
        set(value) { appInfoData.label = value }

    override var subIcon: Drawable?
        get() = appInfoData.subIcon
        set(value) { appInfoData.subIcon = value }

    override var subLabel: String?
        get() = appInfoData.subLabel
        set(value) { appInfoData.subLabel = value }

    override var actionIcon: Drawable?
        get() = appInfoData.actionIcon
        set(value) { appInfoData.actionIcon = value }

    override var extraLabel: String?
        get() = appInfoData.extraLabel
        set(value) { appInfoData.extraLabel = value }

    override var selected: Boolean
        get() = appInfoData.selected
        set(value) { appInfoData.selected = value }

    override var dimmed: Boolean
        get() = appInfoData.dimmed
        set(value) { appInfoData.dimmed = value }

    override var isValueInSubLabel: Boolean
        get() = appInfoData.isValueInSubLabel
        set(value) { appInfoData.isValueInSubLabel = value }

    /** The type of this item, used for view recycling in RecyclerView. */
    @ItemType
    override val itemType: Int
        get() = appInfoData.itemType

    override val packageName: String
        get() = appInfoData.packageName

    override val activityName: String
        get() = appInfoData.activityName

    override val appInfo: AppInfo
        get() = appInfoData.appInfo


    /**
     * Updates the current [AppInfoViewData] with new application data.
     *
     * This function checks for identity and equality to avoid unnecessary updates. If the new data
     * is different, it updates the underlying [AppInfoData] and creates a new [AppInfoViewData]
     * instance with the updated data. If the new data has a null icon or label, it retains the
     * existing icon or label.
     *
     * @param newData The new [AppInfoData] to update with.
     * @return A new [AppInfoViewData] instance with the updated data, or `this` if the data is
     *   identical, or `null` if the data is equal but not identical (no visual change).
     */
    fun update(newData: AppInfoData): AppInfoViewData? {
        if (appInfoData == newData) return this
        if (newData.icon == null) newData.icon = icon
        if (newData.label == null) newData.label = label
        updateBase(newData)
        return copy(appInfoData = newData)
    }

    /**
     * Updates the base data for the [selectableItem], [dimmedItem], and [iconFlow].
     *
     * This method is used to propagate changes in the underlying [AppInfoData] to the
     * UI-related components.
     *
     * @param newData The new [AppInfoData] to update with.
     */
    private fun updateBase(newData: AppInfoData) {
        (selectableItem as? AppDataSelectableItem)?.updateBase(newData)
        dimmedItem.update(newData)
        iconFlow.base.updateBase(newData)
    }

}