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
import android.graphics.Point
import android.util.Size
import androidx.annotation.RestrictTo
import kotlin.jvm.JvmInline

@JvmInline
value class AppWidgetSize(val mask: Int) : Comparable<AppWidgetSize> {

    override operator fun compareTo(other: AppWidgetSize): Int {
        return mask.compareTo(other.mask)
    }

    operator fun contains(other: AppWidgetSize): Boolean {
        return (other.mask or mask) == mask
    }

    operator fun plus(size: AppWidgetSize): AppWidgetSize {
        return AppWidgetSize(mask or size.mask)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    operator fun plus(mask: Int): AppWidgetSize {
        return AppWidgetSize(this.mask or mask)
    }

    operator fun minus(size: AppWidgetSize): AppWidgetSize {
        return AppWidgetSize(mask and size.mask.inv())
    }

    fun toArrayList(): ArrayList<AppWidgetSize> {
        val list = ArrayList<AppWidgetSize>()
        if (contains(Tiny)) list.add(Tiny)
        if (contains(Small)) list.add(Small)
        if (contains(WideSmall)) list.add(WideSmall)
        if (contains(Medium)) list.add(Medium)
        if (contains(Large)) list.add(Large)
        if (contains(ExtraLarge)) list.add(ExtraLarge)
        if (contains(ExtraLargeLong)) list.add(ExtraLargeLong)
        return list
    }

    @Deprecated(message = "", replaceWith = ReplaceWith("toSpan(context)"))
    fun toCell(context: Context): Size {
        return when (this) {
            Tiny -> Size(1, 1)
            Small -> Size(2, 1)
            WideSmall -> Size(4, 1)
            Medium -> Size(2, 2)
            Large -> Size(4, 2)
            ExtraLarge -> Size(4, 4)
            ExtraLargeLong -> {
                if (context.resources.configuration.orientation == 1) Size(4, 6) else Size(6, 4)
            }
            else -> Size(0, 0)
        }
    }

    fun toSpan(context: Context): Point {
        return when (this) {
            Tiny -> Point(1, 1)
            Small -> Point(2, 1)
            WideSmall -> Point(4, 1)
            Medium -> Point(2, 2)
            Large -> Point(4, 2)
            ExtraLarge -> Point(4, 4)
            ExtraLargeLong -> {
                if (context.resources.configuration.orientation == 1) Point(4, 6) else Point(6, 4)
            }
            else -> Point(0, 0)
        }
    }

    override fun toString(): String {
        return when (this) {
            Unknown -> "unknown"
            Tiny -> "tiny"
            Small -> "small"
            WideSmall -> "widesmall"
            Medium -> "medium"
            Large -> "large"
            ExtraLarge -> "extralarge"
            ExtraLargeLong -> "extralargelong"
            All -> "all"
            else -> "mixed"
        }
    }

    fun toInt(): Int = mask

    companion object {
        val Unknown = AppWidgetSize(0)
        val Tiny = AppWidgetSize(1)
        val Small = AppWidgetSize(2)
        val WideSmall = AppWidgetSize(4)
        val Medium = AppWidgetSize(8)
        val Large = AppWidgetSize(16)
        val ExtraLarge = AppWidgetSize(32)
        val ExtraLargeLong = AppWidgetSize(64)

        val All = combine(
            listOf(
                Tiny, Small, WideSmall, Medium, Large, ExtraLarge, ExtraLargeLong
            )
        )

        fun combine(sizes: List<AppWidgetSize>): AppWidgetSize {
            var mask = 0
            for (size in sizes) {
                mask = mask or size.mask
            }
            return AppWidgetSize(mask)
        }

        fun get(mask: Int): AppWidgetSize {
            return when (mask) {
                Tiny.mask -> Tiny
                Small.mask -> Small
                WideSmall.mask -> WideSmall
                Medium.mask -> Medium
                Large.mask -> Large
                ExtraLarge.mask -> ExtraLarge
                ExtraLargeLong.mask -> ExtraLargeLong
                else -> Unknown
            }
        }

        fun get(context: Context, spanX: Int, spanY: Int): AppWidgetSize {
            return when (spanY) {
                1 -> when (spanX) {
                    1 -> Tiny
                    2 -> Small
                    4 -> WideSmall
                    else -> Unknown
                }
                2 -> when (spanX) {
                    2 -> Medium
                    4 -> Large
                    else -> Unknown
                }
                4 -> when (spanX) {
                    4 -> ExtraLarge
                    6 -> if (context.resources.configuration.orientation == 2) ExtraLargeLong else Unknown
                    else -> Unknown
                }
                6 -> if (spanX == 4 && context.resources.configuration.orientation == 1) ExtraLargeLong else Unknown
                else -> Unknown
            }
        }

        fun get(str: String): AppWidgetSize {
            return when (str) {
                "medium" -> Medium
                "widesmall" -> WideSmall
                "tiny" -> Tiny
                "large" -> Large
                "small" -> Small
                "extralarge" -> ExtraLarge
                "extralargelong" -> ExtraLargeLong
                else -> Unknown
            }
        }
    }
}
