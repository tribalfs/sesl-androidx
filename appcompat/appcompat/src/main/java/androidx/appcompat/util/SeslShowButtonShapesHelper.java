/*
 * Copyright (C) 2022 The Android Open Source Project
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

package androidx.appcompat.util;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.content.ContentResolver;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.view.ViewCompat;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */

/**
 * Helper class for implementing the Samsung "Show button shapes" feature.
 *
 * <p>This class assists in dynamically changing the background of a View based on the
 * "show_button_background" system setting. It allows specifying different drawables
 * for when the setting is enabled or disabled.</p>
 *
 * <p>Usage:
 * <pre>
 * {@code
 * SeslShowButtonShapesHelper helper = new SeslShowButtonShapesHelper(
 *     myButton,
 *     ContextCompat.getDrawable(getContext(), R.drawable.button_background_on),
 *     ContextCompat.getDrawable(getContext(), R.drawable.button_background_off)
 * );
 * helper.updateButtonBackground(); // Initial update
 *
 * // To listen for changes in the setting (optional, but recommended for dynamic updates):
 * ContentObserver observer = new ContentObserver(new Handler()) {
 *     @Override
 *     public void onChange(boolean selfChange) {
 *         helper.updateButtonBackground();
 *     }
 * };
 * getContentResolver().registerContentObserver(
 *     Settings.Global.getUriFor("show_button_background"),
 *     false,
 *     observer
 * );
 * }
 * </pre>
 * </p>
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public class SeslShowButtonShapesHelper {
    private final ContentResolver mContentResolver;
    private final View mView;
    private Drawable mBackgroundOn;
    private Drawable mBackgroundOff;

    public SeslShowButtonShapesHelper(@NonNull View view, @Nullable Drawable backgroundOn,
                                      @Nullable Drawable backgroundOff) {
        mView = view;
        mContentResolver = view.getContext().getContentResolver();
        mBackgroundOn = backgroundOn;
        mBackgroundOff = backgroundOff;
    }

    public void setBackgroundOff(@Nullable Drawable backgroundOff) {
        if (mBackgroundOn == null || mBackgroundOn != backgroundOff) {
            mBackgroundOff = backgroundOff;
        } else {
            Log.w("SeslSBBHelper", backgroundOff + "is same drawable with mBackgroundOn");
        }
    }

    public void setBackgroundOn(@Nullable Drawable backgroundOn) {
        mBackgroundOn = backgroundOn;
    }

    public void updateButtonBackground() {
        final boolean show = Settings.Global.getInt(mContentResolver, "show_button_background", 0) == 1;
        mView.setBackground(show ? mBackgroundOn : mBackgroundOff);
    }

    public void updateOverflowButtonBackground(Drawable backgroundOn) {
        mBackgroundOn = backgroundOn;
        updateButtonBackground();
    }
}
