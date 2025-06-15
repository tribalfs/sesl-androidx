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

package androidx.picker.features.composable

import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.Keep
import androidx.annotation.RestrictTo
import androidx.core.util.Supplier
import androidx.picker.model.viewdata.ViewData

/**
 * Abstract extension of [ComposableViewHolder] designed to be actionable, meaning it can trigger an action when clicked.
 * The action to be performed is defined by the [doAction] supplier.
 * Subclasses must implement the [bindData] method to bind data to the Composable content.
 *
 * @param frameView The root view of the ViewHolder.
 */
@Keep
@RestrictTo(RestrictTo.Scope.LIBRARY)
abstract class ActionableComposableViewHolder(
    frameView: View
) : ComposableViewHolder(frameView) {

    /**
     * A [Supplier] that defines the action to be performed by this [ComposableViewHolder].
     */
    var doAction: Supplier<Boolean>? = null

    abstract override fun bindData(viewData: ViewData)

    /**
     * Binds click events on the parent [ViewHolder][androidx.recyclerview.widget.RecyclerView.ViewHolder]'s view
     * to this [ActionableComposableViewHolder]'s [doAction] if the parent view doesn't handle click events itself.
     *
     * @param itemView The view from the parent view holder to which the click listener might be bound.
     */
    @CallSuper
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun onBind(itemView: View) {
        super.onBind(itemView)
        if (doAction == null || itemView.hasOnClickListeners()) {
            return
        }
        itemView.setOnClickListener { doAction?.get() }
    }

    /**
     * Cleans up any resources when the view is recycled.
     * It removes the click listener from the itemView and sets the [doAction] to null.
     *
     * @param itemView The view that was recycled.
     */
    @CallSuper
    override fun onViewRecycled(itemView: View) {
        super.onViewRecycled(itemView)
        if (doAction != null) {
            itemView.setOnClickListener(null)
        }
        doAction = null
    }
}