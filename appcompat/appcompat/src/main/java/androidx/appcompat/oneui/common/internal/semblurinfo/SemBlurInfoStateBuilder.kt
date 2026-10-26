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

package androidx.appcompat.oneui.common.internal.semblurinfo

import android.graphics.drawable.Drawable
import androidx.annotation.RestrictTo
import androidx.appcompat.oneui.common.internal.resource.ThemeResourceColor
import androidx.core.oneui.common.internal.semblurinfo.SemBlurInfoState
import androidx.core.view.SemBlurCompat.BLUR_MODE_CANVAS
import androidx.core.view.SemBlurCompat.BLUR_MODE_WINDOW
import androidx.core.view.SemBlurCompat.CANVAS_BLUR_USE_TYPE_STATIC
import androidx.core.view.SemBlurCompat.SeslBlurMode
import androidx.core.view.SemBlurCompat.SeslUseTypeCanvasBlur

//sesl9
/**
 * Builder for constructing [SemBlurInfoState] instances.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
class SemBlurInfoStateBuilder(
    @param:SeslBlurMode private val blurMode: Int = BLUR_MODE_CANVAS,
    private var colorCurvePreset: ColorCurvePreset,
    private val blurBackgroundColor: ThemeResourceColor? = null,
    @param:SeslUseTypeCanvasBlur private val useTypeCanvasBlur: Int? = CANVAS_BLUR_USE_TYPE_STATIC
) {
    private var cornerRadius: Float? = null
    private var nonBlurBackground: Drawable? = null

    /**
     * Sets corner radius for window blur.
     *
     * @param radius Corner radius value.
     * @return This builder for chaining.
     */
    fun cornerRadius(radius: Float): SemBlurInfoStateBuilder {
        this.cornerRadius = radius
        return this
    }

    /**
     * Sets non-blur fallback background.
     *
     * @param background Fallback drawable.
     * @return This builder for chaining.
     */
    fun nonBlurBackground(background: Drawable): SemBlurInfoStateBuilder {
        this.nonBlurBackground = background
        return this
    }

    /**
     * Sets the color curve preset.
     *
     * @param colorCurvePreset Preset parameter.
     * @return This builder for chaining.
     */
    fun colorCurvePreset(colorCurvePreset: ColorCurvePreset): SemBlurInfoStateBuilder {
        this.colorCurvePreset = colorCurvePreset
        return this
    }

    /**
     * Builds and returns the configured [SemBlurInfoState].
     *
     * @return A new [SemBlurInfoState] instance.
     */
    fun build(): SemBlurInfoState {
        return when (blurMode) {
            BLUR_MODE_WINDOW -> {
                val bg = blurBackgroundColor ?: checkNotNull(blurBackgroundColor)
                SemBlurInfoStateWindow(blurMode, cornerRadius, colorCurvePreset, bg, nonBlurBackground)
            }
            BLUR_MODE_CANVAS -> {
                val bg = blurBackgroundColor ?: checkNotNull(blurBackgroundColor)
                SemBlurInfoStateCanvas(blurMode, colorCurvePreset, bg, nonBlurBackground, useTypeCanvasBlur)
            }
            else -> error("blurMode($blurMode) is not supported. Supported modes: BLUR_MODE_CANVAS, BLUR_MODE_WINDOW")
        }
    }
}
