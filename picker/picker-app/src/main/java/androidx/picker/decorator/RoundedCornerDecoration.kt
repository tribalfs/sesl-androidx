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

package androidx.picker.decorator

import android.content.Context
import android.graphics.Canvas
import androidx.annotation.ColorInt
import androidx.appcompat.util.SeslRoundedCorner
import androidx.appcompat.util.SeslRoundedCorner.ROUNDED_CORNER_ALL
import androidx.appcompat.util.SeslRoundedCorner.ROUNDED_CORNER_BOTTOM_LEFT
import androidx.appcompat.util.SeslRoundedCorner.ROUNDED_CORNER_BOTTOM_RIGHT
import androidx.appcompat.util.SeslRoundedCorner.ROUNDED_CORNER_NONE
import androidx.appcompat.util.SeslRoundedCorner.ROUNDED_CORNER_TOP_LEFT
import androidx.appcompat.util.SeslRoundedCorner.ROUNDED_CORNER_TOP_RIGHT
import androidx.appcompat.util.SeslSubheaderRoundedCorner
import androidx.core.view.children
import androidx.picker.adapter.HeaderFooterAdapter
import androidx.picker.adapter.viewholder.GroupTitleViewHolder
import androidx.recyclerview.widget.RecyclerView

/**
 * An [RecyclerView.ItemDecoration] that draws rounded corners for items in a [RecyclerView].
 *
 * This decoration handles different types of items, including headers, footers, and regular items,
 * applying appropriate rounded corners to each.
 *
 * @param context The [Context] used to create the rounded corner drawables.
 * @param adapter The [RecyclerView.Adapter] associated with the [RecyclerView]. This is used to
 *                determine the type of each item and apply the correct rounded corners.
 * @param roundedCornerColor The color to be used for the rounded corners.
 */
class RoundedCornerDecoration(
    context: Context,
    val adapter: RecyclerView.Adapter<*>,
    @ColorInt val roundedCornerColor: Int
) : RecyclerView.ItemDecoration() {

    private val itemRoundedCorner = SeslRoundedCorner(context).apply {
        setRoundedCornerColor(ROUNDED_CORNER_ALL, roundedCornerColor)
    }
    private val subHeaderRoundedCorner = SeslSubheaderRoundedCorner(context).apply {
        setRoundedCornerColor(ROUNDED_CORNER_ALL, roundedCornerColor)
    }


    override fun seslOnDispatchDraw(
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.seslOnDispatchDraw(c, parent, state)
        for (view in parent.children) {
            val childViewHolder = parent.getChildViewHolder(view)
            if (childViewHolder is GroupTitleViewHolder) {
                subHeaderRoundedCorner.roundedCorners = 15
                subHeaderRoundedCorner.drawRoundedCorner(view, c)
            } else {
                val headerFooterAdapter = adapter as? HeaderFooterAdapter
                if (headerFooterAdapter != null) {
                    val headersCount = headerFooterAdapter.headersCount
                    val footersCount = headerFooterAdapter.footersCount
                    val headerIndex = headersCount - 1
                    val itemCount = headerFooterAdapter.itemCount - footersCount
                    when {
                        headersCount > 0 && childViewHolder == parent.findViewHolderForAdapterPosition(headerIndex) -> {
                            subHeaderRoundedCorner.roundedCorners = ROUNDED_CORNER_TOP_LEFT or ROUNDED_CORNER_TOP_RIGHT
                            subHeaderRoundedCorner.drawRoundedCorner(view, c)
                        }
                        footersCount > 0 && childViewHolder == parent.findViewHolderForAdapterPosition(itemCount) -> {
                            subHeaderRoundedCorner.roundedCorners = ROUNDED_CORNER_BOTTOM_LEFT or ROUNDED_CORNER_BOTTOM_RIGHT
                            subHeaderRoundedCorner.drawRoundedCorner(view, c)
                        }
                        else -> {
                            itemRoundedCorner.roundedCorners = ROUNDED_CORNER_NONE
                            itemRoundedCorner.drawRoundedCorner(view, c)
                        }
                    }
                }
            }
        }
    }
}