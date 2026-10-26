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

abstract class SizePolicy {
    abstract val heightLevel0: Float
    abstract val heightLevel1: Float
    abstract val heightLevel2: Float
    abstract val heightLevel3: Float
    abstract val heightLevel4: Float
    abstract val widthLevel0: Float
    abstract val widthLevel1: Float
    abstract val widthLevel2: Float
    abstract val widthLevel3: Float

    private val sizeDpRangeMap: Map<AppWidgetSize, Pair<SizeF, SizeF>> by lazy {
        mapOf(
            AppWidgetSize.Tiny to Pair(
                SizeF(widthLevel0, heightLevel0),
                SizeF(widthLevel1 - 1.0f, heightLevel1 - 1.0f)
            ),
            AppWidgetSize.Small to Pair(
                SizeF(widthLevel1, heightLevel0),
                SizeF(widthLevel2 - 1.0f, heightLevel1 - 1.0f)
            ),
            AppWidgetSize.Medium to Pair(
                SizeF(widthLevel1, heightLevel1),
                SizeF(widthLevel2 - 1.0f, heightLevel2 - 1.0f)
            ),
            AppWidgetSize.WideSmall to Pair(
                SizeF(widthLevel2, heightLevel0),
                SizeF(widthLevel3, heightLevel1 - 1.0f)
            ),
            AppWidgetSize.Large to Pair(
                SizeF(widthLevel2, heightLevel1),
                SizeF(widthLevel3, heightLevel2 - 1.0f)
            ),
            AppWidgetSize.ExtraLarge to Pair(
                SizeF(widthLevel2, heightLevel2),
                SizeF(widthLevel3, heightLevel3 - 1.0f)
            ),
            AppWidgetSize.ExtraLargeLong to Pair(
                SizeF(widthLevel2, heightLevel3),
                SizeF(widthLevel3, heightLevel4)
            )
        )
    }

    internal fun convertDpToSize(width: Float, height: Float): AppWidgetSize {
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

    internal fun convertSizeToDp(size: AppWidgetSize): Pair<SizeF, SizeF> {
        return sizeDpRangeMap[size] ?: Pair(SizeF(0.0f, 0.0f), SizeF(0.0f, 0.0f))
    }
}
