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
import androidx.picker.loader.select.SelectableItem
import androidx.picker.model.Selectable
import androidx.picker.model.appdata.CategoryAppData

/**
 * The ui model for a category item managed by [androidx.picker.adapter.AbsAdapter].
 *
 * This class holds the application-specific data ([CategoryAppData]), the selection state
 * ([SelectableItem]), and a list of child items that are currently not visible but are part
 * of this category. It implements interfaces for searchability, selection, and application-side
 * data handling.
 *
 * @property appData The underlying application-specific data for this category.
 * @property selectableItem Manages the selection state of this category item.
 * @property invisibleChildren A list of [ViewData] items that belong to this category but are
 *   currently not displayed. This can be used for features like "show more" or lazy loading.
 */
data class CategoryViewData(
    override val appData: CategoryAppData,
    override val selectableItem: SelectableItem,
    val invisibleChildren: MutableList<ViewData>
) : SearchableViewData, Selectable, AppSideViewData {

    override val key: Any = appData.appInfo

    override val searchable = listOf(title)

    /**
     * The icon representing the category. This can be null if no icon is set.
     *
     * This property directly accesses the `icon` field of the underlying [appData].
     */
    var icon: Drawable?
        get() = appData.icon
        set(value) { appData.icon = value }

    /** The title or label associated with the category, derived from the appData's label. */
    var title: String
        get() = appData.label
        set(value) { appData.label = value }
}