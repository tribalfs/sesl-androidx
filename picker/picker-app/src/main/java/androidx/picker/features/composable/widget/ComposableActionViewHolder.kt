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
import android.widget.ImageButton
import androidx.annotation.Keep
import androidx.core.util.Supplier
import androidx.core.view.isVisible
import androidx.picker.R
import androidx.picker.features.composable.ActionableComposableViewHolder
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData

/**
 * A concrete implementation of the [ActionableComposableViewHolder]
 * for displaying item view with an action button inside it.
 *
 * This ViewHolder is responsible for binding data to the view, handling click events on the
 * action button, and managing the visibility of a divider. The divider's visibility is
 * determined by whether a custom click listener is set on the item view itself.
 *
 * @param frameView The view to be used by this [ComposableActionViewHolder]
 */
@Keep
class ComposableActionViewHolder(
    frameView: View
) : ActionableComposableViewHolder(frameView) {

    private val divider: View = frameView.findViewById(R.id.switch_divider_widget)
    private var hasCustomClickListener: Boolean? = null
    private val imageButton: ImageButton = frameView.findViewById(R.id.image_button)

    private fun setHasCustomClickListener(value: Boolean) {
        hasCustomClickListener = value
        divider.isVisible = value
    }

    override fun bindData(viewData: ViewData) {
        val appInfoViewData = viewData as AppInfoViewData
        imageButton.setImageDrawable(appInfoViewData.actionIcon)
        appInfoViewData.onActionClick?.let {
            imageButton.setOnClickListener { v -> it.invoke(appInfoViewData) }
        }
        doAction = Supplier {
            appInfoViewData.onActionClick?.invoke(appInfoViewData)
            true
        }
    }


    override fun onBind(itemView: View) {
        if (hasCustomClickListener == null) {
            setHasCustomClickListener(itemView.hasOnClickListeners())
        }
        super.onBind(itemView)
    }

    override fun onViewRecycled(itemView: View) {
        super.onViewRecycled(itemView)
        imageButton.setOnClickListener(null)
    }
}