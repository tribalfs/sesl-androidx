/*
 * Copyright 2023 The Android Open Source Project
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

package androidx.coordinatorlayout.widget;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */

public interface AppBarLayoutBehavior {

    public static final int SESL_STATE_IDLE = 0;
    public static final int SESL_STATE_EXPANDED = 1;
    public static final int SESL_STATE_COLLAPSED = 2;
    public static final int SESL_STATE_HIDE = 4;

    /**
     * Returns whether the {@link com.google.android.material.appbar.AppBarLayout} is collapsed.
     */
    boolean seslIsCollapsed();

    /**
     * Sets whether the AppBarLayout is expanded or not.
     *
     * @param expanded True if the AppBarLayout should be expanded, false otherwise.
     */
    void seslSetExpanded(boolean expanded);

    /**
     * Sets whether the current interaction is from a mouse.
     * This affects how the AppBarLayout behaves, especially in terms of drag sensitivity.
     *
     * @param isMouse {@code true} if the interaction is from a mouse, {@code false} otherwise.
     */
    void seslSetIsMouse(boolean isMouse);

    //Sesl9
    /**
     * Returns whether the AppBarLayout can change to hide state.
     */
    boolean seslCanChangeToHideState();

    /**
     * Returns the current state of the AppBarLayout.
     */
    int seslGetCurrentAppBarState();

    /**
     * Returns whether the AppBarLayout is hidden.
     */
    boolean seslIsHided();

    /**
     * Hides the AppBarLayout.
     */
    void seslSetHide();

    /**
     * Returns whether the floating toolbar behavior is enabled.
     */
    boolean useFloatingToolbar();
    //sesl9
}
