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
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.RestrictTo
import androidx.core.oneui.OneUI
import androidx.core.view.SemBlurCompat.BLUR_MODE_CANVAS
import androidx.core.view.SemBlurCompat.BLUR_MODE_WINDOW
import androidx.core.view.SemBlurCompat.BLUR_MODE_WINDOW_CAPTURED
import androidx.core.view.SemBlurCompat.BLUR_UI_HIGH_ULTRA_THICK_DARK
import androidx.core.view.SemBlurCompat.CANVAS_BLUR_USE_TYPE_DYNAMIC
import androidx.core.view.SemBlurCompat.CANVAS_BLUR_USE_TYPE_STATIC
import androidx.core.view.SemBlurCompat.setBlurEffect
import androidx.core.view.SemBlurCompat.setBlurEffectPreset
import androidx.reflect.DeviceInfo
import androidx.reflect.feature.SeslFloatingFeatureReflector
import androidx.reflect.feature.SeslFloatingFeatureReflector.SURFACE_TRANSITION_FLAG
import androidx.reflect.os.SeslSystemPropertiesReflector
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
    private const val TAG = "SemBlurCompat"

    const val BLUR_MODE_WINDOW = 0
    const val BLUR_MODE_WINDOW_CAPTURED = 1
    const val BLUR_MODE_CANVAS = 2

    //Sesl9
    const val CANVAS_BLUR_USE_TYPE_STATIC = 0
    const val CANVAS_BLUR_USE_TYPE_DYNAMIC = 1

    @IntDef(
        CANVAS_BLUR_USE_TYPE_STATIC,
        CANVAS_BLUR_USE_TYPE_DYNAMIC,
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class SeslUseTypeCanvasBlur

    @IntDef(
        BLUR_MODE_CANVAS,
        BLUR_MODE_WINDOW,
        BLUR_MODE_WINDOW_CAPTURED,
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class SeslBlurMode
    //sesl9

    const val BLUR_BASE_OFFSET = 101
    const val BLUR_UI_HIGH_ULTRA_THICK_DARK = 130
    const val BLUR_UI_HIGH_ULTRA_THICK_LIGHT = 115
    const val BLUR_UI_LOW_ULTRA_THICK_DARK = 120
    const val BLUR_UI_LOW_ULTRA_THICK_LIGHT= 105
    const val BLUR_UI_MEDIUM_ULTRA_THICK_DARK = 125
    const val BLUR_UI_MEDIUM_ULTRA_THICK_LIGHT = 110

    //Sesl9
    /**
     * Parameters for a custom blur color curve, added in One UI 8.5.
     */
    data class CurveParameter(
        val blurRadius: Int,
        val saturation: Float,
        val curveLevel: Float,
        val curveMinX: Float,
        val curveMaxX: Float,
        val curveMinY: Float,
        val curveMaxY: Float,
    )

    @JvmField
    val DEBUG_BLUR_COMPAT_BLUR_FORCE_Off: Boolean = run {
        SeslSystemPropertiesReflector.getStringProperties("sesl.debug.blurcompat.blur_force_off")?.toIntOrNull() == 1
    }
    //sesl9

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

    //Sesl9
    private fun isCanvasBlurSupport(): Boolean {
        val supports3dSurfaceTransition = SeslFloatingFeatureReflector.getString(SURFACE_TRANSITION_FLAG)?.toBoolean() ?: false
        return supports3dSurfaceTransition && OneUI.isGreaterOrEqual(OneUI.Version.ONEUI_8_5)
    }

    private fun isNotBlurSupport(
        context: Context,
        @SeslBlurMode blurMode: Int,
        @SeslUseTypeCanvasBlur useTypeCanvasBlur: Int? = CANVAS_BLUR_USE_TYPE_STATIC,
    ): Boolean {
        if (DEBUG_BLUR_COMPAT_BLUR_FORCE_Off) {
            Log.d(TAG, "Force blur off")
            return true
        }

        val canvasBlurSupport = isCanvasBlurSupport()

        if (!isThemeApplied(context) && !isReduceTransparencySettingsEnabled(context)) {
            if (blurMode != BLUR_MODE_CANVAS) {
                return !canvasBlurSupport
            }

            if (useTypeCanvasBlur == null || useTypeCanvasBlur != CANVAS_BLUR_USE_TYPE_DYNAMIC) {
                return false
            }

            if (canvasBlurSupport) return false

        }

        return true
    }
    //sesl9

    /**
     * Sets a blur effect on the given view on a device running Samsung's One UI.
     *
     * This function allows applying a blur effect with specific parameters for blur mode,
     * color, radius, and corner radius.
     *
     * The blur effect will not be applied if a custom theme is applied or if the "reduce transparency and blur"
     * accessibility setting is enabled.
     *
     * @param view The view to apply the blur effect to.
     * @param blurMode The blur mode to use. Must be one of [BLUR_MODE_WINDOW],
     *   [BLUR_MODE_WINDOW_CAPTURED], or [BLUR_MODE_CANVAS].
     * @param color The background color for the blur effect.
     * @param radius The radius of the blur.
     * @param cornerRadius The corner radius for the blurred background.
     * @param useTypeCanvasBlur Whether the canvas blur should be dynamic, one of
     *   [CANVAS_BLUR_USE_TYPE_STATIC] or [CANVAS_BLUR_USE_TYPE_DYNAMIC].
     * @return `true` if the blur effect was successfully applied, `false` otherwise (e.g., if
     *   themes are applied, reduce transparency is enabled, or the blur builder creation failed).
     */
    @JvmStatic
    @JvmOverloads
    fun setBlurEffect(
        view: View,
        @SeslBlurMode blurMode: Int,
        @ColorInt color: Int,
        radius: Int,
        cornerRadius: Float,
        @SeslUseTypeCanvasBlur useTypeCanvasBlur: Int? = CANVAS_BLUR_USE_TYPE_STATIC,
    ): Boolean {
        val context = view.context

        if (isNotBlurSupport(context, blurMode, useTypeCanvasBlur)) {
            return false
        }

        val blurBuilder = SeslSemBlurInfoReflector.semCreateBlurBuilder(blurMode) ?: return false

        SeslSemBlurInfoReflector.semSetBuilderBlurRadius(blurBuilder, radius)
        SeslSemBlurInfoReflector.semSetBuilderBlurBackgroundColor(blurBuilder, color)
        SeslSemBlurInfoReflector.semSetBuilderBlurBackgroundCornerRadius(blurBuilder, cornerRadius)
        SeslSemBlurInfoReflector.semBuildSetBlurInfo(blurBuilder, view)
        return true
    }

    //sesl9
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
     * @param useTypeCanvasBlur Whether the canvas blur should be dynamic, one of
     *   [CANVAS_BLUR_USE_TYPE_STATIC] or [CANVAS_BLUR_USE_TYPE_DYNAMIC].
     * @return `true` if the blur effect was successfully applied, `false` otherwise (e.g., if
     *   themes are applied, reduce transparency is enabled, or the blur builder creation failed).
     */
    @JvmStatic
    @JvmOverloads
    fun setBlurEffectPreset(
        view: View,
        @SeslBlurMode blurMode: Int,
        colorCurvePreset: Int,
        @ColorInt color: Int? = null,
        cornerRadius: Float? = null,
        @SeslUseTypeCanvasBlur useTypeCanvasBlur: Int? = CANVAS_BLUR_USE_TYPE_STATIC,
    ): Boolean {
        var blurBuilder: Any? = null
        val context = view.context
        if (isNotBlurSupport(context, blurMode, useTypeCanvasBlur)
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
     * Sets a blur effect with a custom color curve on the given view on a device running
     * Samsung's One UI.
     *
     * This function allows applying a blur effect using explicit [CurveParameter] values
     * instead of an integer preset. It also allows optional customization of the background
     * color and corner radius.
     *
     * @param view The view to apply the blur effect to.
     * @param blurMode The blur mode to use. Must be one of [BLUR_MODE_WINDOW],
     *   [BLUR_MODE_WINDOW_CAPTURED], or [BLUR_MODE_CANVAS].
     * @param curveParameter The custom color curve parameters.
     * @param color Optional: The background color for the blur effect. If null, a default
     *   color might be used.
     * @param cornerRadius Optional: The corner radius for the blurred background. If null,
     *   a default corner radius might be used.
     * @param useTypeCanvasBlur Whether the canvas blur should be dynamic, one of
     *   [CANVAS_BLUR_USE_TYPE_STATIC] or [CANVAS_BLUR_USE_TYPE_DYNAMIC].
     * @return `true` if the blur effect was successfully applied, `false` otherwise.
     */
    @JvmStatic
    @JvmOverloads
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun setBlurEffectPreset(
        view: View,
        @SeslBlurMode blurMode: Int,
        curveParameter: CurveParameter,
        @ColorInt color: Int? = null,
        cornerRadius: Float? = null,
        @SeslUseTypeCanvasBlur useTypeCanvasBlur: Int? = CANVAS_BLUR_USE_TYPE_STATIC,
    ): Boolean {
        val context = view.context

        if (isNotBlurSupport(context, blurMode, useTypeCanvasBlur)) {
            return false
        }

        val blurBuilder = SeslSemBlurInfoReflector.semCreateBlurBuilder(blurMode)
            ?: return false

        SeslSemBlurInfoReflector.semSetBuilderBlurRadius(blurBuilder, curveParameter.blurRadius)
        SeslSemBlurInfoReflector.semSetColorCurve(
            blurBuilder,
            curveParameter.saturation,
            curveParameter.curveLevel,
            curveParameter.curveMinX,
            curveParameter.curveMaxX,
            curveParameter.curveMinY,
            curveParameter.curveMaxY,
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
     * This method verifies if the Android SDK version is 35 or higher and if the device
     * is running Samsung's One UI.
     *
     * This method verifies if the Android SDK version is 35 or higher.
     *
     * @return `true` if blur effect presets are supported, `false` otherwise.
     */
    @JvmStatic
    @SuppressLint("AnnotateVersionCheck")
    fun isBlurEffectPresetSupport(): Boolean = Build.VERSION.SDK_INT >= 35 && DeviceInfo.isOneUI() //custom

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