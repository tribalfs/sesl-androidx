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

package androidx.appcompat.oneui.common

import android.content.Context
import android.view.View
import androidx.annotation.RestrictTo
import androidx.core.view.SemBlurCompat

//sesl9
/**
 * Interface implemented by One UI views and components that support background blur state management.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
interface BlurSupportable {
    /** Applies blur effect using theme resources from the given [context]. Returns `true` if blur was applied. */
    fun applyBlurInfo(context: Context): Boolean

    /** Applies blur effect using custom [curveParameter]. */
    fun applyBlurInfo(curveParameter: SemBlurCompat.CurveParameter): Boolean = false

    /** Clears blur effect using the given [context]. */
    fun clearBlurInfo(context: Context)

    /** Returns the target view to which blur effect is applied. */
    fun getBlurTargetView(): View? = null

    /** Returns whether blur effect is currently applied. */
    fun isBlurApplied(): Boolean = true

    /** Sets the blur mode (e.g. [SemBlurCompat.BLUR_MODE_CANVAS]). */
    fun setBlurMode(semBlurInfoMode: Int)
}
