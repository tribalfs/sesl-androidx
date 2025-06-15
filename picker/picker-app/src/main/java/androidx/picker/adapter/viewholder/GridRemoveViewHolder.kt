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

import android.content.Context.ACCESSIBILITY_SERVICE
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.ImageView
import androidx.picker.R
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData

/**
 * ViewHolder class for displaying a grid item with a remove icon.
 * This class extends [GridViewHolder] and adds functionality for showing a remove icon.
 *
 * @param view The view for the grid item.
 */
class GridRemoveViewHolder(view: View) : GridViewHolder(view) {

    val removeIcon: ImageView = view.findViewById(R.id.remove_icon)

    override fun bindData(data: ViewData) {
        super.bindData(data)
        if (data is AppInfoViewData) {
            removeIcon.visibility = if (data.dimmed) View.GONE else View.VISIBLE
            gridItem.setBackgroundResource(R.drawable.picker_app_grid_background)
        }
        val accessibilityManager = itemView.context.getSystemService(ACCESSIBILITY_SERVICE) as? AccessibilityManager
        if (accessibilityManager?.isEnabled == true) {
            val item = this.itemView
            val format = String.format(
                item.context.resources.getText(R.string.accs_remove).toString(),
                appName.text
            )
            item.contentDescription = format
        }
    }

    override fun setViewEnableState(enable: Boolean) {
        itemView.isEnabled = enable
    }
}