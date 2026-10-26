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

package androidx.reflect.feature;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.os.Build;

import androidx.annotation.RestrictTo;
import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Method;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */

@RestrictTo(LIBRARY_GROUP_PREFIX)
public class SeslFloatingFeatureReflector {
    public static final String SURFACE_TRANSITION_FLAG = "SEC_FLOATING_FEATURE_GRAPHICS_SUPPORT_3D_SURFACE_TRANSITION_FLAG";

    private static String mClassName;

    private SeslFloatingFeatureReflector() {
    }

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mClassName = "com.samsung.sesl.feature.SemFloatingFeature";
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mClassName = "com.samsung.android.feature.SemFloatingFeature";
        } else {
            mClassName = "com.samsung.android.feature.FloatingFeature";
        }
    }

    private static Object getInstance() {
        Method method = SeslBaseReflector.getMethod(mClassName, "getInstance");
        if (method != null) {
            Object result = SeslBaseReflector.invoke(null, method);
            if (result.getClass().getName().equals(mClassName)) {
                return result;
            }
        }

        return null;
    }

    public static String getString(String tag, String defaultValue) {
        Object result = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Method method = SeslBaseReflector.getDeclaredMethod(mClassName, "hidden_getString", String.class, String.class);
            result = SeslBaseReflector.invoke(null, method, tag, defaultValue);
        } else {
            Object semFloatingFeature = getInstance();
            if (semFloatingFeature != null) {
                Method method = SeslBaseReflector.getMethod(mClassName, "getString", String.class, String.class);
                result = SeslBaseReflector.invoke(semFloatingFeature, method, tag, defaultValue);
            }
        }

        if (result instanceof String) {
            return (String) result;
        } else {
            return defaultValue;
        }
    }

    //sesl9
    /** Retrieves floating feature string value for tag. */
    public static String getString(String tag) {
        return getString(tag, null);
    }

    /** Retrieves floating feature boolean value for tag with default false. */
    public static boolean getBoolean(String tag) {
        return getBoolean(tag, false);
    }

    /** Retrieves floating feature boolean value for tag with specified default value. */
    public static boolean getBoolean(String tag, boolean defaultValue) {
        Object result = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Method method = SeslBaseReflector.getDeclaredMethod(mClassName, "hidden_getBoolean", String.class, boolean.class);
            result = SeslBaseReflector.invoke(null, method, tag, defaultValue);
        } else {
            Object semFloatingFeature = getInstance();
            if (semFloatingFeature != null) {
                Method method = SeslBaseReflector.getMethod(mClassName, "getBoolean", String.class, boolean.class);
                result = SeslBaseReflector.invoke(semFloatingFeature, method, tag, defaultValue);
            }
        }

        if (result instanceof Boolean) {
            return (Boolean) result;
        } else {
            return defaultValue;
        }
    }
}
