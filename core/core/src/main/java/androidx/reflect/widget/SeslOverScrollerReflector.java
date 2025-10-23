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

package androidx.reflect.widget;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;
import static androidx.reflect.DeviceInfo.isOneUI;

import android.os.Build;
import android.widget.OverScroller;

import androidx.annotation.RestrictTo;
import androidx.reflect.SeslBaseReflector;

import org.jspecify.annotations.NonNull;

import java.lang.reflect.Method;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */

@RestrictTo(LIBRARY_GROUP_PREFIX)
public class SeslOverScrollerReflector {
    private static final Class<?> mClass = OverScroller.class;

    private SeslOverScrollerReflector() {
    }

    public static void fling(@NonNull OverScroller overScroller, int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, boolean isSkipMove, float frameLatencyY) {
        if (isOneUI() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Method method = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_fling", Integer.TYPE, Integer.TYPE, Boolean.TYPE, Float.TYPE);
            if (method != null) {
                SeslBaseReflector.invoke(overScroller, method, velocityX, velocityY, isSkipMove, frameLatencyY);
                return;
            }
        }

        overScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
    }

    public static void fling2(@NonNull OverScroller overScroller, int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, boolean isSkipMove) {
        if (isOneUI() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Method method = SeslBaseReflector.getDeclaredMethod(mClass, "hidden_fling", Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Boolean.TYPE);
            if (method != null) {
                SeslBaseReflector.invoke(overScroller, method, startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, isSkipMove);
                return;
            }
        }

        overScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
    }

    /**
     * Enables or disables Samsung-specific smooth scrolling behavior on the given {@link OverScroller} instance.
     * <p>
     * On Samsung devices running One UI (Android Oreo/API 26 or higher), this method uses Java reflection
     * to invoke the hidden method {@code semSetSmoothScrollEnabled(boolean)} on the {@code OverScroller} class.
     * This method enables/disables temporarily boosting the CPU/GPU frequencies and scheduling when scrolling.
     *
     * @param overScroller the {@link OverScroller} object whose scrolling behavior should be adjusted
     * @param enabled      {@code true} to enable Samsung’s smooth scrolling optimization, {@code false} to disable it
     *
     */
    public static void setSmoothScrollEnabled(@NonNull OverScroller overScroller, boolean enabled) {
        if (isOneUI() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Method method = SeslBaseReflector.getDeclaredMethod(mClass, "semSetSmoothScrollEnabled", Boolean.TYPE);
            if (method != null) {
                SeslBaseReflector.invoke(overScroller, method, enabled);
            }
        }
    }
}
