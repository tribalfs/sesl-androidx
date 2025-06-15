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

import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView

/**
 * ListUpdateCallback that dispatches update events to the given adapter.
 *
 * This class is a wrapper around [RecyclerView.Adapter] that implements
 * [ListUpdateCallback]. It is used to dispatch update events to the adapter when the
 * underlying data changes.
 *
 * It also handles updating the nearby items when an item is inserted or removed at the end of the
 * list. This is to ensure that the dividers are drawn correctly.
 *
 * @param adapter The adapter to dispatch update events to.
 * @see ListUpdateCallback
 * @see RecyclerView.Adapter
 */
class NearbyListUpdateCallback(
    private val adapter: RecyclerView.Adapter<*>
) : ListUpdateCallback {

    override fun onChanged(position: Int, count: Int,  payload: Any?) {
        adapter.notifyItemRangeChanged(position, count, payload)
    }

    override fun onInserted(position: Int, count: Int) {
        adapter.notifyItemRangeInserted(position, count)
        val itemCount = adapter.itemCount
        if (itemCount - 1 == position && itemCount - 2 >= 0) {
            adapter.notifyItemChanged(itemCount - 2, 1)
        }
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        adapter.notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRemoved(position: Int, count: Int) {
        adapter.notifyItemRangeRemoved(position, count)
        val itemCount = adapter.itemCount
        if (itemCount - 1 == position && itemCount - 2 >= 0) {
            adapter.notifyItemChanged(itemCount - 2, 1)
        }
    }
}