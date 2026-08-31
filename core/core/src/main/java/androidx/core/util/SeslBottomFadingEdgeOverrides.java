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

package androidx.core.util;

import android.content.Context;

import androidx.annotation.IntDef;
import androidx.annotation.RestrictTo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

//sesl9
/**
 * SESL-specific fading edge height and interpolator overrides for the bottom edge.
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public final class SeslBottomFadingEdgeOverrides {
    /** An empty set of bottom fading edge overrides. */
    public static final SeslBottomFadingEdgeOverrides EMPTY = new SeslBottomFadingEdgeOverrides(null);

    /** Constant representing an unspecified fading edge height. */
    public static final int HEIGHT_UNSPECIFIED = -1;

    /** Slot index for standard bottom fading edge. */
    public static final int SLOT_BOTTOM = 0;
    /** Slot index for extra bottom fading edge. */
    public static final int SLOT_BOTTOM_EXTRA = 1;
    /** Slot index for bottom fading edge when navigation bar is present. */
    public static final int SLOT_BOTTOM_WITH_NAVI_BAR = 2;
    /** Slot index for bottom fading edge when taskbar is present. */
    public static final int SLOT_BOTTOM_WITH_TASK_BAR = 3;
    /** Slot index for extra bottom fading edge when navigation bar is present. */
    public static final int SLOT_BOTTOM_EXTRA_WITH_NAVI_BAR = 4;
    private static final int SLOT_COUNT = 5;

    private final SlotOverride[] mSlots;

    /** Annotation for bottom slot index parameters. */
    @IntDef({SLOT_BOTTOM, SLOT_BOTTOM_EXTRA, SLOT_BOTTOM_WITH_NAVI_BAR,
            SLOT_BOTTOM_WITH_TASK_BAR, SLOT_BOTTOM_EXTRA_WITH_NAVI_BAR,
            SLOT_COUNT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BottomSlot {}

    /**
     * Builder for constructing a {@link SeslBottomFadingEdgeOverrides} instance.
     */
    public static final class Builder {
        private final SlotOverride[] mSlots = new SlotOverride[SLOT_COUNT];

        /**
         * Builds and returns a new {@link SeslBottomFadingEdgeOverrides} instance.
         *
         * @return the constructed overrides instance, or {@link #EMPTY} if no overrides were set
         */
        @NonNull
        public SeslBottomFadingEdgeOverrides build() {
            boolean hasOverride = false;
            for (SlotOverride slot : mSlots) {
                if (slot != null) {
                    hasOverride = true;
                    break;
                }
            }
            if (!hasOverride) {
                return EMPTY;
            }
            return new SeslBottomFadingEdgeOverrides(Arrays.copyOf(mSlots, SLOT_COUNT));
        }

        /** Sets override parameters for the standard bottom slot. */
        @NonNull
        public Builder setBottom(@Nullable SlotOverride override) {
            mSlots[SLOT_BOTTOM] = override;
            return this;
        }

        /** Sets override parameters for the extra bottom slot. */
        @NonNull
        public Builder setBottomExtra(@Nullable SlotOverride override) {
            mSlots[SLOT_BOTTOM_EXTRA] = override;
            return this;
        }

        /** Sets override parameters for the bottom slot with navigation bar. */
        @NonNull
        public Builder setBottomWithNaviBar(@Nullable SlotOverride override) {
            mSlots[SLOT_BOTTOM_WITH_NAVI_BAR] = override;
            return this;
        }

        /** Sets override parameters for the bottom slot with taskbar. */
        @NonNull
        public Builder setBottomWithTaskBar(@Nullable SlotOverride override) {
            mSlots[SLOT_BOTTOM_WITH_TASK_BAR] = override;
            return this;
        }

        /** Sets override parameters for the extra bottom slot with navigation bar. */
        @NonNull
        public Builder setBottomExtraWithNaviBar(@Nullable SlotOverride override) {
            mSlots[SLOT_BOTTOM_EXTRA_WITH_NAVI_BAR] = override;
            return this;
        }
    }

    /**
     * Configuration parameters for a specific bottom slot override.
     */
    public static final class SlotOverride {
        private final int mHeightPx;
        private final float[] mInterpolator;
        private final int mInterpolatorFromSlot;

        SlotOverride(float @Nullable [] interpolator, int interpolatorFromSlot, int heightPx) {
            mInterpolator = interpolator;
            mInterpolatorFromSlot = interpolatorFromSlot;
            mHeightPx = heightPx;
        }

        /**
         * Builder for constructing a {@link SlotOverride}.
         */
        public static final class Builder {
            private float[] mInterpolator;
            private int mInterpolatorFromSlot = -1;
            private int mHeightPx = -1;

            /**
             * Builds and returns a new {@link SlotOverride}.
             *
             * @return the constructed {@link SlotOverride}
             * @throws IllegalStateException if interpolator control points or slot references are invalid
             */
            @NonNull
            public SlotOverride build() {
                if (mInterpolator != null && mInterpolator.length != 4) {
                    throw new IllegalStateException("Interpolator must have 4 control values");
                }
                if (mInterpolatorFromSlot != -1 && (mInterpolatorFromSlot < 0 || mInterpolatorFromSlot >= SLOT_COUNT)) {
                    throw new IllegalStateException("Invalid interpolatorFrom slot");
                }
                return new SlotOverride(mInterpolator, mInterpolatorFromSlot, mHeightPx);
            }

            /** Sets the height in pixels for this slot override. */
            @NonNull
            public Builder setHeight(int heightPx) {
                if (heightPx < 0) {
                    throw new IllegalArgumentException("height must be >= 0");
                }
                mHeightPx = heightPx;
                return this;
            }

            /** Sets the height from a dimension resource ID. */
            @NonNull
            public Builder setHeightResource(@NonNull Context context, int resId) {
                mHeightPx = context.getResources().getDimensionPixelSize(resId);
                return this;
            }

            /** Sets a cubic bezier interpolator using four control points. */
            @NonNull
            public Builder setInterpolator(float x1, float y1, float x2, float y2) {
                mInterpolator = new float[]{x1, y1, x2, y2};
                mInterpolatorFromSlot = -1;
                return this;
            }

            /** Reuses the interpolator from another bottom slot. */
            @NonNull
            public Builder setInterpolatorFrom(@BottomSlot int slot) {
                if (slot < 0 || slot >= SLOT_COUNT) {
                    throw new IllegalArgumentException("Invalid bottom slot: " + slot);
                }
                mInterpolatorFromSlot = slot;
                mInterpolator = null;
                return this;
            }
        }

        /** Returns the overridden height in pixels, or {@link #HEIGHT_UNSPECIFIED} if unset. */
        public int getHeightPx() {
            return mHeightPx;
        }

        /** Returns the custom interpolator control points array, or {@code null} if unset. */
        public float @Nullable [] getInterpolator() {
            return mInterpolator != null ? Arrays.copyOf(mInterpolator, mInterpolator.length) : null;
        }

        /** Returns the index of the slot whose interpolator should be reused, or -1 if unset. */
        public int getInterpolatorFromSlot() {
            return mInterpolatorFromSlot;
        }

        /** Returns whether a height override is set. */
        public boolean hasHeight() {
            return mHeightPx >= 0;
        }

        /** Returns whether an interpolator override is set. */
        public boolean hasInterpolator() {
            return mInterpolator != null || mInterpolatorFromSlot >= 0;
        }
    }

    SeslBottomFadingEdgeOverrides(SlotOverride[] slots) {
        mSlots = slots;
    }

    /** Returns the {@link SlotOverride} for the specified slot index, or {@code null} if unset. */
    @Nullable
    public SlotOverride getSlot(@BottomSlot int slot) {
        if (mSlots == null || slot < 0 || slot >= SLOT_COUNT) {
            return null;
        }
        return mSlots[slot];
    }

    /** Returns whether no slot overrides are configured. */
    public boolean isEmpty() {
        return mSlots == null;
    }

    /** Resolves the height for the specified slot, falling back to [defaultHeight] if unset. */
    public int resolveHeight(@BottomSlot int slot, int defaultHeight) {
        SlotOverride override = getSlot(slot);
        return (override == null || !override.hasHeight()) ? defaultHeight : override.getHeightPx();
    }

    /** Resolves the interpolator control points for the specified slot. */
    public float @Nullable [] resolveInterpolator(@BottomSlot int slot) {
        SlotOverride override = getSlot(slot);
        if (override != null) {
            float[] interpolator = override.getInterpolator();
            if (interpolator != null) {
                return interpolator;
            }
            int fromSlot = override.getInterpolatorFromSlot();
            if (fromSlot >= 0) {
                return SeslFadingEdgeShaderController.getDefaultBottomInterpolator(fromSlot);
            }
        }
        return SeslFadingEdgeShaderController.getDefaultBottomInterpolator(slot);
    }
}
