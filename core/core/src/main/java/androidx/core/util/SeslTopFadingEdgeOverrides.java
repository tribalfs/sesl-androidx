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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

//sesl9
/**
 * SESL-specific fading edge height and interpolator overrides for the top edge.
 */
public final class SeslTopFadingEdgeOverrides {
    /** An empty set of top fading edge overrides. */
    public static final SeslTopFadingEdgeOverrides EMPTY = new SeslTopFadingEdgeOverrides(null);

    /** Constant representing an unspecified fading edge height. */
    public static final int HEIGHT_UNSPECIFIED = -1;

    /** Slot index for standard top fading edge. */
    public static final int SLOT_TOP = 0;
    /** Slot index for extra top fading edge. */
    public static final int SLOT_TOP_EXTRA = 1;
    /** Slot index for top fading edge when status bar is present. */
    public static final int SLOT_TOP_WITH_STATUS_BAR = 2;
    /** Slot index for extra top fading edge when status bar is present. */
    public static final int SLOT_TOP_EXTRA_WITH_STATUS_BAR = 3;
    public static final int SLOT_COUNT = 4;

    private final SlotOverride[] mSlots;

    /** Annotation for top slot index parameters. */
    @IntDef({SLOT_TOP,
            SLOT_TOP_EXTRA,
            SLOT_TOP_WITH_STATUS_BAR,
            SLOT_TOP_EXTRA_WITH_STATUS_BAR,
            SLOT_COUNT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TopSlot {}

    /**
     * Builder for constructing a {@link SeslTopFadingEdgeOverrides} instance.
     */
    public static final class Builder {
        private final SlotOverride[] mSlots = new SlotOverride[SLOT_COUNT];

        /**
         * Builds and returns a new {@link SeslTopFadingEdgeOverrides} instance.
         *
         * @return the constructed overrides instance, or {@link #EMPTY} if no overrides were set
         */
        @NonNull
        public SeslTopFadingEdgeOverrides build() {
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
            return new SeslTopFadingEdgeOverrides(Arrays.copyOf(mSlots, SLOT_COUNT));
        }

        /** Sets override parameters for the standard top slot. */
        @NonNull
        public Builder setTop(@Nullable SlotOverride override) {
            mSlots[SLOT_TOP] = override;
            return this;
        }

        /** Sets override parameters for the extra top slot. */
        @NonNull
        public Builder setTopExtra(@Nullable SlotOverride override) {
            mSlots[SLOT_TOP_EXTRA] = override;
            return this;
        }

        /** Sets override parameters for the top slot with status bar. */
        @NonNull
        public Builder setTopWithStatusBar(@Nullable SlotOverride override) {
            mSlots[SLOT_TOP_WITH_STATUS_BAR] = override;
            return this;
        }

        /** Sets override parameters for the extra top slot with status bar. */
        @NonNull
        public Builder setTopExtraWithStatusBar(@Nullable SlotOverride override) {
            mSlots[SLOT_TOP_EXTRA_WITH_STATUS_BAR] = override;
            return this;
        }
    }

    /**
     * Configuration parameters for a specific top slot override.
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

            /** Reuses the interpolator from another top slot. */
            @NonNull
            public Builder setInterpolatorFrom(@TopSlot int slot) {
                if (slot < 0 || slot >= SLOT_COUNT) {
                    throw new IllegalArgumentException("Invalid top slot: " + slot);
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

    SeslTopFadingEdgeOverrides(SlotOverride[] slots) {
        mSlots = slots;
    }

    /** Returns the {@link SlotOverride} for the specified slot index, or {@code null} if unset. */
    @Nullable
    public SlotOverride getSlot(int slot) {
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
    public int resolveHeight(int slot, int defaultHeight) {
        SlotOverride override = getSlot(slot);
        return (override == null || !override.hasHeight()) ? defaultHeight : override.getHeightPx();
    }

    /** Resolves the interpolator control points for the specified slot. */
    public float @Nullable [] resolveInterpolator(int slot) {
        SlotOverride override = getSlot(slot);
        if (override != null) {
            float[] interpolator = override.getInterpolator();
            if (interpolator != null) {
                return interpolator;
            }
            int fromSlot = override.getInterpolatorFromSlot();
            if (fromSlot >= 0) {
                return SeslFadingEdgeShaderController.getDefaultTopInterpolator(fromSlot);
            }
        }
        return SeslFadingEdgeShaderController.getDefaultTopInterpolator(slot);
    }
}
