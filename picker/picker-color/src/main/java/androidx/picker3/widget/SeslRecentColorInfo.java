/*
 * Copyright 2024 The Android Open Source Project
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

package androidx.picker3.widget;

import org.jspecify.annotations.Nullable;
import java.util.ArrayList;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */
/**
 * A utility class to manage recent color information for {@link SeslColorPicker}.
 *
 * <p>This class stores and manages the currently selected color, the new color being picked,
 * the current color before picking, and a list of recently used colors. It is used internally
 * by {@link SeslColorPicker} to provide recent color functionality.
 */
public class SeslRecentColorInfo {
    private Integer mSelectedColor = null;
    private Integer mCurrentColor = null;
    private Integer mNewColor = null;
    private final ArrayList<Integer> mRecentColorInfo = new ArrayList<>();

    ArrayList<Integer> getRecentColorInfo() {
        return mRecentColorInfo;
    }

    public void setCurrentColor(@Nullable Integer currentColor) {
        mCurrentColor = currentColor;
    }

    public @Nullable Integer getCurrentColor() {
        return mCurrentColor;
    }

    public void setNewColor(@Nullable Integer newColor) {
        mNewColor = newColor;
    }

    public @Nullable Integer getNewColor() {
        return mNewColor;
    }

    public @Nullable Integer getSelectedColor() {
        return mSelectedColor;
    }

    public void saveSelectedColor(int selectedColor) {
        mSelectedColor = selectedColor;
    }

    public void initRecentColorInfo(@Nullable int[] colorIntegerArray) {
        if (colorIntegerArray != null) {
            if (colorIntegerArray.length <= SeslColorPicker.RECENT_COLOR_SLOT_COUNT) {
                for (int selectedColor : colorIntegerArray) {
                    mRecentColorInfo.add(selectedColor);
                }
            } else {
                for (int i = 0; i < SeslColorPicker.RECENT_COLOR_SLOT_COUNT; i++) {
                    mRecentColorInfo.add(colorIntegerArray[i]);
                }
            }
        }
    }
}
