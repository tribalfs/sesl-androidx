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

package androidx.picker.features.composable.icon

import android.view.View
import android.widget.ImageView
import androidx.annotation.Keep
import androidx.picker.R
import androidx.picker.features.composable.ComposableViewHolder
import androidx.picker.helper.loadIcon
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.CategoryViewData
import androidx.picker.model.viewdata.ViewData
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.DisposableHandle

@Keep
class ComposableIconViewHolder(frameView: View) : ComposableViewHolder(frameView) {

    private var disposableHandle: DisposableHandle? = null

    private val iconView: ImageView = frameView.findViewById(R.id.icon)
    private val shimmerLayout: ShimmerFrameLayout = frameView.findViewById(R.id.shimmerFrameLayout)
    private val subIconView: ImageView = frameView.findViewById(R.id.sub_icon)

    override fun bindData(viewData: ViewData) {
        when (viewData) {
            is AppInfoViewData -> {
                if (viewData.icon != null) {
                    iconView.setImageDrawable(viewData.icon)
                } else {
                    disposableHandle = iconView.loadIcon(
                        iconFlow = viewData.iconFlow,
                        shimmerLayout = shimmerLayout
                    )
                }
                subIconView.setImageDrawable(viewData.subIcon)
            }
            is CategoryViewData -> {
                iconView.setImageDrawable(viewData.icon)
            }
            else -> {
                // Optionally clear icon if not AppInfoViewData or CategoryViewData
                iconView.setImageDrawable(null)
                subIconView.setImageDrawable(null)
            }
        }
    }

    override fun onViewRecycled(itemView: View) {
        super.onViewRecycled(itemView)
        iconView.setImageDrawable(null)
        subIconView.setImageDrawable(null)
        disposableHandle?.dispose()
        disposableHandle = null
    }
}