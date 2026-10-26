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

package androidx.reflect.graphics;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.RestrictTo;
import androidx.reflect.SeslBaseReflector;

import org.jspecify.annotations.NonNull;

import java.lang.reflect.Method;

//sesl9
/**
 * Reflection wrapper for unclipped layer operations on {@link Canvas}.
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public class SeslCanvasReflector {
    public static final Class<?> mClass = Canvas.class;

    private SeslCanvasReflector() {
    }

    /** Saves an unclipped canvas layer within specified bounds. */
    public static int saveUnclippedLayer(@NonNull Canvas canvas, int left, int top, int right, int bottom) {
        Method method = SeslBaseReflector.getDeclaredMethod(mClass, "saveUnclippedLayer",
                Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
        if (method != null) {
            Object result = SeslBaseReflector.invoke(canvas, method, left, top, right, bottom);
            if (result instanceof Integer) {
                return (Integer) result;
            }
        }
        return -1;
    }

    /** Restores a previously saved unclipped canvas layer using specified paint. */
    public static void restoreUnclippedLayer(@NonNull Canvas canvas, int saveCount, @NonNull Paint paint) {
        Method method = SeslBaseReflector.getDeclaredMethod(mClass, "restoreUnclippedLayer",
                Integer.TYPE, Paint.class);
        if (method != null) {
            SeslBaseReflector.invoke(canvas, method, saveCount, paint);
        }
    }
}
