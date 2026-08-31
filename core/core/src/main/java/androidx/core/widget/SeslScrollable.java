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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.graphics.Rect;

import androidx.annotation.RestrictTo;

//sesl9
/**
 * Common contract for SESL scrollable widgets that support go-to-top, hover scrolling
 * and fading edge coordination.
 */
public interface SeslScrollable {
    /** Forces the bottom fading edge height to a clamped value. */
    void seslForceBottomFadingEdgeClamped(int i);

    /** Forces the top fading edge height to a clamped value. */
    void seslForceTopFadingEdgeClamped(int clamped);

    /** Returns available drawing bounds for the widget. */
    Rect seslGetAvailableBounds();

    /** Returns current bottom padding applied to the GoToTop button position. */
    int seslGetGoToTopBottomPadding();

    /** Returns default bottom padding applied to the GoToTop button position. */
    int seslGetGoToTopDefaultBottomPadding();

    /** Hides the GoToTop button. */
    void seslHideGoToTop();

    /** Sets available drawing bounds for the widget. */
    void seslSetAvailableBounds(Rect rect);

    /** Sets available drawing bounds for the widget with an optional dispatchFakeScroll flag. */
    void seslSetAvailableBounds(Rect bounds, boolean dispatchFakeScroll);

    /** Sets vertical offset for the bottom scroll boundary. */
    void seslSetBottomScrollOffset(int offset);

    /** Sets bottom padding for positioning the GoToTop button. */
    void seslSetGoToTopBottomPadding(int padding);

    /** Sets whether GoToTop button display is suppressed. */
    void seslSetGoToTopSuppressed(boolean suppressed);

    /** Sets top padding for hover scroll trigger area. */
    void seslSetHoverBottomPadding(int padding);

    /** Sets bottom padding for hover scroll trigger area. */
    void seslSetHoverTopPadding(int padding);

    /** Sets bottom offset for vertical scrollbar positioning. */
    void seslSetScrollBarBottomOffset(int offset);

    /** Sets top offset for vertical scrollbar positioning. */
    void seslSetScrollBarTopOffset(int offset);

    /** Shows the GoToTop button. */
    void seslShowGoToTop();
}
