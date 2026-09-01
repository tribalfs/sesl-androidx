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

package androidx.recyclerview.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.PathInterpolator;

import androidx.appcompat.R;
import androidx.appcompat.util.SeslMisc;
import androidx.core.widget.SeslBlurController;

//sesl9
class SeslIndexTipView extends View {
    private static final int ALPHA_DURATION = 150;
    private static final int CHANGE_TEXT_DELAY = 90;
    private static final int FADE_OUT_DELAY = 300;
    private static final int FONT_WEIGHT_REGULAR = 400;
    private static final int LAYOUT_MAX_LINE = 2;
    private static final int SCALE_DURATION = 200;
    private static final float SHAPE_COLOR_ALPHA_RATIO = 0.92f;
    private static final int TIMER_DURATION = 450;

    private ObjectAnimator mAlphaAnimator;
    private float mAlphaAnimatorTarget;
    private final PathInterpolator mAlphaInterpolator;
    private float mAnimatingHalfWidth;
    private final Runnable mApplyDelayedTextRunnable;
    private int mAvailableWidth;
    private final SeslBlurController mBlurController;
    private int mCenterX;
    private StaticLayout mDisplayedTextLayout;
    private ValueAnimator mHideTimer;
    private int mHorizontalPadding;
    private final View mHostView;
    private int mHostWidth;
    private boolean mImmersivePositionDirty;
    private int mLastLeftPadding;
    private int mLastRightPadding;
    private boolean mLayoutSpecSet;
    private int mMaxWidth;
    private int mMinWidth;
    private Runnable mPendingFadeOutRunnable;
    private float mPreviousHalfWidth;
    private String mPreviousText;
    private float mRadius;
    private final PathInterpolator mScaleInterpolator;
    private int mStatusBarHeight;
    private String mTargetText;
    private String mText;
    private StaticLayout mTextLayout;
    private final TextPaint mTextPaint;
    private int mTopMargin;
    private int mTopOffset;
    private int mVerticalPadding;
    private ValueAnimator mWidthAnimator;

    public final class BackgroundProvider implements SeslBlurController.BackgroundProvider {
        private final int mBackgroundColor;

        public BackgroundProvider(int backgroundColor) {
            mBackgroundColor = backgroundColor;
        }

        @Override
        public Drawable getBackgroundBlur() {
            return createRoundedBackground(mBackgroundColor);
        }

        @Override
        public Drawable getBackgroundDark() {
            return createRoundedBackground(mBackgroundColor);
        }

        @Override
        public Drawable getBackgroundLight() {
            return createRoundedBackground(mBackgroundColor);
        }

        @Override
        public float getElevation() {
            return getContext().getResources().getDimension(R.dimen.sesl_go_to_top_elevation);
        }

        @Override
        public float getOpaqueAlphaWithoutBlur() {
            return SHAPE_COLOR_ALPHA_RATIO;
        }
    }

    public SeslIndexTipView(Context context, View view) {
        super(context);
        mBlurController = new SeslBlurController();
        mAlphaInterpolator = new PathInterpolator(0.0f, 0.0f, 1.0f, 1.0f);
        mScaleInterpolator = new PathInterpolator(0.22f, 0.25f, 0.0f, 1.0f);
        mTextPaint = new TextPaint();
        mText = "";
        mTargetText = "";
        mPreviousText = "";
        mAlphaAnimator = new ObjectAnimator();
        mAlphaAnimatorTarget = -1.0f;
        mApplyDelayedTextRunnable = () -> {
            mDisplayedTextLayout = mTextLayout;
            invalidate();
        };
        mHostView = view;
        initResources();
        setAlpha(0.0f);
    }

    private void applySolidBackground(int color) {
        setBackground(createRoundedBackground(color));
        setBackgroundTintList(null);
        setClipToOutline(false);
    }

    private StaticLayout buildMultiLineLayout(String text) {
        return StaticLayout.Builder.obtain(text, 0, text.length(), mTextPaint,
                Math.max(1, (int) getMaxLineWidth(StaticLayout.Builder.obtain(text, 0, text.length(),
                        mTextPaint, ((mAvailableWidth / 2) - mHorizontalPadding) * 2).build())))
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setMaxLines(2)
                .setEllipsize(TextUtils.TruncateAt.END)
                .build();
    }

    private StaticLayout buildSingleLineLayout(String text) {
        return StaticLayout.Builder.obtain(text, 0, text.length(), mTextPaint,
                Math.max(1, (int) Math.ceil(mTextPaint.measureText(text)))).build();
    }

    private float calculateHalfWidth() {
        if (TextUtils.isEmpty(mText)) {
            return mMinWidth / 2.0f;
        }
        float halfWidth = (mTextPaint.measureText(mText) / 2.0f) + mHorizontalPadding;
        if (halfWidth < mMinWidth / 2.0f) {
            halfWidth = mMinWidth / 2.0f;
        } else if (mAvailableWidth > 0 && halfWidth > mAvailableWidth / 2.0f) {
            mTextLayout = buildMultiLineLayout(mText);
            halfWidth = (getMaxLineWidth(mTextLayout) / 2.0f) + mHorizontalPadding;
            mDisplayedTextLayout = mTextLayout;
        }
        return Math.min((float) mCenterX, halfWidth);
    }

    private int calculateImmersiveGap() {
        if (!mImmersivePositionDirty) {
            return 0;
        }
        int[] loc = new int[2];
        mHostView.getLocationOnScreen(loc);
        return Math.max(0, mStatusBarHeight - loc[1]);
    }

    private void commitLayout() {
        int gap = calculateImmersiveGap();
        int layoutHeight = (mVerticalPadding * 2) + getLayoutHeight(mTextLayout);
        int cx = mCenterX;
        float halfWidth = mAnimatingHalfWidth;
        int top = mTopMargin + gap + mTopOffset;
        super.layout((int) (cx - halfWidth), top, (int) (cx + halfWidth), layoutHeight + top);
    }

    /* package */ GradientDrawable createRoundedBackground(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(getColorWithAlpha(color, SHAPE_COLOR_ALPHA_RATIO));
        drawable.setCornerRadius(mRadius);
        return drawable;
    }

    private int getColorWithAlpha(int color, float alphaRatio) {
        return Color.argb(Math.round(Color.alpha(color) * alphaRatio), Color.red(color), Color.green(color), Color.blue(color));
    }

    private int getLayoutHeight(StaticLayout layout) {
        if (layout.getLineCount() == 0) {
            return 0;
        }
        return layout.getLineBottom(layout.getLineCount() - 1) - layout.getLineTop(0);
    }

    private float getMaxLineWidth(StaticLayout layout) {
        int lineCount = Math.min(2, layout.getLineCount());
        float max = 0.0f;
        for (int i = 0; i < lineCount; i++) {
            max = Math.max(max, layout.getLineWidth(i));
        }
        return max;
    }

    private void initResources() {
        Resources res = getContext().getResources();
        Resources.Theme theme = getContext().getTheme();
        boolean isLight = SeslMisc.isLightTheme(getContext());

        mTextPaint.setAntiAlias(true);
        if (Build.VERSION.SDK_INT >= 34) {
            mTextPaint.setTypeface(Typeface.create(Typeface.create("sec", Typeface.NORMAL), 400, false));
        } else {
            mTextPaint.setTypeface(Typeface.create(getContext().getString(R.string.sesl_font_family_regular), Typeface.NORMAL));
        }
        mTextPaint.setTextSize(res.getDimensionPixelSize(androidx.recyclerview.R.dimen.sesl_index_tip_text_size));

        if (Build.VERSION.SDK_INT >= 36) {
            mTextPaint.setColor(isLight ? res.getColor(R.color.sesl_index_tip_text_color_light, theme) : res.getColor(R.color.sesl_index_tip_text_color_dark, theme));
        } else {
            if (isLight) {
                mTextPaint.setColor(res.getColor(R.color.sesl_black, theme));//custom
            } else {
                mTextPaint.setColor(res.getColor(R.color.sesl_white, theme));
            }
        }
        mHorizontalPadding = res.getDimensionPixelSize(androidx.recyclerview.R.dimen.sesl_index_tip_horizontal_padding);
        mVerticalPadding = res.getDimensionPixelSize(androidx.recyclerview.R.dimen.sesl_index_tip_vertical_padding);
        mMinWidth = res.getDimensionPixelSize(androidx.recyclerview.R.dimen.sesl_index_tip_min_width);
        mMaxWidth = res.getDimensionPixelSize(androidx.recyclerview.R.dimen.sesl_index_tip_max_width);
        mTopMargin = res.getDimensionPixelSize(androidx.recyclerview.R.dimen.sesl_index_tip_margin_top);
        mRadius = res.getDimension(androidx.recyclerview.R.dimen.sesl_index_tip_radius);
        int identifier = res.getIdentifier("status_bar_height", "dimen", "android");
        mStatusBarHeight = identifier > 0 ? res.getDimensionPixelSize(identifier) : 0;

        int colorRes = isLight ? R.color.sesl_scrollbar_index_tip_color : R.color.sesl_scrollbar_index_tip_color_dark;
        int color = res.getColor(colorRes, theme);
        if (Build.VERSION.SDK_INT >= 36) {
            mBlurController.setBlurEnabledInternal(this, true, isLight, new BackgroundProvider(color));
        } else {
            applySolidBackground(color);
        }

        StaticLayout layout = buildSingleLineLayout("");
        mTextLayout = layout;
        mDisplayedTextLayout = layout;
    }

    private void refreshLayout() {
        if (mLayoutSpecSet) {
            int width = (mHostWidth - mLastLeftPadding) - mLastRightPadding;
            mAvailableWidth = width > mHorizontalPadding * 2 ? Math.min(width - (mHorizontalPadding * 2), mMaxWidth) : Math.max(width, 0);
            mCenterX = Math.round(width / 2.0f) + mLastLeftPadding;
            float calcHalfWidth = calculateHalfWidth();
            if (mPreviousHalfWidth > 0.0f && mPreviousHalfWidth != calcHalfWidth) {
                startWidthAnimation(calcHalfWidth);
            }
            if (mAnimatingHalfWidth == 0.0f || mPreviousHalfWidth == 0.0f) {
                mAnimatingHalfWidth = calcHalfWidth;
            }
            commitLayout();
            if (TextUtils.equals(mText, mPreviousText)) {
                return;
            }
            mPreviousHalfWidth = calcHalfWidth;
        }
    }

    private void scheduleTextChange(boolean isNewTextLonger) {
        mTargetText = mText;
        if (!isNewTextLonger) {
            mDisplayedTextLayout = mTextLayout;
        } else {
            removeCallbacks(mApplyDelayedTextRunnable);
            postDelayed(mApplyDelayedTextRunnable, CHANGE_TEXT_DELAY);
        }
    }

    private void startAlphaAnimation(float targetAlpha, final Runnable onEnd) {
        if ((mAlphaAnimator.isRunning() && mAlphaAnimatorTarget == targetAlpha) || getAlpha() == targetAlpha) {
            return;
        }
        if (mAlphaAnimator.isRunning()) {
            mAlphaAnimator.cancel();
        }
        mAlphaAnimatorTarget = targetAlpha;
        mAlphaAnimator = ObjectAnimator.ofFloat(this, "alpha", getAlpha(), targetAlpha);
        mAlphaAnimator.setDuration(ALPHA_DURATION);
        mAlphaAnimator.setInterpolator(mAlphaInterpolator);
        if (onEnd != null) {
            mAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onEnd.run();
                }
            });
        }
        mAlphaAnimator.start();
    }

    private void startWidthAnimation(float targetHalfWidth) {
        if (mWidthAnimator != null) {
            mWidthAnimator.cancel();
        }
        mWidthAnimator = ValueAnimator.ofFloat(mAnimatingHalfWidth, targetHalfWidth);
        mWidthAnimator.setDuration(SCALE_DURATION);
        mWidthAnimator.setInterpolator(mScaleInterpolator);
        mWidthAnimator.addUpdateListener(anim -> {
            mAnimatingHalfWidth = (Float) anim.getAnimatedValue();
            commitLayout();
            invalidate();
        });
        mWidthAnimator.start();
    }

    public void applyLayout(int width, int topOffset, int leftPadding, int rightPadding, boolean immersivePositionDirty) {
        mHostWidth = width;
        mLastLeftPadding = leftPadding;
        mLastRightPadding = rightPadding;
        mTopOffset = topOffset;
        mImmersivePositionDirty = immersivePositionDirty;
        mLayoutSpecSet = true;
        refreshLayout();
    }

    public void cancelFadeAnimation() {
        if (mAlphaAnimator.isRunning()) {
            mAlphaAnimator.cancel();
        }
    }

    public void cancelHideTimer() {
        if (mHideTimer != null) {
            mHideTimer.cancel();
            mHideTimer.removeAllListeners();
        }
    }

    public void cancelPendingFadeOut() {
        if (mPendingFadeOutRunnable != null) {
            removeCallbacks(mPendingFadeOutRunnable);
            mPendingFadeOutRunnable = null;
        }
    }

    public void fadeIn() {
        cancelPendingFadeOut();
        startAlphaAnimation(1.0f, null);
    }

    public void fadeOut(Runnable onEnd) {
        runFadeOut(onEnd, 0);
    }

    public void fadeOutDelayed(Runnable onEnd) {
        runFadeOut(onEnd, FADE_OUT_DELAY);
    }

    public int getHorizontalPaddingLeft() {
        return getPaddingLeft();
    }

    public int getHorizontalPaddingRight() {
        return getPaddingRight();
    }

    @Override
    public void getLocationInWindow(int[] outLocation) {
        super.getLocationInWindow(outLocation);
        int[] hostLoc = new int[2];
        mHostView.getLocationInWindow(hostLoc);
        outLocation[0] += hostLoc[0];
        outLocation[1] = (hostLoc[1] - getScrollY()) + outLocation[1];
    }

    public void hideImmediate() {
        cancelPendingFadeOut();
        setAlpha(0.0f);
    }

    public void invalidateIfNeed() {
        if (mBlurController.isBlurEnabled()) {
            invalidate();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (TextUtils.isEmpty(mText) || mDisplayedTextLayout.getLineCount() == 0) {
            return;
        }
        canvas.save();
        canvas.translate((getWidth() - getMaxLineWidth(mDisplayedTextLayout)) / 2.0f, mVerticalPadding);
        mDisplayedTextLayout.draw(canvas);
        canvas.restore();
    }

    public void onImmersivePositionChanged(boolean immersivePositionDirty) {
        mImmersivePositionDirty = immersivePositionDirty;
        refreshLayout();
        invalidate();
    }

    public void runFadeOut(final Runnable onEnd, int delay) {
        cancelPendingFadeOut();
        Runnable runnable = () -> {
            mPendingFadeOutRunnable = null;
            startAlphaAnimation(0.0f, onEnd);
        };
        mPendingFadeOutRunnable = runnable;
        postDelayed(runnable, delay);
    }

    public void setHorizontalPadding(int left, int right) {
        setPadding(left, getPaddingTop(), right, getPaddingBottom());
    }

    public void setTopMargin(int topMargin) {
        mTopMargin = topMargin;
    }

    public void startHideTimer(final Runnable onExpired) {
        cancelHideTimer();
        mHideTimer = ValueAnimator.ofFloat(0.0f, 0.0f);
        mHideTimer.setDuration(TIMER_DURATION);
        mHideTimer.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onExpired.run();
            }
        });
        mHideTimer.start();
    }

    public void updateText(String text) {
        if (text == null) {
            text = "";
        }
        if (TextUtils.equals(mText, text)) {
            return;
        }
        mPreviousText = mText;
        mText = text;
        mTextLayout = buildSingleLineLayout(text);
        if (TextUtils.isEmpty(mTargetText)) {
            mTargetText = mText;
            mDisplayedTextLayout = mTextLayout;
        } else if (!TextUtils.equals(mText, mTargetText)) {
            scheduleTextChange(mText.length() > mTargetText.length());
        }
        refreshLayout();
        invalidate();
    }
}
