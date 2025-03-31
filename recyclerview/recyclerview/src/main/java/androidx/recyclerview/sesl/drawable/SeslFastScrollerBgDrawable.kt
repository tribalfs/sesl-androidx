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
@RestrictTo(RestrictTo.Scope.LIBRARY)
class SeslFastScrollerBgDrawable : Drawable(), SeslAutowiredDrawable<Float> {

    override var value: Float = 0f

    private val paint = Paint(1).apply {
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
        alpha = 8
        color = ColorUtils.setAlphaComponent(Color.BLACK, 255)
    }

    override fun draw(canvas: Canvas) {
        paint.strokeWidth = value
        canvas.drawLine(
            canvas.width / 2f,
            paint.strokeWidth / 2f,
            canvas.width / 2f,
            canvas.height - (paint.strokeWidth / 2f),
            paint
        )
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("PixelFormat.TRANSPARENT", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int = PixelFormat.TRANSPARENT

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.setColorFilter(colorFilter)
    }

    fun setArgb(argb: Int) { paint.color = argb }

}