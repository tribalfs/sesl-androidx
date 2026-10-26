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

import kotlin.jvm.JvmInline

@JvmInline
value class AppWidgetFeatures(private val mask: Int) {
    companion object {
        val None = AppWidgetFeatures(0)
    }

    operator fun contains(other: AppWidgetFeatures): Boolean {
        return (other.mask or mask) == mask
    }

    fun toInt(): Int = mask

    override fun toString(): String {
        return "AppWidgetFeatures(mask=$mask)"
    }
}

internal operator fun Int.contains(size: AppWidgetSize): Boolean {
    return (size.toInt() or this) == this
}
