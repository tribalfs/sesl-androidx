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
value class AppWidgetStyle(val mask: Int) {

    operator fun contains(other: AppWidgetStyle): Boolean {
        return (other.mask or mask) == mask
    }

    operator fun plus(style: AppWidgetStyle): AppWidgetStyle {
        return AppWidgetStyle(mask or style.mask)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    operator fun plus(mask: Int): AppWidgetStyle {
        return AppWidgetStyle(this.mask or mask)
    }

    fun toInt(): Int = mask

    override fun toString(): String {
        return when (mask) {
            2 -> "monotone"
            1 -> "colorful"
            3 -> "colorful|monotone"
            else -> "colorful"
        }
    }

    companion object {
        val Colorful = AppWidgetStyle(1)
        val Monotone = AppWidgetStyle(2)

        fun get(mask: Int): AppWidgetStyle {
            return if (mask != 2) Colorful else Monotone
        }
    }
}
