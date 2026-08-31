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
import androidx.annotation.FloatRange
import androidx.annotation.RestrictTo
import androidx.core.view.SemBlurCompat

//sesl9
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class BlurCurveEvaluator(
    interpolator: TimeInterpolator? = null
) : SeslEvaluator<SemBlurCompat.CurveParameter>(interpolator) {

    override fun evaluate(
        @FloatRange(from = 0.0, to = 1.0) fraction: Float,
        startValue: SemBlurCompat.CurveParameter,
        endValue: SemBlurCompat.CurveParameter
    ): SemBlurCompat.CurveParameter {
        val f = resolveFraction(fraction)
        return SemBlurCompat.CurveParameter(
            blurRadius = lerp(f, startValue.blurRadius, endValue.blurRadius),
            saturation = lerp(f, startValue.saturation, endValue.saturation),
            curveLevel = lerp(f, startValue.curveLevel, endValue.curveLevel),
            curveMinX = lerp(f, startValue.curveMinX, endValue.curveMinX),
            curveMaxX = lerp(f, startValue.curveMaxX, endValue.curveMaxX),
            curveMinY = lerp(f, startValue.curveMinY, endValue.curveMinY),
            curveMaxY = lerp(f, startValue.curveMaxY, endValue.curveMaxY)
        )
    }
}
