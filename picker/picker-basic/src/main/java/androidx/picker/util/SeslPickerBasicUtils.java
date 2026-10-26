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

package androidx.picker.util;

import android.content.Context;
import android.graphics.Typeface;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.util.SeslMisc;

public class SeslPickerBasicUtils {
    private static final String TAG = "SeslPickerBasicUtils";

    public static Typeface getOpenThemeTypeface(Context context) {
        String fontPath = Settings.System.getString(context.getContentResolver(), "theme_font_clock");
        if (fontPath != null) {
            try {
                if (!TextUtils.isEmpty(fontPath) && SeslMisc.isLightTheme(context)) {
                    return Typeface.createFromFile(fontPath);
                }
            } catch (Exception e) {
                Log.e(TAG, "Open Theme Font not found");
            }
        }
        return null;
    }
}
