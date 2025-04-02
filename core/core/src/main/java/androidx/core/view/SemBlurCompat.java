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

package androidx.core.view;

import android.content.Context;
import android.provider.Settings;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.reflect.provider.SeslSettingsReflector;
import androidx.reflect.view.SeslSemBlurInfoReflector;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */

//added in sesl7
public class SemBlurCompat {
    public static final int BLUR_MODE_CANVAS = 2;
    public static final int BLUR_MODE_WINDOW = 0;
    public static final int BLUR_MODE_WINDOW_CAPTURED = 1;

    @Retention(RetentionPolicy.SOURCE)
    public @interface SeslBlurMode {
    }

    private static boolean isReduceTransparencySettingsEnabled(Context context) {
        String field_A11Y_REDUCE_TRANSPARENCY = SeslSettingsReflector.SeslSystemReflector.getField_SEM_ACCESSIBILITY_REDUCE_TRANSPARENCY();
        return !field_A11Y_REDUCE_TRANSPARENCY.equals("not_supported")
                && Settings.System.getInt(context.getContentResolver(), field_A11Y_REDUCE_TRANSPARENCY, BLUR_MODE_WINDOW) == BLUR_MODE_WINDOW_CAPTURED;
    }

    public static boolean setBlurEffect(@NonNull View view, @ColorInt int color, int radius, int blurMode, float cornerRadius) {
        Context context = view.getContext();

        if (Settings.System.getString(context.getContentResolver(), "current_sec_active_themepackage") != null
                || isReduceTransparencySettingsEnabled(context)){
            return false;
        }

        Object blurBuilder = SeslSemBlurInfoReflector.semCreateBlurBuilder(blurMode);
        if (blurBuilder == null){
            return false;
        }

        SeslSemBlurInfoReflector.semSetBuilderBlurRadius(blurBuilder, radius);
        SeslSemBlurInfoReflector.semSetBuilderBlurBackgroundColor(blurBuilder, color);
        SeslSemBlurInfoReflector.semSetBuilderBlurBackgroundCornerRadius(blurBuilder, cornerRadius);
        SeslSemBlurInfoReflector.semBuildSetBlurInfo(blurBuilder, view);
        return true;
    }
}