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

package androidx.picker.controller

/**
 * Abstract controller for managing a list of data and notifying listeners on changes.
 */
abstract class DataController<T> {

    /**
     * The mutable backing list of [T].
     */
    val dataList = mutableListOf<T>()

    /**
     * Unmodifiable copy of the current list of [T].
     */
    val currentList: List<T> get() = dataList

    /**
     * Listeners to be notified on data changes.
     */
    var listeners = mutableListOf<OnDataEventListener<T>>()

    /**
     * Listener interface for data change events.
     */
    fun interface OnDataEventListener<T> {
        fun onListChange(list: List<T>)
    }

    /**
     * Add a listener to be notified of the changes to [T] list.
     * @see [removeOnDataEventListener]
     */
    fun addOnDataEventListener(listener: OnDataEventListener<T>) {
        listeners.add(listener)
    }

    /** Remove a listener previously register with [addOnDataEventListener] */
    fun removeOnDataEventListener(listener: OnDataEventListener<T>) {
        listeners.remove(listener)
    }

    /**
     * Notify all listeners that the data has changed.
     */
    open fun notifyChanged() {
        for (listener in listeners) {
            listener.onListChange(dataList)
        }
    }

    /**
     * Reset the data list to the given elements and notify listeners.
     */
    fun reset(elements: List<T>) {
        dataList.clear()
        dataList.addAll(elements)
        notifyChanged()
    }
}