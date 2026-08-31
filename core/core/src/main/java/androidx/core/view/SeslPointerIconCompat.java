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

package androidx.core.view;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import androidx.annotation.RestrictTo;
import androidx.reflect.view.SeslPointerIconReflector;

//sesl9
/**
 * Compatibility class for accessing Samsung S-Pen stylus pointer icon types.
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public final class SeslPointerIconCompat {

    /** Pointer icon type for default stylus hover icon. */
    public static final int TYPE_SEM_STYLUS_DEFAULT =
            SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_DEFAULT();
    /** Pointer icon type for stylus scroll up indicator. */
    public static final int TYPE_SEM_STYLUS_SCROLL_UP =
            SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_SCROLL_UP();
    /** Pointer icon type for stylus scroll down indicator. */
    public static final int TYPE_SEM_STYLUS_SCROLL_DOWN =
            SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_SCROLL_DOWN();
    /** Pointer icon type for stylus scroll left indicator. */
    public static final int TYPE_SEM_STYLUS_SCROLL_LEFT =
            SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_SCROLL_LEFT();
    /** Pointer icon type for stylus scroll right indicator. */
    public static final int TYPE_SEM_STYLUS_SCROLL_RIGHT =
            SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_SCROLL_RIGHT();
    /** Pointer icon type for stylus pen selection. */
    public static final int TYPE_SEM_STYLUS_PEN_SELECT =
            SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_PEN_SELECT();
    /** Pointer icon type for stylus "more" indicator. */
    public static final int TYPE_SEM_STYLUS_MORE =
            SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_MORE();

    /**
     * Checks whether the given pointer icon ID is the default Samsung stylus icon.
     *
     * @param iconId the icon type integer to check
     * @return {@code true} if iconId matches {@link #TYPE_SEM_STYLUS_DEFAULT}, {@code false} otherwise
     */
    public static boolean isSemStylusDefault(int iconId) {
        return iconId == TYPE_SEM_STYLUS_DEFAULT;
    }

    private SeslPointerIconCompat() {
    }
}
