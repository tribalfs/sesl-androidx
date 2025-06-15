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
import androidx.appcompat.util.SeslRoundedCorner
import androidx.appcompat.util.SeslRoundedCorner.ROUNDED_CORNER_ALL
import androidx.core.graphics.Insets
import androidx.recyclerview.widget.RecyclerView
import kotlin.jvm.JvmOverloads

/**
 * An [RecyclerView.ItemDecoration] that draws rounded corners for a [RecyclerView].
 *
 * This decoration utilizes [SeslRoundedCorner] to achieve the rounded corner effect.
 * It considers the padding of the RecyclerView when drawing the corners.
 *
 * @param context The [Context] used to create the [SeslRoundedCorner].
 * @param seslRoundedCorners An integer representing the corners to be rounded.
 *                           Defaults to [SeslRoundedCorner.ROUNDED_CORNER_ALL].
 *                           See [SeslRoundedCorner] for available options like
 *                           [SeslRoundedCorner.ROUNDED_CORNER_TOP_LEFT],
 *                           [SeslRoundedCorner.ROUNDED_CORNER_TOP_RIGHT], etc.
 */
class RecyclerViewCornerDecoration @JvmOverloads constructor(
    context: Context,
    seslRoundedCorners: Int = ROUNDED_CORNER_ALL
) : RecyclerView.ItemDecoration() {

    private val roundedCorner = SeslRoundedCorner(context).apply { roundedCorners = seslRoundedCorners }

    override fun seslOnDispatchDraw(
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.seslOnDispatchDraw(c, parent, state)
        val paddingLeft = parent.paddingLeft
        val paddingRight = parent.paddingRight
        if (paddingLeft > 0 || paddingRight > 0) {
            roundedCorner.drawRoundedCorner(c, Insets.of(paddingLeft, 0, paddingRight, 0))
        } else {
            roundedCorner.drawRoundedCorner(c)
        }
    }
}