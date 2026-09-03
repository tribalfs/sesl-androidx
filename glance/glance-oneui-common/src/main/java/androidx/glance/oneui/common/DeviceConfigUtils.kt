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

package androidx.glance.oneui.common

import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.util.Size
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RestrictTo
import androidx.glance.oneui.common.devicefeature.DeviceFeature
import androidx.reflect.content.res.SeslConfigurationReflector
import kotlin.math.roundToInt

private const val TAG = "GlanceDeviceConfigUtils"

internal fun Context.getDisplayDeviceType(): DeviceType {
    if (DeviceFeature.isFlipModel) {
        return DeviceType.Flip
    }
    if (!DeviceFeature.isFoldModel) {
        return if (DeviceFeature.isTabletModel) DeviceType.Tablet else DeviceType.Phone
    }
    val semDisplayDeviceType = SeslConfigurationReflector.getField_semDisplayDeviceType(resources.configuration)
    return if (DeviceFeature.isMultiFoldModel) {
        when (semDisplayDeviceType) {
            0 -> DeviceType.MultiFoldMain
            5 -> DeviceType.MultiFoldSub
            else -> DeviceType.MultiFoldMain
        }
    } else {
        when (semDisplayDeviceType) {
            0 -> DeviceType.FoldMain
            5 -> DeviceType.FoldSub
            else -> DeviceType.FoldMain
        }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Context.getScreenSize(): Size {
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val metrics = windowManager.currentWindowMetrics
    val windowInsets = metrics.windowInsets
    val insets = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars())

    val point = Point()
    @Suppress("DEPRECATION")
    windowManager.defaultDisplay.getRealSize(point)

    val width = point.x - (insets.left + insets.right)
    val height = point.y - (insets.top + insets.bottom)
    val density = metrics.density

    return Size((width / density).roundToInt(), (height / density).roundToInt())
}

internal fun Context.isOverSW360Dp(): Boolean {
    return resources.getBoolean(R.bool.is_sw_over_360_dp)
}

fun Configuration.isPortrait(): Boolean {
    return orientation == Configuration.ORIENTATION_PORTRAIT
}
