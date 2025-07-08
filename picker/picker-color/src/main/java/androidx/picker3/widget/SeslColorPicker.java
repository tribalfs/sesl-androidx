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
import org.jspecify.annotations.NonNull;
import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;
import static android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK;
import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.RestrictTo;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.graphics.drawable.SeslRecoilDrawable;
import androidx.appcompat.util.SeslShapeDrawable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.picker.R;
import androidx.picker.eyeDropper.SeslEyeDropperActivity;

import com.google.android.material.tabs.TabLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Locale;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */

/**
 * SeslColorPicker is a widget for selecting a color.
 *
 * <p>It consists of a color spectrum view, a color swatch view,
 * a gradient color seek bar for saturation, an opacity seek bar,
 * and input fields for HEX and RGB color values.
 * It also displays a list of recently used colors.
 *
 * <p>The picker can operate in two modes, selectable via tabs:
 * <ul>
 *   <li><b>Spectrum Tab (Default):</b> Allows color selection from a continuous spectrum using
 *       {@link SeslColorSpectrumView} and a saturation seek bar.
 *   <li><b>Swatch Tab:</b> Allows color selection from a predefined set of swatches using
 *       {@link SeslColorSwatchView}.
 * </ul>
 */
public class SeslColorPicker extends LinearLayout {
    static int RECENT_COLOR_SLOT_COUNT = 6;

    private static final int CURRENT_COLOR_VIEW = 0;
    private static final int NEW_COLOR_VIEW = 1;
    private static final int RIPPLE_EFFECT_OPACITY = 61;

    String beforeValue;
    EditText mColorPickerBlueEditText;
    EditText mColorPickerGreenEditText;
    EditText mColorPickerHexEditText;
    EditText mColorPickerOpacityEditText;
    EditText mColorPickerRedEditText;
    EditText mColorPickerSaturationEditText;
    SeslColorSpectrumView mColorSpectrumView;
    private SeslColorSwatchView mColorSwatchView;
    final Context mContext;
    private GradientDrawable mCurrentColorBackground;
    private ImageView mCurrentColorView;
    SeslGradientColorSeekBar mGradientColorSeekBar;
    LinearLayout mGradientSeekBarContainer;
    OnColorChangedListener mOnColorChangedListener;
    ArrayList<EditText> editTexts = new ArrayList<>();
    private String[] mColorDescription = null;
    boolean mFlagVar;
    private final boolean mIsLightTheme;
    boolean mFromRecentLayoutTouch = false;
    boolean mIsInputFromUser = false;
    private boolean mIsOpacityBarEnabled = false;
    private boolean mIsSpectrumSelected = false;
    EditText mLastFocussedEditText;
    private final TabLayout.OnTabSelectedListener mOnTabSelectListener;
    private LinearLayout mOpacityLayout;
    SeslOpacitySeekBar mOpacitySeekBar;
    private FrameLayout mOpacitySeekBarContainer;
    PickedColor mPickedColor;
    private ImageView mPickedColorView;
    private final SeslRecentColorInfo mRecentColorInfo;
    LinearLayout mRecentColorListLayout;
    final ArrayList<Integer> mRecentColorValues;
    final Resources mResources;
    GradientDrawable mSelectedColorBackground;
    private final int[] mSmallestWidthDp = {320, 360, 411};
    FrameLayout mSpectrumViewContainer;
    FrameLayout mSwatchViewContainer;

    static final int MODE_SPECTRUM = 0;
    static final int MODE_SWATCH = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MODE_SPECTRUM, MODE_SWATCH})
    public @interface PickerMode {}

    int mTabIndex = MODE_SPECTRUM;
    private final TabLayout mTabLayoutContainer;
    boolean mTextFromRGB = false;
    boolean mfromEditText = false;
    boolean mfromRGB = false;
    boolean mfromSaturationSeekbar = false;
    private boolean mfromSpectrumTouch = false;

    //Sesl7
    final HorizontalScrollView mHorizontalScrollView;
    final AppCompatImageView mEyeDropperView;

    /**
     * Interface definition for a callback to be invoked when the eye dropper tool
     * is clicked by the user.
     *
     * @see #setOnEyeDropperListener(OnEyeDropperListener)
     * @see SeslEyeDropperActivity
     */
    public interface OnEyeDropperListener {
        void onEyeDropperClicked();
    }

    @Nullable
    private OnEyeDropperListener mOnEyeDropperListener;
    //sesl7

    private final View.OnClickListener mImageButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            mFromRecentLayoutTouch = true;
            int size = mRecentColorValues.size();
            if (mLastFocussedEditText != null) {
                mLastFocussedEditText.clearFocus();
            }

            try {
                ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(getWindowToken(), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (int i = 0; i < size && i < SeslColorPicker.RECENT_COLOR_SLOT_COUNT; i++) {
                if (mRecentColorListLayout.getChildAt(i).equals(view)) {
                    mIsInputFromUser = true;

                    int color = mRecentColorValues.get(i);
                    mPickedColor.setColor(color);
                    mapColorOnColorWheel(color);
                    updateHexAndRGBValues(color);

                    if (mGradientColorSeekBar != null) {
                        int progress = mGradientColorSeekBar.getProgress();
                        mColorPickerSaturationEditText.setText(
                                "" + String.format(Locale.getDefault(), "%d", progress));
                        mColorPickerSaturationEditText.setSelection(
                                String.valueOf(progress).length());
                    }

                    if (mOnColorChangedListener != null) {
                        mOnColorChangedListener.onColorChanged(color);
                    }
                }
            }
            mFromRecentLayoutTouch = false;
        }
    };


    public interface OnColorChangedListener {
        void onColorChanged(int color);
    }

    public SeslColorPicker(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);


        mContext = context;
        mResources = getResources();
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(androidx.appcompat.R.attr.isLightTheme, typedValue,
                true);
        mIsLightTheme = typedValue.data != 0;

        LayoutInflater.from(context).inflate(R.layout.sesl_color_picker_oneui_3_layout, this);
        mHorizontalScrollView = findViewById(R.id.horizontal_scroll_view);//sesl7
        mEyeDropperView = findViewById(R.id.sesl_eye_dropper);//sesl7

        mRecentColorInfo = new SeslRecentColorInfo();
        mRecentColorValues = mRecentColorInfo.getRecentColorInfo();

        mTabLayoutContainer = findViewById(R.id.sesl_color_picker_tab_layout);
        mTabLayoutContainer.seslSetSubTabStyle();
        setPickerMode(MODE_SPECTRUM);

        mOnTabSelectListener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mTabIndex = tab.getPosition();
                if (mTabIndex == MODE_SPECTRUM) {
                    mSwatchViewContainer.setVisibility(View.VISIBLE);
                    mSpectrumViewContainer.setVisibility(View.GONE);
                    if (mResources.getConfiguration().orientation != ORIENTATION_LANDSCAPE
                            || SeslColorPicker.isTablet(mContext)) {
                        mGradientSeekBarContainer.setVisibility(View.GONE);
                    } else {
                        mGradientSeekBarContainer.setVisibility(View.INVISIBLE);
                    }
                } else if (mTabIndex == MODE_SWATCH) {
                    initColorSpectrumView();
                    mSwatchViewContainer.setVisibility(View.GONE);
                    mSpectrumViewContainer.setVisibility(View.VISIBLE);
                    mGradientSeekBarContainer.setVisibility(View.VISIBLE);
                }

                if (mLastFocussedEditText != null) {
                    mLastFocussedEditText.clearFocus();
                }
                try {
                    ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(getWindowToken(), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

        };

        mPickedColor = new PickedColor();
        initDialogPadding();
        initCurrentColorView();
        initColorSwatchView();
        initGradientColorSeekBar();
        initColorSpectrumView();
        initOpacitySeekBar(mIsOpacityBarEnabled);
        initRecentColorLayout();
        initEyeDropperView();
        updateCurrentColor();
        setInitialColors();
        initCurrentColorValuesLayout();
    }

    /**
     * @param pickerMode Set to either {@link #MODE_SPECTRUM} or {@link #MODE_SWATCH}
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    public void setPickerMode(@PickerMode int pickerMode) {
        TabLayout.Tab tabAt = mTabLayoutContainer.getTabAt(pickerMode);
        if (tabAt != null) {
            mTabIndex = pickerMode;
            tabAt.select();
        }
    }


    /**
     * @return Either {@link #MODE_SPECTRUM} or {@link #MODE_SWATCH}
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    @PickerMode
    public int getPickerMode() {
        return mTabIndex;
    }

    /**
     * Configures the ColorPicker to only display the spectrum view.
     * This method hides the tab layout and swatch view, making the color spectrum
     * the sole method of color selection.
     *
     * <p>It also disables direct input into the HEX and RGB EditText fields by
     * setting their input type to {@link InputType#TYPE_NULL}.
     * </p>
     *
     * <p>Key actions performed:
     * <ul>
     *   <li>Hides the tab layout ({@code mTabLayoutContainer}).</li>
     *   <li>Initializes the color spectrum view ({@code mColorSpectrumView}).</li>
     *   <li>Ensures {@code mIsSpectrumSelected} is set to true.</li>
     *   <li>Hides the swatch view container ({@code mSwatchViewContainer}).</li>
     *   <li>Makes the spectrum view container ({@code mSpectrumViewContainer}) visible.</li>
     *   <li>Disables text input for HEX, Red, Green, and Blue EditText fields.</li>
     * </ul>
     * </p>
     */
    public void setOnlySpectrumMode() {
        mTabLayoutContainer.setVisibility(View.GONE);

        initColorSpectrumView();
        if (!mIsSpectrumSelected) {
            mIsSpectrumSelected = true;
        }

        mSwatchViewContainer.setVisibility(View.GONE);
        mSpectrumViewContainer.setVisibility(View.VISIBLE);

        mColorPickerHexEditText.setInputType(InputType.TYPE_NULL);
        mColorPickerRedEditText.setInputType(InputType.TYPE_NULL);
        mColorPickerBlueEditText.setInputType(InputType.TYPE_NULL);
        mColorPickerGreenEditText.setInputType(InputType.TYPE_NULL);
    }


    private void initDialogPadding() {
        if (mResources.getConfiguration().orientation == 1) {
            DisplayMetrics displayMetrics = mResources.getDisplayMetrics();
            float density = displayMetrics.density;
            if (density % 1.0f != 0.0f) {
                float widthPixels = displayMetrics.widthPixels;
                if (isContains((int) (widthPixels / density))) {
                    int dimensionPixelSize =
                            mResources.getDimensionPixelSize(
                                    R.dimen.sesl_color_picker_seekbar_width);
                    if (widthPixels < (mResources.getDimensionPixelSize(
                            R.dimen.sesl_color_picker_oneui_3_dialog_padding_left) * 2)
                            + dimensionPixelSize) {
                        int i = (int) ((widthPixels - dimensionPixelSize) / 2.0f);
                        findViewById(
                                R.id.sesl_color_picker_main_content_container).setPadding(i,
                                mResources.getDimensionPixelSize(
                                        R.dimen.sesl_color_picker_oneui_3_dialog_padding_top),
                                i,
                                mResources.getDimensionPixelSize(
                                        R.dimen.sesl_color_picker_oneui_3_dialog_padding_bottom)
                        );
                    }
                }
            }
        }
    }

    private boolean isContains(int value) {
        for (int i : mSmallestWidthDp) {
            if (i == value) {
                return true;
            }
        }
        return false;
    }

    private void initCurrentColorView() {
        mCurrentColorView = findViewById(R.id.sesl_color_picker_current_color_view);
        mPickedColorView = findViewById(R.id.sesl_color_picker_picked_color_view);
        mColorPickerOpacityEditText =
                findViewById(R.id.sesl_color_seek_bar_opacity_value_edit_view);
        mColorPickerSaturationEditText =
                findViewById(R.id.sesl_color_seek_bar_saturation_value_edit_view);
        mColorPickerOpacityEditText.setPrivateImeOptions("disableDirectWriting=true;");
        mColorPickerSaturationEditText.setPrivateImeOptions("disableDirectWriting=true;");
        mColorPickerOpacityEditText.setTag(1);
        mFlagVar = true;
        mSelectedColorBackground = (GradientDrawable) mPickedColorView.getBackground();

        final Integer color = mPickedColor.getColor();
        if (color != null) {
            mSelectedColorBackground.setColor(color);
        }

        mCurrentColorBackground = (GradientDrawable) mCurrentColorView.getBackground();
        mTabLayoutContainer.addOnTabSelectedListener(mOnTabSelectListener);
        mColorPickerOpacityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                int intValue;
                if (mOpacitySeekBar == null
                        || charSequence.toString().trim().isEmpty()
                        || (intValue = Integer.parseInt(charSequence.toString())) > 100
                        || !mIsInputFromUser) {
                    return;
                }
                mColorPickerOpacityEditText.setTag(0);
                mOpacitySeekBar.setProgress((intValue * 255) / 100);
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (mColorPickerOpacityEditText.getText().toString().startsWith("0")
                            && mColorPickerOpacityEditText.getText().length() > 1
                    ) {
                        mColorPickerOpacityEditText.setText(
                                "" + Integer.parseInt(
                                        mColorPickerOpacityEditText.getText().toString()));
                    } else if (Integer.parseInt(s.toString()) > 100) {
                        mColorPickerOpacityEditText.setText(
                                "" + String.format(Locale.getDefault(), "%d", 100));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                mColorPickerOpacityEditText.setSelection(
                        mColorPickerOpacityEditText.getText().length());
            }
        });

        mColorPickerOpacityEditText.setOnFocusChangeListener((view, z) -> {
            if (mColorPickerOpacityEditText.hasFocus()
                    || !mColorPickerOpacityEditText.getText().toString().isEmpty()) {
                return;
            }
            mColorPickerOpacityEditText.setText(
                    "" + String.format(Locale.getDefault(), "%d", 0));
        });

        mColorPickerOpacityEditText.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                        if (i == 5) {
                            mColorPickerHexEditText.requestFocus();
                            return true;
                        }
                        return false;
                    }
                });
    }

    private void initColorSwatchView() {
        mColorSwatchView = findViewById(R.id.sesl_color_picker_color_swatch_view);
        mSwatchViewContainer = findViewById(R.id.sesl_color_picker_color_swatch_view_container);
        mColorSwatchView.setOnColorSwatchChangedListener(
                new SeslColorSwatchView.OnColorSwatchChangedListener() {
                    @Override
                    public void onColorSwatchChanged(int i) {
                        mIsInputFromUser = true;
                        mColorSpectrumView.mFromSwatchTouch = true;

                        if (mLastFocussedEditText != null) {
                            mLastFocussedEditText.clearFocus();
                        }

                        try {
                            ((InputMethodManager) mContext.getSystemService(
                                    Context.INPUT_METHOD_SERVICE))
                                    .hideSoftInputFromWindow(getWindowToken(), 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        mPickedColor.setColorWithAlpha(i, mOpacitySeekBar.getProgress());
                        updateCurrentColor();
                        updateHexAndRGBValues(i);
                        mColorSpectrumView.mFromSwatchTouch = false;
                    }
                });
    }

    void initColorSpectrumView() {
        mColorSpectrumView = findViewById(R.id.sesl_color_picker_color_spectrum_view);
        mSpectrumViewContainer = findViewById(R.id.sesl_color_picker_color_spectrum_view_container);

        mColorPickerSaturationEditText.setText(
                "" + String.format(Locale.getDefault(), "%d", mGradientColorSeekBar.getProgress()));

        mColorSpectrumView.setOnSpectrumColorChangedListener((hue, saturation) -> {
            mIsInputFromUser = true;
            if (mLastFocussedEditText != null) {
                mLastFocussedEditText.clearFocus();
            }
            try {
                ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(getWindowToken(), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mPickedColor.setHS(hue, saturation, mOpacitySeekBar.getProgress());
            updateCurrentColor();
            updateHexAndRGBValues(mPickedColor.getColor());
        });

        mColorPickerSaturationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before,
                    int count) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                if (mTextFromRGB) return;
                try {
                    if (mGradientColorSeekBar != null && !s.toString().trim().isEmpty()) {
                        final int progress = Integer.parseInt(s.toString());
                        mfromEditText = true;
                        mFlagVar = false;
                        if (progress <= 100) {
                            mColorPickerSaturationEditText.setTag(0);
                            mGradientColorSeekBar.setProgress(progress);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mTextFromRGB) {
                    return;
                }
                try {
                    if (mColorPickerSaturationEditText.getText().toString().startsWith("0")
                            && mColorPickerSaturationEditText.getText().length() > 1) {
                        mColorPickerSaturationEditText.setText(
                                "" + Integer.parseInt(
                                        mColorPickerSaturationEditText.getText().toString()));
                    } else if (Integer.parseInt(editable.toString()) > 100) {
                        mColorPickerSaturationEditText.setText(
                                "" + String.format(Locale.getDefault(), "%d", 100));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                mColorPickerSaturationEditText.setSelection(
                        mColorPickerSaturationEditText.getText().length());
            }
        });

        mColorPickerSaturationEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (!mColorPickerSaturationEditText.hasFocus()
                    && mColorPickerSaturationEditText.getText().toString().isEmpty()) {
                mColorPickerSaturationEditText.setText(
                        "" + String.format(Locale.getDefault(), "%d", 0));
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initGradientColorSeekBar() {
        mGradientSeekBarContainer = findViewById(R.id.sesl_color_picker_saturation_layout);
        mGradientColorSeekBar = findViewById(R.id.sesl_color_picker_saturation_seekbar);
        mGradientColorSeekBar.init(mPickedColor.getColor());
        mGradientColorSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mIsInputFromUser = true;
                    mfromSaturationSeekbar = true;
                }

                final float value = (float) seekBar.getProgress() / (float) seekBar.getMax();

                mColorSpectrumView.setProgress(seekBar.getProgress());

                if (progress >= 0 && mFlagVar) {
                    mColorPickerSaturationEditText.setText(
                            "" + String.format(Locale.getDefault(), "%d", progress));
                    mColorPickerSaturationEditText.setSelection(String.valueOf(progress).length());
                }
                if (mfromRGB) {
                    mTextFromRGB = true;
                    mColorPickerSaturationEditText.setText(
                            "" + String.format(Locale.getDefault(), "%d", progress));
                    mColorPickerSaturationEditText.setSelection(String.valueOf(progress).length());
                    mTextFromRGB = false;
                }
                if (!mFromRecentLayoutTouch) {
                    mPickedColor.setV(value);
                }

                final int pickedIntegerColor = mPickedColor.getColor();
                if (mfromEditText) {
                    updateHexAndRGBValues(pickedIntegerColor);
                    mfromEditText = false;
                }
                if (mSelectedColorBackground != null) {
                    mSelectedColorBackground.setColor(pickedIntegerColor);
                }
                if (mOpacitySeekBar != null) {
                    mOpacitySeekBar.changeColorBase(pickedIntegerColor, mPickedColor.getAlpha());
                }

                if (mOnColorChangedListener != null) {
                    mOnColorChangedListener.onColorChanged(pickedIntegerColor);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mfromSaturationSeekbar = false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mLastFocussedEditText != null) {
                    mLastFocussedEditText.clearFocus();
                }
                try {
                    ((InputMethodManager) mContext.getSystemService(
                            Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindowToken(),
                            0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mGradientColorSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mFlagVar = true;
                int action = motionEvent.getAction();
                if (action == ACTION_DOWN) {
                    //Sesl7
                    if (mHorizontalScrollView != null) {
                        mHorizontalScrollView.requestDisallowInterceptTouchEvent(true);
                    }
                    //sesl7
                    mGradientColorSeekBar.setSelected(true);
                    return true;
                } else if (action == ACTION_UP || action == ACTION_CANCEL) {
                    mGradientColorSeekBar.setSelected(false);
                    return false;
                } else {
                    return false;
                }
            }
        });
        findViewById(R.id.sesl_color_picker_saturation_seekbar_container)
                .setContentDescription(
                        mResources.getString(R.string.sesl_color_picker_hue_and_saturation) +
                                ", " + mResources.getString(R.string.sesl_color_picker_slider) +
                                ", " + mResources.getString(
                                R.string.sesl_color_picker_double_tap_to_select));
    }


    @SuppressLint("ClickableViewAccessibility")
    public void initOpacitySeekBar(boolean enabled) {
        mOpacitySeekBar = findViewById(R.id.sesl_color_picker_opacity_seekbar);
        mOpacitySeekBarContainer = findViewById(R.id.sesl_color_picker_opacity_seekbar_container);
        mOpacityLayout = findViewById(R.id.sesl_color_picker_opacity_layout);
        if (enabled) {
            mOpacityLayout.setVisibility(View.VISIBLE);
        } else {
            mOpacityLayout.setVisibility(View.GONE);
        }
        if (!mIsOpacityBarEnabled) {
            mOpacitySeekBar.setVisibility(View.GONE);
            mOpacitySeekBarContainer.setVisibility(View.GONE);
        }
        mOpacitySeekBar.init(mPickedColor.getColor());
        mOpacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mIsInputFromUser = true;
                }
                mPickedColor.setAlpha(progress);

                if (progress >= 0
                        && Integer.parseInt(mColorPickerOpacityEditText.getTag().toString()) == 1) {
                    mColorPickerOpacityEditText.setText(
                            "" + String.format(Locale.getDefault(),
                                    "%d", (int) Math.ceil((progress * 100) / 255.0f)));
                }

                final Integer pickedIntegerColor = mPickedColor.getColor();
                if (pickedIntegerColor != null) {
                    if (mSelectedColorBackground != null) {
                        mSelectedColorBackground.setColor(pickedIntegerColor);
                    }
                    if (mOnColorChangedListener != null) {
                        mOnColorChangedListener.onColorChanged(pickedIntegerColor);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mLastFocussedEditText != null) {
                    mLastFocussedEditText.clearFocus();
                }
                try {
                    ((InputMethodManager) mContext.getSystemService(
                            Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindowToken(),
                            0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });
        mOpacitySeekBar.setOnTouchListener((view, event) -> {
            mColorPickerOpacityEditText.setTag(1);
            return event.getAction() == MotionEvent.ACTION_DOWN;
        });
        FrameLayout frameLayout = mOpacitySeekBarContainer;
        frameLayout.setContentDescription(mResources.getString(R.string.sesl_color_picker_opacity)
                + ", " + mResources.getString(R.string.sesl_color_picker_slider)
                + ", " + mResources.getString(R.string.sesl_color_picker_double_tap_to_select));
    }

    private void initCurrentColorValuesLayout() {
        mColorPickerHexEditText = findViewById(R.id.sesl_color_hex_edit_text);
        mColorPickerRedEditText = findViewById(R.id.sesl_color_red_edit_text);
        mColorPickerBlueEditText = findViewById(R.id.sesl_color_blue_edit_text);
        mColorPickerGreenEditText = findViewById(R.id.sesl_color_green_edit_text);
        mColorPickerRedEditText.setPrivateImeOptions("disableDirectWriting=true;");
        mColorPickerBlueEditText.setPrivateImeOptions("disableDirectWriting=true;");
        mColorPickerGreenEditText.setPrivateImeOptions("disableDirectWriting=true;");
        editTexts.add(mColorPickerRedEditText);
        editTexts.add(mColorPickerGreenEditText);
        editTexts.add(mColorPickerBlueEditText);
        editTexts.add(mColorPickerHexEditText);
        setTextWatcher();

        for (EditText editText : editTexts) {
            editText.setOnFocusChangeListener((view, z) -> {
                if (z) {
                    mLastFocussedEditText = editText;
                    mIsInputFromUser = true;
                }
            });
        }
        mColorPickerBlueEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == 6) {
                mColorPickerBlueEditText.clearFocus();
                return false;
            }
            return false;
        });
    }

    private void setTextWatcher() {
        mColorPickerHexEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final int hexColorLength = s.toString().trim().length();
                if (hexColorLength == 6) {
                    final int parseColor = Color.parseColor("#" + s);

                    if (!mColorPickerRedEditText.getText().toString().trim()
                            .equalsIgnoreCase("" + Color.red(parseColor))) {
                        mColorPickerRedEditText.setText("" + Color.red(parseColor));
                    }

                    if (!mColorPickerGreenEditText.getText().toString().trim()
                            .equalsIgnoreCase("" + Color.green(parseColor))) {
                        mColorPickerGreenEditText.setText("" + Color.green(parseColor));
                    }

                    if (!mColorPickerBlueEditText.getText().toString().trim()
                            .equalsIgnoreCase("" + Color.blue(parseColor))) {
                        mColorPickerBlueEditText.setText("" + Color.blue(parseColor));
                    }
                }
            }
        });

        beforeValue = "";

        for (int i = 0; i < editTexts.size() - 1; i++) {
            final EditText editText = editTexts.get(i);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int before, int count) {
                    beforeValue = s.toString().trim();
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    final String value = s.toString();
                    if (!value.equalsIgnoreCase(beforeValue) && value.trim().length() > 0) {
                        updateHexData();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    try {
                        if (Integer.parseInt(editable.toString()) > 255) {
                            if (editText == editTexts.get(0)) {
                                mColorPickerRedEditText.setText("255");
                            }
                            if (editText == editTexts.get(1)) {
                                mColorPickerGreenEditText.setText("255");
                            }
                            if (editText == editTexts.get(2)) {
                                mColorPickerBlueEditText.setText("255");
                            }
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        if (editText == editTexts.get(0)) {
                            mColorPickerRedEditText.setText("0");
                        }
                        if (editText == editTexts.get(1)) {
                            mColorPickerGreenEditText.setText("0");
                        }
                        if (editText == editTexts.get(2)) {
                            mColorPickerBlueEditText.setText("0");
                        }
                    }
                    mfromRGB = true;
                    mColorPickerRedEditText.setSelection(
                            mColorPickerRedEditText.getText().length());
                    mColorPickerGreenEditText.setSelection(
                            mColorPickerGreenEditText.getText().length());
                    mColorPickerBlueEditText.setSelection(
                            mColorPickerBlueEditText.getText().length());
                }
            });
        }
    }

    void updateHexData() {

        final int red =
                Integer.parseInt(!mColorPickerRedEditText.getText().toString().trim().isEmpty()
                        ? mColorPickerRedEditText.getText().toString().trim() : "0");
        final int green =
                Integer.parseInt(!mColorPickerGreenEditText.getText().toString().trim().isEmpty()
                        ? mColorPickerGreenEditText.getText().toString().trim() : "0");

        final int blue =
                Integer.parseInt(!mColorPickerBlueEditText.getText().toString().trim().isEmpty() ?
                        mColorPickerBlueEditText.getText().toString().trim() : "0");

        final int color = ((red & 255) << 16)
                | ((mOpacitySeekBar.getProgress() & 255) << 24)
                | ((green & 255) << 8)
                | (blue & 255);

        final String colorStr = String.format("%08x", color);
        mColorPickerHexEditText.setText(
                "" + colorStr.substring(2, colorStr.length()).toUpperCase(Locale.getDefault()));
        mColorPickerHexEditText.setSelection(mColorPickerHexEditText.getText().length());

        if (!mfromSaturationSeekbar && !mfromSpectrumTouch) {
            mapColorOnColorWheel(color);
        }

        if (mOnColorChangedListener != null) {
            mOnColorChangedListener.onColorChanged(color);
        }
    }

    void updateHexAndRGBValues(int color) {
        if (color != 0) {
            final String format = String.format("%08x", color);
            final String colorStr = format.substring(2);
            mColorPickerHexEditText.setText(colorStr.toUpperCase(Locale.getDefault()));
            mColorPickerHexEditText.setSelection(mColorPickerHexEditText.getText().length());

            final int parseColor = Color.parseColor("#" + colorStr);
            mColorPickerRedEditText.setText("" + Color.red(parseColor));
            mColorPickerBlueEditText.setText("" + Color.blue(parseColor));
            mColorPickerGreenEditText.setText("" + Color.green(parseColor));
        }
    }

    private void initRecentColorLayout() {
        mRecentColorListLayout = findViewById(R.id.sesl_color_picker_used_color_item_list_layout);
        mColorDescription = new String[]{mResources.getString(R.string.sesl_color_picker_color_one),
                mResources.getString(R.string.sesl_color_picker_color_two),
                mResources.getString(R.string.sesl_color_picker_color_three),
                mResources.getString(R.string.sesl_color_picker_color_four),
                mResources.getString(R.string.sesl_color_picker_color_five),
                mResources.getString(R.string.sesl_color_picker_color_six),
                mResources.getString(R.string.sesl_color_picker_color_seven)};

        final int emptyColor = ContextCompat.getColor(mContext,
                mIsLightTheme
                        ? R.color.sesl_color_picker_used_color_item_empty_slot_color_light
                        : R.color.sesl_color_picker_used_color_item_empty_slot_color_dark);

        if (mResources.getConfiguration().orientation == ORIENTATION_LANDSCAPE && !isTablet(
                mContext)) {
            RECENT_COLOR_SLOT_COUNT = 7;
        } else {
            RECENT_COLOR_SLOT_COUNT = 6;
        }

        for (int i = 0; i < RECENT_COLOR_SLOT_COUNT; i++) {
            View recentColorSlot = mRecentColorListLayout.getChildAt(i);
            setImageColor(recentColorSlot, emptyColor);
            recentColorSlot.setFocusable(false);
            recentColorSlot.setClickable(false);
        }
    }

    private void initEyeDropperView() {
        mEyeDropperView.setFocusable(true);
        mEyeDropperView.setClickable(true);
        if (Build.VERSION.SDK_INT >= 26) {
            mEyeDropperView.setTooltipText(
                    getResources().getString(R.string.sesl_color_picker_eye_dropper));
        }

        mEyeDropperView.setContentDescription(getResources().getString(R.string.sesl_color_picker_eye_dropper));
        setupEyeDropperBackground();
        mEyeDropperView.setOnClickListener(view -> {
            if (mOnEyeDropperListener != null) {
                mOnEyeDropperListener.onEyeDropperClicked();
            }

            if (mLastFocussedEditText != null) {
                mLastFocussedEditText.clearFocus();
            }
        });
    }

    public void updateRecentColorLayout() {
        final int size = mRecentColorValues != null
                ? mRecentColorValues.size() : 0;

        final String str = ", "
                + mResources.getString(R.string.sesl_color_picker_option);

        if (mResources.getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
            RECENT_COLOR_SLOT_COUNT = 7;
        } else {
            RECENT_COLOR_SLOT_COUNT = 6;
        }
        for (int i = 0; i < RECENT_COLOR_SLOT_COUNT; i++) {
            View recentColorSlot = mRecentColorListLayout.getChildAt(i);
            if (i < size) {
                final int color = mRecentColorValues.get(i);
                setImageColor(recentColorSlot, color);

                StringBuilder recentDescription = new StringBuilder();
                recentDescription.append(
                        mColorSwatchView.getColorSwatchDescriptionAt(color));
                recentDescription.insert(0,
                        mColorDescription[i] + str + ", ");
                recentColorSlot.setContentDescription(recentDescription);

                recentColorSlot.setFocusable(true);
                recentColorSlot.setClickable(true);
            }
        }

        if (mRecentColorInfo.getCurrentColor() != null) {
            final int currentColor = mRecentColorInfo.getCurrentColor();
            mCurrentColorBackground.setColor(currentColor);
            setCurrentColorViewDescription(currentColor, CURRENT_COLOR_VIEW);
            mSelectedColorBackground.setColor(currentColor);
            mapColorOnColorWheel(currentColor);
            updateHexAndRGBValues(currentColor);
        } else if (size != 0) {
            final int firstColor = mRecentColorValues.get(0);
            mCurrentColorBackground.setColor(firstColor);
            setCurrentColorViewDescription(firstColor, CURRENT_COLOR_VIEW);
            mSelectedColorBackground.setColor(firstColor);
            mapColorOnColorWheel(firstColor);
            updateHexAndRGBValues(firstColor);
        }

        if (mRecentColorInfo.getNewColor() != null) {
            final int newColor = mRecentColorInfo.getNewColor();
            mSelectedColorBackground.setColor(newColor);
            mapColorOnColorWheel(newColor);
            updateHexAndRGBValues(newColor);
        }
    }

    /**
     * Sets a listener to be notified when the selected color changes.
     *
     * @param listener The {@link OnColorChangedListener} to set, or {@code null}
     *                 to remove the current listener. The listener will be invoked
     *                 with the new color (as an integer) whenever the user selects
     *                 a different color in the picker.
     */
    public void setOnColorChangedListener(@Nullable OnColorChangedListener listener) {
        mOnColorChangedListener = listener;
    }

    private void setInitialColors() {
        final Integer pickedIntegerColor = mPickedColor.getColor();
        if (pickedIntegerColor != null) {
            mapColorOnColorWheel(pickedIntegerColor);
        }
    }

    void updateCurrentColor() {
        final Integer pickedIntegerColor = mPickedColor.getColor();
        if (pickedIntegerColor != null) {
            if (mOpacitySeekBar != null) {
                mOpacitySeekBar.changeColorBase(pickedIntegerColor, mPickedColor.getAlpha());

                final int progress = mOpacitySeekBar.getProgress();
                mColorPickerOpacityEditText.setText(
                        "" + String.format(Locale.getDefault(), "%d", progress));
                mColorPickerOpacityEditText.setSelection(String.valueOf(progress).length());
            }

            if (mSelectedColorBackground != null) {
                mSelectedColorBackground.setColor(pickedIntegerColor);
                setCurrentColorViewDescription(pickedIntegerColor, NEW_COLOR_VIEW);
            }

            if (mColorSpectrumView != null) {
                mColorSpectrumView.updateCursorColor(pickedIntegerColor);
                mColorSpectrumView.setColor(pickedIntegerColor);
            }

            if (mGradientColorSeekBar != null) {
                final int progress = mGradientColorSeekBar.getProgress();
                mGradientColorSeekBar.changeColorBase(pickedIntegerColor);
                mfromSpectrumTouch = true;
                mColorPickerSaturationEditText.setText(
                        "" + String.format(Locale.getDefault(), "%d", progress));
                mColorPickerSaturationEditText.setSelection(String.valueOf(progress).length());
                mfromSpectrumTouch = false;
            }

            if (mOnColorChangedListener != null) {
                mOnColorChangedListener.onColorChanged(pickedIntegerColor);
            }
        }
    }

    private void setImageColor(View button, Integer color) {
        GradientDrawable gradientDrawable
                = (GradientDrawable) AppCompatResources.getDrawable(
                mContext,
                mIsLightTheme
                        ? R.drawable.sesl_color_picker_used_color_item_slot_light
                        : R.drawable.sesl_color_picker_used_color_item_slot_dark);
        if (color != null) {
            gradientDrawable.setColor(color);
        }

        final int rippleColor = Color.argb(RIPPLE_EFFECT_OPACITY, 0, 0, 0);
        ColorStateList myList = new ColorStateList(new int[][]{new int[0]}, new int[]{rippleColor});
        button.setBackground(new RippleDrawable(myList, gradientDrawable, null));
        button.setOnClickListener(mImageButtonClickListener);
    }

    void mapColorOnColorWheel(int color) {
        mPickedColor.setColor(color);

        if (mColorSwatchView != null) {
            mColorSwatchView.updateCursorPosition(color);
        }

        if (mColorSpectrumView != null) {
            mColorSpectrumView.setColor(color);
        }

        if (mGradientColorSeekBar != null) {
            mGradientColorSeekBar.restoreColor(color);
        }

        if (mOpacitySeekBar != null) {
            mOpacitySeekBar.restoreColor(color);
        }

        if (mSelectedColorBackground != null) {
            mSelectedColorBackground.setColor(color);
            setCurrentColorViewDescription(color, 1);
        }
        if (mColorSpectrumView != null) {
            final float value = mPickedColor.getV();
            final int alpha = mPickedColor.getAlpha();
            mPickedColor.setV(1.0f);
            mPickedColor.setAlpha(255);
            mColorSpectrumView.updateCursorColor(mPickedColor.getColor());
            mPickedColor.setV(value);
            mPickedColor.setAlpha(alpha);
        }
        if (mOpacitySeekBar != null) {
            final int progress = (int) Math.ceil((mOpacitySeekBar.getProgress() * 100) / 255.0f);
            mColorPickerOpacityEditText.setText(String.format(Locale.getDefault(), "%d", progress));
            mColorPickerOpacityEditText.setSelection(String.valueOf(progress).length());
        }
    }

    private void setCurrentColorViewDescription(int color, int flag) {
        StringBuilder description = new StringBuilder();
        StringBuilder colorDescription = new StringBuilder();
        SeslColorSwatchView seslColorSwatchView = mColorSwatchView;
        if (seslColorSwatchView != null) {
            colorDescription = seslColorSwatchView.getColorSwatchDescriptionAt(color);
        }
        if (colorDescription != null) {
            description.append(", ");
            description.append(colorDescription);
        }
        switch (flag) {
            case CURRENT_COLOR_VIEW:
                description.insert(0,
                        mResources.getString(R.string.sesl_color_picker_current));
                break;
            case NEW_COLOR_VIEW:
                description.insert(0,
                        mResources.getString(R.string.sesl_color_picker_new));
                break;
        }
    }

    /**
     * Saves the currently selected color to the list of recent colors.
     * <p>
     * This method retrieves the color currently held by the {@link PickedColor} instance
     * (which reflects the user's latest selection in the picker) and, if a color is set,
     * it calls {@link SeslRecentColorInfo#saveSelectedColor(int)} to add it to the
     * persistent list of recently used colors. This list is typically displayed to the
     * user for quick re-selection.
     * </p>
     * <p>
     * It is recommended to call this method when the color selection is confirmed by the user,
     * for example, when a dialog containing the color picker is dismissed with a positive action.
     * </p>
     *
     * @see #getRecentColorInfo()
     * @see SeslRecentColorInfo#saveSelectedColor(int)
     */
    public void saveSelectedColor() {
        Integer color = mPickedColor.getColor();
        if (color != null) {
            mRecentColorInfo.saveSelectedColor(color);
        }
    }

    /**
     * Retrieves the {@link SeslRecentColorInfo} object associated with this color picker.
     * <p>
     * This object manages the list of recently used colors and the currently selected color.
     *
     * @return The {@link SeslRecentColorInfo} instance for this color picker.
     *         This method never returns {@code null}.
     */
    public @NonNull SeslRecentColorInfo getRecentColorInfo() {
        return mRecentColorInfo;
    }

    /**
     * Checks if the current color change was initiated by the user.
     * <p>
     * This method can be used to distinguish between color changes triggered by user
     * interaction (e.g., touching the spectrum, swatch, or typing in input fields)
     * and programmatic changes (e.g., setting an initial color or restoring state).
     *
     * @return {@code true} if the color was changed by user input, {@code false} otherwise.
     */
    public boolean isUserInputValid() {
        return mIsInputFromUser;
    }

    /**
     * Enables or disables the opacity seek bar.
     * <p>
     * When enabled, the opacity seek bar and its container become visible, allowing the user
     * to adjust the alpha (transparency) of the selected color.
     * When disabled, these views are hidden.
     * </p>
     *
     * @param enabled {@code true} to enable and show the opacity bar, {@code false} to disable
     *                and hide it.
     */
    public void setOpacityBarEnabled(boolean enabled) {
        mIsOpacityBarEnabled = enabled;
        if (enabled) {
            mOpacitySeekBar.setVisibility(View.VISIBLE);
            mOpacitySeekBarContainer.setVisibility(View.VISIBLE);
        }
    }

    public static class PickedColor {
        private Integer mColor = null;
        private int mAlpha = 255;
        private final float[] mHsv = new float[3];

        public void setColor(@NonNull Integer color) {
            mColor = color;
            mAlpha = Color.alpha(color);
            Color.colorToHSV(mColor, mHsv);
        }

        @Nullable
        public Integer getColor() {
            return mColor;
        }

        public void setColorWithAlpha(int i, int i2) {
            mColor = i;
            mAlpha = (int) Math.ceil((i2 * 100) / 255.0f);
            Color.colorToHSV(mColor, mHsv);
        }

        public void setHS(float f, float f2, int i) {
            float[] fArr = mHsv;
            fArr[0] = f;
            fArr[1] = f2;
            fArr[2] = 1.0f;
            mColor = Color.HSVToColor(mAlpha, fArr);
            mAlpha = (int) Math.ceil((i * 100) / 255.0f);
        }

        public void setV(float f) {
            float[] fArr = mHsv;
            fArr[2] = f;
            mColor = Color.HSVToColor(mAlpha, fArr);
        }

        public void setAlpha(int i) {
            mAlpha = i;
            mColor = Color.HSVToColor(i, mHsv);
        }

        public float getV() {
            return mHsv[2];
        }

        public int getAlpha() {
            return mAlpha;
        }
    }

    private static boolean isTablet(@NonNull Context context) {
        return (context.getResources().getConfiguration().screenLayout & SCREENLAYOUT_SIZE_MASK)
                >= SCREENLAYOUT_SIZE_LARGE;
    }

    //Sesl7
    private void setupEyeDropperBackground() {
        GradientDrawable seslShapeDrawable;
        if (Build.VERSION.SDK_INT >= 23) {
            seslShapeDrawable = new SeslShapeDrawable();
        } else {
            seslShapeDrawable = new GradientDrawable();
        }
        seslShapeDrawable.setColor(ContextCompat.getColor(mContext, R.color.sesl_color_picker_transparent));
        seslShapeDrawable.setShape(GradientDrawable.OVAL);

        final int rippleColor = ContextCompat.getColor(mContext,
                mIsLightTheme ? androidx.appcompat.R.color.sesl_ripple_color_light
                        : androidx.appcompat.R.color.sesl_ripple_color_dark);

        Drawable eyeDropperBackground;
        if (Build.VERSION.SDK_INT >= 29) {
            eyeDropperBackground = new SeslRecoilDrawable(rippleColor, new Drawable[]{seslShapeDrawable}, null);
        } else {
            eyeDropperBackground = new RippleDrawable(ColorStateList.valueOf(rippleColor), seslShapeDrawable, null);
        }
        mEyeDropperView.setBackground(eyeDropperBackground);
    }

    /**
     * Sets the listener to be invoked when the eye dropper icon is clicked.
     */
    public void setOnEyeDropperListener(@Nullable OnEyeDropperListener listener) {
        mOnEyeDropperListener = listener;
    }

    /**
     * Disables or enables the eye dropper tool.
     * <p>
     * When disabled, the eye dropper icon is hidden, and the last used color slot
     * (if available and applicable in the layout) might be made visible as a fallback.
     * When enabled, the eye dropper icon is shown, and the last used color slot is hidden.
     * </p>
     *
     * @param disable {@code true} to disable and hide the eye dropper, {@code false} to
     *                enable and show it.
     */
    public void setEyeDropperDisable(boolean disable) {
        if (disable) {
            mEyeDropperView.setVisibility(View.GONE);
            findViewById(R.id.sesl_last_used_color_slot).setVisibility(View.VISIBLE);
        } else {
            mEyeDropperView.setVisibility(View.VISIBLE);
            findViewById(R.id.sesl_last_used_color_slot).setVisibility(View.GONE);
        }
    }
}