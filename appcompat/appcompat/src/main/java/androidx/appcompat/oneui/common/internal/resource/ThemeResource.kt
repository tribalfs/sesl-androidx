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

package androidx.appcompat.oneui.common.internal.resource

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RestrictTo
import androidx.appcompat.util.SeslMisc
import androidx.core.util.SeslDisplayUtils

//sesl9
/**
 * Base class for theme-dependent resources.
 *
 * @param T The type of resource returned.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class ThemeResource<T> {
    /**
     * Resolves the resource for the given context.
     *
     * @param context Context used to resolve theme properties.
     * @return The resolved resource.
     */
    abstract fun getResource(context: Context): T
}

/**
 * Implementation of [ThemeResource] supporting separate light and dark theme values.
 *
 * @param T The type of resource returned.
 * @property resourceLight The resource value for light theme.
 * @property resourceDark The resource value for dark theme.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
open class ThemeResourceImpl<T>(
    private val resourceLight: T,
    private val resourceDark: T
) : ThemeResource<T>() {

    /**
     * Returns the light or dark resource based on [SeslMisc.isLightTheme].
     *
     * @param context Context used for light/dark theme check.
     * @return [resourceLight] if light theme is active, otherwise [resourceDark].
     */
    override fun getResource(context: Context): T {
        return if (SeslMisc.isLightTheme(context)) resourceLight else resourceDark
    }
}

/**
 * Theme-dependent color resource representation.
 *
 * @property resourceLight The raw ARGB color int for light theme.
 * @property resourceDark The raw ARGB color int for dark theme, defaulting to [resourceLight].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class ThemeResourceColor @JvmOverloads constructor(
    val resourceLight: Int = 0,
    val resourceDark: Int = resourceLight
) : ThemeResourceImpl<Int>(resourceLight, resourceDark)

/**
 * Implementation of [ThemeResource] supporting open theme (overlay theme) resources.
 *
 * Resolves to [defaultThemeResource] if DeX mode is enabled or no overlay theme is applied,
 * otherwise resolves to [openThemeResource].
 *
 * @property defaultThemeResource The default [ThemeResource] used when open theme is not applied or DeX is active.
 * @property openThemeResource The open theme [ThemeResource] used when overlay theme is applied.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
open class OpenThemeResourceImpl(
    val defaultThemeResource: ThemeResource<Int>,
    val openThemeResource: ThemeResource<Int>
) : ThemeResource<Int>() {

    /**
     * Resolves the resource value based on DeX state and overlay theme application.
     *
     * @param context Context used to check DeX state and overlay theme status.
     * @return The integer resource value from [defaultThemeResource] or [openThemeResource].
     */
    override fun getResource(context: Context): Int {
        return if (SeslDisplayUtils.isDexEnabled(context) || !SeslMisc.isOverlayThemeApplied(context)) {
            defaultThemeResource.getResource(context)
        } else {
            openThemeResource.getResource(context)
        }
    }
}

/**
 * Theme-dependent color resource ID representation.
 *
 * @property resourceLight Color resource ID for light theme.
 * @property resourceDark Color resource ID for dark theme, defaulting to [resourceLight].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class ThemeResourceColorRes @JvmOverloads constructor(
    @param:ColorRes val resourceLight: Int,
    @param:ColorRes val resourceDark: Int = resourceLight
) : ThemeResourceImpl<Int>(resourceLight, resourceDark)

/**
 * Theme-dependent drawable resource ID representation.
 *
 * @property resourceLight Drawable resource ID for light theme.
 * @property resourceDark Drawable resource ID for dark theme, defaulting to [resourceLight].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class ThemeResourceDrawableRes @JvmOverloads constructor(
    @param:DrawableRes val resourceLight: Int,
    @param:DrawableRes val resourceDark: Int = resourceLight
) : ThemeResourceImpl<Int>(resourceLight, resourceDark)

/**
 * Open theme implementation for color resource IDs.
 *
 * @param defaultThemeResource The default color resource ID for standard theme.
 * @param openThemeResource The color resource ID for open (overlay) theme.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class OpenThemeResourceColorRes(
    defaultThemeResource: ThemeResourceColorRes,
    openThemeResource: ThemeResourceColorRes
) : OpenThemeResourceImpl(defaultThemeResource, openThemeResource)

/**
 * Open theme implementation for drawable resource IDs.
 *
 * @param defaultThemeResource The default drawable resource ID for standard theme.
 * @param openThemeResource The drawable resource ID for open (overlay) theme.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class OpenThemeResourceDrawableRes(
    defaultThemeResource: ThemeResourceDrawableRes,
    openThemeResource: ThemeResourceDrawableRes
) : OpenThemeResourceImpl(defaultThemeResource, openThemeResource)
