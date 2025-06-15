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

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.picker.R
import androidx.picker.helper.getPrimaryDarkColor
import androidx.picker.helper.limitFontLarge
import androidx.picker.helper.loadIcon
import androidx.picker.helper.setHighLightText
import androidx.picker.loader.select.SelectableItem
import androidx.picker.model.Highlightable
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.DisposableHandle
import kotlin.LazyThreadSafetyMode

/**
 * ViewHolder for displaying items in a grid layout.
 *
 * This ViewHolder is responsible for binding data to the views within a grid item,
 * including handling icon loading, selection state, highlighting, and accessibility.
 * It also implements the [Inducible] interface to provide a visual indication (shake animation)
 * when an action is performed on the item.
 *
 * @param view The root view of the grid item.
 */
open class GridViewHolder(view: View) : PickerViewHolder(view), Inducible {

    val shimmerLayout: ShimmerFrameLayout = view.findViewById(R.id.shimmerFrameLayout)
    val gridItem: ConstraintLayout = view.findViewById(R.id.item)
    val icon: ImageView = view.findViewById(R.id.icon)
    val subIcon: ImageView = view.findViewById(R.id.sub_icon)
    val appName: TextView = view.findViewById<TextView>(R.id.title).apply { limitFontLarge() }

    private val highlightColor: Int by lazy(LazyThreadSafetyMode.NONE) {
        view.context.getPrimaryDarkColor()
    }

    private var disposableHandle: DisposableHandle? = null

    override fun bindData(data: ViewData) {
        val disposableHandleList = mutableListOf<DisposableHandle>()
        if (data is AppInfoViewData) {
            icon.tag = data.appInfo
            val iconDrawable: Drawable? = data.icon
            if (iconDrawable != null) {
                icon.setImageDrawable(iconDrawable)
            } else {
                disposableHandleList.add(
                    icon.loadIcon(iconFlow = data.iconFlow, shimmerLayout = shimmerLayout)
                )
            }
            subIcon.setImageDrawable(data.subIcon)
            appName.text = data.label
            val selectableItem: SelectableItem? = data.selectableItem
            if (selectableItem != null) {
                disposableHandle?.dispose()
                disposableHandle = selectableItem.bind { isSelected ->
                    gridItem.setBackgroundResource(
                        if (isSelected) R.drawable.picker_app_grid_selected_background
                        else R.drawable.picker_app_grid_background
                    )
                }
            }
        }
        val accessibilityManager = itemView.context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        if (accessibilityManager?.isEnabled == true) {
            gridItem.contentDescription = appName.text
        }
        if (data is Highlightable) {
            disposableHandleList.add(
                data.getHighlightText().bind { highlight -> appName.setHighLightText(highlight, highlightColor) }
            )
        }
        // Compose all disposables into one
        disposableHandle = object : DisposableHandle {
            private val handles = disposableHandleList.toList()
            override fun dispose() {
                handles.forEach { it.dispose() }
            }
        }
        super.bindData(data)
    }

    override fun onViewRecycled() {
        super.onViewRecycled()
        disposableHandle?.dispose()
        disposableHandle = null
        icon.setImageDrawable(null)
        subIcon.setImageDrawable(null)
    }

    /**
     * Induces a visual effect (shake animation) on the item view.
     *
     * @return A [DisposableHandle] to control the animation. This is used by
     * [SeslAppPickerView][androidx.picker.widget.SeslAppPickerView]
     * to automatically cancel this animation when the view is scrolled.
     */
    override fun induce(): DisposableHandle {
        val anim = AnimationUtils.loadAnimation(itemView.context, R.anim.shake)
        itemView.clearAnimation()
        itemView.startAnimation(anim)
        return object : DisposableHandle {
            override fun dispose() {
                anim.cancel()
                itemView.clearAnimation()
            }
        }
    }
}