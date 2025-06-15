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

package androidx.picker.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.PathInterpolator;

import androidx.annotation.NonNull;

/**
 * Provides reveal animation for the {@link SeslCircularSeekBarView}.
 *
 * <p>This class manages the animation that reveals the circular seek bar,
 * typically when it first becomes visible or when its content changes significantly.
 * It uses a {@link ValueAnimator} to smoothly transition the reveal effect.
 *
 * <p>The animation progresses from 0.0f to 1.0f, where these values represent
 * the completion state of the reveal. The associated {@link SeslCircularSeekBarView}
 * is updated at each animation step to redraw itself based on the current reveal value.
 *
 * <p>Key properties of the animation include:
 * <ul>
 *     <li>Duration: {@link #REVEAL_ANIMATION_DURATION} (800 milliseconds)
 *     <li>Interpolator: A {@link PathInterpolator} with control points (0.22f, 0.25f, 0.0f, 1.0f)
 *         for a custom easing effect.
 * </ul>
 *
 * <p>The class also tracks the state of the animation (whether it's currently running)
 * via the {@code mIsRevealAnimation} flag.
 */
public  final class SeslCircularSeekBarRevealAnimation {

    private static final long REVEAL_ANIMATION_DURATION = 800;
    private static final String TAG = "CircularRevealAnimation";
    private float mSweepProgress;
    boolean mIsRevealAnimation;
    SeslCircularSeekBarView mView;

    final PathInterpolator mAniInterpolator = new PathInterpolator(0.22f, 0.25f, 0.0f, 1.0f);

        public SeslCircularSeekBarRevealAnimation(@NonNull View view) {
        if (view instanceof SeslCircularSeekBarView) {
            mView = (SeslCircularSeekBarView) view;
        }
    }

    public void setmSweepProgress(float f) {
        this.mSweepProgress = f;
    }

    public float getmSweepProgress() {
        return this.mSweepProgress;
    }

    public void startAnimators() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setDuration(REVEAL_ANIMATION_DURATION).setInterpolator(mAniInterpolator);
        ofFloat.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(@NonNull Animator animator) {
                animator.cancel();
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                Log.d(TAG, "onAnimationEnd()");
                mIsRevealAnimation = false;
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {
            }

            @Override
            public void onAnimationStart(@NonNull Animator animator) {
                Log.d(TAG, "onAnimationStart()");
                mIsRevealAnimation = true;
            }
        });
        ofFloat.addUpdateListener(valueAnimator -> {
            mView.setRevealAnimationValue((Float) valueAnimator.getAnimatedValue());
            mView.invalidate();
        });
        ofFloat.start();
    }
}
