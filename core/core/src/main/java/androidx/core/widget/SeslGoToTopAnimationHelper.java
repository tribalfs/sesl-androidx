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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.dynamicanimation.animation.FloatValueHolder;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

/**
 * Holds alpha and scale animation state for the GoToTop button.
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public class SeslGoToTopAnimationHelper {
    private static final int FADE_OUT_STATE_NONE = 0;
    private static final int FADE_OUT_STATE_RUNNING = 1;
    private static final int FADE_OUT_STATE_COMPLETED = 2;

    private ValueAnimator mAlphaAnimator;
    private SpringAnimation mSpringAnimation;

    private float mAlphaTargetValue;
    private int mFadeOutState = FADE_OUT_STATE_NONE;

    private boolean mIsBlueEnabled;
    private Runnable mOnFadeOutEnded;

    private static float getTargetAlpha(boolean isBlurEnabled) {
        return isBlurEnabled ? SeslGoToTopAnimationConfig.ALPHA_OPAQUE
                : SeslGoToTopAnimationConfig.ALPHA_OPAQUE_WITHOUT_BLUR;
    }

    boolean isFadeInAnimatorRunning() {
        if (mAlphaAnimator == null || !mAlphaAnimator.isRunning()) {
            return false;
        }
        return mAlphaTargetValue == SeslGoToTopAnimationConfig.ALPHA_OPAQUE
                || mAlphaTargetValue == SeslGoToTopAnimationConfig.ALPHA_OPAQUE_WITHOUT_BLUR;
    }

    boolean isFadeOutAnimatorRunning() {
        if (mAlphaAnimator == null || !mAlphaAnimator.isRunning()) {
            return false;
        }
        return mAlphaTargetValue == SeslGoToTopAnimationConfig.ALPHA_TRANSPARENT;
    }

    void setFadeOutState(int state) {
        if (mFadeOutState != state) {
            mFadeOutState = state;
        }
    }

    private void startAlphaAnimator(float from, float to) {
        mAlphaTargetValue = to;
        mAlphaAnimator.removeAllListeners();
        if (to == SeslGoToTopAnimationConfig.ALPHA_TRANSPARENT) {
            mAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    setFadeOutState(FADE_OUT_STATE_COMPLETED);
                    if (mOnFadeOutEnded != null) {
                        mOnFadeOutEnded.run();
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    setFadeOutState(FADE_OUT_STATE_RUNNING);
                }
            });
        }
        mAlphaAnimator.setFloatValues(from, to);
        mAlphaAnimator.start();
    }

    void startSpringTo(float to) {
        mSpringAnimation.animateToFinalPosition(to * SeslGoToTopAnimationConfig.SPRING_ANIMATION_SCALE_FACTOR);
    }

    public void cancelAll() {
        if (mAlphaAnimator != null) {
            mAlphaAnimator.cancel();
        }
        if (mSpringAnimation != null) {
            mSpringAnimation.cancel();
        }
        setFadeOutIdle();
    }

    public void init(@NonNull final SeslGoToTopImageView view, boolean isBlurEnabled, Runnable onFadeOutEnded) {
        mIsBlueEnabled = isBlurEnabled;
        mOnFadeOutEnded = onFadeOutEnded;
        if (mAlphaAnimator == null) {
            mAlphaAnimator = new ValueAnimator();
            mAlphaAnimator.setDuration(SeslGoToTopAnimationConfig.getDuration());
            mAlphaAnimator.setInterpolator(SeslGoToTopAnimationConfig.getAlphaInterpolator());
            mAlphaAnimator.addUpdateListener(animation -> {
                try {
                    view.setAlpha((Float) animation.getAnimatedValue());
                } catch (Exception ignored) {
                }
            });
        }
        if (mSpringAnimation == null) {
            SpringForce force = new SpringForce()
                    .setDampingRatio(SeslGoToTopAnimationConfig.SPRING_DAMPING_RATIO)
                    .setStiffness(SeslGoToTopAnimationConfig.SPRING_STIFFNESS);
            mSpringAnimation = new SpringAnimation(new FloatValueHolder());
            mSpringAnimation.setStartValue(SeslGoToTopAnimationConfig.SCALE_MIN
                    * SeslGoToTopAnimationConfig.SPRING_ANIMATION_SCALE_FACTOR);
            mSpringAnimation.setSpring(force);
            mSpringAnimation.addUpdateListener((animation, value, velocity) -> {
                float scale = value / SeslGoToTopAnimationConfig.SPRING_ANIMATION_SCALE_FACTOR;
                view.setScaleX(scale);
                view.setScaleY(scale);
            });
        }
    }

    public boolean isFadeOutDone() {
        return mFadeOutState == FADE_OUT_STATE_COMPLETED;
    }

    public boolean isFadeOutIdle() {
        return mFadeOutState == FADE_OUT_STATE_NONE;
    }

    public boolean isFadeOutRunning() {
        return mFadeOutState == FADE_OUT_STATE_RUNNING;
    }

    public boolean isReady() {
        return mAlphaAnimator != null && mSpringAnimation != null;
    }

    public void playHide(@NonNull SeslGoToTopImageView view) {
        if (isReady() && !isFadeOutAnimatorRunning()) {
            if (isFadeInAnimatorRunning()) {
                mAlphaAnimator.cancel();
            }
            if (mSpringAnimation.isRunning()) {
                mSpringAnimation.cancel();
            }
            startSpringTo(SeslGoToTopAnimationConfig.SCALE_MIN);
            startAlphaAnimator(view.getAlpha(), SeslGoToTopAnimationConfig.ALPHA_TRANSPARENT);
        }
    }

    public void playShow(@NonNull SeslGoToTopImageView view) {
        if (isReady() && !isFadeInAnimatorRunning()) {
            if (isFadeOutAnimatorRunning()) {
                mAlphaAnimator.cancel();
            }
            if (mSpringAnimation.isRunning()) {
                mSpringAnimation.cancel();
            }
            startSpringTo(SeslGoToTopAnimationConfig.SCALE_MAX);
            startAlphaAnimator(view.getAlpha(), getTargetAlpha(mIsBlueEnabled));
        }
    }

    public void release() {
        cancelAll();
        mAlphaAnimator = null;
        mSpringAnimation = null;
    }

    public void setFadeOutCompleted() {
        setFadeOutState(FADE_OUT_STATE_COMPLETED);
    }

    public void setFadeOutIdle() {
        setFadeOutState(FADE_OUT_STATE_NONE);
    }
}
