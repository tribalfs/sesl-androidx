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

package androidx.appcompat.widget;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.StringRes;
import androidx.appcompat.R;
import androidx.appcompat.animation.SeslAnimationUtils;
import androidx.appcompat.graphics.drawable.SeslRecoilDrawable;
import androidx.appcompat.util.SeslMisc;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */

/**
 * SeslSwitchBar is a view that provides a standard switch control similar to {@link SwitchCompat}
 * but a more prominent one. This displays "On" or "Off" text depending on its state
 * This view also allows to show {@link SeslProgressBar} within itself to indicate ongoing operations.
 *
 */
public class SeslSwitchBar extends LinearLayout implements CompoundButton.OnCheckedChangeListener {

    public interface OnSwitchChangeListener {
        /**
         * Called when the checked state of the Switch has changed.
         *
         * @param switchView The Switch view whose state has changed.
         * @param isChecked  The new checked state of switchView.
         */
        void onSwitchChanged(@NonNull SwitchCompat switchView, boolean isChecked);
    }

    static final int SWITCH_ON_STRING_RESOURCE_ID = R.string.sesl_switchbar_on_text;
    static final int SWITCH_OFF_STRING_RESOURCE_ID = R.string.sesl_switchbar_off_text;
    static final Long BACKGROUND_COLOR_CHANGE_DURATION = 350L; //sesl9

    private final List<OnSwitchChangeListener> mSwitchChangeListeners = new ArrayList<>();
    private final SwitchBarDelegate mDelegate;
    private String mSessionDesc = null;

    SeslToggleSwitch mSwitch;
    private final SeslProgressBar mProgressBar;
    private final TextView mTextView;
    private String mLabel;
    @StringRes
    private int mOnTextId;
    @ColorInt
    private final int mOnTextColor;
    @StringRes
    private int mOffTextId;
    @ColorInt
    private final int mOffTextColor;
    private final LinearLayout mBackground;
    @ColorInt
    private final int mBackgroundColor;
    @ColorInt
    private final int mBackgroundActivatedColor;
    //sesl9
    private ValueAnimator mBackgroundColorInAnimator;
    private ValueAnimator mBackgroundColorOutAnimator;
    private boolean mIsUpdatingFromClick = false;

    public SeslSwitchBar(Context context) {
        this(context, null);
    }

    public SeslSwitchBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.seslSwitchBarStyle);
    }

    public SeslSwitchBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeslSwitchBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        LayoutInflater.from(context).inflate(R.layout.sesl_switchbar, this);

        final Resources res = getResources();
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeslSwitchBar, defStyleAttr, defStyleRes);
        mBackgroundColor = a.getColor(R.styleable.SeslSwitchBar_seslSwitchBarBackgroundColor,
                res.getColor(R.color.sesl_switchbar_off_background_color_light));
        mBackgroundActivatedColor = a.getColor(R.styleable.SeslSwitchBar_seslSwitchBarBackgroundActivatedColor,
                res.getColor(R.color.sesl_switchbar_on_background_color_light));
        mOnTextColor = a.getColor(R.styleable.SeslSwitchBar_seslSwitchBarTextActivatedColor,
                res.getColor(R.color.sesl_switchbar_on_text_color_light));
        mOffTextColor = a.getColor(R.styleable.SeslSwitchBar_seslSwitchBarTextColor,
                res.getColor(R.color.sesl_switchbar_on_text_color_light));
        a.recycle();
        mProgressBar = findViewById(R.id.sesl_switchbar_progress);
        mBackground = findViewById(R.id.sesl_switchbar_container);
        mBackground.setOnClickListener(v -> {
            if (mSwitch != null && mSwitch.isEnabled()) {
                try { //sesl9
                    mIsUpdatingFromClick = true; //sesl9
                    mSwitch.setChecked(!mSwitch.isChecked()); //sesl9
                } finally { //sesl9
                    mIsUpdatingFromClick = false; //sesl9
                }
            }
        });
        initBackgroundColorAnimator(); //sesl9
        mOnTextId = SWITCH_ON_STRING_RESOURCE_ID;
        mOffTextId = SWITCH_OFF_STRING_RESOURCE_ID;

        mTextView = findViewById(R.id.sesl_switchbar_text);
        MarginLayoutParams lp = (MarginLayoutParams) mTextView.getLayoutParams();
        lp.setMarginStart((int) res.getDimension(R.dimen.sesl_switchbar_margin_start));

        mSwitch = findViewById(R.id.sesl_switchbar_switch);
        // Prevent onSaveInstanceState() to be called as we are managing the state of the Switch
        // on our own
        mSwitch.setSaveEnabled(false);
        // Set the ToggleSwitch non-focusable and non-clickable to avoid multiple focus.
        mSwitch.setFocusable(false);
        mSwitch.setClickable(false);
        mSwitch.setOnCheckedChangeListener(this);

        setSwitchBarText(mOnTextId, mOffTextId);
        //sesl9
        addOnSwitchChangeListener((switchView, isChecked) -> setTextViewLabelAndBackground(isChecked, mIsUpdatingFromClick));
        lp = (MarginLayoutParams) mSwitch.getLayoutParams();
        lp.setMarginEnd((int) res.getDimension(R.dimen.sesl_switchbar_margin_end));

        mDelegate = new SwitchBarDelegate(this);
        ViewCompat.setAccessibilityDelegate(mBackground, mDelegate);

        setSessionDescription(getActivityTitle());
    }

    // Override the performClick method to eliminate redundant click.
    @Override
    public boolean performClick() {
        return mSwitch.performClick();
    }

    /**
     * Set the visibility of the progress bar.
     *
     * @param visible {@code true} to make the progress bar visible, {@code false} to make it
     *                gone.
     */
    public void setProgressBarVisible(boolean visible) {
        try {
            mProgressBar.setVisibility(visible ?
                    View.VISIBLE : View.GONE);
        } catch (IndexOutOfBoundsException e) {
            Log.i("SetProgressBarVisible", "Invalid argument" + e);
        }
    }

    //sesl9
    private void setTextViewLabelAndBackground(boolean isChecked, boolean animate) {
        String label = getResources().getString(isChecked ? mOnTextId : mOffTextId);
        TextView textView = mTextView;
        if (textView != null && label.contentEquals(textView.getText())
                //Only return early if on/off texts are different.
                && mOnTextId != mOffTextId/*custom*/) {
            return;
        }

        mLabel = label;

        if (animate) {
            if (mBackgroundColorInAnimator == null || mBackgroundColorOutAnimator == null) {
                initBackgroundColorAnimator();
            }

            if (isChecked) {
                if (mBackgroundColorOutAnimator.isRunning()) {
                    mBackgroundColorOutAnimator.cancel();
                }

                mBackgroundColorInAnimator.start();
            } else {
                if (mBackgroundColorInAnimator.isRunning()) {
                    mBackgroundColorInAnimator.cancel();
                }

                mBackgroundColorOutAnimator.start();
            }
        } else {
            setSwitchBarBackgroundColor(isChecked ? mBackgroundActivatedColor : mBackgroundColor);
        }

        textView.setTextColor(isChecked ? mOnTextColor : mOffTextColor);

        if (isEnabled()) {
            textView.setAlpha(1.0f);
        } else if (SeslMisc.isLightTheme(getContext()) && isChecked) {
            textView.setAlpha(0.55f);
        } else {
            textView.setAlpha(0.4f);
        }

        //Custom: Add gate as the same label can already be applied.
        if (!label.contentEquals(textView.getText())) {
            textView.setText(mLabel);
        }
    }

    //sesl9
    private void initBackgroundColorAnimator() {
        mBackgroundColorInAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), mBackgroundColor,
                mBackgroundActivatedColor);
        mBackgroundColorInAnimator.setDuration(BACKGROUND_COLOR_CHANGE_DURATION);
        mBackgroundColorInAnimator.setInterpolator(SeslAnimationUtils.SINE_OUT_33);
        mBackgroundColorInAnimator.addUpdateListener(animation ->
                setSwitchBarBackgroundColor((Integer) animation.getAnimatedValue()));

        mBackgroundColorOutAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), mBackgroundActivatedColor,
                mBackgroundColor);
        mBackgroundColorOutAnimator.setDuration(BACKGROUND_COLOR_CHANGE_DURATION);
        mBackgroundColorOutAnimator.setInterpolator(SeslAnimationUtils.SINE_OUT_33);
        mBackgroundColorOutAnimator.addUpdateListener(animation ->
                setSwitchBarBackgroundColor((Integer) animation.getAnimatedValue()));
    }

    //sesl9
    private void setSwitchBarBackgroundColor(@ColorInt int color) {
        if (mBackground == null) {
            return;
        }

        Drawable drawable = DrawableCompat.wrap(mBackground.getBackground().mutate()).mutate();
        if (!(drawable instanceof SeslRecoilDrawable)) {
            DrawableCompat.setTintList(drawable, ColorStateList.valueOf(color));
        } else {
            SeslRecoilDrawable recoilDrawable = (SeslRecoilDrawable) drawable;
            if (recoilDrawable.getNumberOfLayers() > 0) {
                Drawable layer = recoilDrawable.getDrawable(0);
                if (layer instanceof GradientDrawable) {
                    ((GradientDrawable) layer).setColor(color);
                }
            }
        }
    }


    /**
     * Set the session description for accessibility.
     * This will be used by Talkback to announce the context of the SwitchBar.
     * For example, if the SwitchBar controls "Wi-Fi", you should set this to "Wi-Fi".
     *
     * @param sessionDescription The description of the session.
     */
    public void setSessionDescription(String sessionDescription) {
        mSessionDesc = sessionDescription;
        mDelegate.setSessionName(sessionDescription);
    }

    /**
     * Set the text to display when the switch is in the on or off state.
     *
     * @param onTextId The string resource for when the switch is on.
     * @param offTextId The string resource for when the switch is off.
     */
    public void setSwitchBarText(int onTextId, int offTextId) {
        mOnTextId = onTextId;
        mOffTextId = offTextId;
        setTextViewLabelAndBackground(isChecked(), false); //sesl9
    }

    /**
     * Set the "On" or "Off" text label of this switch bar.
     *
     * @param isChecked The current checked state of the switch bar.
     */
    //sesl9
    public void setTextViewLabel(boolean isChecked) {
        mLabel = getResources().getString(isChecked ? mOnTextId : mOffTextId);
        mTextView.setText(mLabel);
    }

    public void setChecked(boolean checked) {
        setTextViewLabelAndBackground(checked, false); //sesl9
        mSwitch.setChecked(checked);
    }

    public void setCheckedInternal(boolean checked) {
        setTextViewLabelAndBackground(checked, false); //sesl9
        mSwitch.setCheckedInternal(checked);
    }

    public boolean isChecked() {
        return mSwitch.isChecked();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mTextView.setEnabled(enabled);
        mSwitch.setEnabled(enabled);
        mBackground.setEnabled(enabled);
        setTextViewLabelAndBackground(isChecked(), false); //sesl9
    }

    /**
     * Returns the {@link SeslToggleSwitch switch} child view.
     *
     * @return The switch.
     */
    public final SeslToggleSwitch getSwitch() {
        return mSwitch;
    }

    public void show() {
        if (!isShowing()) {
            setVisibility(View.VISIBLE);
            mSwitch.setOnCheckedChangeListener(this);
        }
        if (TextUtils.isEmpty(mSessionDesc)) {
            mDelegate.setSessionName(getActivityTitle());
        } else {
            mDelegate.setSessionName(mSessionDesc);
        }
    }

    public void hide() {
        if (isShowing()) {
            setVisibility(View.GONE);
            mSwitch.setOnCheckedChangeListener(null);
        }
        mDelegate.setSessionName(" ");
        mSessionDesc = null;
    }

    public boolean isShowing() {
        return (getVisibility() == View.VISIBLE);
    }

    private void propagateChecked(boolean isChecked) {
        final int count = mSwitchChangeListeners.size();
        for (int n = 0; n < count; n++) {
            mSwitchChangeListeners.get(n).onSwitchChanged(mSwitch, isChecked);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        propagateChecked(isChecked);
    }

    //sesl9
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mBackgroundColorInAnimator != null) {
            mBackgroundColorInAnimator.removeAllUpdateListeners();
        }

        if (mBackgroundColorOutAnimator != null) {
            mBackgroundColorOutAnimator.removeAllUpdateListeners();
        }
    }

    /**
     * Add a listener for switch changes
     *
     * @param listener The listener to add
     * @throws IllegalStateException If the listener is already added
     */
    public void addOnSwitchChangeListener(OnSwitchChangeListener listener) {
        if (mSwitchChangeListeners.contains(listener)) {
            throw new IllegalStateException("Cannot add twice the same OnSwitchChangeListener");
        }
        mSwitchChangeListeners.add(listener);
    }

    public void removeOnSwitchChangeListener(OnSwitchChangeListener listener) {
        if (!mSwitchChangeListeners.contains(listener)) {
            throw new IllegalStateException("Cannot remove OnSwitchChangeListener");
        }
        mSwitchChangeListeners.remove(listener);
    }

    static class SavedState extends BaseSavedState {
        boolean checked;
        boolean visible;

        SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        SavedState(Parcel in) {
            super(in);
            checked = (Boolean) in.readValue(null);
            visible = (Boolean) in.readValue(null);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(checked);
            out.writeValue(visible);
        }

        @NonNull
        @Override
        public String toString() {
            return "SeslSwitchBar.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " checked=" + checked
                    + " visible=" + visible + "}";
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }


    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);
        ss.checked = mSwitch.isChecked();
        ss.visible = isShowing();
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;

        super.onRestoreInstanceState(ss.getSuperState());

        mSwitch.setCheckedInternal(ss.checked);
        setTextViewLabelAndBackground(ss.checked, false); //sesl9
        setVisibility(ss.visible ? View.VISIBLE : View.GONE);
        mSwitch.setOnCheckedChangeListener(ss.visible ? this : null);

        requestLayout();
    }

    private String getActivityTitle() {
        Context context = getContext();

        while (context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
            if (context instanceof Activity) {
                CharSequence title = ((Activity) context).getTitle();
                return title != null ? title.toString() : "";
            }
        }

        return "";
    }

    private static class SwitchBarDelegate extends AccessibilityDelegateCompat {
        private String mSessionName = "";
        private final SeslToggleSwitch mSwitch;
        private final TextView mText;

        public SwitchBarDelegate(View switchBar) {
            mText = switchBar.findViewById(R.id.sesl_switchbar_text);
            mSwitch = switchBar.findViewById(R.id.sesl_switchbar_switch);
        }

        public void setSessionName(String sessionName) {
            mSessionName = sessionName;
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(@NonNull View host,
                @NonNull AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(host, info);

            //sesl9
            mSwitch.setContentDescription(mText.getText());
            if (TextUtils.isEmpty(mSessionName)) {
                return;
            }

            info.setText(mSessionName);
        }
    }

    /**
     * Updates the horizontal margins of the text view and switch.
     * This is useful when the layout direction changes.
     */
    public void updateHorizontalMargins() {
        final Resources res = getResources();
        if (mTextView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mTextView.getLayoutParams();
            lp.setMarginStart((int) res.getDimension(R.dimen.sesl_switchbar_margin_start));
            mTextView.setLayoutParams(lp);
        }
        if (mSwitch != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mSwitch.getLayoutParams();
            lp.setMarginEnd((int) res.getDimension(R.dimen.sesl_switchbar_margin_end));
            mSwitch.setLayoutParams(lp);
        }
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return SeslSwitchBar.class.getName();
    }
}
