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
package androidx.recyclerview.sesl.drawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.annotation.RestrictTo
import androidx.core.graphics.ColorUtils

//Added in sesl7
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
class SeslFastScrollerBgDrawable : Drawable(), SeslAutowiredDrawable<Float> {

    override var value: Float = 0f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
        alpha = 8
        color = ColorUtils.setAlphaComponent(Color.BLACK, 255)
    }

    override fun draw(canvas: Canvas) {
        paint.strokeWidth = value
        val halfWidth = canvas.width / 2.0f
        val strokeHalf = paint.strokeWidth / 2.0f
        canvas.drawLine(halfWidth, strokeHalf, halfWidth, canvas.height - strokeHalf, paint)
    }

    @Deprecated("Deprecated from Drawable#getOpacity")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    fun setArgb(argb: Int) { paint.color = argb }

}