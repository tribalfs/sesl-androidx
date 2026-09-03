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

package androidx.glance.oneui.common.sizepolicy

import android.util.SizeF
import androidx.glance.oneui.common.AppWidgetSize

abstract class AppWidgetSizePolicy {
    abstract val widthLevel0: Float
    abstract val widthLevel1: Float
    abstract val widthLevel2: Float
    abstract val widthLevel3: Float

    abstract val heightLevel0: Float
    abstract val heightLevel1: Float
    abstract val heightLevel2: Float
    abstract val heightLevel3: Float
    abstract val heightLevel4: Float

    private val sizeDpRangeMap: Map<AppWidgetSize, Pair<SizeF, SizeF>> by lazy {
        mapOf(
            AppWidgetSize.Tiny to (SizeF(widthLevel0, heightLevel0) to SizeF(widthLevel1 - 1f, heightLevel1 - 1f)),
            AppWidgetSize.Small to (SizeF(widthLevel1, heightLevel0) to SizeF(widthLevel2 - 1f, heightLevel1 - 1f)),
            AppWidgetSize.Medium to (SizeF(widthLevel1, heightLevel1) to SizeF(widthLevel2 - 1f, heightLevel2 - 1f)),
            AppWidgetSize.WideSmall to (SizeF(widthLevel2, heightLevel0) to SizeF(widthLevel3, heightLevel1 - 1f)),
            AppWidgetSize.Large to (SizeF(widthLevel2, heightLevel1) to SizeF(widthLevel3, heightLevel2 - 1f)),
            AppWidgetSize.ExtraLarge to (SizeF(widthLevel2, heightLevel2) to SizeF(widthLevel3, heightLevel3 - 1f)),
            AppWidgetSize.ExtraLargeLong to (SizeF(widthLevel2, heightLevel3) to SizeF(widthLevel3, heightLevel4))
        )
    }

    fun convertDpToSize(width: Float, height: Float): AppWidgetSize {
        return if (width < widthLevel2) {
            if (width >= widthLevel1) {
                if (height >= heightLevel1) AppWidgetSize.Medium else AppWidgetSize.Small
            } else {
                AppWidgetSize.Tiny
            }
        } else if (height >= heightLevel3) {
            AppWidgetSize.ExtraLargeLong
        } else if (height >= heightLevel2) {
            AppWidgetSize.ExtraLarge
        } else if (height >= heightLevel1) {
            AppWidgetSize.Large
        } else {
            AppWidgetSize.WideSmall
        }
    }

    fun convertSizeToDp(size: AppWidgetSize): Pair<SizeF, SizeF> {
        return sizeDpRangeMap[size] ?: (SizeF(0f, 0f) to SizeF(0f, 0f))
    }
}
