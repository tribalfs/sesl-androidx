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

package androidx.appcompat.oneui.common.internal.util

import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.RestrictTo

//sesl9

/**
 * Limits the text size growth caused by the user font scale to [maxFontScale].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun TextView.checkMaxFontScale(baseSizeDp: Int, maxFontScale: MaxFontScaleRatio) {
    val fontScale = resources.configuration.fontScale.coerceAtMost(maxFontScale.ratio)
    setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(baseSizeDp).toFloat() * fontScale)
}
