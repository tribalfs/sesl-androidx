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

package androidx.appcompat.oneui.common.internal.policy

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.appcompat.oneui.common.internal.resource.ThemeResourceColor
import androidx.appcompat.oneui.common.internal.semblurinfo.ColorCurvePreset
import androidx.appcompat.oneui.common.internal.semblurinfo.FIGMA_BLUR_COMPONENT_DARK_MD
import androidx.appcompat.oneui.common.internal.semblurinfo.FIGMA_BLUR_COMPONENT_DARK_ZERO
import androidx.appcompat.oneui.common.internal.semblurinfo.FIGMA_BLUR_COMPONENT_LIGHT_MD
import androidx.appcompat.oneui.common.internal.semblurinfo.FIGMA_BLUR_COMPONENT_LIGHT_ZERO
import androidx.appcompat.oneui.common.internal.semblurinfo.SemBlurInfoStateBuilder
import androidx.core.view.SemBlurCompat
import androidx.core.view.SemBlurCompat.SeslBlurMode

//sesl9
/**
 * Factory object for generating blur info state builders.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
object BlurInfoState {
    /**
     * Generates a builder for drawer component blur state.
     *
     * @param context App context.
     * @param blurMode Blur mode integer.
     * @return Configured [SemBlurInfoStateBuilder].
     */
    fun generateDrawerComponentBlurInfoStateBuilder(context: Context, @SeslBlurMode blurMode: Int): SemBlurInfoStateBuilder {
        return SemBlurInfoStateBuilder(
            blurMode = blurMode,
            colorCurvePreset = ColorCurvePreset(FIGMA_BLUR_COMPONENT_LIGHT_ZERO, FIGMA_BLUR_COMPONENT_DARK_ZERO),
            blurBackgroundColor = ThemeResourceColor(0, 0),
            useTypeCanvasBlur = SemBlurCompat.CANVAS_BLUR_USE_TYPE_DYNAMIC
        )
    }

    /**
     * Generates a builder for floating component blur state.
     *
     * @param context App context.
     * @param blurMode Blur mode integer.
     * @return Configured [SemBlurInfoStateBuilder].
     */
    fun generateFloatingComponentBlurInfoStateBuilder(context: Context, @SeslBlurMode blurMode: Int): SemBlurInfoStateBuilder {
        return SemBlurInfoStateBuilder(
            blurMode = blurMode,
            colorCurvePreset = ColorCurvePreset(FIGMA_BLUR_COMPONENT_LIGHT_MD, FIGMA_BLUR_COMPONENT_DARK_MD),
            blurBackgroundColor = ThemeResourceColor(0, 0),
            useTypeCanvasBlur = SemBlurCompat.CANVAS_BLUR_USE_TYPE_DYNAMIC
        )
    }
}
