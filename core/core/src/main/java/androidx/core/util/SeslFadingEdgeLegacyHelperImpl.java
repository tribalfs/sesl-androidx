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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.R;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//sesl9
/**
 * Legacy LinearGradient based fading edge renderer for SESL scrollable widgets (API < 33).
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public class SeslFadingEdgeLegacyHelperImpl implements SeslFadingEdgeHelper {

    private static final long COLOR_ANIMATION_DURATION = 300L;
    private static final String NAVIGATION_MODE = "navigation_mode";
    private static final int NAV_BAR_MODE_3BUTTON = 0;
    private static final int NAV_BAR_MODE_GESTURAL = 2;
    private static final int NONE_COLOR = 0;
    private static final String TAG = "SeslFadingEdgeLegacyHelperImpl";

    static final int[] GRADIENT_ALPHA_TOP = {255, 224, 0};
    static final int[] GRADIENT_ALPHA_BOTTOM = {255, 224, 0};
    static final int[] GRADIENT_ALPHA_TOP_EXTRA = {255, 224, 163, 0};
    static final int[] GRADIENT_ALPHA_BOTTOM_EXTRA = {255, 224, 163, 0};

    static final float[] GRADIENT_POSITION_TOP = {0.0f, 0.28f, 1.0f};
    static final float[] GRADIENT_POSITION_BOTTOM = {0.0f, 0.32f, 1.0f};
    static final float[] GRADIENT_POSITION_TOP_EXTRA = {0.0f, 0.16f, 0.65f, 1.0f};
    static final float[] GRADIENT_POSITION_BOTTOM_EXTRA = {0.0f, 0.12f, 0.48f, 1.0f};

    private boolean mAllowTopFadingEdgeEdgeWithoutEdgeToEdge;
    private final ColorAnimationManager mAnimationManager;
    LinearGradient mBottomFadingEdgeGradient;
    private SeslBottomFadingEdgeOverrides mBottomFadingEdgeOverrides;
    private final ColorStateManager mColorStateManager;
    private final Context mContext;
    private int mDistanceFromWindowBottom;
    private boolean mExtendBottomFadingEdge;
    private boolean mExtendTopFadingEdge;
    LinearGradient mExtraBottomFadingEdgeGradient;
    LinearGradient mExtraTopFadingEdgeGradient;
    private float mExtraTopRatio;
    private int mFadingEdgeBottomHeight;
    private int mFadingEdgeBottomOffset;
    private int mFadingEdgeBottomPadding;
    private final Matrix mFadingEdgeMatrix;
    private int mFadingEdgeOnNaviBarBottomHeight;
    private final Paint mFadingEdgePaint;
    private int mFadingEdgeTopHeight;
    private int mForcedFadingEdgeTopHeight;
    private int mForcedFadingEdgeBottomHeight;
    final int[] mGradientTopColors;
    final int[] mGradientBottomColors;
    final int[] mGradientTopExtraColors;
    final int[] mGradientBottomExtraColors;
    private boolean mHideBottom;
    private boolean mHideTop;
    private boolean mIsAppCustomized;
    private boolean mIsFadingEdgeEnabled;
    private int mNaviBarTop;
    private Rect mRectForFadingEffect;
    private View mTargetView;
    LinearGradient mTopFadingEdgeGradient;
    private SeslTopFadingEdgeOverrides mTopFadingEdgeOverrides;
    private boolean mWindowBottomAlignment;

    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public class ColorAnimationManager {
        private ValueAnimator mAnimator;
        private int mCurrentColor = 0;
        private int mTargetColor = 0;

        public ColorAnimationManager() {
        }

        public void cancelCurrentAnimation() {
            ValueAnimator valueAnimator = mAnimator;
            if (valueAnimator != null && valueAnimator.isRunning()) {
                mAnimator.cancel();
            }
        }

        public int getTargetColor() {
            return mTargetColor;
        }

        public boolean isAnimating() {
            ValueAnimator valueAnimator = mAnimator;
            return valueAnimator != null && valueAnimator.isRunning();
        }

        public void setTargetColorImmediate(int color) {
            mTargetColor = color;
            mCurrentColor = color;
        }

        public void startAnimation(int startColor, int targetColor, Runnable runnable) {
            cancelCurrentAnimation();
            mCurrentColor = startColor;
            mTargetColor = targetColor;
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            mAnimator = valueAnimatorOfFloat;
            valueAnimatorOfFloat.setDuration(COLOR_ANIMATION_DURATION);
            mAnimator.addUpdateListener(animation -> {
                int blendedColor = ColorUtils.blendARGB(startColor, targetColor, animation.getAnimatedFraction());
                mCurrentColor = blendedColor;
                applyAnimatedColor(blendedColor);
                if (runnable != null) {
                    runnable.run();
                }
            });
            mAnimator.start();
        }
    }

    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public class ColorStateManager {
        public ColorStateManager() {
        }

        private void applyGradientAlpha(int[] targetColors, int baseColor, int[] alphaComponents) {
            if (targetColors == null || alphaComponents == null || targetColors.length != alphaComponents.length) {
                return;
            }
            for (int i = 0; i < targetColors.length; i++) {
                targetColors[i] = ColorUtils.setAlphaComponent(baseColor, alphaComponents[i]);
            }
        }

        public int getCurrentBaseColor() {
            return mGradientTopColors[0] & 0x00FFFFFF;
        }

        public void recreateGradients() {
            mTopFadingEdgeGradient = createFadingEdgeGradient(mGradientTopColors, GRADIENT_POSITION_TOP);
            mBottomFadingEdgeGradient = createFadingEdgeGradient(mGradientBottomColors, GRADIENT_POSITION_BOTTOM);
            if (mExtraTopFadingEdgeGradient != null) {
                mExtraTopFadingEdgeGradient = createFadingEdgeGradient(mGradientTopExtraColors, GRADIENT_POSITION_TOP_EXTRA);
            }
            if (mExtraBottomFadingEdgeGradient != null) {
                mExtraBottomFadingEdgeGradient = createFadingEdgeGradient(mGradientBottomExtraColors, GRADIENT_POSITION_BOTTOM_EXTRA);
            }
        }

        public boolean shouldAnimateColorChange(int startColor, int targetColor) {
            return startColor != NONE_COLOR && targetColor != NONE_COLOR;
        }

        public void updateGradientColors(int color) {
            applyGradientAlpha(mGradientTopColors, color, GRADIENT_ALPHA_TOP);
            applyGradientAlpha(mGradientBottomColors, color, GRADIENT_ALPHA_BOTTOM);
            applyGradientAlpha(mGradientTopExtraColors, color, GRADIENT_ALPHA_TOP_EXTRA);
            applyGradientAlpha(mGradientBottomExtraColors, color, GRADIENT_ALPHA_BOTTOM_EXTRA);
        }
    }

    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public enum EdgeType {
        TOP(false, 0.0f),
        BOTTOM(true, 180.0f);

        private final boolean mCanUseExtendedGradient;
        private final float mRotationDegrees;

        EdgeType(boolean canUseExtendedGradient, float rotationDegrees) {
            mCanUseExtendedGradient = canUseExtendedGradient;
            mRotationDegrees = rotationDegrees;
        }

        public boolean canUseExtendedGradient() {
            return mCanUseExtendedGradient;
        }

        public float getRotationDegrees() {
            return mRotationDegrees;
        }
    }

    public SeslFadingEdgeLegacyHelperImpl(@NonNull Context context) {
        Paint paint = new Paint();
        mFadingEdgePaint = paint;
        mFadingEdgeMatrix = new Matrix();
        mTopFadingEdgeGradient = null;
        mBottomFadingEdgeGradient = null;
        mExtraTopFadingEdgeGradient = null;
        mExtraBottomFadingEdgeGradient = null;
        mExtraTopRatio = -1.0f;
        mIsFadingEdgeEnabled = false;
        mForcedFadingEdgeTopHeight = 0;
        mForcedFadingEdgeBottomHeight = 0;
        mFadingEdgeBottomPadding = 0;
        mExtendTopFadingEdge = false;
        mExtendBottomFadingEdge = false;
        mGradientTopColors = new int[3];
        mGradientBottomColors = new int[3];
        mGradientTopExtraColors = new int[4];
        mGradientBottomExtraColors = new int[4];
        mFadingEdgeBottomOffset = 0;
        mDistanceFromWindowBottom = 0;
        mNaviBarTop = -1;
        mIsAppCustomized = false;
        mHideTop = false;
        mHideBottom = false;
        mAllowTopFadingEdgeEdgeWithoutEdgeToEdge = false;
        mWindowBottomAlignment = true;
        mAnimationManager = new ColorAnimationManager();
        ColorStateManager colorStateManager = new ColorStateManager();
        mColorStateManager = colorStateManager;
        mContext = context;
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        colorStateManager.updateGradientColors(getRoundedCornerColor(context));
    }

    private void animateColorChange(int startColor, int targetColor, Runnable runnable) {
        mAnimationManager.startAnimation(startColor, targetColor, runnable);
    }

    void applyAnimatedColor(int color) {
        mColorStateManager.updateGradientColors(color);
        mColorStateManager.recreateGradients();
    }

    private int calculateDynamicBottomHeight(ScrollInfoProvider scrollInfoProvider) {
        if (mTargetView == null) {
            return 0;
        }
        Rect rect = mRectForFadingEffect;
        int top = rect.top;
        int bottom = rect.bottom;
        int resolvedBottomHeight = (mIsAppCustomized || !isNaviBarOverlapped())
                ? getResolvedBottomHeight()
                : getResolvedBottomOnNaviBarHeight();
        if (top + resolvedBottomHeight > bottom - resolvedBottomHeight) {
            resolvedBottomHeight = (bottom - top) / 2;
        }
        int maxBottomHeight = Math.max(resolvedBottomHeight, 0);
        if (maxBottomHeight == 0) {
            return 0;
        }
        int offset = Math.max(scrollInfoProvider.computeVerticalScrollOffset(), 0);
        int range = Math.max(Math.max(scrollInfoProvider.computeVerticalScrollRange() + mDistanceFromWindowBottom, 0)
                - Math.max(scrollInfoProvider.computeVerticalScrollExtent(), 0), 0);
        int remaining = Math.max(range - offset, 0);
        int targetHeight;
        if (scrollInfoProvider.shouldNormalizeFadingEdge()) {
            float lastItemHeightVisibleRatio = scrollInfoProvider.getLastItemHeightVisibleRatio();
            if (lastItemHeightVisibleRatio > 0.0f) {
                targetHeight = (int) ((1.0f - lastItemHeightVisibleRatio) * maxBottomHeight);
            } else {
                targetHeight = maxBottomHeight;
            }
        } else if (mDistanceFromWindowBottom > 0) {
            targetHeight = maxBottomHeight;
        } else {
            targetHeight = normalizeHeight(scrollInfoProvider, remaining, range, maxBottomHeight);
        }
        return Math.max(mForcedFadingEdgeBottomHeight, Math.min(targetHeight, maxBottomHeight));
    }

    private int calculateDynamicTopHeight(ScrollInfoProvider scrollInfoProvider) {
        if (mTargetView == null) {
            return 0;
        }
        Rect rect = mRectForFadingEffect;
        int top = rect.top;
        int bottom = rect.bottom;
        int resolvedTopHeight = getResolvedTopHeight();
        if (top + resolvedTopHeight > bottom - resolvedTopHeight) {
            resolvedTopHeight = (bottom - top) / 2;
        }
        int maxTopHeight = Math.max(resolvedTopHeight, 0);
        if (maxTopHeight == 0) {
            return 0;
        }
        int offset = Math.max(scrollInfoProvider.computeVerticalScrollOffset(), 0);
        int targetHeight = normalizeHeight(scrollInfoProvider, Math.min(offset, maxTopHeight),
                Math.max(scrollInfoProvider.computeVerticalScrollRange() - scrollInfoProvider.computeVerticalScrollExtent(), 0), maxTopHeight);
        if (!mAllowTopFadingEdgeEdgeWithoutEdgeToEdge) {
            int distanceFromWindowTop = getDistanceFromWindowTop();
            if (distanceFromWindowTop < 0 || distanceFromWindowTop > targetHeight) {
                return mForcedFadingEdgeTopHeight;
            }
            targetHeight -= distanceFromWindowTop;
        }
        return Math.min(Math.max(mForcedFadingEdgeTopHeight, targetHeight), maxTopHeight);
    }

    LinearGradient createFadingEdgeGradient(int[] colors, float[] positions) {
        if (colors == null || positions == null || colors.length != positions.length) {
            return null;
        }
        return new LinearGradient(0.0f, 0.0f, 0.0f, 1.0f, colors, positions, Shader.TileMode.CLAMP);
    }

    private int getDistanceFromWindowBottom() {
        View view = mTargetView;
        if (view == null || !mWindowBottomAlignment) {
            return 0;
        }
        int[] location = new int[2];
        view.getLocationInWindow(location);
        int windowTop = location[1];
        int viewBottom = view.getHeight() + windowTop;
        int rootHeight = view.getRootView().getHeight();
        int overflow = viewBottom - rootHeight;
        if (overflow > 0) {
            Rect rect = new Rect();
            view.getLocalVisibleRect(rect);
            overflow += Math.max(0, rootHeight - (windowTop + rect.bottom));
        }
        return Math.max(0, overflow);
    }

    private int getDistanceFromWindowTop() {
        View view = mTargetView;
        if (view == null) {
            return 0;
        }
        int[] location = new int[2];
        view.getLocationInWindow(location);
        return location[1];
    }

    private LinearGradient getGradientForEdge(EdgeType edgeType) {
        LinearGradient gradient;
        if (edgeType == EdgeType.TOP) {
            return (!mExtendTopFadingEdge || (gradient = mExtraTopFadingEdgeGradient) == null)
                    ? mTopFadingEdgeGradient
                    : gradient;
        }
        return (!mExtendBottomFadingEdge || (gradient = mExtraBottomFadingEdgeGradient) == null)
                ? mBottomFadingEdgeGradient
                : gradient;
    }

    private int getPreviousColor() {
        int targetColor = mAnimationManager.getTargetColor();
        return targetColor != NONE_COLOR ? targetColor : mColorStateManager.getCurrentBaseColor();
    }

    private int getResolvedBottomHeight() {
        return resolveBottomHeight(mExtendBottomFadingEdge ? 1 : 0, mFadingEdgeBottomHeight);
    }

    private int getResolvedBottomOnNaviBarHeight() {
        return resolveBottomHeight(mExtendBottomFadingEdge ? 4 : 2, mFadingEdgeOnNaviBarBottomHeight);
    }

    private int getResolvedTopHeight() {
        return resolveTopHeight(mExtendTopFadingEdge ? 1 : 0, mFadingEdgeTopHeight);
    }

    private int getRoundedCornerColor(Context context) {
        try {
            return mContext.getColor(mContext.getResources().getIdentifier(
                    isNightMode(mContext) ? "sesl_round_and_bgcolor_dark" : "sesl_round_and_bgcolor_light",
                    "color", mContext.getPackageName()));
        } catch (Resources.NotFoundException unused) {
            return NONE_COLOR;
        }
    }

    private boolean isNaviBarOverlapped() {
        View view = mTargetView;
        if (view != null) {
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int viewBottom = view.getHeight() + location[1];
            int naviBarTop = mNaviBarTop;
            if (naviBarTop > 0 && viewBottom > naviBarTop) {
                return true;
            }
        }
        return false;
    }

    private boolean isNightMode(Context context) {
        return (context.getResources().getConfiguration().uiMode & 48) == 32;
    }

    private int normalizeHeight(ScrollInfoProvider scrollInfoProvider, int currentVal, int range, int maxHeight) {
        return !shouldNormalizeFadingEdgeForDistance(scrollInfoProvider, range, maxHeight)
                ? currentVal
                : Math.min(Math.round(((float) currentVal / range) * maxHeight), maxHeight);
    }

    private void renderBottomFadingEdge(Canvas canvas, int height) {
        if (mHideBottom) {
            return;
        }
        Rect rect = mRectForFadingEffect;
        renderFadingEdge(canvas, EdgeType.BOTTOM, height, rect.left, rect.bottom - mFadingEdgeBottomPadding);
    }

    private void renderFadingEdge(Canvas canvas, EdgeType edgeType, int height, float x, float y) {
        mFadingEdgeMatrix.setScale(1.0f, height);
        if (edgeType.getRotationDegrees() > 0.0f) {
            mFadingEdgeMatrix.postRotate(edgeType.getRotationDegrees());
        }
        mFadingEdgeMatrix.postTranslate(x, y);
        LinearGradient gradient = getGradientForEdge(edgeType);
        if (gradient != null) {
            gradient.setLocalMatrix(mFadingEdgeMatrix);
            mFadingEdgePaint.setShader(gradient);
            if (height > 0) {
                try {
                    if (edgeType == EdgeType.TOP) {
                        Rect rect = mRectForFadingEffect;
                        canvas.drawRect(rect.left, rect.top, rect.right, rect.top + height, mFadingEdgePaint);
                        return;
                    }
                    Rect rect = mRectForFadingEffect;
                    canvas.drawRect(rect.left, (rect.bottom - height) - mFadingEdgeBottomPadding, rect.right, rect.bottom, mFadingEdgePaint);
                } catch (Exception e) {
                    Log.e(TAG, "Unable to draw on Canvas.", e);
                }
            }
        }
    }

    private void renderTopFadingEdge(Canvas canvas, int height) {
        if (mHideTop) {
            return;
        }
        Rect rect = mRectForFadingEffect;
        renderFadingEdge(canvas, EdgeType.TOP, height, rect.left, rect.top);
    }

    private int resolveBottomHeight(int slot, int defaultHeight) {
        SeslBottomFadingEdgeOverrides overrides = mBottomFadingEdgeOverrides;
        return (overrides == null || overrides.isEmpty()) ? defaultHeight : overrides.resolveHeight(slot, defaultHeight);
    }

    private int resolveTopHeight(int slot, int defaultHeight) {
        SeslTopFadingEdgeOverrides overrides = mTopFadingEdgeOverrides;
        return (overrides == null || overrides.isEmpty()) ? defaultHeight : overrides.resolveHeight(slot, defaultHeight);
    }

    private void setColorImmediate(int color) {
        mAnimationManager.cancelCurrentAnimation();
        mAnimationManager.setTargetColorImmediate(color);
        mColorStateManager.updateGradientColors(color);
        mColorStateManager.recreateGradients();
    }

    private void setOnApplyWindowInsetsListener() {
        View view = mTargetView;
        if (view != null) {
            ViewCompat.setOnApplyWindowInsetsListener(view, (!mIsFadingEdgeEnabled || mIsAppCustomized) ? null : (v, insets) -> {
                int navBarBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                if (navBarBottom <= 0 || Settings.Secure.getInt(v.getContext().getContentResolver(), NAVIGATION_MODE, 0) == NAV_BAR_MODE_GESTURAL) {
                    mNaviBarTop = -1;
                } else {
                    mNaviBarTop = Resources.getSystem().getDisplayMetrics().heightPixels - navBarBottom;
                }
                return insets;
            });
        }
    }

    private boolean shouldNormalizeFadingEdgeForDistance(ScrollInfoProvider scrollInfoProvider, int range, int maxHeight) {
        return scrollInfoProvider.shouldNormalizeFadingEdgeForDistance() && range > 0 && range < maxHeight;
    }

    public int getBottomPaddingResource() {
        return mContext.getResources().getDimensionPixelSize(R.dimen.sesl_fading_edge_bottom_padding);
    }

    public int getFadingEdgeBottomHeight() {
        return mFadingEdgeBottomHeight;
    }

    public int getFadingEdgeTopHeight() {
        return mFadingEdgeTopHeight;
    }

    @Override
    public void forceBottomFadingEdgeClamped(int height) {
        mForcedFadingEdgeBottomHeight = Math.max(0, height);
    }

    @Override
    public void forceTopFadingEdgeClamped(int height) {
        float extraRatio = mExtraTopRatio;
        if (extraRatio > 0.0f) {
            height = (int) (extraRatio * height);
        }
        mForcedFadingEdgeTopHeight = Math.max(0, height);
    }

    @Override
    public int getFadingEdgeBottomOffset() {
        return mFadingEdgeBottomOffset;
    }

    @Override
    public void hideBottomFadingEdge(boolean hide) {
        if (mHideBottom != hide) {
            mHideBottom = hide;
            View view = mTargetView;
            if (view != null) {
                view.invalidate();
            }
        }
    }

    @Override
    public void hideTopFadingEdge(boolean hide) {
        if (mHideTop != hide) {
            mHideTop = hide;
            View view = mTargetView;
            if (view != null) {
                view.invalidate();
            }
        }
    }

    @Override
    public boolean isFadingEdgeEnabled() {
        return mIsFadingEdgeEnabled;
    }

    @Override
    public void prepareFadingEffect(Canvas canvas, int left, int top, int right, int bottom) {
        if (mIsFadingEdgeEnabled) {
            int distanceFromWindowBottom = getDistanceFromWindowBottom();
            mDistanceFromWindowBottom = distanceFromWindowBottom;
            mRectForFadingEffect = new Rect(left, top, right, bottom - distanceFromWindowBottom);
        }
    }

    @Override
    public void renderFadingEffect(Canvas canvas, ScrollInfoProvider scrollInfoProvider) {
        if (!mIsFadingEdgeEnabled || mRectForFadingEffect == null) {
            return;
        }
        int topHeight = calculateDynamicTopHeight(scrollInfoProvider);
        renderBottomFadingEdge(canvas, calculateDynamicBottomHeight(scrollInfoProvider));
        renderTopFadingEdge(canvas, topHeight);
    }

    @Override
    public void setAllowTopFadingEdgeWithoutEdgeToEdge(boolean allow) {
        mAllowTopFadingEdgeEdgeWithoutEdgeToEdge = allow;
    }

    @Override
    public void setBottomFadingEdgeOverrides(@Nullable SeslBottomFadingEdgeOverrides overrides) {
        if (overrides == null || overrides.isEmpty()) {
            overrides = null;
        }
        mBottomFadingEdgeOverrides = overrides;
        View view = mTargetView;
        if (view != null && mIsFadingEdgeEnabled) {
            view.invalidate();
        }
    }

    @Override
    public void setFadingEdgeBottomOffset(int offset) {
        mFadingEdgeBottomOffset = offset;
    }

    @Override
    public void setFadingEdgeColor(@ColorInt int color) {
        setColorImmediate(color);
    }

    @Override
    public void setFadingEdgeColor(@ColorInt int color, @Nullable Runnable runnable) {
        int previousColor = getPreviousColor();
        if (mColorStateManager.shouldAnimateColorChange(previousColor, color)) {
            animateColorChange(previousColor, color, runnable);
        } else {
            setColorImmediate(color);
        }
    }

    @Override
    public void setFadingEdgeEnabled(boolean enabled) {
        Resources resources = mContext.getResources();
        setFadingEdgeEnabled(enabled,
                resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_top_height),
                resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_bottom_height));
        mFadingEdgeOnNaviBarBottomHeight = resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_on_navi_bar_bottom_height);
    }

    @Override
    public void setFadingEdgeEnabled(boolean enabled, int topHeight, int bottomHeight) {
        mExtendTopFadingEdge = false;
        mExtendBottomFadingEdge = false;
        if (mIsFadingEdgeEnabled != enabled || (enabled && (mFadingEdgeTopHeight != topHeight || mFadingEdgeBottomHeight != bottomHeight))) {
            mIsFadingEdgeEnabled = enabled;
            if (enabled) {
                mFadingEdgeTopHeight = topHeight;
                mFadingEdgeBottomHeight = bottomHeight;
                mTopFadingEdgeGradient = createFadingEdgeGradient(mGradientTopColors, GRADIENT_POSITION_TOP);
                mBottomFadingEdgeGradient = createFadingEdgeGradient(mGradientBottomColors, GRADIENT_POSITION_BOTTOM);
            } else {
                mTopFadingEdgeGradient = null;
                mBottomFadingEdgeGradient = null;
                mFadingEdgeBottomPadding = 0;
            }
        }
        setOnApplyWindowInsetsListener();
    }

    @Override
    @Deprecated
    public void setFadingEdgeEnabled(boolean enabled, int topHeight, int bottomHeight, boolean customized) {
        mIsAppCustomized = customized;
        setFadingEdgeEnabled(enabled, topHeight, bottomHeight);
    }

    @Override
    public void setFadingEdgeEnabled(boolean enabled, boolean extendTop, boolean extendBottom) {
        Resources resources = mContext.getResources();
        int topHeight;
        int bottomHeight;
        if (extendTop) {
            mExtraTopFadingEdgeGradient = createFadingEdgeGradient(mGradientTopExtraColors, GRADIENT_POSITION_TOP_EXTRA);
            topHeight = resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_extra_top_height);
            mExtraTopRatio = (float) topHeight / resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_top_height);
        } else {
            mExtraTopFadingEdgeGradient = null;
            mExtraTopRatio = -1.0f;
            topHeight = resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_top_height);
        }
        if (extendBottom) {
            mExtraBottomFadingEdgeGradient = createFadingEdgeGradient(mGradientBottomExtraColors, GRADIENT_POSITION_BOTTOM_EXTRA);
            bottomHeight = resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_extra_bottom_height);
            mFadingEdgeOnNaviBarBottomHeight = resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_on_navi_bar_extra_bottom_height);
        } else {
            mExtraBottomFadingEdgeGradient = null;
            bottomHeight = resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_bottom_height);
            mFadingEdgeOnNaviBarBottomHeight = resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_on_navi_bar_bottom_height);
        }
        setFadingEdgeEnabled(enabled, topHeight, bottomHeight);
        mExtendTopFadingEdge = extendTop;
        mExtendBottomFadingEdge = extendBottom;
    }

    @Override
    public void setForceLegacyXfermode(boolean force) {
    }

    @Override
    public void setTargetView(View view) {
        mTargetView = view;
    }

    @Override
    public void setTopFadingEdgeOverrides(@Nullable SeslTopFadingEdgeOverrides overrides) {
        if (overrides == null || overrides.isEmpty()) {
            overrides = null;
        }
        mTopFadingEdgeOverrides = overrides;
        View view = mTargetView;
        if (view != null && mIsFadingEdgeEnabled) {
            view.invalidate();
        }
    }

    @Override
    public void setWindowBottomAlignment(boolean align) {
        mWindowBottomAlignment = align;
    }
}
