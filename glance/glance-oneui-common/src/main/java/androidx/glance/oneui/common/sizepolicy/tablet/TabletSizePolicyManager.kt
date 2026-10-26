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

package androidx.glance.oneui.common.sizepolicy.tablet

import android.content.Context
import android.util.Size
import android.util.SizeF
import androidx.glance.oneui.common.AppWidgetSize
import androidx.glance.oneui.common.GridSpanInfo

object TabletSizePolicyManager {
    fun convertDpToSize(
        context: Context,
        width: Float,
        height: Float,
        gridSpanInfo: GridSpanInfo,
        screenSize: Size
    ): AppWidgetSize {
        return TabletPolicy.convertDpToSize(context, width, height, gridSpanInfo, screenSize)
    }

    fun convertSizeToDp(
        context: Context,
        size: AppWidgetSize,
        gridSpanInfo: GridSpanInfo,
        screenSize: Size
    ): Pair<SizeF, SizeF> {
        return TabletPolicy.convertSizeToDp(context, size, gridSpanInfo, screenSize)
    }
}
