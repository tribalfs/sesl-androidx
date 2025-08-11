package androidx.appcompat.animation;

import android.view.animation.Interpolator;

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

/**
 * An elastic interpolator based on Samsung's design language.  This class
 * produces an easing curve that overshoots the target value and then
 * oscillates back towards it, simulating an elastic spring behaviour.  The
 * implementation is derived from the decompiled smali code contained in the
 * provided apk.  See {@link #getInterpolation(float)} for usage.
 */
@RestrictTo(Scope.LIBRARY_GROUP)
class SeslElasticInterpolator implements Interpolator {
    private float mAmplitude;
    private float mPeriod;

    /**
     * Constructs a new elastic interpolator.
     *
     * @param amplitude the amplitude of the overshoot, where {@code 1.0} means
     *                  the oscillation overshoots by 100 % of the target value
     * @param period    the period of the oscillation in seconds.  A smaller
     *                  period results in faster oscillations
     */
    public SeslElasticInterpolator(float amplitude, float period) {
        mAmplitude = amplitude;
        mPeriod = period;
    }

    /**
     * Computes the elastic "out" easing curve.  This helper is extracted
     * directly from the smali implementation.  It returns the value of the
     * curve at time {@code t} given the specified amplitude and period.
     */
    private float out(float t, float amplitude, float period) {
        // Start and end conditions: if the animation hasn't begun or has
        // completed, return the appropriate boundary value.
        if (t == 0f) {
            return 0f;
        }
        final float one = 1f;
        if (t >= one) {
            return one;
        }
        // Default the period to the canonical value if a zero period was
        // supplied.  This matches the smali code which substitutes 0.3f.
        if (period == 0f) {
            period = 0.3f;
        }
        float s;
        // If the amplitude is zero or less than one, normalise it to one and
        // compute the offset as a quarter of the period.  Otherwise compute
        // the offset such that the curve starts at zero.  The calculation
        // derives from the identity for the inverse sine of 1/a.
        if (amplitude == 0f || amplitude < one) {
            amplitude = one;
            s = period / 4f;
        } else {
            double p = period / (2 * Math.PI);
            s = (float) (Math.asin(one / amplitude) * p);
        }
        // Apply the elastic easing formula: scaled exponential decay multiplied
        // by a sinusoid with the computed phase offset, then translated to end
        // at 1.
        return (float) (amplitude * Math.pow(2d, -10d * t)
                * Math.sin((t - s) * (2d * Math.PI) / period) + 1d);
    }

    /**
     * Returns the amplitude of this interpolator.
     */
    public float getAmplitude() {
        return mAmplitude;
    }

    /**
     * Returns the period of this interpolator.
     */
    public float getPeriod() {
        return mPeriod;
    }

    /**
     * Computes the interpolated value.  This implementation delegates to
     * {@link #out(float, float, float)} which implements an elastic ease‐out
     * curve.
     *
     * @param input a value between 0 and 1 indicating the elapsed fraction
     *              of the animation
     * @return the interpolated output value
     */
    @Override
    public float getInterpolation(float input) {
        return out(input, mAmplitude, mPeriod);
    }

    /**
     * Sets the amplitude of this interpolator.
     */
    public void setAmplitude(float amplitude) {
        mAmplitude = amplitude;
    }

    /**
     * Sets the period of this interpolator.
     */
    public void setPeriod(float period) {
        mPeriod = period;
    }
}