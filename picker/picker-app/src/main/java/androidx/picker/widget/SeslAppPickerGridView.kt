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

package androidx.picker.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.picker.R
import androidx.picker.adapter.AbsAdapter
import androidx.picker.adapter.GridAdapter
import androidx.picker.adapter.HeaderFooterAdapter
import androidx.picker.adapter.layoutmanager.AutoFitGridLayoutManager
import androidx.picker.common.log.LogTag
import androidx.picker.decorator.GridSpacingItemDecoration
import androidx.picker.decorator.RoundedCornerDecoration
import androidx.picker.helper.SeslAppInfoDataHelper
import androidx.picker.model.AppData
import androidx.picker.model.AppData.ListCheckBoxAppDataBuilder
import androidx.picker.model.SpanData
import androidx.recyclerview.widget.GridLayoutManager
import kotlin.jvm.java

/**
 * A specialized [SeslAppPickerView] that displays app items in a grid layout.
 *
 * This class extends [SeslAppPickerView] and provides a grid-based representation for app selection.
 * It uses a [GridAdapter] and an [AutoFitGridLayoutManager] to achieve the grid layout.
 *
 * Key features:
 * - Displays items in a grid.
 * - Supports setting the number of columns (span count) for the grid.
 * - Automatically adjusts padding and item spacing for a visually appealing grid.
 * - Implements custom item decorations for rounded corners and spacing between grid items.
 *
 * @constructor Creates an instance of SeslAppPickerGridView.
 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
 * @param attrs The attributes of the XML tag that is inflating the view.
 * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource that supplies default values for the view. Can be 0 to not look for defaults.
 */
class SeslAppPickerGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SeslAppPickerView(context, attrs, defStyleAttr) {

    init {
        val dimensionPixelOffset = context.resources.getDimensionPixelOffset(R.dimen.picker_app_grid_item_interval_spacing) / 2
        setPadding(0, dimensionPixelOffset, 0, dimensionPixelOffset)
        clipToPadding = false
        viewType = TYPE_GRID
        initialize()

        if (isInEditMode) {
            val packages = SeslAppInfoDataHelper(context, AppData.GridAppDataBuilder::class.java).getPackages()
            submitList(packages)
        }
    }

    /**
     * Retrieves the adapter for the app picker view.
     *
     * This method returns a [GridAdapter] configured for the grid view.
     * It sets `hasStableIds` to true for better performance with item animations.
     *
     * @param viewType The type of the view, although this parameter is not currently used in this implementation.
     *                 It is kept for consistency with the superclass method signature.
     * @return An [AbsAdapter] instance, specifically a [GridAdapter], for the app picker.
     */
    override fun getAppPickerAdapter(@AppPickerType viewType: Int): AbsAdapter {
        val gridAdapter = GridAdapter(context, groupTitleStyleData)
        gridAdapter.setHasStableIds(true)
        return gridAdapter
    }


    /**
     * Returns the [AutoFitGridLayoutManager] to be used for the RecyclerView.
     *
     * This method creates an [AutoFitGridLayoutManager] and sets its `spanSizeLookup`
     * to determine the span size for each item in the grid.
     *
     * @param viewType The view type for which the layout manager is being requested.
     *                 This parameter is currently not used in this implementation.
     * @return An [AutoFitGridLayoutManager] configured for this view.
     */
    override fun getLayoutManager(@AppPickerType viewType: Int): AutoFitGridLayoutManager {
        return AutoFitGridLayoutManager(context).apply {
            spanSizeLookup = createSpanSizeLookup(this)
        }
    }

    /**
     * Sets the number of columns (span count) for the grid layout.
     *
     * This method updates the span count of the underlying [GridLayoutManager].
     * If the current layout manager is an [AutoFitGridLayoutManager], it disables
     * automatic span calculation and forces the span count to the provided `spanCount`.
     *
     * After updating the span count, it reconfigures the [GridLayoutManager.SpanSizeLookup]
     * to ensure that items are correctly sized across the new number of columns. Finally,
     * it notifies the adapter that the data set has changed to refresh the layout.
     *
     * If the provided `spanCount` is the same as the current span count,
     * or if the layout manager is not a [GridLayoutManager], this method does nothing.
     *
     * @param spanCount The desired number of columns in the grid.
     */
    fun setGridSpanCount(spanCount: Int) {
        val gridLayoutManager = layoutManager as? GridLayoutManager ?: return
        if (gridLayoutManager.spanCount == spanCount) return

        if (gridLayoutManager is AutoFitGridLayoutManager) {
            gridLayoutManager.setSpanCount(spanCount, true)
        } else {
            gridLayoutManager.spanCount = spanCount
        }

        gridLayoutManager.spanSizeLookup =createSpanSizeLookup(gridLayoutManager)
        @SuppressLint("NotifyDataSetChanged")
        (headerFooterAdapter.notifyDataSetChanged())
    }

    private fun createSpanSizeLookup(layoutManager: GridLayoutManager): GridLayoutManager.SpanSizeLookup {
        return object: GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val headerFooterAdapter = adapter as? HeaderFooterAdapter ?: return 1
                if (position < 0 || position >= headerFooterAdapter.itemCount) return 1
                val item = headerFooterAdapter.getItem(position)
                return if (item is SpanData) {
                    val spanCount = item.spanCount
                    if (spanCount == -1) layoutManager.spanCount else spanCount
                } else {
                    layoutManager.spanCount
                }
            }
        }
    }

    override fun setItemDecoration(i: Int, headerFooterAdapter: HeaderFooterAdapter) {
        super.setItemDecoration(i, headerFooterAdapter)
        addItemDecoration(
            GridSpacingItemDecoration(
                context.resources.getDimensionPixelOffset(R.dimen.picker_app_grid_main_item_view_title_width),
                context.resources.getDimensionPixelOffset(R.dimen.picker_app_grid_item_interval_spacing)
            )
        )
        addItemDecoration(
            RoundedCornerDecoration(
                context,
                headerFooterAdapter,
                ContextCompat.getColor(context, groupTitleStyleData.backgroundColorId)
            )
        )
    }

}