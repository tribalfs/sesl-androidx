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

package androidx.picker.features.composable.widget

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import androidx.annotation.Keep
import androidx.picker.R
import androidx.picker.adapter.AbsAdapter
import androidx.picker.features.composable.ActionableComposableViewHolder
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.CategoryViewData
import androidx.picker.model.viewdata.ViewData

/**
 * A concrete implementation of the [ActionableComposableViewHolder]
 * for displaying and managing an expandable item, such as a category that can be collapsed or
 * expanded to show/hide its children.
 *
 * This ViewHolder is responsible for handling the expand/collapse interaction and updating the
 * adapter accordingly. It expects the provided `frameView` to contain an `ImageView` with the ID
 * `R.id.image_button` which acts as the toggle button, and a `View` with the ID
 * `R.id.switch_divider_widget` which is used as a visual divider.
 *
 * The expansion state is managed by modifying the underlying data set of the adapter. When an item
 * is collapsed, its children (expected to be of type [AppInfoViewData]) are moved to an internal
 * `invisibleChildren` list within the [CategoryViewData]. When expanded, these children are
 * re-inserted into the adapter's data set.
 *
 * @param frameView The root view of the item. This view must contain an `ImageView` (with the id
 *   `R.id.image_button`) and a divider `View` (with the id `R.id.switch_divider_widget`).
 */
@Keep
class ComposableExpanderViewHolder(
    frameView: View
) : ActionableComposableViewHolder(frameView) {

    private var refferalItem: CategoryViewData? = null
    private val toggle: ImageView = frameView.findViewById(R.id.image_button)

    init {
        requireNotNull(frameView.findViewById<View>(R.id.switch_divider_widget))
    }

    override fun bindAdapter(adapter: AbsAdapter) {
        toggle.setOnClickListener {
            val categoryViewData = refferalItem ?: throw UninitializedPropertyAccessException("refferalItem")
            toggle.isSelected = categoryViewData.invisibleChildren.isEmpty()
            checkCollapsed(adapter, toggle.isSelected)
        }
    }

    override fun bindData(viewData: ViewData) {
        if (viewData is CategoryViewData) {
            refferalItem = viewData
            toggle.isSelected = viewData.invisibleChildren.isEmpty()
        }
    }

    private fun checkCollapsed(adapter: AbsAdapter, collapsed: Boolean) {
        val viewDataList = adapter.getDataSetFiltered()
        @Suppress("UNCHECKED_CAST")
        val mutableViewDataList = viewDataList as ArrayList<ViewData>
        val categoryViewData = refferalItem ?: throw UninitializedPropertyAccessException("refferalItem")
        val categoryIndex = mutableViewDataList.indexOf(categoryViewData)
        if (collapsed) {
            var removedItemsCount = 0
            while (true) {
                val nextItemIndex = categoryIndex + 1
                if (mutableViewDataList.size <= nextItemIndex) break
                val nextItem = mutableViewDataList[nextItemIndex]
                if (!checkCollapsedIsCanBeCollapsed(nextItem)) break
                val invisibleChildren = categoryViewData.invisibleChildren
                invisibleChildren.add(mutableViewDataList.removeAt(nextItemIndex))
                removedItemsCount++
            }
            adapter.notifyItemRangeRemoved(categoryIndex + 1, removedItemsCount)
        } else {
            val insertionStartIndex = categoryIndex + 1
            val invisibleChildren = categoryViewData.invisibleChildren
            var currentInsertionIndex = insertionStartIndex
            for (child in invisibleChildren) {
                mutableViewDataList.add(currentInsertionIndex, child)
                currentInsertionIndex++
            }
            adapter.notifyItemRangeInserted(insertionStartIndex, (currentInsertionIndex - categoryIndex) - 1)
            invisibleChildren.clear()
        }
        adapter.notifyItemChanged(categoryIndex)
    }

    private fun checkCollapsedIsCanBeCollapsed(viewData: ViewData): Boolean {
        return viewData is AppInfoViewData
    }

    override fun onViewRecycled(itemView: View) {
        super.onViewRecycled(itemView)
        toggle.isSelected = false
        @SuppressLint("ClickableViewAccessibility")
        toggle.setOnTouchListener(null)
        toggle.setOnClickListener(null)
    }
}