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

package androidx.fragment.app;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Resources;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;

import androidx.annotation.RestrictTo;
import androidx.fragment.R;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;

//sesl9
/**
 * Helper for constructing and updating predictive back and standard fragment transition animators on One UI.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public class SeslFragmentTransitionHelper {
    private static final float AFTER_MOST_TRANSLATE_MIN_SWIPE = -0.33f;
    private static final int ALPHA_DURATION = 150;
    private static final int DEPTH_IN_DURATION = 450;
    private static final int DEPTH_OUT_DURATION = 400;
    private static final float PROGRESS_INPUT_MAX = 1.0f;
    private static final float PROGRESS_INPUT_MID = 0.5f;
    private static final float PROGRESS_MAPPED_MAX = 0.6f;
    private static final float PROGRESS_MAPPED_MIN = 0.5f;

    private static final PathInterpolator DEPTH_OUT_INTERPOLATION = new PathInterpolator(0.22f, 0.25f, 0.0f, 1.0f);
    private static final PathInterpolator DEPTH_IN_INTERPOLATION = new PathInterpolator(0.22f, 0.5f, 0.0f, 1.0f);
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();

    private static final EnumMap<AnimationType, AnimatorStrategy> STRATEGIES;

    private final Context mContext;
    private final int mScreenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private View mView;

    public enum AnimationType {
        CLOSE_EXIT(R.animator.sesl_fragment_close_exit),
        CLOSE_ENTER(R.animator.sesl_fragment_close_enter),
        OPEN_ENTER(R.animator.sesl_fragment_open_enter),
        OPEN_EXIT(R.animator.sesl_fragment_open_exit),
        CLOSE_EXIT_RTL(R.animator.sesl_fragment_close_exit_rtl),
        CLOSE_ENTER_RTL(R.animator.sesl_fragment_close_enter_rtl),
        OPEN_ENTER_RTL(R.animator.sesl_fragment_open_enter_rtl),
        OPEN_EXIT_RTL(R.animator.sesl_fragment_open_exit_rtl);

        private static final SparseArray<AnimationType> LOOKUP = new SparseArray<>();
        final int resId;

        static {
            for (AnimationType type : values()) {
                LOOKUP.put(type.resId, type);
            }
        }

        AnimationType(int resId) {
            this.resId = resId;
        }

        @Nullable
        public static AnimationType fromResId(int resId) {
            return LOOKUP.get(resId);
        }
    }

    @FunctionalInterface
    public interface AnimatorStrategy {
        AnimatorSet build(SeslFragmentTransitionHelper helper, boolean isPop, boolean isRtl, TransitionGeometry geometry);
    }

    public static final class TransitionGeometry {
        private final int width;
        private final int leftMargin;
        private final int rightMargin;

        public TransitionGeometry(int width, int[] margins) {
            this.width = width;
            this.leftMargin = margins[0];
            this.rightMargin = margins[1];
        }

        public int getWidth() {
            return width;
        }

        public int getLeftMargin() {
            return leftMargin;
        }

        public int getRightMargin() {
            return rightMargin;
        }
    }

    static {
        STRATEGIES = new EnumMap<>(AnimationType.class);

        AnimatorStrategy closeExitStrategy = (helper, isPop, isRtl, geometry) ->
                helper.animatorSetOf(helper.buildTranslateXAnimator(
                        isPop ? helper.getInterpolator(true) : LINEAR_INTERPOLATOR,
                        DEPTH_OUT_DURATION,
                        helper.mView.getTranslationX() + geometry.getLeftMargin(),
                        geometry.getWidth()
                ));

        AnimatorStrategy closeEnterStrategy = (helper, isPop, isRtl, geometry) -> {
            ObjectAnimator translateX = helper.buildTranslateXAnimator(
                    isPop ? helper.getInterpolator(true) : LINEAR_INTERPOLATOR,
                    DEPTH_OUT_DURATION,
                    isRtl ? helper.mView.getTranslationX() + geometry.getLeftMargin()
                          : (geometry.getWidth() + geometry.getLeftMargin() + geometry.getRightMargin()) * AFTER_MOST_TRANSLATE_MIN_SWIPE,
                    geometry.getLeftMargin()
            );
            return (!isPop || isRtl) ? helper.animatorSetOf(translateX)
                    : helper.animatorSetOf(translateX, helper.buildAlphaAnimator(ALPHA_DURATION, 0.0f, 1.0f));
        };

        AnimatorStrategy openEnterStrategy = (helper, isPop, isRtl, geometry) ->
                helper.animatorSetOf(helper.buildTranslateXAnimator(
                        helper.getInterpolator(false),
                        DEPTH_IN_DURATION,
                        geometry.getWidth(),
                        geometry.getLeftMargin()
                ));

        AnimatorStrategy openExitStrategy = (helper, isPop, isRtl, geometry) ->
                helper.animatorSetOf(
                        helper.buildTranslateXAnimator(
                                helper.getInterpolator(false),
                                DEPTH_IN_DURATION,
                                geometry.getLeftMargin(),
                                geometry.getWidth() * AFTER_MOST_TRANSLATE_MIN_SWIPE
                        ),
                        helper.buildAlphaAnimator(ALPHA_DURATION, 1.0f, 0.0f)
                );

        STRATEGIES.put(AnimationType.CLOSE_EXIT, closeExitStrategy);
        STRATEGIES.put(AnimationType.CLOSE_ENTER, closeEnterStrategy);
        STRATEGIES.put(AnimationType.OPEN_ENTER, openEnterStrategy);
        STRATEGIES.put(AnimationType.OPEN_EXIT, openExitStrategy);

        STRATEGIES.put(AnimationType.CLOSE_EXIT_RTL, (helper, isPop, isRtl, geometry) ->
                closeExitStrategy.build(helper, isPop, isRtl, helper.mirrorGeometry(geometry)));
        STRATEGIES.put(AnimationType.CLOSE_ENTER_RTL, (helper, isPop, isRtl, geometry) ->
                closeEnterStrategy.build(helper, isPop, isRtl, helper.mirrorGeometry(geometry)));
        STRATEGIES.put(AnimationType.OPEN_ENTER_RTL, (helper, isPop, isRtl, geometry) ->
                openEnterStrategy.build(helper, isPop, isRtl, helper.mirrorGeometry(geometry)));
        STRATEGIES.put(AnimationType.OPEN_EXIT_RTL, (helper, isPop, isRtl, geometry) ->
                openExitStrategy.build(helper, isPop, isRtl, helper.mirrorGeometry(geometry)));
    }

    public SeslFragmentTransitionHelper(@NonNull View view) {
        this.mView = view;
        this.mContext = view.getContext();
    }

    public void update(@NonNull View view) {
        if (this.mView != view) {
            this.mView = view;
        }
    }

    public void initTransition() {
        if (mView != null) {
            mView.setTranslationX(0.0f);
        }
    }

    public float getProgress(float progress) {
        float clamped = Math.clamp(progress, 0.0f, 1.0f);
        if (clamped > 0.5f && clamped <= 1.0f) {
            return lerp(0.5f, 0.6f, (clamped - 0.5f) / 0.5f);
        }
        return clamped;
    }

    @Nullable
    public AnimatorSet createAnimator(int resId, boolean isPop, boolean isRtl, boolean isPopOver) {
        if (isPopOver) {
            return loadPopOverAnimator(resId);
        }
        AnimationType animationType = AnimationType.fromResId(resId);
        if (animationType == null) {
            return null;
        }
        AnimatorStrategy strategy = STRATEGIES.get(animationType);
        if (strategy == null) {
            return null;
        }
        return strategy.build(this, isPop, isRtl, getTransitionGeometry());
    }

    private AnimatorSet loadPopOverAnimator(int resId) {
        int popOverResId = getPopOverAnimatorResId(resId);
        if (popOverResId == 0) {
            return null;
        }
        Animator animator = AnimatorInflater.loadAnimator(mContext, popOverResId);
        if (animator == null) {
            return null;
        }
        return animatorSetOf(animator);
    }

    private int getPopOverAnimatorResId(int resId) {
        AnimationType type = AnimationType.fromResId(resId);
        if (type == null) {
            return 0;
        }
        switch (type) {
            case CLOSE_EXIT:
            case CLOSE_EXIT_RTL:
                return R.animator.sesl_fragment_close_exit_pop_over;
            case CLOSE_ENTER:
            case CLOSE_ENTER_RTL:
                return R.animator.sesl_fragment_close_enter_pop_over;
            case OPEN_ENTER:
            case OPEN_ENTER_RTL:
                return R.animator.sesl_fragment_open_enter_pop_over;
            case OPEN_EXIT:
            case OPEN_EXIT_RTL:
                return R.animator.sesl_fragment_open_exit_pop_over;
            default:
                return 0;
        }
    }

    private TransitionGeometry getTransitionGeometry() {
        return new TransitionGeometry(getEffectiveWidth(), getHorizontalMargins());
    }

    private int getEffectiveWidth() {
        return mView.getWidth() > 0 ? mView.getWidth() : mScreenWidth;
    }

    private int[] getHorizontalMargins() {
        int left = 0;
        int right = 0;
        ViewGroup.LayoutParams params = mView.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
            left = marginParams.leftMargin;
            right = marginParams.rightMargin;
        }
        return new int[]{left, right};
    }

    private TransitionGeometry mirrorGeometry(TransitionGeometry geometry) {
        return new TransitionGeometry(-geometry.getWidth(), new int[]{-geometry.getRightMargin(), -geometry.getLeftMargin()});
    }

    private Interpolator getInterpolator(boolean isDepthOut) {
        return isDepthOut ? DEPTH_OUT_INTERPOLATION : DEPTH_IN_INTERPOLATION;
    }

    private ObjectAnimator buildTranslateXAnimator(Interpolator interpolator, int duration, float from, float to) {
        return createAnimator(interpolator, duration, "x", Keyframe.ofFloat(0.0f, from), Keyframe.ofFloat(1.0f, to));
    }

    private ObjectAnimator buildAlphaAnimator(int duration, float from, float to) {
        return createAnimator(LINEAR_INTERPOLATOR, duration, "alpha", Keyframe.ofFloat(0.0f, from), Keyframe.ofFloat(1.0f, to));
    }

    private ObjectAnimator createAnimator(Interpolator interpolator, int duration, String propertyName, Keyframe... keyframes) {
        PropertyValuesHolder pvh = PropertyValuesHolder.ofKeyframe(propertyName, keyframes);
        ObjectAnimator animator = new ObjectAnimator();
        animator.setInterpolator(interpolator);
        animator.setValues(pvh);
        animator.setDuration(duration);
        return animator;
    }

    private AnimatorSet animatorSetOf(Animator... animators) {
        AnimatorSet animatorSet = new AnimatorSet();
        if (animators != null && animators.length > 0) {
            if (animators.length == 1) {
                animatorSet.play(animators[0]);
            } else {
                animatorSet.playTogether(animators);
            }
        }
        return animatorSet;
    }

    public static int getAlphaDuration() {
        return ALPHA_DURATION;
    }

    public static int getDepthOutDuration() {
        return DEPTH_OUT_DURATION;
    }

    public static int getDepthInDuration() {
        return DEPTH_IN_DURATION;
    }


    private static float lerp(float start, float stop, float amount) {
        return start + (stop - start) * amount;
    }
}
