/*
 * Copyright 2025 The Android Open Source Project
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
package androidx.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import androidx.reflect.DeviceInfo
import androidx.reflect.provider.SeslSettingsReflector
import androidx.reflect.view.SeslSemBlurInfoReflector
import androidx.reflect.view.SeslViewReflector


/*
 * Original code by Samsung, all rights reserved to the original author. Added in Sesl7
 */
/**
 * Provides compatibility methods for Samsung's blur effects.
 *
 * This object allows applying blur effects to views, with options for different blur modes,
 * colors, radii, and corner radii. It also handles checks for theme application and
 * accessibility settings that might disable blur effects.
 *
 * The blur functionality is based on Samsung's internal APIs and may not work on
 * all devices or Android versions.
 *
 */
object SemBlurCompat {
    const val BLUR_MODE_WINDOW = 0
    const val BLUR_MODE_WINDOW_CAPTURED = 1
    const val BLUR_MODE_CANVAS = 2

    const val BLUR_BASE_OFFSET = 101
    const val BLUR_UI_HIGH_ULTRA_THICK_DARK = 130
    @Deprecated("This is a typo error for `BLUR_UI_HIGH_ULTRA_THICK_DARK` constant.",
        level = DeprecationLevel.ERROR)
    const val BLUR_UI_HIGH_ULTRA_THICK_D = BLUR_UI_HIGH_ULTRA_THICK_DARK
    const val BLUR_UI_HIGH_ULTRA_THICK_LIGHT = 115
    const val BLUR_UI_LOW_ULTRA_THICK_DARK = 120
    const val BLUR_UI_LOW_ULTRA_THICK_LIGHT= 105
    const val BLUR_UI_MEDIUM_ULTRA_THICK_DARK = 125
    const val BLUR_UI_MEDIUM_ULTRA_THICK_LIGHT = 110

    @IntDef(
        BLUR_MODE_CANVAS,
        BLUR_MODE_WINDOW,
        BLUR_MODE_WINDOW_CAPTURED,
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class SeslBlurMode

    private fun isReduceTransparencySettingsEnabled(context: Context): Boolean {
        val field_A11Y_REDUCE_TRANSPARENCY = SeslSettingsReflector.SeslSystemReflector.getField_SEM_ACCESSIBILITY_REDUCE_TRANSPARENCY()
        return field_A11Y_REDUCE_TRANSPARENCY != "not_supported" && Settings.System.getInt(
            context.contentResolver,
            field_A11Y_REDUCE_TRANSPARENCY,
            BLUR_MODE_WINDOW
        ) == BLUR_MODE_WINDOW_CAPTURED
    }


    private fun isThemeApplied(context: Context): Boolean {
        return Settings.System.getString(context.contentResolver, "current_sec_active_themepackage") != null
    }

    /**
     * Sets a blur effect on the given view on a device running Samsung's One UI.
     *
     * This function allows applying a blur effect with specific parameters for color, radius,
     * blur mode, and corner radius.
     *
     * The blur effect will not be applied if a custom theme is applied or if the "reduce transparency and blur"
     * accessibility setting is enabled.
     *
     * @param view The view to apply the blur effect to.
     * @param color The background color for the blur effect.
     * @param radius The radius of the blur.
     * @param blurMode The blur mode to use. Must be one of [BLUR_MODE_WINDOW],
     *   [BLUR_MODE_WINDOW_CAPTURED], or [BLUR_MODE_CANVAS].
     * @param cornerRadius The corner radius for the blurred background.
     * @return `true` if the blur effect was successfully applied, `false` otherwise (e.g., if
     *   themes are applied, reduce transparency is enabled, or the blur builder creation failed).
     */
    @JvmStatic
    fun setBlurEffect(
        view: View,
        @ColorInt color: Int,
        radius: Int,
        @SeslBlurMode blurMode: Int,
        cornerRadius: Float
    ): Boolean {
        val context = view.context

        if (isThemeApplied(context) || isReduceTransparencySettingsEnabled(context)) {
            return false
        }

        val blurBuilder = SeslSemBlurInfoReflector.semCreateBlurBuilder(blurMode)
        if (blurBuilder == null) {
            return false
        }

        SeslSemBlurInfoReflector.semSetBuilderBlurRadius(blurBuilder, radius)
        SeslSemBlurInfoReflector.semSetBuilderBlurBackgroundColor(blurBuilder, color)
        SeslSemBlurInfoReflector.semSetBuilderBlurBackgroundCornerRadius(blurBuilder, cornerRadius)
        SeslSemBlurInfoReflector.semBuildSetBlurInfo(blurBuilder, view)
        return true
    }

    /**
     * Sets a preset blur effect on the given view on a device running Samsung's One UI.
     *
     * This function allows applying a predefined blur effect using a color curve preset.
     * It also allows optional customization of the background color and corner radius.
     *
     * The blur effect will not be applied if a custom theme is applied or if the "reduce transparency"
     * accessibility setting is enabled.
     *
     * @param view The view to apply the blur effect to.
     * @param blurMode The blur mode to use. Must be one of [BLUR_MODE_WINDOW],
     *   [BLUR_MODE_WINDOW_CAPTURED], or [BLUR_MODE_CANVAS].
     * @param colorCurvePreset The preset for the color curve of the blur effect.
     *   Refer to constants like [BLUR_UI_HIGH_ULTRA_THICK_DARK] for available presets.
     * @param color Optional: The background color for the blur effect. If null, a default
     *   color based on the will might be used.
     * @param cornerRadius Optional: The corner radius for the blurred background. If null,
     *   a default corner radius might be used.
     * @return `true` if the blur effect was successfully applied, `false` otherwise (e.g., if
     *   themes are applied, reduce transparency is enabled, or the blur builder creation failed).
     */
    @JvmStatic
    @JvmOverloads
    @RequiresApi(35)
    fun setBlurEffectPreset(
        view: View,
        @SeslBlurMode blurMode: Int,
        colorCurvePreset: Int,
        @ColorInt color: Int? = null,
        cornerRadius: Float? = null
    ): Boolean {
        var blurBuilder: Any? = null
        val context = view.context
        if (isThemeApplied(context!!)
            || isReduceTransparencySettingsEnabled(context)
            || (SeslSemBlurInfoReflector.semCreateBlurBuilder(blurMode).also { blurBuilder = it }) == null
        ) {
            return false
        }

        SeslSemBlurInfoReflector.semSetBuilderColorCurvePreset(
            blurBuilder,
            colorCurvePreset
        )
        if (color != null) {
            SeslSemBlurInfoReflector.semSetBuilderBlurBackgroundColor(blurBuilder, color)
        }
        if (cornerRadius != null) {
            SeslSemBlurInfoReflector.semSetBuilderBlurBackgroundCornerRadius(
                blurBuilder,
                cornerRadius
            )
        }
        SeslSemBlurInfoReflector.semBuildSetBlurInfo(blurBuilder, view)
        return true
    }

    /**
     * Checks if the blur effect preset functionality is supported on the current device.
     *
     * This method verifies if the Android SDK version is 35 or higher and if the device
     * is running Samsung's One UI.
     *
     * @return `true` if blur effect presets are supported, `false` otherwise.
     */
    @JvmStatic
    @SuppressLint("AnnotateVersionCheck")
    fun isBlurEffectPresetSupport(): Boolean = Build.VERSION.SDK_INT >= 35 && DeviceInfo.isOneUI()

    /**
     * Clears any blur information previously set on the given view.
     *
     * This function effectively removes any blur effect that was applied to the view
     * using [setBlurEffect] or [setBlurEffectPreset].
     *
     * @param view The view from which to clear the blur information.
     */
    @JvmStatic
    fun setBlurInfoClear(view: View) {
        SeslViewReflector.semSetBlurInfo(view, null)
    }


}