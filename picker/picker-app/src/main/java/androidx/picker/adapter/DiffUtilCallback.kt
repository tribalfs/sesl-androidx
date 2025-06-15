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

import androidx.picker.model.viewdata.ViewData
import androidx.recyclerview.widget.DiffUtil

/**
 * A [DiffUtil.Callback] for calculating the difference between two lists of [ViewData].
 *
 * This class is used to efficiently update a RecyclerView adapter when the underlying data changes.
 *
 * @param oldList The old list of [ViewData].
 * @param newList The new list of [ViewData].
 */
class DiffUtilCallback(
    private val oldList: List<ViewData>,
    private val newList: List<ViewData>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList.getOrNull(oldItemPosition)
        val newItem = newList.getOrNull(newItemPosition)
        if (oldItem == null || newItem == null) return false
        return oldItem.key == newItem.key
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList.getOrNull(oldItemPosition)
        val newItem = newList.getOrNull(newItemPosition)
        return oldItem != null && newItem != null && oldItem === newItem
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        if (oldItemPosition < 0 || oldItemPosition >= oldList.size ||
            newItemPosition < 0 || newItemPosition >= newList.size
        ) {
            return null
        }
        return if (oldList[oldItemPosition] == newList[newItemPosition]) {
            true
        } else {
            null
        }
    }
}