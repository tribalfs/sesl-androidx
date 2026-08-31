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

package androidx.appcompat.oneui.common.internal.util.evaluator

import android.animation.TimeInterpolator
import android.animation.TypeEvaluator
import androidx.annotation.FloatRange
import androidx.annotation.RestrictTo

//sesl9
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class SeslEvaluator<T>(
    val interpolator: TimeInterpolator? = null
) : TypeEvaluator<T> {

    //sesl9: public final in AAR
    fun resolveFraction(@FloatRange(from = 0.0, to = 1.0) fraction: Float): Float {
        val clamped = fraction.coerceIn(0.0f, 1.0f)
        val interpolated = interpolator?.getInterpolation(clamped) ?: clamped
        return interpolated.coerceIn(0.0f, 1.0f)
    }

    fun lerp(@FloatRange(from = 0.0, to = 1.0) fraction: Float, start: Float, end: Float): Float {
        return start + fraction * (end - start)
    }

    fun lerp(@FloatRange(from = 0.0, to = 1.0) fraction: Float, start: Int, end: Int): Int {
        return (start + fraction * (end - start)).toInt()
    }
}
