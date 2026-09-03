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

package androidx.glance.oneui.common.sizepolicy.multifold

import android.content.Context
import android.util.Log
import android.util.Size
import android.util.SizeF
import androidx.glance.oneui.common.AppWidgetSize
import androidx.glance.oneui.common.DeviceType
import androidx.glance.oneui.common.DeviceTypeUtil
import androidx.glance.oneui.common.GridSpanInfo

object MultiFoldableSizePolicyManager {
    fun convertDpToSize(
        context: Context,
        width: Float,
        height: Float,
        isFoldSync: Boolean,
        isEasyMode: Boolean,
        gridSpanInfo: GridSpanInfo,
        screenSize: Size
    ): AppWidgetSize {
        val deviceType = DeviceTypeUtil.get(context)
        Log.i("heec.choi", "convertDpToSize / $deviceType $isFoldSync $gridSpanInfo $screenSize $width $height")

        return when (deviceType) {
            DeviceType.MultiFoldMain -> {
                if (isFoldSync) {
                    MultiFoldMainSyncPolicy.convertDpToSize(context, width, height, screenSize, gridSpanInfo)
                } else {
                    MultiFoldMainPolicy.convertDpToSize(context, width, height, screenSize, gridSpanInfo)
                }
            }
            DeviceType.MultiFoldSub -> {
                MultiFoldCoverPolicy.convertDpToSize(context, width, height, screenSize, isEasyMode)
            }
            else -> AppWidgetSize.Unknown
        }
    }

    fun convertSizeToDp(
        context: Context,
        size: AppWidgetSize,
        isEasyMode: Boolean,
        isFoldSync: Boolean,
        gridSpanInfo: GridSpanInfo,
        screenSize: Size
    ): Pair<SizeF, SizeF> {
        return when (DeviceTypeUtil.get(context)) {
            DeviceType.MultiFoldMain -> {
                if (isFoldSync) {
                    MultiFoldMainSyncPolicy.convertSizeToDp(context, size, screenSize, gridSpanInfo)
                } else {
                    MultiFoldMainPolicy.convertSizeToDp(context, size, screenSize, gridSpanInfo)
                }
            }
            DeviceType.MultiFoldSub -> {
                MultiFoldCoverPolicy.convertSizeToDp(context, size, screenSize, isEasyMode)
            }
            else -> Pair(SizeF(0f, 0f), SizeF(0f, 0f))
        }
    }
}
