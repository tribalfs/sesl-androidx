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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.picker.R;

/**
 * Represents a SeekBar for adjusting opacity.
 * This class extends SeekBar and provides functionality for setting the opacity level of a color.
 *
 * <p>The SeekBar displays a gradient representing the opacity range, from fully transparent to
 * fully opaque. The user can drag the thumb to select the desired opacity level.
 *
 * <p>The maximum value of the SeekBar is 255, corresponding to the alpha channel of a color.
 * The progress of the SeekBar directly maps to the alpha value.
 *
 * <p>This class is intended for internal use within the SeslColorPicker library.
 *
 * @hide
 */ /*
 * Original code by Samsung, all rights reserved to the original author.
 */
class SeslOpacitySeekBar extends SeekBar {
    private static final String TAG = "SeslOpacitySeekBar";
    private static final int SEEKBAR_MAX_VALUE = 255;
    private GradientDrawable mProgressDrawable;
    private final int[] mColors = {Color.WHITE, Color.BLACK};

    public SeslOpacitySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void init(Integer color) {
        setMax(SEEKBAR_MAX_VALUE);

        if (color != null) {
            initColor(color);
        }

        Context context = getContext();
        mProgressDrawable = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.sesl_color_picker_opacity_seekbar);
        setProgressDrawable(mProgressDrawable);

        setThumb(ContextCompat.getDrawable(context, R.drawable.sesl_color_picker_seekbar_cursor));
        setThumbOffset(0);
        setSplitTrack(false);
    }

    private void initColor(int color) {
        final float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        final int alpha = Color.alpha(color);
        mColors[0] = Color.HSVToColor(0, hsv);
        mColors[1] = Color.HSVToColor(255, hsv);

        setProgress(alpha);
    }

    void restoreColor(int color) {
        initColor(color);
        mProgressDrawable.setColors(mColors);
        setProgressDrawable(mProgressDrawable);
    }

    void changeColorBase(int color, int alpha) {
        if (mProgressDrawable != null) {
            color = ColorUtils.setAlphaComponent(color, 255);

            mColors[1] = color;
            mProgressDrawable.setColors(mColors);
            setProgressDrawable(mProgressDrawable);

            final float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            mColors[0] = Color.HSVToColor(0, hsv);
            mColors[1] = Color.HSVToColor(255, hsv);

            setProgress(alpha);
        }
    }
}
