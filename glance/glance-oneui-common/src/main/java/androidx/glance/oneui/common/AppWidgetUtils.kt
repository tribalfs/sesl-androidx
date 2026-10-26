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
import android.os.Build
import android.os.Bundle
import android.util.Size
import android.util.SizeF
import androidx.annotation.RestrictTo
import androidx.glance.oneui.common.sizepolicy.flip.FlipSizePolicyManager
import androidx.glance.oneui.common.sizepolicy.foldable.FoldableSizePolicyManager
import androidx.glance.oneui.common.sizepolicy.multifold.MultiFoldableSizePolicyManager
import androidx.glance.oneui.common.sizepolicy.phone.PhoneSizePolicyManager
import androidx.glance.oneui.common.sizepolicy.tablet.TabletSizePolicyManager
import androidx.glance.oneui.common.sizepolicy.third.SizePolicyAt3rd

private const val TAG = "AppWidgetUtils"

fun Bundle.explicitWidgetSize(): AppWidgetSize {
    return AppWidgetSize.get(getInt("semWidgetSize", AppWidgetSize.Unknown.toInt()))
}

fun Bundle.extractWidgetHost(): AppWidgetHostType {
    return AppWidgetHostType.get(getInt("semHostType", AppWidgetHostType.Unknown.toInt()))
}

fun Bundle.extractWidgetStyle(): AppWidgetStyle {
    return AppWidgetStyle.get(getInt("semWidgetStyle", AppWidgetStyle.Colorful.toInt()))
}

fun Bundle.extractDisplayDensity(): Float {
    return getFloat("semDisplayDensity", 0.0f)
}

fun Bundle.extractDpSizeFromOptions(isPortrait: Boolean): SizeF? {
    val arraySizes = extractFromArraySizes(isPortrait)
    return arraySizes ?: extractFromMinMaxSizes(isPortrait)
}

private fun Bundle.extractFromArraySizes(isPortrait: Boolean): SizeF? {
    val sizes = if (Build.VERSION.SDK_INT >= 33) {
        getParcelableArrayList("appWidgetSizes", SizeF::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableArrayList("appWidgetSizes")
    } ?: return null

    val widths = sizes.map { it.width }
    val heights = sizes.map { it.height }

    return if (isPortrait) {
        SizeF(widths.minOrNull() ?: 0f, heights.maxOrNull() ?: 0f)
    } else {
        SizeF(widths.maxOrNull() ?: 0f, heights.minOrNull() ?: 0f)
    }
}

private fun Bundle.extractFromMinMaxSizes(isPortrait: Boolean): SizeF? {
    val minHeight = getInt("appWidgetMinHeight", 0)
    val maxHeight = getInt("appWidgetMaxHeight", 0)
    val minWidth = getInt("appWidgetMinWidth", 0)
    val maxWidth = getInt("appWidgetMaxWidth", 0)

    if (minHeight == 0 || maxHeight == 0 || minWidth == 0 || maxWidth == 0) {
        return null
    }

    return if (isPortrait) {
        SizeF(minWidth.toFloat(), maxHeight.toFloat())
    } else {
        SizeF(maxWidth.toFloat(), minHeight.toFloat())
    }
}

private fun calculateWidgetSizeAt3rdLauncher(bundle: Bundle, context: Context): AppWidgetSize {
    SeslAppWidgetLog.init(context.packageName)
    val isPortrait = context.resources.configuration.isPortrait()
    val size = bundle.extractDpSizeFromOptions(isPortrait) ?: return AppWidgetSize.Unknown

    return if (isPortrait) {
        SizePolicyAt3rd.PortraitPolicy.convertDpToSize(size.width, size.height)
    } else {
        SizePolicyAt3rd.LandscapePolicy.convertDpToSize(size.width, size.height)
    }
}

fun convertDpToSize(
    context: Context,
    width: Float,
    height: Float,
    isEasyMode: Boolean = false,
    isFoldSync: Boolean = false,
    gridSpanInfo: GridSpanInfo = GridSpanInfo.Unspecified,
    screenSize: Size = context.getScreenSize()
): AppWidgetSize {
    SeslAppWidgetLog.init(context.packageName)
    val deviceType = DeviceTypeUtil.get(context)
    val result = when (deviceType) {
        DeviceType.MultiFoldMain, DeviceType.MultiFoldSub ->
            MultiFoldableSizePolicyManager.convertDpToSize(context, width, height, isFoldSync, isEasyMode, gridSpanInfo, screenSize)
        DeviceType.FoldMain, DeviceType.FoldSub ->
            FoldableSizePolicyManager.convertDpToSize(context, width, height, isEasyMode, isFoldSync, screenSize)
        DeviceType.Tablet ->
            TabletSizePolicyManager.convertDpToSize(context, width, height, gridSpanInfo, screenSize)
        DeviceType.Flip ->
            FlipSizePolicyManager.convertDpToSize(context, width, height, isEasyMode, screenSize)
        else ->
            PhoneSizePolicyManager.convertDpToSize(context, width, height, isEasyMode, screenSize)
    }

    SeslAppWidgetLog.i(TAG, "convert: dp($width, $height) to size($result) port=${context.resources.configuration.isPortrait()} isSw360dp=${context.isOverSW360Dp()}, device=$deviceType")
    return result
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun convertSizeToDp(
    context: Context,
    size: AppWidgetSize,
    isEasyMode: Boolean = false,
    isFoldSync: Boolean = false,
    gridSpanInfo: GridSpanInfo = GridSpanInfo.Unspecified,
    screenSize: Size = context.getScreenSize()
): Pair<SizeF, SizeF> {
    SeslAppWidgetLog.init(context.packageName)
    return when (DeviceTypeUtil.get(context)) {
        DeviceType.MultiFoldMain, DeviceType.MultiFoldSub ->
            MultiFoldableSizePolicyManager.convertSizeToDp(context, size, isEasyMode, isFoldSync, gridSpanInfo, screenSize)
        DeviceType.FoldMain, DeviceType.FoldSub ->
            FoldableSizePolicyManager.convertSizeToDp(context, size, isEasyMode, isFoldSync, screenSize)
        DeviceType.Tablet ->
            TabletSizePolicyManager.convertSizeToDp(context, size, gridSpanInfo, screenSize)
        DeviceType.Flip ->
            FlipSizePolicyManager.convertSizeToDp(context, size, isEasyMode, screenSize)
        else ->
            PhoneSizePolicyManager.convertSizeToDp(context, size, isEasyMode, screenSize)
    }
}

fun Bundle.getLayoutMode(context: Context, widgetId: Int, width: Float, height: Float): AppWidgetSize {
    SeslAppWidgetLog.init(context.packageName)
    val density = context.resources.displayMetrics.density
    SeslAppWidgetLog.i(TAG, "[common-1.1.4] $widgetId-widget size dp: w=$width h=$height, px: w=${width * density}.px h=${height * density}.px")

    val explicitSize = explicitWidgetSize()
    SeslAppWidgetLog.i(TAG, "mode=$explicitSize from options")

    if (explicitSize != AppWidgetSize.Unknown) {
        return explicitSize
    }

    if (extractWidgetStyle() == AppWidgetStyle.Monotone || Build.VERSION.SDK_INT <= 34) {
        return if (width / height < 1.5f) AppWidgetSize.Tiny else AppWidgetSize.Small
    }

    val calculatedSize = calculateWidgetSizeAt3rdLauncher(this, context)
    SeslAppWidgetLog.i(TAG, "$calculatedSize size is calculated at 3rd launcher")
    return calculatedSize
}
