/*
 * Copyright 2024 The Android Open Source Project
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
package androidx.appcompat.util.theme.resource

import android.content.Context
import androidx.appcompat.util.SeslMisc

/**
 * Abstract class representing a theme-dependent color resource.
 *
 * This class provides a framework for defining colors that adapt to the current theme (light/dark)
 * and potentially to whether a default or custom theme (on a One UI device) is active.
 *
 * It contains the following nested classes to handle specific color resource scenarios:
 * - [ResourceColor]
 * - [OpenThemeResourceColor]
 * - [ThemeResourceColor]
 */
abstract class SeslThemeResourceColor private constructor() {

    /** The base class for all theme-dependent color resources. */
    abstract class ResourceColor {
        abstract fun getColor(context: Context): Int
    }

    /**
     * Represents a color that has different definitions for default and custom themes
     * (on a One UI device).
     *
     * This class allows specifying different color resources for the default theme and
     * for any other "open" (custom) theme. The appropriate color will be chosen at runtime
     * based on the currently active theme.
     *
     * @property defaultThemeResource The [ThemeResourceColor] to be used when the default
     *  theme is active.
     * @property openThemeResource The [ThemeResourceColor] to be used when a custom
     *  theme is active. This applies to One UI devices only.
     */
    data class OpenThemeResourceColor(
        val defaultThemeResource: ThemeResourceColor,
        val openThemeResource: ThemeResourceColor
    ) : ResourceColor() {

        override fun getColor(context: Context): Int =
            if (SeslMisc.isDefaultTheme(context)) {
                defaultThemeResource.getColor(context)
            } else openThemeResource.getColor(context)

        companion object {
            fun copydefault(
                originalColor: OpenThemeResourceColor,
                newDefaultColor: ThemeResourceColor,
                newOpenColor: ThemeResourceColor,
                flags: Int
            ): OpenThemeResourceColor {
                val defaultColor = if ((flags and 1) != 0) {
                    originalColor.defaultThemeResource
                } else {
                    newDefaultColor
                }

                val openColor = if ((flags and 2) != 0) {
                    originalColor.openThemeResource
                } else {
                    newOpenColor
                }

                return originalColor.copy(defaultColor, openColor)
            }
        }
    }

    /**
     * Data class encapsulating alternative color resources for light and dark themes.
     *
     * This class holds resource IDs for colors to be used in light and dark themes.
     * It extends [ResourceColor] and provides a concrete implementation for retrieving
     * the appropriate color resource ID based on the current theme (light or dark).
     *
     * @property lightThemeResId The resource ID of the color to be used in a light theme.
     * @property darkThemeResId The resource ID of the color to be used in a dark theme.
     *                          Defaults to [lightThemeResId] if not specified.
     */
    data class ThemeResourceColor @JvmOverloads constructor(
        val lightThemeResId: Int,
        val darkThemeResId: Int = lightThemeResId
    ) : ResourceColor() {

        override fun getColor(context: Context): Int =
            if (SeslMisc.isLightTheme(context)) this.lightThemeResId else this.darkThemeResId

        companion object {
            fun copydefault(
                original: ThemeResourceColor,
                lightResId: Int,
                darkResId: Int,
                flags: Int
            ): ThemeResourceColor {
                val finalLightResId = if ((flags and 1) != 0) original.lightThemeResId else lightResId
                val finalDarkResId = if ((flags and 2) != 0) original.darkThemeResId else darkResId
                return original.copy(finalLightResId, finalDarkResId)
            }
        }
    }
}