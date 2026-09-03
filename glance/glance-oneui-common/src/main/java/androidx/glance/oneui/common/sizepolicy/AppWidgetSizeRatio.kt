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
import androidx.glance.oneui.common.SeslAppWidgetLog

data class AppWidgetSizeRatio(
    val fromRatio: SizeF,
    val toRatio: SizeF
) {
    fun checkFitInSizeRange(width: Float, height: Float, screenSize: Size): Boolean {
        val screenWidth = screenSize.width.toFloat()
        val screenHeight = screenSize.height.toFloat()
        SeslAppWidgetLog.i("SizePolicy", "checkFitInSizeRange at $screenSize / $fromRatio, $toRatio")

        return width >= fromRatio.width * screenWidth &&
            width <= toRatio.width * screenWidth &&
            height >= fromRatio.height * screenHeight &&
            height <= toRatio.height * screenHeight
    }

    fun toDp(screenSize: Size): Pair<SizeF, SizeF> {
        SeslAppWidgetLog.i("SizePolicy", "toDp / $screenSize $fromRatio $toRatio")
        val screenWidth = screenSize.width.toFloat()
        val screenHeight = screenSize.height.toFloat()
        return Pair(
            SizeF(fromRatio.width * screenWidth, fromRatio.height * screenHeight),
            SizeF(toRatio.width * screenWidth, toRatio.height * screenHeight)
        )
    }
}
