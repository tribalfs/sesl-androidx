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

package androidx.window.extensions.embedding;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.window.extensions.RequiresVendorApiLevel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * The attributes of the embedded Activity Window.
 */
public class WindowAttributes {

    /**
     * The dim effect is applying on the {@link ActivityStack} of the Activity window when
     * needed. If the {@link ActivityStack} is split and displayed side-by-side with another
     * {@link ActivityStack}, the dim effect is applying only on the {@link ActivityStack} of
     * the requested Activity.
     */
    @RequiresVendorApiLevel(level = 5)
    public static final int DIM_AREA_ON_ACTIVITY_STACK = 1;

    /**
     * The dim effect is applying on the area of the whole Task when needed. If the embedded
     * transparent activity is split and displayed side-by-side with another activity, the dim
     * effect is applying on the Task, which across over the two {@link ActivityStack}s.
     */
    @RequiresVendorApiLevel(level = 5)
    public static final int DIM_AREA_ON_TASK = 2;

    @IntDef({
            DIM_AREA_ON_ACTIVITY_STACK,
            DIM_AREA_ON_TASK
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface DimArea {
    }

    @DimArea
    private final int mDimArea;

    /**
     * The {@link WindowAttributes} constructor.
     *
     * @param dimArea the type of area that the dim layer is applying.
     */
    @RequiresVendorApiLevel(level = 5)
    public WindowAttributes(@DimArea int dimArea) {
        mDimArea = dimArea;
    }

    /**
     * Returns the {@link DimArea} to use when dim behind the Activity window is
     * needed.
     * @return The dim area.
     */
    @DimArea
    @RequiresVendorApiLevel(level = 5)
    public int getDimArea() {
        return mDimArea;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || !(obj instanceof WindowAttributes)) return false;
        final WindowAttributes other = (WindowAttributes) obj;
        return mDimArea == other.getDimArea();
    }

    @Override
    public int hashCode() {
        return Objects.hash(mDimArea);
    }

    @NonNull
    @Override
    public String toString() {
        return "dimArea=" + mDimArea;
    }
}
