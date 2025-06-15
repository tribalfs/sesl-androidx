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
import androidx.picker.adapter.viewholder.AppListItemViewHolder
import androidx.picker.adapter.viewholder.GroupTitleViewHolder
import androidx.picker.adapter.viewholder.PickerViewHolder
import androidx.picker.features.composable.ComposableFactory
import androidx.picker.features.composable.ComposableStrategy
import androidx.picker.model.GroupTitleStyleData
import androidx.picker.model.viewdata.GroupTitleViewData
import kotlin.ranges.IntRange

/**
 * This RecyclerView adapter handles different view types for items and group headers.
 * It uses a [ComposableFactory] to create and manage composable views for items.
 *
 * @param context The context used to inflate views.
 * @param composableStrategy The strategy for creating composable views.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
class ListAdapter(
    context: Context,
    composableStrategy: ComposableStrategy
) : AbsAdapter(context) {

    private val composableFactory: ComposableFactory = ComposableFactory(composableStrategy)
    private val composableViewTypeRange: IntRange = composableFactory.viewTypeRange
    val typeGroupHeader: Int = composableFactory.viewTypeRange.last + 1

    override fun getItemViewType(position: Int): Int {
        val appInfo = getAppInfo(position)
        val itemType = composableFactory.getItemType(appInfo)
        if (itemType != null) {
            return itemType
        }
        if (appInfo is GroupTitleViewData) {
            return typeGroupHeader
        }
        throw RuntimeException("Not Implemented")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickerViewHolder {
        return when {
            composableViewTypeRange.contains(viewType) -> {
                AppListItemViewHolder(
                    composableFactory.inflateComposableView(parent, viewType),
                    composableFactory.getComposableType(viewType)
                )
            }
            viewType == typeGroupHeader -> {
                GroupTitleViewHolder(
                    inflate(parent, R.layout.picker_app_text),
                    GroupTitleStyleData.SOLID
                )
            }
            else -> throw RuntimeException("Not Implemented")
        }
    }
}