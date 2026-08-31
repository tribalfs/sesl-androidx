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

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.animation.Interpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.RestrictTo;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Immutable configuration for {@link SeslGoToTopController}, built via {@link Builder}.
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public final class SeslGoToTopConfig {

    private static final String TAG = "SeslGoToTopConfig";

    private Drawable mIconLight;
    private Drawable mIconDark;
    private Drawable mBackgroundLight;
    private Drawable mBackgroundDark;
    private Drawable mBackgroundBlur;
    private int mBackgroundColorBlur;

    private int mPaddingBottom;
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mDefaultPaddingBottom;
    private int mSize;
    private float mElevation;
    private int mOverlayFeatureHiddenHeightPx;
    private boolean mSizeChanged = false;
    private int mScrollToTopDurationMs;

    /** Returns the blur background drawable. */
    public Drawable getBackgroundBlur() {
        return mBackgroundBlur;
    }

    /** Returns the dark mode background drawable. */
    public Drawable getBackgroundDark() {
        return mBackgroundDark;
    }

    /** Returns the light mode background drawable. */
    public Drawable getBackgroundLight() {
        return mBackgroundLight;
    }

    /** Returns the elevation in pixels for the GoToTop button. */
    public float getElevation() {
        return mElevation;
    }

    /** Returns the bottom padding in pixels. */
    public int getPaddingBottom() {
        return mPaddingBottom;
    }

    /** Returns the left padding in pixels. */
    public int getPaddingLeft() {
        return mPaddingLeft;
    }

    /** Returns the right padding in pixels. */
    public int getPaddingRight() {
        return mPaddingRight;
    }

    /** Returns the size (width/height) of the button in pixels. */
    public int getSize() {
        return mSize;
    }

    /** Returns whether a default bottom padding is configured. */
    public boolean hasDefaultBottomPadding() {
        return mDefaultPaddingBottom != -1;
    }

    /** Returns default bottom padding in pixels. */
    public int getDefaultPaddingBottom() {
        return mDefaultPaddingBottom;
    }

    /** Returns hidden height threshold in pixels for overlay feature logic. */
    public int getOverlayFeatureHiddenHeightPx() {
        return mOverlayFeatureHiddenHeightPx;
    }

    /** Returns whether button size was changed. */
    public boolean isSizeChanged() {
        return mSizeChanged;
    }

    /** Returns scroll-to-top animation duration in milliseconds. */
    public int getScrollToTopDurationMs() {
        return mScrollToTopDurationMs;
    }

    /** Sets bottom padding in pixels. */
    public void setPaddingBottom(int paddingBottom) {
        mPaddingBottom = paddingBottom;
    }

    /** Sets left padding in pixels. */
    public void setPaddingLeft(int paddingLeft) {
        mPaddingLeft = paddingLeft;
    }

    /** Sets right padding in pixels. */
    public void setPaddingRight(int paddingRight) {
        mPaddingRight = paddingRight;
    }

    /** Sets overlay feature hidden height threshold in pixels. */
    public void setOverlayFeatureHiddenHeightPx(int height) {
        mOverlayFeatureHiddenHeightPx = height;
    }

    /** Sets size changed status flag. */
    public void setSizeChanged(boolean sizeChanged) {
        mSizeChanged = sizeChanged;
    }

    /** Sets elevation in pixels. */
    public void setElevation(float elevation) {
        mElevation = elevation;
    }

    /** Returns icon drawable matching light or dark theme. */
    public Drawable getIcon(boolean isLightTheme) {
        return isLightTheme ? mIconLight : mIconDark;
    }

    /**
     * Builder for {@link SeslGoToTopConfig}.
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public static final class Builder {
        private Drawable iconLight;
        private Drawable iconDark;
        private Drawable backgroundLight;
        private Drawable backgroundDark;
        private Drawable backgroundBlur;
        private int backgroundColorBlur = -1;
        private Interpolator fadeInInterpolator;
        private Interpolator fadeOutInterpolator;
        private int paddingBottom;
        private int paddingLeft;
        private int paddingRight;
        private int size;
        private float elevation;
        private int overlayFeatureHiddenHeightPx;
        private int scrollToTopDurationMs;

        /** Sets light mode icon drawable. */
        @NonNull
        public Builder setIconLight(@Nullable Drawable iconLight) {
            this.iconLight = iconLight;
            return this;
        }

        /** Sets dark mode icon drawable. */
        @NonNull
        public Builder setIconDark(@Nullable Drawable iconDark) {
            this.iconDark = iconDark;
            return this;
        }

        /** Sets light mode background drawable. */
        @NonNull
        public Builder setBackgroundLight(@Nullable Drawable backgroundLight) {
            this.backgroundLight = backgroundLight;
            return this;
        }

        /** Sets dark mode background drawable. */
        @NonNull
        public Builder setBackgroundDark(@Nullable Drawable backgroundDark) {
            this.backgroundDark = backgroundDark;
            return this;
        }

        /** Sets blur background drawable. */
        @NonNull
        public Builder setBackgroundBlur(@Nullable Drawable backgroundBlur) {
            this.backgroundBlur = backgroundBlur;
            return this;
        }

        /** Sets blur background color. */
        @NonNull
        public Builder setBackgroundColorBlur(@ColorInt int backgroundColorBlur) {
            this.backgroundColorBlur = backgroundColorBlur;
            return this;
        }

        /** Sets fade-in animator interpolator. */
        @NonNull
        public Builder setFadeInInterpolator(@Nullable Interpolator fadeInInterpolator) {
            this.fadeInInterpolator = fadeInInterpolator;
            return this;
        }

        /** Sets fade-out animator interpolator. */
        @NonNull
        public Builder setFadeOutInterpolator(@Nullable Interpolator fadeOutInterpolator) {
            this.fadeOutInterpolator = fadeOutInterpolator;
            return this;
        }

        /** Sets bottom padding in pixels. */
        @NonNull
        public Builder setPaddingBottom(int paddingBottom) {
            this.paddingBottom = paddingBottom;
            return this;
        }

        /** Sets left padding in pixels. */
        @NonNull
        public Builder setPaddingLeft(int paddingLeft) {
            this.paddingLeft = paddingLeft;
            return this;
        }

        /** Sets right padding in pixels. */
        @NonNull
        public Builder setPaddingRight(int paddingRight) {
            this.paddingRight = paddingRight;
            return this;
        }

        /** Sets button size in pixels. */
        @NonNull
        public Builder setSize(int size) {
            this.size = size;
            return this;
        }

        /** Sets button elevation in pixels. */
        @NonNull
        public Builder setElevation(float elevation) {
            this.elevation = elevation;
            return this;
        }

        /** Sets overlay feature hidden height threshold in pixels. */
        @NonNull
        public Builder setOverlayFeatureHiddenHeightPx(int overlayFeatureHiddenHeightPx) {
            this.overlayFeatureHiddenHeightPx = overlayFeatureHiddenHeightPx;
            return this;
        }

        /** Sets size changed flag. */
        @NonNull
        public Builder setSizeChanged(boolean sizeChanged) {
            return this;
        }

        /** Sets scroll-to-top animation duration in milliseconds. */
        @NonNull
        public Builder setScrollToTopDurationMs(int scrollToTopDurationMs) {
            this.scrollToTopDurationMs = scrollToTopDurationMs;
            return this;
        }

        /** Constructs and validates a {@link SeslGoToTopConfig} instance. */
        public SeslGoToTopConfig build() {
            if (iconLight == null || iconDark == null || backgroundLight == null
                    || backgroundDark == null || backgroundBlur == null) {
                Log.e(TAG, "All drawables must be provided");
                return null;
            }
            if (backgroundColorBlur == -1) {
                Log.e(TAG, "All colors must be provided");
                return null;
            }
            if (fadeInInterpolator == null || fadeOutInterpolator == null) {
                Log.e(TAG, "Fade interpolators must be provided");
                return null;
            }
            if (size <= 0) {
                Log.e(TAG, "size must be > 0");
                return null;
            }
            if (elevation < 0.0f) {
                Log.e(TAG, "elevation must be >= 0");
                return null;
            }

            SeslGoToTopConfig config = new SeslGoToTopConfig();
            config.mIconLight = iconLight;
            config.mIconDark = iconDark;
            config.mBackgroundLight = backgroundLight;
            config.mBackgroundDark = backgroundDark;
            config.mBackgroundBlur = backgroundBlur;
            config.mBackgroundColorBlur = backgroundColorBlur;
            config.mPaddingBottom = paddingBottom;
            config.mPaddingLeft = paddingLeft;
            config.mPaddingRight = paddingRight;
            config.mDefaultPaddingBottom = paddingBottom;
            config.mSize = size;
            config.mElevation = elevation;
            config.mOverlayFeatureHiddenHeightPx = overlayFeatureHiddenHeightPx;
            config.mSizeChanged = false;
            config.mScrollToTopDurationMs = scrollToTopDurationMs;
            return config;
        }
    }
}
