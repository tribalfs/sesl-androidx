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

package androidx.core.widget;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.RestrictTo;
import androidx.reflect.feature.SeslFloatingFeatureReflector;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;
import static androidx.reflect.feature.SeslFloatingFeatureReflector.SURFACE_TRANSITION_FLAG;

/**
 * Animation configuration constants for the GoToTop button.
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public class SeslGoToTopAnimationConfig {
    public static final int ALPHA_DURATION = 150;
    public static final int ALPHA_DURATION_SHORT = 80;
    public static final Interpolator ALPHA_INTERPOLATOR = new LinearInterpolator();
    public static final float ALPHA_OPAQUE = 1.0f;
    public static final float ALPHA_OPAQUE_WITHOUT_BLUR = 0.9f;
    public static final float ALPHA_TRANSPARENT = 0.0f;
    public static final float SCALE_MAX = 1.0f;
    public static final float SCALE_MIN = 0.94f;
    public static final int SPRING_ANIMATION_SCALE_FACTOR = 10000;
    public static final float SPRING_DAMPING_RATIO = 1.0f;
    public static final int SPRING_STIFFNESS = 361;

    /** Returns the alpha interpolator. */
    public static Interpolator getAlphaInterpolator() {
        return ALPHA_INTERPOLATOR;
    }

    /** Returns animation duration in milliseconds. */
    public static int getDuration() {
        return shouldShortDuration() ? ALPHA_DURATION_SHORT : ALPHA_DURATION;
    }

    /** Returns whether shortened animation duration should be used based on hardware features. */
    public static boolean shouldShortDuration() {
        String support3d = SeslFloatingFeatureReflector.getString(SURFACE_TRANSITION_FLAG);
        return "false".equalsIgnoreCase(support3d != null ? support3d : "false");
    }
}
