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
import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DeviceTypeUtil(internal val type: DeviceType) {
    companion object {
        private var instance: DeviceTypeUtil? = null

        private fun canBeChanged(type: DeviceType?): Boolean {
            return type == DeviceType.FoldMain || type == DeviceType.FoldSub ||
                    type == DeviceType.MultiFoldMain || type == DeviceType.MultiFoldSub
        }

        fun get(context: Context): DeviceType {
            val currentInstance = instance
            if (currentInstance == null || canBeChanged(currentInstance.type)) {
                instance = DeviceTypeUtil(context.getDisplayDeviceType())
            }
            val deviceType = instance!!.type
            SeslAppWidgetLog.d("GlanceDeviceConfigUtils", "DeviceType is $deviceType")
            return deviceType
        }
    }
}
