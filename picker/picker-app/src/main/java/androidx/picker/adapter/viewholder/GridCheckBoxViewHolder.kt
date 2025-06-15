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

package androidx.picker.adapter.viewholder

import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.CheckBox
import androidx.core.view.isVisible
import androidx.picker.R
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData
import kotlinx.coroutines.DisposableHandle

/**
 * ViewHolder for a grid item that includes a checkbox for selection.
 *
 * This ViewHolder extends [GridViewHolder] and adds functionality for handling checkbox
 * selection states and accessibility. It binds to [AppInfoViewData] to display application
 * information and manage the selection state of the associated item.
 *
 * @param view The view for this ViewHolder, typically inflated from a layout resource.
 */
class GridCheckBoxViewHolder(view: View) : GridViewHolder(view) {

    private val checkBox = view.findViewById<CheckBox>(R.id.check_widget).apply { isVisible = true }

    private var disposableHandle: DisposableHandle? = null

    override fun bindData(data: ViewData) {
        super.bindData(data)
        if (data is AppInfoViewData) {
            val selectableItem = data.selectableItem
            if (selectableItem != null) {
                disposableHandle?.dispose()
                disposableHandle = selectableItem.bind { isChecked ->
                    checkBox.isChecked = isChecked
                }
                checkBox.setOnClickListener {
                    selectableItem.setValue(checkBox.isChecked)
                }
            }
            gridItem.setBackgroundResource(R.drawable.picker_app_grid_background)
        }
        val accessibilityManager = itemView.context.getSystemService("accessibility") as? AccessibilityManager
        if (accessibilityManager != null && accessibilityManager.isEnabled) {
            checkBox.isFocusable = false
            checkBox.isClickable = false
            itemView.contentDescription = appName.text
        }
    }

    override fun onViewRecycled() {
        super.onViewRecycled()
        checkBox.setOnClickListener(null)
        disposableHandle?.dispose()
        disposableHandle = null
    }
}