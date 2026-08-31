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

package androidx.core.util;

import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.RestrictTo;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

//sesl9

/**
 * Contract for AGSL-based fading edge rendering used by SESL scrollable widgets.
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public interface SeslFadingEdgeHelper {

    /**
     * Provides scroll information to {@link SeslFadingEdgeHelperImpl} for computing
     * fading edge heights.
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    interface ScrollInfoProvider {

        /** Computes the vertical scroll extent of the target view. */
        int computeVerticalScrollExtent();

        /** Computes the vertical scroll offset of the target view. */
        int computeVerticalScrollOffset();

        /** Computes the vertical scroll range of the target view. */
        int computeVerticalScrollRange();

        /** Returns the visible height ratio of the last item, or -1.0f if unsupported. */
        default float getLastItemHeightVisibleRatio() {
            return -1.0f;
        }

        /** Returns whether fading edge height should be normalized. */
        boolean shouldNormalizeFadingEdge();

        /** Returns whether fading edge height should be normalized based on scroll distance. */
        default boolean shouldNormalizeFadingEdgeForDistance() {
            return false;
        }
    }

    /** Forces the bottom fading edge height to a clamped value. */
    void forceBottomFadingEdgeClamped(int i);

    /** Forces the top fading edge height to a clamped value. */
    void forceTopFadingEdgeClamped(int height);

    /** Returns the bottom offset for the fading edge. */
    int getFadingEdgeBottomOffset();

    /** Hides or shows the bottom fading edge. */
    void hideBottomFadingEdge(boolean z);

    /** Hides or shows the top fading edge. */
    void hideTopFadingEdge(boolean hide);

    /** Returns whether fading edge rendering is enabled. */
    boolean isFadingEdgeEnabled();

    /** Prepares canvas layer clipping/saving before drawing fading edge effect. */
    void prepareFadingEffect(Canvas canvas, int left, int top, int right, int bottom);

    /** Renders the fading edge effect onto the canvas using scroll info from the provider. */
    void renderFadingEffect(Canvas canvas, ScrollInfoProvider scrollInfoProvider);

    /** Sets whether top fading edge is allowed when edge-to-edge mode is not active. */
    void setAllowTopFadingEdgeWithoutEdgeToEdge(boolean allow);

    /** Sets bottom fading edge height and interpolator overrides. */
    void setBottomFadingEdgeOverrides(SeslBottomFadingEdgeOverrides seslBottomFadingEdgeOverrides);

    /** Sets the bottom offset for the fading edge. */
    void setFadingEdgeBottomOffset(int offset);

    /** Sets the color for fading edge rendering. */
    void setFadingEdgeColor(@ColorInt int color);

    /** Sets the color for fading edge rendering with an optional completion callback. */
    void setFadingEdgeColor(@ColorInt int color, Runnable runnable);

    /** Enables or disables fading edge rendering. */
    void setFadingEdgeEnabled(boolean enabled);

    /** Enables or disables fading edge rendering with specific top and bottom heights. */
    void setFadingEdgeEnabled(boolean enabled, int topHeight, int bottomHeight);

    /** Enables or disables fading edge rendering with specific heights and unused flag. */
    void setFadingEdgeEnabled(boolean enabled, int topHeight, int bottomHeight, boolean unused);

    /** Enables or disables fading edge rendering with top/bottom extend flags. */
    void setFadingEdgeEnabled(boolean enabled, boolean extendTop, boolean extendBottom);

    /** Sets whether legacy transfer mode should be forced. */
    void setForceLegacyXfermode(boolean force);

    /** Sets the target view for fading edge rendering. */
    void setTargetView(View view);

    /** Sets top fading edge height and interpolator overrides. */
    void setTopFadingEdgeOverrides(SeslTopFadingEdgeOverrides seslTopFadingEdgeOverrides);

    /** Sets whether the bottom fading edge should align to the window bottom. */
    void setWindowBottomAlignment(boolean align);
}
