/*
 * Copyright (C) 2026 The Android Open Source Project
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

package androidx.core.os;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;
import android.content.res.Configuration;
import androidx.annotation.RestrictTo;
import androidx.reflect.content.res.SeslConfigurationReflector;
import org.jspecify.annotations.NonNull;

//sesl9
/**
 * Compatibility class for querying Samsung-specific {@link Configuration} extensions.
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public final class SeslConfigurationCompat {

    private SeslConfigurationCompat() {
    }

    /**
     * Returns whether the given {@link Configuration} represents the main display.
     *
     * @param configuration the {@link Configuration} to query
     * @return {@code true} if the configuration is for the main display, {@code false} otherwise
     */
    public static boolean isMainDisplay(@NonNull Configuration configuration) {//sesl9
        Integer displayDeviceType =
                SeslConfigurationReflector.getField_semDisplayDeviceType(configuration);
        return displayDeviceType != null
                && displayDeviceType.equals(
                        SeslConfigurationReflector.getField_SEM_DISPLAY_DEVICE_TYPE_MAIN());
    }

    /**
     * Returns whether night mode is active in the given {@link Configuration}.
     *
     * @param configuration the {@link Configuration} to query
     * @return {@code true} if night mode is active, {@code false} otherwise
     */
    public static boolean isNightModeActive(@NonNull Configuration configuration) {//sesl9
        return (configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Returns whether the given {@link Configuration} represents the sub display.
     *
     * @param configuration the {@link Configuration} to query
     * @return {@code true} if the configuration is for the sub display, {@code false} otherwise
     */
    public static boolean isSubDisplay(@NonNull Configuration configuration) {//sesl9
        Integer displayDeviceType =
                SeslConfigurationReflector.getField_semDisplayDeviceType(configuration);
        return displayDeviceType != null
                && displayDeviceType.equals(
                        SeslConfigurationReflector.getField_SEM_DISPLAY_DEVICE_TYPE_SUB());
    }

    /**
     * Returns whether the given {@link Configuration} represents a Samsung pop-over window.
     *
     * @param configuration the {@link Configuration} to query
     * @return {@code true} if the configuration is a pop-over window, {@code false} otherwise
     */
    public static boolean semIsPopOver(@NonNull Configuration configuration) {
        return Boolean.TRUE.equals(SeslConfigurationReflector.getField_semIsPopOver(configuration));
    }
}
