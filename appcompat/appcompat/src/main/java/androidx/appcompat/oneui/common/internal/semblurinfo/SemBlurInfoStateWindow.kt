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
import android.view.View
import androidx.annotation.RestrictTo
import androidx.appcompat.oneui.common.internal.resource.ThemeResourceColor
import androidx.core.oneui.common.internal.semblurinfo.SemBlurInfoState
import androidx.core.view.SemBlurCompat
import androidx.core.view.SemBlurCompat.SeslBlurMode

//sesl9
/**
 * Window blur state implementation.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
data class SemBlurInfoStateWindow @JvmOverloads constructor(
    @param:SeslBlurMode override val blurMode: Int,
    val cornerRadius: Float?,
    val colorCurvePreset: ColorCurvePreset,
    val blurBackgroundColor: ThemeResourceColor?,
    val nonBlurBackground: Drawable? = null
) : SemBlurInfoState(blurMode) {

    override fun applyBlurInfo(view: View): Boolean {
        val context = view.context
        if (!SemBlurCompat.setBlurEffectPreset(view, blurMode, colorCurvePreset.getResource(context), blurBackgroundColor?.getResource(context), cornerRadius)) {
            return false
        }
        view.clipToOutline = true
        return true
    }

    override fun clearBlurInfo(view: View) {
        SemBlurCompat.setBlurInfoClear(view)
        if (nonBlurBackground != null) {
            view.background = nonBlurBackground
        }
    }
}
