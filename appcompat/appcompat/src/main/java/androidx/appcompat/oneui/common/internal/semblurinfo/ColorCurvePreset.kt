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

import androidx.annotation.RestrictTo
import androidx.appcompat.oneui.common.internal.resource.ThemeResourceImpl
import androidx.core.view.SemBlurCompat

//sesl9
/**
 * Color curve presets for light and dark themes used in One UI blur effects.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class ColorCurvePreset @JvmOverloads constructor(
    val colorCurvePresetLight: SemBlurCompat.CurveParameter,
    val colorCurvePresetDark: SemBlurCompat.CurveParameter = colorCurvePresetLight
) : ThemeResourceImpl<SemBlurCompat.CurveParameter>(colorCurvePresetLight, colorCurvePresetDark)
