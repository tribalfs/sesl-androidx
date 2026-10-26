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

import androidx.annotation.RestrictTo
import kotlin.jvm.JvmInline

@JvmInline
value class AppWidgetHostType(private val mask: Int) {
    companion object {
        val Unknown = AppWidgetHostType(0)
        val Home = AppWidgetHostType(1)
        val LockAndAOD = AppWidgetHostType(2)
        val Cover = AppWidgetHostType(4)
        val DexHome = AppWidgetHostType(16)
        val All = AppWidgetHostType(Home.mask or LockAndAOD.mask or Cover.mask or DexHome.mask)

        fun get(mask: Int): AppWidgetHostType {
            return when (mask) {
                Home.mask -> Home
                LockAndAOD.mask -> LockAndAOD
                Cover.mask -> Cover
                DexHome.mask -> DexHome
                else -> Unknown
            }
        }
    }

    operator fun contains(other: AppWidgetHostType): Boolean {
        return (other.mask or mask) == mask
    }

    operator fun plus(type: AppWidgetHostType): AppWidgetHostType {
        return AppWidgetHostType(mask or type.mask)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    operator fun plus(mask: Int): AppWidgetHostType {
        return AppWidgetHostType(this.mask or mask)
    }

    fun toInt(): Int = mask

    override fun toString(): String {
        return when (this) {
            Home -> "Home"
            LockAndAOD -> "LockAndAOD"
            Cover -> "Cover"
            DexHome -> "DexHome"
            else -> "Unknown"
        }
    }
}
