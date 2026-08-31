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

package androidx.reflect.app;

import androidx.annotation.RestrictTo;
import androidx.reflect.SeslBaseReflector;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;

//sesl9
/**
 * Reflection wrapper for Samsung's {@code android.app.WindowConfiguration}.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public class SeslWindowConfigurationReflector {
    private static final String mClassName = "android.app.WindowConfiguration";

    private SeslWindowConfigurationReflector() {
    }

    /**
     * Checks whether the given window configuration represents an embedded window state.
     *
     * @param obj the WindowConfiguration object to query
     * @return {@code true} if embedded, {@code false} otherwise
     */
    public static boolean isEmbedded(@Nullable Object obj) {
        Method method = SeslBaseReflector.getMethod(mClassName, "isEmbedded");
        if (method != null) {
            Object result = SeslBaseReflector.invoke(obj, method);
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
        }
        return false;
    }
}
