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

package androidx.core.widget;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import androidx.annotation.RestrictTo;
import androidx.core.view.SemBlurCompat;

//sesl9
/**
 * Controller for managing dynamic blur effects on views on One UI devices.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public class SeslBlurController {
    private boolean mIsBlurEnabled = false;
    private static final SemBlurCompat.CurveParameter LIGHT_THICK = new SemBlurCompat.CurveParameter(300, 0.4f, 15.0f, 15.0f, 235.0f, 176.7f, 253.2f);
    private static final SemBlurCompat.CurveParameter DARK_THICK = new SemBlurCompat.CurveParameter(300, 0.5f, -15.0f, 0.0f, 255.0f, 33.8f, 153.7f);

    /**
     * Provider interface for background drawables and parameters used when applying blur.
     */
    public interface BackgroundProvider {
        /** Returns background drawable for blurred state. */
        Drawable getBackgroundBlur();

        /** Returns background drawable for dark mode non-blurred state. */
        Drawable getBackgroundDark();

        /** Returns background drawable for light mode non-blurred state. */
        Drawable getBackgroundLight();

        /** Returns view elevation in pixels. */
        float getElevation();

        /** Returns view alpha for opaque non-blurred state. */
        float getOpaqueAlphaWithoutBlur();
    }

    private boolean applyBlur(View view, boolean isLightTheme) {
        return SemBlurCompat.setBlurEffectPreset(
                view,
                SemBlurCompat.BLUR_MODE_CANVAS,
                isLightTheme ? LIGHT_THICK : DARK_THICK,
                null,
                null,
                SemBlurCompat.CANVAS_BLUR_USE_TYPE_DYNAMIC
        );
    }

    private void disableBlurEffect(View view, boolean isLightTheme, BackgroundProvider provider) {
        clearBlur(view);
        setSolidBackground(view, isLightTheme, provider);
    }

    private boolean enableBlurEffect(View view, boolean isLightTheme, BackgroundProvider provider) {
        if (!applyBlur(view, isLightTheme)) {
            return false;
        }
        setBlurBackground(view, provider);
        return true;
    }

    private void setBlurBackground(View view, BackgroundProvider provider) {
        view.setBackground(provider.getBackgroundBlur());
        view.setElevation(provider.getElevation());
        view.setClipToOutline(true);
        view.setBackgroundTintList(ColorStateList.valueOf(0));
    }

    private void setSolidBackground(View view, boolean isLightTheme, BackgroundProvider provider) {
        view.setBackground(isLightTheme ? provider.getBackgroundLight() : provider.getBackgroundDark());
        view.setElevation(provider.getElevation());
        view.setBackgroundTintList(null);
        view.setAlpha(provider.getOpaqueAlphaWithoutBlur());
    }

    /** Clears blur effect from the view. */
    public void clearBlur(View view) {
        SemBlurCompat.setBlurInfoClear(view);
        mIsBlurEnabled = false;
    }

    /** Returns whether blur effect is currently enabled. */
    public boolean isBlurEnabled() {
        return mIsBlurEnabled;
    }

    /** Internal helper to enable or disable blur effect on the view using the provider. */
    public void setBlurEnabledInternal(View view, boolean enabled, boolean isLightTheme, BackgroundProvider provider) {
        if (Build.VERSION.SDK_INT >= 35 && enabled && enableBlurEffect(view, isLightTheme, provider)) {
            mIsBlurEnabled = true;
        } else {
            disableBlurEffect(view, isLightTheme, provider);
            mIsBlurEnabled = false;
        }
    }
}
