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

import android.util.Size
import android.util.SizeF
import androidx.glance.oneui.common.AppWidgetSize
import androidx.glance.oneui.common.SeslAppWidgetLog

abstract class SizeRatioPolicy {
    abstract val hRatioLevel0: Float
    abstract val hRatioLevel1: Float
    abstract val hRatioLevel2: Float
    abstract val hRatioLevel3: Float
    abstract val hRatioLevel4: Float
    abstract val wRatioLevel0: Float
    abstract val wRatioLevel1: Float
    abstract val wRatioLevel2: Float
    abstract val wRatioLevel3: Float

    private val sizeRatioMap: Map<AppWidgetSize, AppWidgetSizeRatio>
        get() = mapOf(
            AppWidgetSize.Tiny to AppWidgetSizeRatio(
                SizeF(wRatioLevel0, hRatioLevel0),
                SizeF(wRatioLevel1, hRatioLevel1)
            ),
            AppWidgetSize.Small to AppWidgetSizeRatio(
                SizeF(wRatioLevel1.increase(), hRatioLevel0),
                SizeF(wRatioLevel2, hRatioLevel1)
            ),
            AppWidgetSize.WideSmall to AppWidgetSizeRatio(
                SizeF(wRatioLevel2.increase(), hRatioLevel0),
                SizeF(wRatioLevel3, hRatioLevel1)
            ),
            AppWidgetSize.Medium to AppWidgetSizeRatio(
                SizeF(wRatioLevel1.increase(), hRatioLevel1.increase()),
                SizeF(wRatioLevel2, hRatioLevel2)
            ),
            AppWidgetSize.Large to AppWidgetSizeRatio(
                SizeF(wRatioLevel2.increase(), hRatioLevel1.increase()),
                SizeF(wRatioLevel3, hRatioLevel2)
            ),
            AppWidgetSize.ExtraLarge to AppWidgetSizeRatio(
                SizeF(wRatioLevel2.increase(), hRatioLevel2.increase()),
                SizeF(wRatioLevel3, hRatioLevel3)
            ),
            AppWidgetSize.ExtraLargeLong to AppWidgetSizeRatio(
                SizeF(wRatioLevel2.increase(), hRatioLevel3.increase()),
                SizeF(wRatioLevel3, hRatioLevel4)
            )
        )

    fun convertDpToSize(width: Float, height: Float, screenSize: Size): AppWidgetSize {
        val size = sizeRatioMap.entries.find {
            it.value.checkFitInSizeRange(width, height, screenSize)
        }?.key ?: AppWidgetSize.Unknown

        SeslAppWidgetLog.i("SizePolicy", "convertDpToSize : $width, $height, $size / $this")
        return size
    }

    fun convertSizeToDp(size: AppWidgetSize, screenSize: Size): Pair<SizeF, SizeF> {
        val dp = sizeRatioMap[size]?.toDp(screenSize) ?: Pair(SizeF(0f, 0f), SizeF(0f, 0f))
        SeslAppWidgetLog.i("SizePolicy", "convertSizeToDp : $size, $dp")
        return dp
    }

    override fun toString(): String {
        return "widthLevel0=$wRatioLevel0,widthLevel1=$wRatioLevel1,widthLevel2=$wRatioLevel2,widthLevel3=$wRatioLevel3," +
            "heightLevel0=$hRatioLevel0,heightLevel1=$hRatioLevel1,heightLevel2=$hRatioLevel2,heightLevel3=$hRatioLevel3," +
            "heightLevel4=$hRatioLevel4"
    }
}
