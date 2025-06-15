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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.picker.R
import androidx.picker.model.GroupTitleStyleData
import androidx.picker.model.viewdata.GroupTitleViewData
import androidx.picker.model.viewdata.ViewData

/**
 * [PickerViewHolder] for group titles in a picker.
 *
 * This ViewHolder is responsible for displaying a title and an optional label, which can be used to
 * group items within the picker. The appearance of the group title (background and text color) can
 * be customized through [GroupTitleStyleData].
 *
 * @param view The View representing the group title item.
 * @param subHeaderStyle The style data to customize the appearance of the group title.
 */
class GroupTitleViewHolder(
    view: View,
    private val subHeaderStyle: GroupTitleStyleData
) : PickerViewHolder(view) {

    /**TextView for displaying the group title.*/
    val title: TextView = view.findViewById(R.id.title)
    val label: TextView = view.findViewById(R.id.label)
    val groupTitle: LinearLayout = view.findViewById(R.id.group_title_view)

    private val backgroundColor: Int by lazy(LazyThreadSafetyMode.NONE) {
        ContextCompat.getColor(itemView.context, subHeaderStyle.backgroundColorId)
    }

    private val textColor: Int by lazy(LazyThreadSafetyMode.NONE) {
        ContextCompat.getColor(itemView.context, subHeaderStyle.textColorId)
    }

    override fun bindData(data: ViewData) {
        if (data is GroupTitleViewData) {
            title.text = data.title
            label.visibility = if (data.label.isBlank()) View.GONE else View.VISIBLE
            label.text = data.label
            groupTitle.setBackgroundColor(backgroundColor)
            title.setTextColor(textColor)
            label.setTextColor(textColor)
        }
        super.bindData(data)
    }
}