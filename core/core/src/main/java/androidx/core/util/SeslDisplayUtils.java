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

package androidx.core.util;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;

import androidx.core.oneui.OneUI;
import androidx.reflect.content.res.SeslConfigurationReflector;

import org.jspecify.annotations.NonNull;

/**
 * Utility class for display-related queries, Dex/Desktop windowing checks, and edge panel settings.
 */
//sesl9
public class SeslDisplayUtils {
    /**
     * Display flag indicating an external desktop windowing display.
     */
    public static final int FLAG_EXTERNAL_DESKTOP_WINDOWING = 131072;
    private static final String TAG = "SeslDisplayUtils";

    /**
     * Gets the active edge area configuration value from system settings.
     *
     * @param context the {@link Context} to retrieve system settings from
     * @return the active edge area setting value, defaulting to 1
     */
    public static int getEdgeArea(@NonNull Context context) {
        return Settings.System.getInt(context.getContentResolver(), "active_edge_area", 1);
    }

    /**
     * Gets the pinned edge width from system settings.
     *
     * @param context the {@link Context} to retrieve system settings from
     * @return the pinned edge width, or 0 if the setting is not found
     */
    public static int getPinnedEdgeWidth(@NonNull Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(), "pinned_edge_width");
        } catch (Settings.SettingNotFoundException e) {
            Log.w(TAG, "Failed get EdgeWidth " + e);
            return 0;
        }
    }

    /**
     * Checks whether desktop windowing is active on any connected display.
     *
     * @param context the {@link Context} to access {@link DisplayManager}
     * @return {@code true} if desktop windowing is active on an external display, {@code false} otherwise
     */
    public static boolean isDesktopWindowing(@NonNull Context context) {
        for (Display display : ((DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE)).getDisplays()) {
            if ((display.getFlags() & FLAG_EXTERNAL_DESKTOP_WINDOWING) != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether Samsung DeX mode is enabled.
     * <p>
     * On One UI 8.0 and above, this checks whether desktop windowing is active on any connected
     * display; on earlier versions, it queries the configuration's DeX mode directly.
     *
     * @param context the {@link Context} to check DeX status
     * @return {@code true} if DeX mode is enabled, {@code false} otherwise
     */
    public static boolean isDexEnabled(@NonNull Context context) {
        return OneUI.isGreaterOrEqual(OneUI.Version.ONEUI_8_0)
                ? isDesktopWindowing(context)
                : SeslConfigurationReflector.isDexEnabled(context.getResources().getConfiguration());
    }

    /**
     * Checks whether the pinned edge panel mode is enabled.
     *
     * @param context the {@link Context} to retrieve system settings from
     * @return {@code true} if pin edge panel mode is enabled, {@code false} otherwise
     */
    public static boolean isPinEdgeEnabled(@NonNull Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(), "panel_mode", 0) == 1;
        } catch (Exception e) {
            Log.w(TAG, "Failed get panel mode " + e.toString());
            return false;
        }
    }
}
