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
import android.view.ViewStub
import androidx.picker.R
import androidx.picker.adapter.AbsAdapter
import androidx.picker.features.composable.ActionableComposableViewHolder
import androidx.picker.features.composable.ComposableType
import androidx.picker.features.composable.ComposableViewHolder
import androidx.picker.model.viewdata.ViewData
import kotlinx.coroutines.DisposableHandle

/**
 * A [PickerViewHolder] responsible for displaying an item in a list of applications in linear layout.
 * This ViewHolder dynamically inflates and manages different composable parts (icon, title, etc.)
 * based on the provided [ComposableType].
 *
 * It also implements the [Inducible] interface to provide visual feedback on interaction.
 *
 * @param view The root view of the item.
 * @param composableType Defines the structure and behavior of the composable elements within this item.
 */
class AppListItemViewHolder(
    view: View,
    val composableType: ComposableType
) : PickerViewHolder(view), Inducible {

    private val iconFrame  = view.findViewById<ViewStub>(R.id.icon_frame)
    private val leftFrame = view.findViewById<ViewStub>(R.id.left_frame)
    private val titleFrame = view.findViewById<ViewStub>(R.id.title_frame)
    private val widgetFrame = view.findViewById<ViewStub>(R.id.widget_frame)

    private val composableItemViewHolderList: List<ComposableViewHolder>

    init {
        val frames = listOf(
            composableType.leftFrame to leftFrame,
            composableType.widgetFrame to widgetFrame,
            composableType.titleFrame to titleFrame,
            composableType.iconFrame to iconFrame
        )
        val holders = mutableListOf<ComposableViewHolder>()
        for ((composableFrame, stub) in frames) {
            if (composableFrame != null) {
                stub.layoutResource = composableFrame.layoutResId
                val viewHolderCtor = composableFrame.viewHolderClass.getDeclaredConstructor(View::class.java)
                val holder = viewHolderCtor.newInstance(stub.inflate())
                holders.add(holder)
            }
        }
        composableItemViewHolderList = holders
    }

    override fun bindAdapter(adapter: AbsAdapter) {
        composableItemViewHolderList.forEach { it.bindAdapter(adapter) }
    }

    /**
     * Binds data to this ViewHolder and its composable components.
     *
     * This method first calls the superclass's [bindData][PickerViewHolder.bindData] method.
     * Then, it iterates through its member [ComposableViewHolder]s, calling their
     * respective `bindData` and `onBind` methods.
     *
     * @param data The [ViewData] to bind to this ViewHolder and its components.
     */
    override fun bindData(data: ViewData) {
        super.bindData(data)
        composableItemViewHolderList.forEach { holder ->
            holder.bindData(data)
            holder.onBind(itemView)
        }
    }

    /**
     * Attempts to perform an action on the first [ActionableComposableViewHolder] with action.
     *
     * Iterates through the member [ComposableViewHolder]s to get the first instance of
     * [ActionableComposableViewHolder] with `doAction`. If found, it invokes it and returns true.
     * Otherwise, returns false.
     *
     * @return `true` if an action was successfully performed by one of the composable view holders,
     * `false` otherwise.
     */
    fun doAction(): Boolean {
        return composableItemViewHolderList.firstOrNull {
            (it as? ActionableComposableViewHolder)?.doAction?.get() == true
        } != null
    }

    /**
     * Provides visual feedback by briefly setting the item view to a pressed state.
     *
     * This simulates a touch interaction, highlighting the item for a short duration.
     *
     * @return A no op [DisposableHandle] as the visual feedback
     *         is managed internally by a delayed task.
     */
    override fun induce(): DisposableHandle {
        itemView.isPressed = true
        itemView.postDelayed({ itemView.isPressed = false }, 100L)
        return noOpDisposable()
    }

    override fun onViewRecycled() {
        super.onViewRecycled()
        composableItemViewHolderList.forEach { it.onViewRecycled(itemView) }
    }

    private fun noOpDisposable() =
        object : DisposableHandle { override fun dispose() { /* no-op */ } }

}