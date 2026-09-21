/*
 * Copyright 2026 The Android Open Source Project
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

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView

/**
 * An item decoration that fills the remaining space at the bottom of a [RecyclerView]
 * with a specified solid background color.
 *
 * @param bottomFillColor The color used to fill the area below the last item.
 */
class BottomFillDecoration(@ColorInt val bottomFillColor: Int) : RecyclerView.ItemDecoration() {
    val paint: Paint = Paint().apply {
        color = bottomFillColor
        style = Paint.Style.FILL
    }

    override fun seslOnDispatchDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        val childCount = recyclerView.childCount
        val height = recyclerView.height
        val width = recyclerView.width.toFloat()
        if (childCount == 0) {
            canvas.drawRect(0f, 0f, width, height.toFloat(), paint)
            return
        }
        val lastChild = recyclerView.getChildAt(childCount - 1)
        val bottom = lastChild.bottom + lastChild.translationY.toInt()
        if (bottom < height) {
            canvas.drawRect(0f, bottom.toFloat(), width, height.toFloat(), paint)
        }
    }
}
