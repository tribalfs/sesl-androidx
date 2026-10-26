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

package androidx.glance.oneui.common.appwidgetsize

import android.os.Bundle
import android.util.SizeF
import androidx.glance.oneui.common.AppWidgetSize

fun Bundle.getDensity(): Float = getFloat("semDisplayDensity", 0.0f)

fun Bundle.getHostKey(): Int = getInt("hostKey", 0)

fun Bundle.toAppWidgetSizeInfo(): AppWidgetSizeInfo? {
    val sizeF = getParcelable("appWidgetSizes", SizeF::class.java) ?: return null
    val row = getInt("semAppWidgetRowSpan", 0)
    val col = getInt("semAppWidgetColumnSpan", 0)
    val widgetSize = getInt("semWidgetSize", 0)
    return AppWidgetSizeInfo(
        dpSize = sizeF,
        spanSize = SpanSize(col, row),
        appWidgetSize = AppWidgetSize(widgetSize)
    )
}

fun AppWidgetSizeInfo.toBundle(hostKey: Int, density: Float): Bundle = Bundle().apply {
    putInt("hostKey", hostKey)
    putFloat("semDisplayDensity", density)
    putParcelable("appWidgetSizes", SizeF(this@toBundle.dpSize.width, this@toBundle.dpSize.height))
    putInt("semAppWidgetRowSpan", this@toBundle.spanSize.row)
    putInt("semAppWidgetColumnSpan", this@toBundle.spanSize.col)
    putInt("semWidgetSize", this@toBundle.appWidgetSize.toInt())
}
