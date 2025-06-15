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

package androidx.picker.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.picker.R
import androidx.picker.adapter.viewholder.GridCheckBoxViewHolder
import androidx.picker.adapter.viewholder.GridRemoveViewHolder
import androidx.picker.adapter.viewholder.GridViewHolder
import androidx.picker.adapter.viewholder.GroupTitleViewHolder
import androidx.picker.adapter.viewholder.PickerViewHolder
import androidx.picker.model.AppData.Companion.TYPE_ITEM_CHECKBOX
import androidx.picker.model.AppData.Companion.TYPE_ITEM_CHECKBOX_REMOVE
import androidx.picker.model.GroupTitleStyleData
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.CustomViewData
import androidx.picker.model.viewdata.GroupTitleViewData

/**
 * An adapter for displaying items in a grid layout.
 *
 * This adapter handles different item types, including group headers, grid items with checkboxes,
 * grid items with remove buttons, and standard grid items.
 *
 * @param context The context used to inflate views.
 * @param groupTitleStyleData Data for styling group titles.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
class GridAdapter(
    context: Context,
    groupTitleStyleData: GroupTitleStyleData
) : AbsAdapter(context, groupTitleStyleData) {

    companion object {
        private const val TYPE_HEADER = 256
        private const val TYPE_GRID = 257
        private const val TYPE_GRID_CHECK = 258
        private const val TYPE_GRID_REMOVE = 259
        private const val TYPE_GROUP_HEADER = 260
        private const val TYPE_CUSTOM = 261
    }

    override fun getItemViewType(position: Int): Int {
        val appInfo = getAppInfo(position)
        return when (appInfo) {
            is CustomViewData -> TYPE_CUSTOM
            is GroupTitleViewData -> TYPE_GROUP_HEADER
            is AppInfoViewData -> when (appInfo.itemType) {
                TYPE_ITEM_CHECKBOX -> TYPE_GRID_CHECK
                TYPE_ITEM_CHECKBOX_REMOVE -> TYPE_GRID_REMOVE
                else -> TYPE_GRID
            }
            else -> TYPE_GRID
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickerViewHolder {
        return when (viewType) {
            TYPE_GROUP_HEADER -> GroupTitleViewHolder(
                inflate(parent, R.layout.picker_app_text),
                groupTitleStyleData
            )
            TYPE_GRID_CHECK -> GridCheckBoxViewHolder(
                inflate(parent, R.layout.picker_app_grid_item_view)
            )
            TYPE_GRID_REMOVE -> GridRemoveViewHolder(
                inflate(parent, R.layout.picker_app_grid_item_view_remove)
            )
            else -> GridViewHolder(
                inflate(parent, R.layout.picker_app_grid_item_view)
            )
        }
    }
}