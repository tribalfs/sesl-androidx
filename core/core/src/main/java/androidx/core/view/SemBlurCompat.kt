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
    const val BLUR_UI_HIGH_ULTRA_THICK_D = 130
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
            context.getContentResolver(),
            field_A11Y_REDUCE_TRANSPARENCY,
            BLUR_MODE_WINDOW
        ) == BLUR_MODE_WINDOW_CAPTURED
    }


    private fun isThemeApplied(context: Context): Boolean {
        return Settings.System.getString(context.contentResolver, "current_sec_active_themepackage") != null
    }

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

    @JvmStatic
    @SuppressLint("AnnotateVersionCheck")
    fun isBlurEffectPresetSupport(): Boolean = Build.VERSION.SDK_INT >= 35

    @JvmStatic
    fun setBlurInfoClear(view: View) {
        SeslViewReflector.semSetBlurInfo(view, null)
    }


}