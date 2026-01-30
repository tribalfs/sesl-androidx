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

package androidx.appcompat.animation;


import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.R;
import java.util.ArrayList;

/*
 * Original code by Samsung, all rights reserved to the original author. Added in sesl7
 */

/**
 * Provides a recoil animation effect for views, typically used for press and release interactions.
 * This class manages the animation of a target view, scaling it down on press and back to its
 * original size on release.
 *
 * <p>It supports scaling the entire view or only its children if the target view is a ViewGroup.
 * The animation uses predefined interpolators and durations for press and release states.
 *
 * <p>A {@link Holder} class is provided to manage multiple SeslRecoilAnimator instances,
 * allowing for efficient reuse of animators.
 *
 * <p><b>Note:</b> This class requires API level 29 or higher.
 */
@RequiresApi(api = 29)
public class SeslRecoilAnimator {
    private static final int PRESS_INTERPOLATR = R.anim.sesl_recoil_pressed;
    private static final int RELEASE_INTERPOLATR = R.anim.sesl_recoil_released;
    private static final long mPressDuration = 100;
    private static  final long mReleaseDuration = 350;
    private static final float mScaleSizeDp = 3f;

    private static TimeInterpolator sPressInterpolator;
    private static TimeInterpolator sReleaseInterpolator;
    final ValueAnimator mAnimator;
    private final Context mContext;
    private float mScaleRatio;
    View mTarget;

    private boolean mIsScaleOnlyChildren = false;
    private boolean mIsPressed = false;

    /**
     * Holder class for managing multiple {@link SeslRecoilAnimator} instances.
     *
     * <p>This class provides a convenient way to create, reuse, and manage recoil animations
     * for different views. It helps optimize resource usage by reusing animators when possible.
     *
     * <p>Usage example:
     * <pre>
     * Holder recoilHolder = new Holder(context);
     * view.setOnTouchListener((v, event) -> {
     *     switch (event.getAction()) {
     *         case MotionEvent.ACTION_DOWN:
     *             recoilHolder.setPress(v);
     *             break;
     *         case MotionEvent.ACTION_UP:
     *         case MotionEvent.ACTION_CANCEL:
     *             recoilHolder.setRelease();
     *             break;
     *     }
     *     return false; // Or true if the event is consumed
     * });
     * </pre>
     */
    public static class Holder {
        ArrayList<SeslRecoilAnimator> mAnimators = new ArrayList<>();
        private final Context mContext;

        public Holder(@NonNull Context context) {
            this.mContext = context;
        }

        /**
         * Creates a new {@link SeslRecoilAnimator} for the given view or reuses an existing one.
         * <p>
         * This method first checks if an animator already exists for the given view or if there is an
         * inactive animator that can be reused to set its target to the given view.
         * If no animator can be reused, this method creates a new animator.
         *
         * @param view The view for which to create or reuse an animator.
         * @return A {@link SeslRecoilAnimator} for the given view.
         */
        @NonNull
        private SeslRecoilAnimator createOrReuseAnimator(View view) {
            for (SeslRecoilAnimator animator : mAnimators) {
                if (animator.mTarget == view) {
                    return animator;
                }
            }
            for (SeslRecoilAnimator animator : mAnimators) {
                if (!animator.isActive()) {
                    animator.mTarget = view;
                    return animator;
                }
            }
            SeslRecoilAnimator seslRecoilAnimator = new SeslRecoilAnimator(view, this.mContext);
            mAnimators.add(seslRecoilAnimator);
            return seslRecoilAnimator;
        }


        /**
         * Removes all {@link ValueAnimator.AnimatorUpdateListener} objects from all
         * {@link SeslRecoilAnimator} instances managed by this holder.
         */
        public void removeAllUpdateListeners() {
            for (SeslRecoilAnimator animator : mAnimators) {
                if (animator.isActive()) {
                    animator.mAnimator.end();
                }
                animator.mAnimator.removeAllUpdateListeners();
            }
            mAnimators.clear();
        }

        /**
         * Initiates the press animation on the specified view.
         *
         * <p>If the view is clickable, this method will either create a new {@link SeslRecoilAnimator}
         * or reuse an existing one associated with the view to start the press animation.
         * The press animation typically involves a scaling effect.
         *
         * @param view The {@link View} to apply the press animation to. Must not be null.
         */
        public void setPress(@NonNull View view) {
            if (view.isClickable()) {
                createOrReuseAnimator(view).setPress();
            }
        }

        /**
         * Initiates the release animation for all active recoil animators.
         * This method iterates through all managed {@link SeslRecoilAnimator} instances
         * and calls their {@link SeslRecoilAnimator#setRelease()} method if they are currently active.
         */
        public void setRelease() {
            this.mAnimators.forEach((animator) -> {
                if (animator.isActive()) {
                    animator.setRelease();
                }
            });
        }
    }

    /**
     * Constructs a new SeslRecoilAnimator.
     *
     * @param view The target view to animate.
     * @param context The context to use for loading resources.
     */
    public SeslRecoilAnimator(@NonNull View view, @NonNull Context context) {
        mTarget = view;
        mContext = context;
        setScaleOnlyChildren(true);
        mAnimator = ValueAnimator.ofFloat(1.0f);
        mAnimator.setCurrentFraction(1.0f);
        if (sPressInterpolator == null) {
            sPressInterpolator = AnimationUtils.loadInterpolator(mContext, PRESS_INTERPOLATR);
        }
        if (sReleaseInterpolator == null) {
            sReleaseInterpolator = AnimationUtils.loadInterpolator(mContext, RELEASE_INTERPOLATR);
        }
        mAnimator.addUpdateListener(animation -> {
            if (mIsScaleOnlyChildren && (mTarget instanceof ViewGroup)) {
                setScaleChildren((Float) animation.getAnimatedValue());
            } else {
                setScale((Float) animation.getAnimatedValue());
            }
        });
    }

    private void setScale(float f) {
        mTarget.setScaleX(f);
        mTarget.setScaleY(f);
    }

    private void setScaleChildren(float scale) {
        ViewGroup viewGroup = (ViewGroup) mTarget;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            Matrix matrix = new Matrix();
            float dx = (mTarget.getWidth() / 2.0f) - child.getLeft();
            float dy = (mTarget.getHeight() / 2.0f) - child.getTop();
            matrix.setTranslate(-dx, -dy);
            matrix.postScale(scale, scale);
            matrix.postTranslate(dx, dy);
            child.setAnimationMatrix(matrix);
        }
    }

    private void setScaleRatioBySize() {
        float width = mTarget.getWidth();
        mScaleRatio = (width - (mContext.getResources().getDisplayMetrics().density * mScaleSizeDp)) / width;
    }


    /**
     * Checks if the recoil animation is currently active.
     * <p>
     * An animation is considered active if it's either in the "pressed" state
     * (meaning a press event has been initiated but not yet released) or if the
     * underlying {@link ValueAnimator} is currently running.
     *
     * @return {@code true} if the animation is active, {@code false} otherwise.
     */
    public boolean isActive() {
        return mIsPressed || mAnimator.isRunning();
    }

    /**
     * Starts the press animation.
     * <p>
     * This method calculates the scale ratio based on the target view's size,
     * then initiates a scaling animation to simulate a press effect.
     * If an animation is already running, it will be cancelled and a new
     * press animation will start from the current animated value.
     * The animation uses a predefined press interpolator and duration.
     * </p>
     */
    public void setPress() {
        setScaleRatioBySize();
        if (mIsPressed) {
            return;
        }
        mIsPressed = true;
        if (mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        float animatedValue = (Float) mAnimator.getAnimatedValue();
        if (animatedValue == 0.0f) {
            animatedValue = 1.0f;
        }
        mAnimator.setFloatValues(animatedValue, mScaleRatio);
        mAnimator.setDuration(mPressDuration);
        mAnimator.setInterpolator(sPressInterpolator);
        mAnimator.start();
    }

    /**
     * Starts the release animation.
     * <p>
     * If the view is currently pressed ({@link #mIsPressed} is true), this method will:
     * <ul>
     *     <li>Set {@link #mIsPressed} to false.
     *     <li>Cancel any ongoing animation.
     *     <li>Set up the animator to animate from the current animated value to 1.0f (original scale).
     *     <li>Set the animation duration to {@link #mReleaseDuration}.
     *     <li>Set the interpolator to {@link #sReleaseInterpolator}.
     *     <li>Start the animation.
     * </ul>
     */
    public void setRelease() {
        if (mIsPressed) {
            mIsPressed = false;
            if (mAnimator.isRunning()) {
                mAnimator.cancel();
            }

            mAnimator.setFloatValues((Float) mAnimator.getAnimatedValue(), 1.0f);
            mAnimator.setDuration(mReleaseDuration);
            mAnimator.setInterpolator(sReleaseInterpolator);
            mAnimator.start();
        }
    }

    /**
     * Sets whether to scale only the children of the target view.
     *
     * <p>If enabled and the target view is a {@link ViewGroup}, only its children will be scaled.
     * Otherwise, the target view itself will be scaled. This is false by default.
     *
     * @param enable {@code true} to scale only children, {@code false} otherwise.
     */
    public void setScaleOnlyChildren(boolean enable) {
        if (mTarget instanceof ViewGroup) {
            mIsScaleOnlyChildren = enable;
        } else {
            mIsScaleOnlyChildren = false;
        }
    }
}