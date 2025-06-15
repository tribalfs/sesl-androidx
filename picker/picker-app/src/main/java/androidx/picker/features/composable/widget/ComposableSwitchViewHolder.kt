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

import android.view.View
import androidx.annotation.Keep
import androidx.appcompat.widget.SwitchCompat
import androidx.core.util.Supplier
import androidx.picker.R
import androidx.picker.features.composable.ActionableComposableViewHolder
import androidx.picker.helper.setAccessibilityFocusable
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData
import kotlinx.coroutines.DisposableHandle

/**
 *  A concrete implementation of [ActionableComposableViewHolder] for displaying an item view
 *  that includes a switch widget.
 *
 *  This ViewHolder is responsible for binding data to the switch, handling its state changes,
 *  and managing interactions with the underlying data model. It observes changes in the
 *  `selectableItem` and updates the switch's checked state accordingly. It also allows the user
 *  to toggle the switch, which in turn updates the `selectableItem`.
 *
 *  Accessibility is handled by making the switch focusable only when there's a custom
 *  click listener attached to the item view, ensuring that both the item view and the switch
 *  are not focusable simultaneously, which can be confusing for accessibility services.
 *
 *  The ViewHolder also manages resources by disposing of any active `DisposableHandle` and
 *  clearing listeners when the view is recycled.
 */
@Keep
class ComposableSwitchViewHolder(
    frameView: View
) : ActionableComposableViewHolder(frameView) {

    private var disposableHandle: DisposableHandle? = null
    private val divider: View = frameView.findViewById(R.id.switch_divider_widget)
    private var hasCustomClickListener: Boolean? = null
    private val switch: SwitchCompat = frameView.findViewById(R.id.switch_widget)

    private fun setHasCustomClickListener(value: Boolean?) {
        hasCustomClickListener = value
        divider.visibility = if (value == true) View.VISIBLE else View.GONE
    }

    override fun bindData(viewData: ViewData) {
        val appInfoViewData = viewData as? AppInfoViewData ?: return
        val selectableItem = appInfoViewData.selectableItem ?: return

        disposableHandle?.dispose()
        disposableHandle = selectableItem.bind { checked ->
            switch.isChecked = checked
        }

        doAction = Supplier {
            selectableItem.setValue(!switch.isChecked)
            true
        }

        switch.setOnClickListener {
            selectableItem.setValue(switch.isChecked)
        }
    }

    override fun onBind(itemView: View) {
        if (hasCustomClickListener == null) {
            setHasCustomClickListener(itemView.hasOnClickListeners())
        }
        switch.setAccessibilityFocusable(hasCustomClickListener == true)
        super.onBind(itemView)
    }

    override fun onViewRecycled(itemView: View) {
        super.onViewRecycled(itemView)
        switch.setOnClickListener(null)
        disposableHandle?.dispose()
        setHasCustomClickListener(null)
    }
}