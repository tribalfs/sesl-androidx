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

package androidx.core.util;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;
import static androidx.core.util.SeslBottomFadingEdgeOverrides.*;
import static androidx.core.util.SeslBottomFadingEdgeOverrides.SLOT_BOTTOM_WITH_TASK_BAR;
import static androidx.reflect.DeviceInfo.isOneUI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RuntimeXfermode;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;

import androidx.core.R;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;

import androidx.core.view.WindowInsetsCompat;
import androidx.reflect.graphics.SeslCanvasReflector;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

//sesl9

/**
 * AGSL RuntimeShader based fading edge renderer for SESL scrollable widgets.
 */

@RestrictTo(LIBRARY_GROUP_PREFIX)
public class SeslFadingEdgeHelperImpl implements SeslFadingEdgeHelper {

    private static final long COLOR_ANIMATION_DURATION = 300L;
    private static final long HEIGHT_ANIMATION_DURATION = 300L;
    private static final String NAVIGATION_MODE = "navigation_mode";
    private static final int NAV_BAR_MODE_3BUTTON = 0;
    private static final int NAV_BAR_MODE_GESTURAL = 2;
    private static final int NONE_COLOR = 0;
    private static final String SEM_TASK_BAR_AVAILABLE = "sem_task_bar_available";
    private static final String TAG = "SeslFadingEdgeHelperImpl";

    private static final String XFERMODE_DST_OUT_SHADER =
            "vec4 main(half4 src, half4 dst) {"
            + "    half alpha = (1-src.a)*dst.a;"
            + "    half3 color =  (1-src.a)*dst.rgb;"
            + "    return vec4(color.rgb, alpha);"
            + "}";

    private static final String XFERMODE_SRC_OVER_SHADER =
            "vec4 main(half4 src, half4 dst) {"
            + "    half alpha = src.a + (1-src.a)*dst.a;"
            + "    half3 color = src.rgb* src.a + (1-src.a)*dst.rgb;"
            + "    return vec4(color.rgb, alpha);"
            + "}";

    private SeslBottomFadingEdgeOverrides mBottomFadingEdgeOverrides;
    private int mCanvasSaveCount;
    private int mColor;
    private final Context mContext;
    private final FadingEdgeHeights mHeights;
    private Rect mRectForFadingEffect;
    View mTargetView;
    private SeslTopFadingEdgeOverrides mTopFadingEdgeOverrides;
    private Paint mFadingEdgePaint = new Paint();
    private final Matrix mFadingEdgeMatrix = new Matrix();
    private final SeslFadingEdgeShaderController mShaderController = new SeslFadingEdgeShaderController();
    private float mExtraTopRatio = -1.0f;
    private boolean mIsFadingEdgeEnabled = false;
    private int mForcedFadingEdgeTopHeight = 0;
    private int mForcedFadingEdgeBottomHeight = 0;
    private int mTopSaveCount = -1;
    private int mBottomSaveCount = -1;
    private int mFadingEdgeBottomOffset = 0;
    private int mDistanceFromWindowBottom = 0;
    private int mNaviBarTop = -1;
    private int mStatusBarHeight = 0;
    private boolean mIsAppCustomized = false;
    private boolean mIsStatusBarOverlapped = false;
    private boolean mIsNaviBarOverlapped = false;
    private boolean mHideTop = false;
    private boolean mHideBottom = false;
    private boolean mAllowTopFadingEdgeEdgeWithoutEdgeToEdge = false;
    private boolean mWindowBottomAlignment = true;
    private boolean mIsTaskBarAvailable = false;
    private boolean mForceLegacyXfermode = false;

    private final ColorAnimationManager mAnimationManager = new ColorAnimationManager();
    private final HeightAnimationManager mTopHeightAnimationManager = new HeightAnimationManager();
    private final HeightAnimationManager mTopOnStatusBarHeightAnimationManager = new HeightAnimationManager();
    private final HeightAnimationManager mBottomHeightAnimationManager = new HeightAnimationManager();
    private final HeightAnimationManager mBottomOnNaviBarHeightAnimationManager = new HeightAnimationManager();

    /**
     * Animates color change over time.
     */
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
                applyColor(blendedColor);
                if (runnable != null) {
                    runnable.run();
                }
            });
            mAnimator.start();
        }
    }

    /**
     * Animates fading edge height over time.
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public class HeightAnimationManager {
        private ValueAnimator mAnimator;
        int mCurrentHeight = 0;
        int mTargetHeight = 0;

        public HeightAnimationManager() {
        }

        public void cancelCurrentAnimation() {
            ValueAnimator valueAnimator = mAnimator;
            if (valueAnimator != null && valueAnimator.isRunning()) {
                mAnimator.cancel();
            }
        }

        public int getCurrentHeight() {
            return mCurrentHeight;
        }

        public boolean isAnimating() {
            ValueAnimator valueAnimator = mAnimator;
            return valueAnimator != null && valueAnimator.isRunning();
        }

        public void setCurrentHeight(int height) {
            mCurrentHeight = height;
        }

        public void startAnimation(final int startHeight, final int targetHeight) {
            cancelCurrentAnimation();
            mCurrentHeight = startHeight;
            mTargetHeight = targetHeight;
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            mAnimator = valueAnimatorOfFloat;
            valueAnimatorOfFloat.setDuration(HEIGHT_ANIMATION_DURATION);
            mAnimator.addUpdateListener(animation -> {
                mCurrentHeight = (int) (((targetHeight - startHeight) * animation.getAnimatedFraction()) + startHeight);
                if (mTargetView != null) {
                    mTargetView.invalidate();
                }
            });
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    HeightAnimationManager.this.mCurrentHeight = targetHeight;
                    HeightAnimationManager.this.mTargetHeight = targetHeight;
                    if (mTargetView != null) {
                        mTargetView.invalidate();
                    }
                }
            });
            mAnimator.start();
        }

        public void updateHeight(int targetHeight, boolean animate) {
            if (!animate) {
                setCurrentHeight(targetHeight);
                return;
            }
            int currentHeight = mCurrentHeight;
            if (currentHeight != targetHeight) {
                startAnimation(currentHeight, targetHeight);
            }
        }
    }

    /**
     * Identifies edge orientation and properties.
     */
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

    /**
     * Holds the fading edge heights resolved from resources.
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public static class FadingEdgeHeights {
        public final int bottomHeight;
        public final int bottomHeightExtra;
        public final int bottomOnNaviBarHeight;
        public final int bottomOnNaviBarHeightExtra;
        public final int bottomOnNaviBarHeightWithTaskBar;
        public final int topHeight;
        public final int topHeightExtra;
        public final int topOnStatusBarHeight;
        public final int topOnStatusBarHeightExtra;

        public FadingEdgeHeights(@NonNull Resources resources) {
            this.topHeight = resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_top_height);
            this.topHeightExtra = resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_extra_top_height);
            this.topOnStatusBarHeight = resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_on_status_bar_top_height);
            this.topOnStatusBarHeightExtra = resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_on_status_bar_extra_top_height);
            this.bottomHeight = resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_bottom_height);
            this.bottomHeightExtra = resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_extra_bottom_height);
            this.bottomOnNaviBarHeight = resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_on_navi_bar_bottom_height);
            this.bottomOnNaviBarHeightExtra = resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_on_navi_bar_extra_bottom_height);
            this.bottomOnNaviBarHeightWithTaskBar = resources.getDimensionPixelSize(R.dimen.sesl_fading_edge_on_navi_bar_bottom_height_with_task_bar);
        }
    }

    public SeslFadingEdgeHelperImpl(@NonNull Context context) {
        mContext = context;
        mHeights = new FadingEdgeHeights(context.getResources());
        if (Build.VERSION.SDK_INT >= 36 && isOneUI()/*custom*/) {
            setFadingEdgeColor(NONE_COLOR);
        } else {
            setFadingEdgeColor(getRoundedCornerColor(context));
        }
    }

    @NonNull
    public static SeslFadingEdgeHelper createSeslFadingEdgeHelper(@NonNull Context context) {
        return (Build.VERSION.SDK_INT >= 33)
                ? new SeslFadingEdgeHelperImpl(context)
                : new SeslFadingEdgeLegacyHelperImpl(context);
    }

    private void animateColorChange(int startColor, int targetColor, Runnable runnable) {
        mAnimationManager.startAnimation(startColor, targetColor, runnable);
    }

    void applyColor(int color) {
        int rgb = color & 0x00FFFFFF;
        mColor = rgb;
        mShaderController.applyColorToAllShaders(rgb);
    }

    private int calculateBottomHeight() {
        return mShaderController.isExtendBottomFadingEdge()
                ? resolveBottomHeight(SLOT_BOTTOM_EXTRA, mHeights.bottomHeightExtra)
                : resolveBottomHeight(SLOT_BOTTOM, mHeights.bottomHeight);
    }

    private int calculateBottomOnNaviBarHeight() {
        if (mShaderController.isExtendBottomFadingEdge()) {
            return resolveBottomHeight(SLOT_BOTTOM_EXTRA_WITH_NAVI_BAR, mHeights.bottomOnNaviBarHeightExtra);
        }
        return mIsTaskBarAvailable
                ? resolveBottomHeight(SLOT_BOTTOM_WITH_TASK_BAR, mHeights.bottomOnNaviBarHeightWithTaskBar)
                : resolveBottomHeight(SLOT_BOTTOM_WITH_NAVI_BAR, mHeights.bottomOnNaviBarHeight);
    }

    private int calculateDynamicBottomHeight(ScrollInfoProvider scrollInfoProvider) {
        if (mTargetView == null) {
            return 0;
        }
        Rect rect = mRectForFadingEffect;
        int top = rect.top;
        int bottom = rect.bottom;
        int fadingEdgeBottomOnNaviBarHeight = mIsNaviBarOverlapped
                ? getFadingEdgeBottomOnNaviBarHeight()
                : getFadingEdgeBottomHeight();
        if (top + fadingEdgeBottomOnNaviBarHeight > bottom - fadingEdgeBottomOnNaviBarHeight) {
            fadingEdgeBottomOnNaviBarHeight = (bottom - top) / 2;
        }
        int maxBottomHeight = Math.max(fadingEdgeBottomOnNaviBarHeight, 0);
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
            if (mDistanceFromWindowBottom != 0 || lastItemHeightVisibleRatio <= 0.0f) {
                targetHeight = maxBottomHeight;
            } else {
                targetHeight = (int) ((1.0f - lastItemHeightVisibleRatio) * maxBottomHeight);
            }
        } else if (mDistanceFromWindowBottom > 0) {
            targetHeight = maxBottomHeight;
        } else {
            targetHeight = normalizeHeightForShortScrollRange(scrollInfoProvider, remaining, range, maxBottomHeight);
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
        int fadingEdgeTopOnStatusBarHeight = mIsStatusBarOverlapped
                ? getFadingEdgeTopOnStatusBarHeight()
                : getFadingEdgeTopHeight();
        if (top + fadingEdgeTopOnStatusBarHeight > bottom - fadingEdgeTopOnStatusBarHeight) {
            fadingEdgeTopOnStatusBarHeight = (bottom - top) / 2;
        }
        int maxTopHeight = Math.max(fadingEdgeTopOnStatusBarHeight, 0);
        if (maxTopHeight == 0) {
            return 0;
        }
        int targetHeight = normalizeHeightForShortScrollRange(
                scrollInfoProvider,
                Math.clamp(scrollInfoProvider.computeVerticalScrollOffset(), 0, maxTopHeight),
                Math.max(scrollInfoProvider.computeVerticalScrollRange() - scrollInfoProvider.computeVerticalScrollExtent(), 0),
                maxTopHeight);
        if (!mAllowTopFadingEdgeEdgeWithoutEdgeToEdge) {
            int distanceFromWindowTop = getDistanceFromWindowTop();
            if (distanceFromWindowTop >= 0) {
                targetHeight = Math.max(targetHeight - distanceFromWindowTop, 0);
            }
        }
        return Math.min(Math.max(mForcedFadingEdgeTopHeight, targetHeight), fadingEdgeTopOnStatusBarHeight);
    }

    private int calculateTopHeight() {
        return mShaderController.isExtendTopFadingEdge()
                ? resolveTopHeight(1, mHeights.topHeightExtra)
                : resolveTopHeight(0, mHeights.topHeight);
    }

    private int calculateTopOnStatusBarHeight() {
        return mShaderController.isExtendTopFadingEdge()
                ? resolveTopHeight(3, mHeights.topOnStatusBarHeightExtra)
                : resolveTopHeight(2, mHeights.topOnStatusBarHeight);
    }

    private void clearFadingEdgeHeight() {
        mTopHeightAnimationManager.cancelCurrentAnimation();
        mTopOnStatusBarHeightAnimationManager.cancelCurrentAnimation();
        mBottomHeightAnimationManager.cancelCurrentAnimation();
        mBottomOnNaviBarHeightAnimationManager.cancelCurrentAnimation();
        mTopHeightAnimationManager.setCurrentHeight(0);
        mTopOnStatusBarHeightAnimationManager.setCurrentHeight(0);
        mBottomHeightAnimationManager.setCurrentHeight(0);
        mBottomOnNaviBarHeightAnimationManager.setCurrentHeight(0);
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

    private Xfermode getLegacyXfermode(int color) {
        return new PorterDuffXfermode(color == 0 ? PorterDuff.Mode.DST_OUT : PorterDuff.Mode.SRC_OVER);
    }

    private int getPreviousColor() {
        int targetColor = mAnimationManager.getTargetColor();
        return targetColor != NONE_COLOR ? targetColor : mColor;
    }

    @RequiresApi(37)
    private RuntimeXfermode getRuntimeXfermode(int color) {
        return new RuntimeXfermode(color == 0 ? XFERMODE_DST_OUT_SHADER : XFERMODE_SRC_OVER_SHADER);
    }

    private void initializeExtraTopRatio() {
        if (!mShaderController.isExtendTopFadingEdge()) {
            mExtraTopRatio = -1.0f;
        } else if (isStatusBarOverlapped()) {
            FadingEdgeHeights fadingEdgeHeights = mHeights;
            mExtraTopRatio = (float) fadingEdgeHeights.topOnStatusBarHeightExtra / fadingEdgeHeights.topOnStatusBarHeight;
        } else {
            FadingEdgeHeights fadingEdgeHeights2 = mHeights;
            mExtraTopRatio = (float) fadingEdgeHeights2.topHeightExtra / fadingEdgeHeights2.topHeight;
        }
    }

    private void initializeFadingEdgeHeight(boolean animate, int topHeight, int topOnStatusBarHeight, int bottomHeight, int bottomOnNaviBarHeight) {
        initializeExtraTopRatio();
        mTopHeightAnimationManager.updateHeight(topHeight, animate);
        mTopOnStatusBarHeightAnimationManager.updateHeight(topOnStatusBarHeight, animate);
        mBottomHeightAnimationManager.updateHeight(bottomHeight, animate);
        mBottomOnNaviBarHeightAnimationManager.updateHeight(bottomOnNaviBarHeight, animate);
    }

    private void initializeFadingEdgeHeight(boolean animate) {
        initializeFadingEdgeHeight(animate, calculateTopHeight(), calculateTopOnStatusBarHeight(), calculateBottomHeight(), calculateBottomOnNaviBarHeight());
    }

    private void initializeShaders() {
        mShaderController.initializeShaders(mColor);
        if (mShaderController.isExtendTopFadingEdge()) {
            mShaderController.initializeExtraTopShader(mColor);
        } else {
            mShaderController.clearExtraTopShader();
        }
        if (mShaderController.isExtendBottomFadingEdge()) {
            mShaderController.initializeExtraBottomShader(mColor);
        } else {
            mShaderController.clearExtraBottomShader();
        }
    }

    private boolean isNaviBarOverlapped() {
        View view = mTargetView;
        if (view != null) {
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int viewBottom = view.getHeight() + location[1];
            int naviBarTop = mNaviBarTop;
            return naviBarTop > 0 && viewBottom > naviBarTop;
        }
        return false;
    }

    private boolean isNightMode(Context context) {
        return (context.getResources().getConfiguration().uiMode & 48) == 32;
    }

    private boolean isStatusBarOverlapped() {
        return mStatusBarHeight > 0;
    }

    private boolean isTaskBarAvailable() {
        try {
            return Settings.Global.getInt(mContext.getContentResolver(), SEM_TASK_BAR_AVAILABLE, 0) == 1;
        } catch (Exception e) {
            Log.w(TAG, "Failed to check task bar availability", e);
            return false;
        }
    }

    private int normalizeHeightForShortScrollRange(ScrollInfoProvider scrollInfoProvider, int currentVal, int range, int maxHeight) {
        return !shouldNormalizeFadingEdgeForNSV(scrollInfoProvider, range, maxHeight)
                ? currentVal
                : Math.min(Math.round(((float) currentVal / range) * maxHeight), maxHeight);
    }

    private void refreshBottomHeightAnimationTargets(boolean animate) {
        mBottomHeightAnimationManager.updateHeight(calculateBottomHeight(), animate);
        mBottomOnNaviBarHeightAnimationManager.updateHeight(calculateBottomOnNaviBarHeight(), animate);
    }

    private void refreshTopHeightAnimationTargets(boolean animate) {
        mTopHeightAnimationManager.updateHeight(calculateTopHeight(), animate);
        mTopOnStatusBarHeightAnimationManager.updateHeight(calculateTopOnStatusBarHeight(), animate);
    }

    private void renderBottomFadingEdge(Canvas canvas, int height) {
        if (mHideBottom) {
            return;
        }
        Rect rect = mRectForFadingEffect;
        renderFadingEdge(canvas, EdgeType.BOTTOM, height, mBottomSaveCount, rect.left, rect.bottom);
    }

    private void renderTopFadingEdge(Canvas canvas, int height) {
        if (mHideTop) {
            return;
        }
        Rect rect = mRectForFadingEffect;
        renderFadingEdge(canvas, EdgeType.TOP, height, mTopSaveCount, rect.left, rect.top);
    }

    private void renderFadingEdge(Canvas canvas, EdgeType edgeType, int height, int saveCount, float x, float y) {
        mFadingEdgeMatrix.setScale(1.0f, height);
        if (edgeType.getRotationDegrees() > 0.0f) {
            mFadingEdgeMatrix.postRotate(edgeType.getRotationDegrees());
        }
        mFadingEdgeMatrix.postTranslate(x, y);

        Shader shader = mShaderController.getGradientForEdge(edgeType == EdgeType.TOP);

        if (shader != null) {
            shader.setLocalMatrix(mFadingEdgeMatrix);
            mFadingEdgePaint.setShader(shader);
            if (getPreviousColor() == NONE_COLOR) {
                if (saveCount > 0) {
                    SeslCanvasReflector.restoreUnclippedLayer(canvas, saveCount, mFadingEdgePaint);
                    return;
                }
                return;
            }
            if (height > 0) {
                try {
                    if (edgeType == EdgeType.TOP) {
                        Rect rect = mRectForFadingEffect;
                        canvas.drawRect(rect.left, rect.top, rect.right, rect.top + height, mFadingEdgePaint);
                        return;
                    }
                    Rect rect = mRectForFadingEffect;
                    canvas.drawRect(rect.left, rect.bottom - height, rect.right, rect.bottom, mFadingEdgePaint);
                } catch (Exception e) {
                    Log.e(TAG, "Unable to draw on Canvas.", e);
                }
            }
        }
    }

    private int resolveBottomHeight(@BottomSlot int slot, int defaultHeight) {
        SeslBottomFadingEdgeOverrides overrides = mBottomFadingEdgeOverrides;
        return (overrides == null || overrides.isEmpty()) ? defaultHeight : overrides.resolveHeight(slot, defaultHeight);
    }

    private int resolveTopHeight(int slot, int defaultHeight) {
        SeslTopFadingEdgeOverrides overrides = mTopFadingEdgeOverrides;
        return (overrides == null || overrides.isEmpty()) ? defaultHeight : overrides.resolveHeight(slot, defaultHeight);
    }

    private void restoreCanvasState(Canvas canvas) {
        if (getPreviousColor() == NONE_COLOR) {
            canvas.restoreToCount(mCanvasSaveCount);
        }
        mRectForFadingEffect = null;
    }

    private void setColorImmediate(int color) {
        mAnimationManager.cancelCurrentAnimation();
        mAnimationManager.setTargetColorImmediate(color);
        applyColor(color);
    }

    private void setOnApplyWindowInsetsListener() {
        View view = mTargetView;
        if (view != null) {
            ViewCompat.setOnApplyWindowInsetsListener(view, (!mIsFadingEdgeEnabled || mIsAppCustomized) ? null : (v, insets) -> {
                int navBarBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                if (navBarBottom <= 0) {
                    mNaviBarTop = -1;
                } else if (Settings.Secure.getInt(v.getContext().getContentResolver(), NAVIGATION_MODE, 0) != NAV_BAR_MODE_GESTURAL) {
                    mNaviBarTop = Resources.getSystem().getDisplayMetrics().heightPixels - navBarBottom;
                }
                mStatusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                initializeExtraTopRatio();
                updateTopShaderType(true);
                mIsTaskBarAvailable = isTaskBarAvailable();
                mBottomOnNaviBarHeightAnimationManager.updateHeight(calculateBottomOnNaviBarHeight(), false);
                updateBottomShaderType(true);
                return insets;
            });
        }
    }

    private boolean shouldNormalizeFadingEdgeForNSV(ScrollInfoProvider scrollInfoProvider, int range, int maxHeight) {
        return scrollInfoProvider.shouldNormalizeFadingEdgeForDistance() && range > 0 && range < maxHeight;
    }

    private void updateBottomShaderType(boolean force) {
        boolean overlapped = !mIsAppCustomized && isNaviBarOverlapped();
        if (force || mIsNaviBarOverlapped != overlapped) {
            mIsNaviBarOverlapped = overlapped;
            mShaderController.updateBottomShaderType(overlapped, mIsTaskBarAvailable);
        }
    }

    private void updateTopShaderType(boolean force) {
        boolean overlapped = isStatusBarOverlapped();
        if (force || mIsStatusBarOverlapped != overlapped) {
            mIsStatusBarOverlapped = overlapped;
            mShaderController.updateTopShaderType(overlapped);
        }
    }

    public int getFadingEdgeBottomHeight() {
        return mBottomHeightAnimationManager.getCurrentHeight();
    }

    public int getFadingEdgeBottomOnNaviBarHeight() {
        return mBottomOnNaviBarHeightAnimationManager.getCurrentHeight();
    }

    public int getFadingEdgeTopHeight() {
        return mTopHeightAnimationManager.getCurrentHeight();
    }

    public int getFadingEdgeTopOnStatusBarHeight() {
        return mTopOnStatusBarHeightAnimationManager.getCurrentHeight();
    }

    public int getRoundedCornerColor(@NonNull Context context) {
        try {
            return mContext.getColor(mContext.getResources().getIdentifier(
                    isNightMode(mContext) ? "sesl_round_and_bgcolor_dark" : "sesl_round_and_bgcolor_light",
                    "color", mContext.getPackageName()));
        } catch (Resources.NotFoundException unused) {
            return NONE_COLOR;
        }
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
        if (mIsFadingEdgeEnabled && Build.VERSION.SDK_INT >= 33) {
            updateTopShaderType(false);
            updateBottomShaderType(false);
            int distanceFromWindowBottom = getDistanceFromWindowBottom();
            mDistanceFromWindowBottom = distanceFromWindowBottom;
            int fadingBottom = bottom - distanceFromWindowBottom;
            int topHeight = mIsStatusBarOverlapped
                    ? getFadingEdgeTopOnStatusBarHeight()
                    : getFadingEdgeTopHeight();
            if (top + topHeight > fadingBottom - topHeight) {
                topHeight = (fadingBottom - top) / 2;
            }
            int bottomHeight = mIsNaviBarOverlapped
                    ? getFadingEdgeBottomOnNaviBarHeight()
                    : getFadingEdgeBottomHeight();
            if (top + bottomHeight > fadingBottom - bottomHeight) {
                bottomHeight = (fadingBottom - top) / 2;
            }
            if (getPreviousColor() == NONE_COLOR) {
                mCanvasSaveCount = canvas.getSaveCount();
                mTopSaveCount = -1;
                mBottomSaveCount = -1;
                if (!mHideTop) {
                    mTopSaveCount = SeslCanvasReflector.saveUnclippedLayer(canvas, left, top, right, top + topHeight);
                }
                if (!mHideBottom) {
                    mBottomSaveCount = SeslCanvasReflector.saveUnclippedLayer(canvas, left, fadingBottom - bottomHeight, right, fadingBottom);
                }
            }
            mRectForFadingEffect = new Rect(left, top, right, fadingBottom);
        }
    }

    @Override
    public void renderFadingEffect(@NonNull Canvas canvas, ScrollInfoProvider scrollInfoProvider) {
        if (!mIsFadingEdgeEnabled || mRectForFadingEffect == null || Build.VERSION.SDK_INT < 33) {
            return;
        }
        int topHeight = calculateDynamicTopHeight(scrollInfoProvider);
        renderBottomFadingEdge(canvas, calculateDynamicBottomHeight(scrollInfoProvider));
        renderTopFadingEdge(canvas, topHeight);
        restoreCanvasState(canvas);
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
        mShaderController.setBottomFadingEdgeOverrides(overrides);
        if (!mIsFadingEdgeEnabled || mIsAppCustomized) {
            return;
        }
        refreshBottomHeightAnimationTargets(true);
        updateBottomShaderType(true);
        View view = mTargetView;
        if (view != null) {
            view.invalidate();
        }
    }

    @Override
    public void setFadingEdgeBottomOffset(int offset) {
        mFadingEdgeBottomOffset = offset;
    }

    @Override
    public void setFadingEdgeColor(@ColorInt int color) {
        setFadingEdgeColor(color, null);
    }

    @Override
    public void setFadingEdgeColor(@ColorInt int color, @Nullable Runnable runnable) {
        Paint paint = new Paint();
        mFadingEdgePaint = paint;
        if (Build.VERSION.SDK_INT < 37 || mForceLegacyXfermode) {
            paint.setXfermode(getLegacyXfermode(color));
        } else {
            paint.setXfermode(getRuntimeXfermode(color));
        }
        int previousColor;
        if (runnable == null || (previousColor = getPreviousColor()) == NONE_COLOR || color == NONE_COLOR) {
            setColorImmediate(color);
        } else {
            animateColorChange(previousColor, color, runnable);
        }
    }

    @Override
    public void setFadingEdgeEnabled(boolean enabled) {
        setFadingEdgeEnabled(enabled, false, false);
    }

    @Override
    public void setFadingEdgeEnabled(boolean enabled, int topHeight, int bottomHeight) {
        mIsAppCustomized = true;
        boolean wasEnabled = mIsFadingEdgeEnabled;
        mIsFadingEdgeEnabled = enabled;
        mShaderController.setExtendTopFadingEdge(false);
        mShaderController.setExtendBottomFadingEdge(false);
        if (enabled) {
            initializeFadingEdgeHeight(wasEnabled, topHeight, topHeight, bottomHeight, bottomHeight);
            initializeShaders();
        } else {
            clearFadingEdgeHeight();
            mShaderController.clearShaders();
        }
        setOnApplyWindowInsetsListener();
    }

    @Override
    @Deprecated
    public void setFadingEdgeEnabled(boolean enabled, int topHeight, int bottomHeight, boolean unused) {
        setFadingEdgeEnabled(enabled, topHeight, bottomHeight);
    }

    @Override
    public void setFadingEdgeEnabled(boolean enabled, boolean extendTop, boolean extendBottom) {
        boolean animate = mIsFadingEdgeEnabled
                && !(mShaderController.isExtendTopFadingEdge() == extendTop
                && mShaderController.isExtendBottomFadingEdge() == extendBottom);
        mIsFadingEdgeEnabled = enabled;
        mShaderController.setExtendTopFadingEdge(extendTop);
        mShaderController.setExtendBottomFadingEdge(extendBottom);
        if (enabled) {
            mIsTaskBarAvailable = isTaskBarAvailable();
            initializeFadingEdgeHeight(animate);
            initializeShaders();
        } else {
            clearFadingEdgeHeight();
            mShaderController.clearShaders();
        }
        setOnApplyWindowInsetsListener();
    }

    @Override
    public void setForceLegacyXfermode(boolean force) {
        mForceLegacyXfermode = force;
        mShaderController.setForceLegacyXfermode(force);
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
        mShaderController.setTopFadingEdgeOverrides(overrides);
        if (!mIsFadingEdgeEnabled || mIsAppCustomized) {
            return;
        }
        refreshTopHeightAnimationTargets(true);
        updateTopShaderType(true);
        View view = mTargetView;
        if (view != null) {
            view.invalidate();
        }
    }

    @Override
    public void setWindowBottomAlignment(boolean align) {
        mWindowBottomAlignment = align;
    }
}
