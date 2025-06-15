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

package androidx.picker.helper

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.view.View
import androidx.appcompat.util.SeslRoundedCorner
import androidx.appcompat.util.SeslRoundedCorner.ROUNDED_CORNER_ALL
import androidx.appcompat.util.SeslRoundedCorner.ROUNDED_CORNER_NONE
import androidx.core.graphics.Insets
import kotlin.collections.listOfNotNull


@JvmOverloads
fun View.seslSetRoundedCorner(
    corner: Int = ROUNDED_CORNER_ALL,
    insets: Insets? = null
) {
    if (corner == ROUNDED_CORNER_NONE && insets == null) return
    val roundedCorner = SeslRoundedCorner(context)
    roundedCorner.roundedCorners = corner
    seslSetRoundedCorner(roundedCorner, insets)
}

@JvmOverloads
fun View.seslSetRoundedCorner(
    roundedCorner: SeslRoundedCorner,
    insets: Insets? = null
) {
    post {
        if (measuredWidth <= 0 || measuredHeight <= 0) return@post
        val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0)
        if (insets != null) {
            roundedCorner.drawRoundedCorner(canvas, insets)
        } else {
            roundedCorner.drawRoundedCorner(canvas)
        }
        val resources: Resources = context.resources
        val seslRoundedLayerDrawable = SeslRoundedLayerDrawable(resources, bitmap)
        if (Build.VERSION.SDK_INT >= 23) {
            val fg = foreground
            if (fg is LayerDrawable) {
                val idx = fg.numberOfLayers - 1
                if (fg.getDrawable(idx) is SeslRoundedLayerDrawable) {
                    fg.setDrawable(idx, seslRoundedLayerDrawable)
                    return@post
                }
            }
            foreground = LayerDrawable(listOfNotNull(fg, seslRoundedLayerDrawable).toTypedArray())
        }
    }
}