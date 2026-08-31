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

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.ViewGroupOverlay;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.RestrictTo;
import androidx.reflect.view.SeslViewReflector;

import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

//sesl9
/**
 * Controls the floating "go to top" button shown over SESL scrollable widgets.
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public class SeslGoToTopController extends SeslBlurController {

    private static final String TAG = "SeslGoToTopController";

    private static final int AUTO_HIDE_REASON_CLICK = 0;
    static final int AUTO_HIDE_REASON_SHOWN_OR_UPDATE = 1;

    private static final int GO_TO_TOP_HIDE = 1500;

    public static final int GTT_STATE_NONE = 0;
    public static final int GTT_STATE_SHOWN = 1;
    public static final int GTT_STATE_PRESSED = 2;
    private static final int GTT_STATE_REQUEST_LAYOUT = -1;

    private static final int MOTION_EVENT_ACTION_PEN_DOWN = 211;
    private static final int MOTION_EVENT_ACTION_PEN_UP = 212;
    private static final int MOTION_EVENT_ACTION_PEN_MOVE = 213;

    /**
     * Host contract for the scrollable widget owning this controller.
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public interface Host {

        boolean canScrollDown();

        boolean canScrollUp();

        Context getContext();

        int getHeight();

        void getLocationInWindow(int[] location);

        ViewGroupOverlay getOverlay();

        int getPaddingBottom();

        int getPaddingLeft();

        int getPaddingRight();

        int getScrollY();

        int getWidth();

        void invalidateHost();

        boolean isFastScrollerEnabled();

        void playSoundEffect(int effectId);

        void post(@NonNull Runnable runnable);

        void postDelayed(@NonNull Runnable runnable, long delayMillis);

        void removeCallbacks(@NonNull Runnable runnable);

        void showTopEdgeEffect();

        void smoothScrollToTop();
    }

    /**
     * Listener notified when the GoToTop button is clicked.
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public interface OnGoToTopClickListener {

        boolean onGoToTopClick();
    }

    Host mHost;
    SeslGoToTopConfig mConfig;
    SeslGoToTopImageView mGoToTopView;
    Drawable mGoToTopImage;
    public final Rect mGoToTopRect = new Rect();

    public int mGoToTopState;
    public int mGoToTopLastState;

    public boolean mEnableGoToTop;
    public boolean mGoToTopSuppressed;

    public final SeslGoToTopAnimationHelper mHelper;
    public OnGoToTopClickListener mOnClickListener;

    public boolean mIsBlurEnabled = false;
    public boolean mIsLightTheme;
    private int mImmersiveBottomPadding = 0;

    public final Runnable mAutoHide;
    public final Runnable mFadeInRunnable;
    public final Runnable mFadeOutRunnable;

    //Sesl9
    private boolean mIsScrollRunning = false;

    private final BackgroundProvider mBackgroundProvider = new BackgroundProvider() {
        @Override
        public Drawable getBackgroundBlur() {
            return mConfig.getBackgroundBlur();
        }

        @Override
        public Drawable getBackgroundDark() {
            return mConfig.getBackgroundDark();
        }

        @Override
        public Drawable getBackgroundLight() {
            return mConfig.getBackgroundLight();
        }

        @Override
        public float getElevation() {
            return mConfig.getElevation();
        }

        @Override
        public float getOpaqueAlphaWithoutBlur() {
            return 0.9f;
        }
    };

    public SeslGoToTopController(@NonNull Host host, @NonNull SeslGoToTopConfig config) {
        mEnableGoToTop = false;
        mGoToTopSuppressed = false;
        mGoToTopState = GTT_STATE_NONE;
        mGoToTopLastState = GTT_STATE_NONE;

        mHelper = new SeslGoToTopAnimationHelper();

        mIsLightTheme = false;

        mFadeInRunnable = this::fadeIn;
        mFadeOutRunnable = this::fadeOut;

        mAutoHide = () -> applyState(GTT_STATE_NONE);

        mHost = host;
        mConfig = config;
    }

    public final void applyState(int state) {
        if (!isEnabled()) {
            return;
        }

        SeslGoToTopAnimationHelper helper = mHelper;
        if (!isEnvironmentAvailable()) {
            helper.setFadeOutCompleted();
            state = GTT_STATE_NONE;
        }

        mHost.removeCallbacks(mAutoHide);

        if (state == GTT_STATE_SHOWN && !mHost.canScrollUp()) {
            state = GTT_STATE_NONE;
        }

        if (state == GTT_STATE_REQUEST_LAYOUT && mConfig.isSizeChanged()) {
            state = (mHost.canScrollUp() || mHost.canScrollDown()) ? mGoToTopLastState : GTT_STATE_NONE;
        } else if (state == GTT_STATE_REQUEST_LAYOUT && (mHost.canScrollUp() || mHost.canScrollDown())) {
            state = GTT_STATE_SHOWN;
        }

        if (state != GTT_STATE_NONE) {
            mHost.removeCallbacks(mFadeOutRunnable);
        }

        if (state != GTT_STATE_SHOWN) {
            mHost.removeCallbacks(mFadeInRunnable);
        }

        if (helper.isFadeOutIdle() && state == GTT_STATE_NONE && mGoToTopLastState != GTT_STATE_NONE) {
            mHost.post(mFadeOutRunnable);
        }

        if (state != GTT_STATE_PRESSED) {
            mGoToTopView.setPressed(false);
        }

        mGoToTopState = state;

        if (state != GTT_STATE_NONE) {
            if (state == GTT_STATE_SHOWN || state == GTT_STATE_PRESSED) {
                mHost.removeCallbacks(mFadeOutRunnable);
                computeTargetRect();
            }
        } else if (helper.isFadeOutDone()) {
            mGoToTopRect.set(0, 0, 0, 0);
        }

        if (helper.isFadeOutDone()) {
            helper.setFadeOutIdle();
        }

        applyLayout();

        if (state == GTT_STATE_SHOWN && (mGoToTopLastState == GTT_STATE_NONE
                || mGoToTopView.getAlpha() == SeslGoToTopAnimationConfig.ALPHA_TRANSPARENT
                || mConfig.isSizeChanged())) {
            mHost.post(mFadeInRunnable);
        }

        mConfig.setSizeChanged(false);
        mGoToTopLastState = mGoToTopState;
    }

    public final void autoHide(int mode) {
        if (!isAvailable()) {
            return;
        }

        if (mode == AUTO_HIDE_REASON_CLICK) {
            if (mHost.isFastScrollerEnabled()) {
                return;
            }
        } else if (mode != AUTO_HIDE_REASON_SHOWN_OR_UPDATE) {
            return;
        }

        mHost.removeCallbacks(mAutoHide);
        mHost.postDelayed(mAutoHide, getAutoHideDelayMs());
    }

    public boolean contains(int x, int y) {
        return mGoToTopRect.contains(x, y);
    }

    public final void cleanupOnDisable() {
        mHost.removeCallbacks(mAutoHide);
        mHost.removeCallbacks(mFadeInRunnable);
        mHost.removeCallbacks(mFadeOutRunnable);
        mHelper.release();

        SeslGoToTopImageView view = mGoToTopView;
        if (view != null) {
            if (mIsBlurEnabled) {
                SeslViewReflector.semSetBlurInfo(view, null);
                mIsBlurEnabled = false;
            }

            try {
                mHost.getOverlay().remove(view);
            } catch (Exception ignored) {
            }

            view.setImageDrawable(null);
        }

        mGoToTopImage = null;
        mGoToTopLastState = GTT_STATE_NONE;
        mGoToTopState = GTT_STATE_NONE;
        mGoToTopRect.set(0, 0, 0, 0);
        mGoToTopView = null;
    }

    private void applyLayout() {
        if (mGoToTopView != null) {
            mGoToTopView.layout(mGoToTopRect.left, mGoToTopRect.top, mGoToTopRect.right,
                    mGoToTopRect.bottom);
        }
    }

    public final void computeTargetRect() {
        int paddingLeft = mHost.getPaddingLeft();
        int contentRight = mHost.getWidth() - mHost.getPaddingRight();

        final int halfSize = mConfig.getSize() / 2;
        final int height = mHost.getHeight();

        int centerX = (contentRight - paddingLeft) / 2 + paddingLeft;
        int minCenter = paddingLeft + halfSize;
        if (centerX < minCenter) {
            centerX = minCenter;
        }

        int maxCenter = contentRight - halfSize;
        if (centerX > maxCenter) {
            centerX = maxCenter;
        }

        int paddingBottom = mConfig.getPaddingBottom() + mImmersiveBottomPadding;
        mGoToTopRect.set(centerX - halfSize, height - mConfig.getSize() - paddingBottom,
                centerX + halfSize, height - paddingBottom);
    }

    public final boolean dispatchHoverEvent(MotionEvent event) {
        if (!isAvailable()) {
            return false;
        }

        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        if (action == MotionEvent.ACTION_HOVER_EXIT) {
            if (mGoToTopRect.contains(x, y)) {
                return true;
            }

            if (mGoToTopState == GTT_STATE_PRESSED) {
                mGoToTopState = GTT_STATE_SHOWN;
                mGoToTopView.setPressed(false);
                autoHide(AUTO_HIDE_REASON_SHOWN_OR_UPDATE);
            }
        }

        return false;
    }

    public final void draw() {
        if (!isEnabled()) {
            return;
        }

        mGoToTopView.setTranslationY(mHost.getScrollY());

        if (mGoToTopState != GTT_STATE_NONE && !mHost.canScrollUp()) {
            applyState(GTT_STATE_NONE);
        }

        if (isAvailable()) {
            return;
        }

        mGoToTopView.setAlpha(SeslGoToTopAnimationConfig.ALPHA_TRANSPARENT);
    }

    public int getAutoHideDelayMs() {
        return GO_TO_TOP_HIDE;
    }

    public final void invalidate() {
        if (isAvailable() && mIsBlurEnabled) {
            mGoToTopView.invalidate();
        }
    }

    public boolean isAvailable() {
        return isEnvironmentAvailable() && isEnabled();
    }

    public final boolean isEnabled() {
        return mEnableGoToTop && mGoToTopView != null;
    }

    public final boolean isEnvironmentAvailable() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) mHost.getContext()
                .getSystemService(Context.ACCESSIBILITY_SERVICE);

        if (accessibilityManager != null && accessibilityManager.isEnabled()) {
            String enabledServices = Settings.Secure.getString(
                    mHost.getContext().getContentResolver(), "enabled_accessibility_services");

            if (enabledServices != null) {
                boolean talkBackOrUniversalSwitchEnabled = enabledServices.matches(
                        "(?i).*com.samsung.accessibility/com.samsung.android.app.talkback.TalkBackService.*")
                        || enabledServices.matches(
                        "(?i).*com.samsung.android.accessibility.talkback/com.samsung.android.marvin.talkback.TalkBackService.*")
                        || enabledServices.matches(
                        "(?i).*com.google.android.marvin.talkback.TalkBackService.*")
                        || enabledServices.matches(
                        "(?i).*com.samsung.accessibility/com.samsung.accessibility.universalswitch.UniversalSwitchService.*");

                if (talkBackOrUniversalSwitchEnabled) {
                    return false;
                }
            }
        }

        return mHost.getHeight() > mConfig.getOverlayFeatureHiddenHeightPx();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!isAvailable()) {
            return false;
        }

        int action = event.getActionMasked();
        int x = (int) (event.getX() + 0.5f);
        int y = (int) (event.getY() + 0.5f);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MOTION_EVENT_ACTION_PEN_DOWN:
                if (mGoToTopState != GTT_STATE_PRESSED && mGoToTopRect.contains(x, y)) {
                    applyState(GTT_STATE_PRESSED);
                    mGoToTopView.setPressed(true);
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
            case MOTION_EVENT_ACTION_PEN_MOVE:
                if (mGoToTopState == GTT_STATE_PRESSED) {
                    if (!mGoToTopRect.contains(x, y)) {
                        mGoToTopState = GTT_STATE_SHOWN;
                        mGoToTopView.setPressed(false);
                        autoHide(AUTO_HIDE_REASON_SHOWN_OR_UPDATE);
                    }
                    return true;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                if (mGoToTopState != GTT_STATE_NONE) {
                    if (mGoToTopState == GTT_STATE_PRESSED) {
                        mGoToTopState = GTT_STATE_SHOWN;
                    }
                    mGoToTopView.setPressed(false);
                }
                // fall through to up handling
            case MotionEvent.ACTION_UP:
            case MOTION_EVENT_ACTION_PEN_UP:
                if (mGoToTopState == GTT_STATE_PRESSED) {
                    if (mHost.canScrollUp()) {
                        OnGoToTopClickListener listener = mOnClickListener;
                        if (listener == null || !listener.onGoToTopClick()) {
                            mIsScrollRunning = true;
                            mHost.smoothScrollToTop();
                        }
                        return true;
                    }

                    autoHide(AUTO_HIDE_REASON_CLICK);
                    mHost.playSoundEffect(SoundEffectConstants.CLICK);
                    return true;
                }
                break;
        }

        return false;
    }

    private void fadeIn() {
        mHelper.playShow(mGoToTopView);
    }

    private void fadeOut() {
        mHelper.playHide(mGoToTopView);
    }

    public final void setBlurEnabled(boolean enable, boolean isLightTheme) {
        if (!isEnabled() || enable == mIsBlurEnabled) {
            return;
        }

        setBlurEnabledInternal(mGoToTopView, enable, isLightTheme, mBackgroundProvider);
    }

    public final void setBottomPadding(int paddingBottom) {
        if (paddingBottom < 0 || !isEnabled()) {
            return;
        }

        SeslGoToTopConfig config = mConfig;
        if (paddingBottom == config.getPaddingBottom()) {
            return;
        }

        config.setPaddingBottom(paddingBottom);
        config.setSizeChanged(true);

        boolean visible = mGoToTopState != GTT_STATE_NONE;
        if (visible || mHelper.isFadeOutRunning()) {
            computeTargetRect();
            applyLayout();

            if (visible) {
                invalidate();
            }
        }
    }

    public final void setEnabled(boolean enable, boolean isLightTheme) {
        if (mEnableGoToTop && mIsLightTheme != isLightTheme) {
            cleanupOnDisable();
            mEnableGoToTop = false;
        }

        mIsLightTheme = isLightTheme;

        if (!isEnabled()) {
            mGoToTopImage = mConfig.getIcon(isLightTheme);

            SeslGoToTopImageView view = new SeslGoToTopImageView(mHost.getContext());
            mGoToTopView = view;

            if (view.mWindowLocationProvider != this) {
                view.mWindowLocationProvider = this;
            }

            view.setImageDrawable(mGoToTopImage);
        }

        SeslGoToTopImageView view = mGoToTopView;
        if (view == null || enable == mEnableGoToTop) {
            return;
        }

        mEnableGoToTop = enable;

        if (!enable) {
            cleanupOnDisable();
            return;
        }

        view.setAlpha(SeslGoToTopAnimationConfig.ALPHA_TRANSPARENT);
        mHost.getOverlay().add(view);

        setBlurEnabled(true, isLightTheme);

        mHelper.init(view, mIsBlurEnabled, () -> applyState(GTT_STATE_NONE));
    }

    public void showIfNeeded() {
        if (mGoToTopSuppressed || !mEnableGoToTop || !mHost.canScrollUp() || mGoToTopState == GTT_STATE_PRESSED) {
            return;
        }
        if (mGoToTopState != GTT_STATE_SHOWN) {
            applyState(GTT_STATE_SHOWN);
        }
        autoHide(AUTO_HIDE_REASON_SHOWN_OR_UPDATE);
    }

    public void setOnGoToTopClickListener(@Nullable OnGoToTopClickListener listener) {
        mOnClickListener = listener;
    }

    public void updateConfig(@NonNull SeslGoToTopConfig config) {
        mConfig = config;
    }

    public void release() {
        if (mEnableGoToTop) {
            cleanupOnDisable();
            mEnableGoToTop = false;
        }
    }

    public void setPaddingHorizontal(int left, int right) {
        if (left < 0 || right < 0 || !isEnabled()) {
            return;
        }
        if (mConfig.getPaddingLeft() != left) {
            mConfig.setPaddingLeft(left);
        }
        if (mConfig.getPaddingRight() != right) {
            mConfig.setPaddingRight(right);
        }
        if (mGoToTopState != GTT_STATE_NONE || mHelper.isFadeOutRunning()) {
            computeTargetRect();
            applyLayout();
        }
    }

    public void setImmersiveBottomPadding(int padding) {
        if (padding >= 0 && isEnabled()) {
            if (((mHost.getHeight() - mConfig.getSize()) - mConfig.getPaddingBottom()) - padding < 0) {
                mImmersiveBottomPadding = 0;
                Log.e(TAG, "The Immersive padding value (" + padding + ") was too large to draw GoToTop.");
                return;
            }
            mImmersiveBottomPadding = padding;
            if (mGoToTopState != GTT_STATE_NONE || mHelper.isFadeOutRunning()) {
                computeTargetRect();
                applyLayout();
            }
        }
    }

    public void setOverlayFeatureHiddenHeightPx(int height) {
        mConfig.setOverlayFeatureHiddenHeightPx(height);
    }

    public boolean verifyDrawable(@NonNull Drawable dr) {
        return mGoToTopImage == dr;
    }

    public final void setScrollRunning(boolean running) {
        mIsScrollRunning = running;
    }

    public final boolean isScrollRunning() {
        return this.mIsScrollRunning;
    }

    public int getBottomPadding() {
        return mConfig != null ? mConfig.getPaddingBottom() : 0;
    }

    public int getDefaultBottomPadding() {
        return mConfig != null ? mConfig.getDefaultPaddingBottom() : 0;
    }

    public SeslGoToTopImageView getView() {
        return mGoToTopView;
    }

    public void hideIfNeeded() {
        if (mGoToTopState != GTT_STATE_NONE) {
            applyState(GTT_STATE_NONE);
        }
    }

    public void setSuppressed(boolean suppressed) {
        mGoToTopSuppressed = suppressed;
        if (suppressed) {
            hideIfNeeded();
        }
    }

    public int getState() {
        return mGoToTopState;
    }

    public void setState(int state) {
        if (this.mGoToTopState != state) {
            this.mGoToTopState = state;
        }
    }

    public void setSizeChanged(boolean isChanged) {
        mConfig.setSizeChanged(isChanged);
    }

    public void onSizeChanged() {
        applyState(GTT_STATE_REQUEST_LAYOUT);
        autoHide(AUTO_HIDE_REASON_SHOWN_OR_UPDATE);
    }
}
