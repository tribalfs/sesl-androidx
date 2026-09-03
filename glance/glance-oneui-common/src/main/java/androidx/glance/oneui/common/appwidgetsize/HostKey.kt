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

import androidx.glance.oneui.common.AppWidgetHostType
import androidx.glance.oneui.common.AppWidgetStyle
import androidx.glance.oneui.common.DisplayDeviceType

object HostKey {
    private fun getBitPosition(mask: Int): Int {
        if (Integer.bitCount(mask) != 1) {
            return -1
        }
        return Integer.numberOfTrailingZeros(mask)
    }

    fun decodeHostKey(hostKey: Int): Triple<AppWidgetHostType, DisplayDeviceType, AppWidgetStyle>? {
        val bitPosition = getBitPosition(hostKey)
        if (bitPosition < 0) {
            return null
        }
        return Triple(
            AppWidgetHostType(1 shl (bitPosition shr 2)),
            DisplayDeviceType(1 shl ((bitPosition shr 1) and 1)),
            AppWidgetStyle(1 shl (bitPosition and 1))
        )
    }

    fun makeHostKey(hostType: Int, displayDeviceType: Int, appWidgetStyle: Int): Int {
        val bitPosition = getBitPosition(hostType)
        val bitPosition2 = getBitPosition(displayDeviceType)
        val bitPosition3 = getBitPosition(appWidgetStyle)
        if (bitPosition < 0 || bitPosition2 < 0 || bitPosition3 < 0) {
            return -1
        }
        return 1 shl (bitPosition3 or ((bitPosition shl 2) or (bitPosition2 shl 1)))
    }
}
