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

package androidx.picker.loader.select

import androidx.annotation.Keep
import androidx.picker.features.observable.MutableState
import androidx.picker.features.observable.UpdateMutableState
import androidx.picker.model.AppInfoData
import kotlin.Unit
import kotlin.reflect.KProperty

/**
 * Represents a selectable item that holds [AppInfoData].
 *
 * This class extends [SelectableItem] and provides specific functionality for handling
 * selection state based on the `selected` property of an [AppInfoData] object.
 *
 * @param mutableState The [AppDataSelectedState] that manages the selection state of the item.
 * @param onUpdated A lambda function that is invoked when the selection state of the item changes.
 *                  It receives a Boolean indicating whether the item is now selected.
 *                  Defaults to null if no specific action is needed on update.
 */
@Suppress("UNCHECKED_CAST")
@Keep
open class AppDataSelectableItem(
    val mutableState: AppDataSelectedState,
    onUpdated: ((isSelected: Boolean) -> Unit)? = null
) : SelectableItem(mutableState as MutableState<Boolean>, onUpdated) {

    constructor(
        appInfoData: AppInfoData,
        onUpdated: (isSelected: Boolean) -> Unit
    ) : this(AppDataSelectedState(appInfoData), onUpdated)

    /**
     * Updates the base [AppInfoData] for this item.
     *
     * This method allows replacing the underlying [AppInfoData] object that this selectable item
     * represents. This is useful if the data associated with the item needs to be refreshed or
     * changed. The selection state will be re-evaluated based on the `selected` property of the
     * new [AppInfoData].
     *
     * @param appInfoData The new [AppInfoData] to set as the base for this item.
     */
    fun updateBase(appInfoData: AppInfoData) {
        mutableState.updateBase(appInfoData)
    }

    /**
     * Manages the selection state of an [AppInfoData] object.
     *
     * This class extends [UpdateMutableState] to provide a way to get and set the
     * `selected` property of an [AppInfoData] object. It acts as a bridge between
     * the selection mechanism and the underlying data model.
     *
     * @param base The initial [AppInfoData] whose selection state is to be managed.
     */
    class AppDataSelectedState(
        base: AppInfoData
    ) : UpdateMutableState<AppInfoData, Boolean>(base) {
        override fun getValue(thisRef: Any?, prop: KProperty<*>): Boolean = base.selected
        override fun setValue(thisRef: Any?, prop: KProperty<*>, value: Boolean?) {
            base.selected = value == true
        }
    }
}