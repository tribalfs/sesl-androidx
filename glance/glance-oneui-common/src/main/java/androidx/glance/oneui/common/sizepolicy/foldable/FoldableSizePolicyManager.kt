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

package androidx.glance.oneui.common.sizepolicy.foldable

import android.content.Context
import android.util.Size
import android.util.SizeF
import androidx.glance.oneui.common.AppWidgetSize
import androidx.glance.oneui.common.DeviceType
import androidx.glance.oneui.common.DeviceTypeUtil

object FoldableSizePolicyManager {
    fun convertDpToSize(
        context: Context,
        width: Float,
        height: Float,
        isEasyMode: Boolean,
        isFoldSync: Boolean,
        screenSize: Size
    ): AppWidgetSize {
        return when (DeviceTypeUtil.get(context)) {
            DeviceType.FoldMain -> {
                if (isFoldSync) {
                    FoldMainSyncPolicy.convertDpToSize(context, width, height, screenSize, isEasyMode)
                } else {
                    FoldMainPolicy.convertDpToSize(context, width, height, screenSize, isEasyMode)
                }
            }
            DeviceType.FoldSub -> {
                FoldCoverPolicy.convertDpToSize(context, width, height, screenSize, isEasyMode)
            }
            else -> AppWidgetSize.Unknown
        }
    }

    fun convertSizeToDp(
        context: Context,
        size: AppWidgetSize,
        isEasyMode: Boolean,
        isFoldSync: Boolean,
        screenSize: Size
    ): Pair<SizeF, SizeF> {
        return when (DeviceTypeUtil.get(context)) {
            DeviceType.FoldMain -> {
                if (isFoldSync) {
                    FoldMainSyncPolicy.convertSizeToDp(context, size, screenSize, isEasyMode)
                } else {
                    FoldMainPolicy.convertSizeToDp(context, size, screenSize, isEasyMode)
                }
            }
            DeviceType.FoldSub -> {
                FoldCoverPolicy.convertSizeToDp(context, size, screenSize, isEasyMode)
            }
            else -> Pair(SizeF(0f, 0f), SizeF(0f, 0f))
        }
    }
}
