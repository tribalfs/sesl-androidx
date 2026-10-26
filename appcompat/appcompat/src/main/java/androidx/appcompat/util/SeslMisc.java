/*
 * Copyright (C) 2022 The Android Open Source Project
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

package androidx.appcompat.util;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.appcompat.R;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.AssetManagerCompat;

import java.io.File;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */

/**
 * Samsung Misc class.
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public class SeslMisc {

    private static final int EXTRA_BUILT_IN_DISPLAY = 1;

    /**
     * Returns whether the current theme is the default theme.
     *
     * @param context The Context to use.
     * @return True if the current theme is the default theme, false if a custom theme is applied.
     * Always returns true on non-oneui device.
     */
    public static boolean isDefaultTheme(@NonNull Context context) {
        return TextUtils.isEmpty(Settings.System.getString(context.getContentResolver(), "current_sec_active_themepackage"));
    }

    public static boolean isColorPaletteApplied(@NonNull Context context) {
        return Settings.System.getInt(context.getContentResolver(), "wallpapertheme_state", 0) == 1;
    }

    public static boolean isFlipCoverScreen(@NonNull Context context) {
        return ContextCompat.getDisplayOrDefault(context).getDisplayId() == EXTRA_BUILT_IN_DISPLAY;
    }

    public static boolean isLightTheme(@NonNull Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.isLightTheme, typedValue, true);
        return typedValue.data != 0;
    }

    public static boolean isOpenThemeApplied(@NonNull Context context) {
        return !TextUtils.isEmpty(Settings.System.getString(context.getContentResolver(), "current_sec_active_themepackage"));
    }

    public static boolean isOpenThemeAppliedAndThemeOverlay(@NonNull Context context) {
        return AssetManagerCompat.hasSamsungThemeOverlays(context.getResources())
                && isOpenThemeApplied(context);
    }

    public static boolean isOverlayThemeApplied(@NonNull Context context) {
        return isOpenThemeApplied(context) || isColorPaletteApplied(context);
    }

    private static boolean isThemeParkApplied(@NonNull Context context) {
        return new File("/data/overlays/themepark/state_applied.txt").exists();
    }
}
