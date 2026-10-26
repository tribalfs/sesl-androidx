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

import android.graphics.Color;
import android.graphics.RuntimeShader;
import android.graphics.Shader;
import android.os.Build;

import androidx.annotation.RestrictTo;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;

//sesl9
/**
 * Controller for AGSL-based fading edge shaders used by SESL scrollable widgets.
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public final class SeslFadingEdgeShaderController {

    private static final float DEFAULT_START_ALPHA = 0.04f;
    private static final float GESTURE_BAR_ONLY_START_ALPHA = 0.2f;

    public static final float[] INTERPOLATOR_TOP = {0.38f, 0.0f, 0.54f, 1.0f};
    public static final float[] INTERPOLATOR_TOP_EXTRA = {0.62f, 0.0f, 0.74f, 1.0f};
    public static final float[] INTERPOLATOR_TOP_WITH_STATUS_BAR = {0.42f, 0.0f, 0.58f, 1.0f};
    public static final float[] INTERPOLATOR_TOP_EXTRA_WITH_STATUS_BAR = {0.7f, 0.0f, 0.8f, 1.0f};
    public static final float[] INTERPOLATOR_BOTTOM_WITH_TASK_BAR = {0.8f, 0.0f, 0.35f, 1.0f};
    public static final float[] INTERPOLATOR_BOTTOM_WITH_NAVI_BAR = {0.46f, 0.0f, 0.58f, 1.0f};
    public static final float[] INTERPOLATOR_BOTTOM_EXTRA_WITH_NAVI_BAR = {0.35f, 0.0f, 0.6f, 1.0f};
    public static final float[] INTERPOLATOR_BOTTOM = {0.35f, 0.0f, 0.4f, 1.0f};
    public static final float[] INTERPOLATOR_BOTTOM_EXTRA = {0.35f, 0.0f, 0.6f, 1.0f};

    private static final float[][] DEFAULT_BOTTOM_INTERPOLATORS = {
            INTERPOLATOR_BOTTOM,
            INTERPOLATOR_BOTTOM_EXTRA,
            INTERPOLATOR_BOTTOM_WITH_NAVI_BAR,
            INTERPOLATOR_BOTTOM_WITH_TASK_BAR,
            INTERPOLATOR_BOTTOM_EXTRA_WITH_NAVI_BAR
    };

    private static final float[][] DEFAULT_TOP_INTERPOLATORS = {
            INTERPOLATOR_TOP,
            INTERPOLATOR_TOP_EXTRA,
            INTERPOLATOR_TOP_WITH_STATUS_BAR,
            INTERPOLATOR_TOP_EXTRA_WITH_STATUS_BAR
    };

    private static final String SHADER_SRC =
            "uniform float2 resolution;"
            + "uniform vec4 color;"
            + "uniform float startAlpha;"
            + "uniform vec4 easing;"
            + "uniform float ditherStrength;"
            + "uniform int customXfer;"
            + "float cubicBezier(float t, float x1, float y1, float x2, float y2) {"
            + "    if (t <= 0.0) return 0.0;"
            + "    if (t >= 1.0) return 1.0;"
            + "    float s = t;"
            + "    float u2, s2, s3;"
            + "    for (int i = 0; i < 6; i++) {"
            + "        float u = 1.0 - s;"
            + "        u2 = u * u;"
            + "        s2 = s * s;"
            + "        float x_current = 3.0 * u2 * s * x1 + 3.0 * u * s2 * x2 + s2 * s;"
            + "        float dx_ds = 3.0 * u2 * x1 + 6.0 * u * s * (x2 - x1) + 3.0 * s2 * (1.0 - x2);"
            + "        if (abs(dx_ds) < 0.0001) break;"
            + "        s = s - (x_current - t) / dx_ds;"
            + "        s = clamp(s, 0.0, 1.0);"
            + "        if (abs(x_current - t) < 0.0001) break;"
            + "    }"
            + "    float u = 1.0 - s;"
            + "    u2 = u * u;"
            + "    s2 = s * s;"
            + "    s3 = s2 * s;"
            + "    return 3.0 * u2 * s * y1 + 3.0 * u * s2 * y2 + s3;"
            + "}"
            + "float random(vec2 st) {"
            + "    return fract(sin(dot(st.xy, vec2(12.9898,78.233))) * 43758.5453123);"
            + "}"
            + "float n2rand_faster(vec2 n, float k) {"
            + "     float nrnd0 = random( n );"
            + "     float orig = k * (nrnd0 * 2.0 - 1.0);"
            + "     nrnd0 = orig * inversesqrt(abs(orig));"
            + "     nrnd0 = max(-k, nrnd0);"
            + "     nrnd0 = k * (nrnd0 - sign(orig));"
            + "     return nrnd0;"
            + "}"
            + "vec3 randomDither(vec2 uv, vec3 col) {"
            + "    float bitError = ditherStrength/255.0;"
            + "    float r = n2rand_faster(uv, 1.0);"
            + "    return col + vec3(r * bitError);"
            + "}"
            + "float randomDither2(vec2 uv, float alpha) {"
            + "    float bitError = ditherStrength/255.0;"
            + "    float r = n2rand_faster(uv, 1.0);"
            + "    return alpha + (r * bitError);"
            + "}"
            + "vec4 main(vec2 fragCoord) {"
            + "    float t = clamp(fragCoord.y / resolution.y, 0.0, 1.0);"
            + "    t = clamp(t * (1.0 - startAlpha) + startAlpha, 0.0, 1.0);"
            + "    float eased;"
            + "    eased = 1.0 - cubicBezier(t, easing.x, easing.y, easing.z, easing.w);"
            + "    float alpha = clamp(eased, 0.0, 1.0);"
            + "    if (ditherStrength > 0.0) {"
            + "        alpha = randomDither2(abs(fragCoord), alpha);"
            + "    }"
            + "    if (customXfer == 0) {"
            + "        return vec4(color.rgb * alpha, alpha);"
            + "    } else {"
            + "        return vec4(color.rgb, alpha);"
            + "    }"
            + "}";

    private SeslBottomFadingEdgeOverrides mBottomOverrides;
    private SeslTopFadingEdgeOverrides mTopOverrides;
    private RuntimeShader mTopShader = null;
    private RuntimeShader mBottomShader = null;
    private RuntimeShader mExtraTopShader = null;
    private RuntimeShader mExtraBottomShader = null;
    private boolean mExtendTopFadingEdge = false;
    private boolean mExtendBottomFadingEdge = false;
    private boolean mForceLegacyXfermode = false;

    private float[] getBottomInterpolatorForSlot(int slot) {
        SeslBottomFadingEdgeOverrides overrides = this.mBottomOverrides;
        return (overrides == null || overrides.isEmpty())
                ? getDefaultBottomInterpolator(slot)
                : overrides.resolveInterpolator(slot);
    }

    /** Returns default bottom interpolator for specified slot index. */
    public static float[] getDefaultBottomInterpolator(int slot) {
        if (slot >= 0 && slot < DEFAULT_BOTTOM_INTERPOLATORS.length) {
            float[] interpolator = DEFAULT_BOTTOM_INTERPOLATORS[slot];
            return Arrays.copyOf(interpolator, interpolator.length);
        }
        return INTERPOLATOR_BOTTOM;
    }

    /** Returns default top interpolator for specified slot index. */
    public static float[] getDefaultTopInterpolator(int slot) {
        if (slot >= 0 && slot < DEFAULT_TOP_INTERPOLATORS.length) {
            float[] interpolator = DEFAULT_TOP_INTERPOLATORS[slot];
            return Arrays.copyOf(interpolator, interpolator.length);
        }
        return INTERPOLATOR_TOP;
    }

    private float[] getTopInterpolatorForSlot(int slot) {
        SeslTopFadingEdgeOverrides overrides = this.mTopOverrides;
        return (overrides == null || overrides.isEmpty())
                ? getDefaultTopInterpolator(slot)
                : overrides.resolveInterpolator(slot);
    }

    private boolean shouldUseCustomXfermode() {
        return !this.mForceLegacyXfermode && Build.VERSION.SDK_INT >= 37;
    }

    /** Applies the specified color to all managed shaders. */
    public void applyColorToAllShaders(int color) {
        RuntimeShader topShader = this.mTopShader;
        if (topShader != null) {
            updateShaderColor(topShader, color);
        }
        RuntimeShader bottomShader = this.mBottomShader;
        if (bottomShader != null) {
            updateShaderColor(bottomShader, color);
        }
        RuntimeShader extraTopShader = this.mExtraTopShader;
        if (extraTopShader != null) {
            updateShaderColor(extraTopShader, color);
        }
        RuntimeShader extraBottomShader = this.mExtraBottomShader;
        if (extraBottomShader != null) {
            updateShaderColor(extraBottomShader, color);
        }
    }

    /** Clears the bottom runtime shader. */
    public void clearBottomShader() {
        this.mBottomShader = null;
    }

    /** Clears the extra bottom runtime shader. */
    public void clearExtraBottomShader() {
        this.mExtraBottomShader = null;
    }

    /** Clears the extra top runtime shader. */
    public void clearExtraTopShader() {
        this.mExtraTopShader = null;
    }

    /** Clears the top runtime shader. */
    public void clearTopShader() {
        this.mTopShader = null;
    }

    /** Clears all managed runtime shaders. */
    public void clearShaders() {
        clearTopShader();
        clearBottomShader();
        clearExtraTopShader();
        clearExtraBottomShader();
    }

    /**
     * Creates an AGSL {@link RuntimeShader} with specified color, start alpha, and interpolator.
     *
     * @param color the color to apply
     * @param startAlpha the starting alpha value
     * @param interpolator the cubic bezier easing control points
     * @return a new {@link RuntimeShader} instance, or {@code null} on API levels < 33
     */
    @Nullable
    public RuntimeShader createRuntimeShader(int color, float startAlpha, float[] interpolator) {
        if (Build.VERSION.SDK_INT < 33) {
            return null;
        }
        RuntimeShader shader = new RuntimeShader(SHADER_SRC);
        shader.setIntUniform("customXfer", shouldUseCustomXfermode() ? 1 : 0);
        shader.setFloatUniform("resolution", 1.0f, 1.0f);
        updateShaderStartAlpha(shader, startAlpha);
        updateShaderInterpolator(shader, interpolator);
        updateShaderColor(shader, color);
        updateShaderDitherStrength(shader, 3.0f);
        return shader;
    }

    /** Returns the bottom runtime shader. */
    @Nullable
    public RuntimeShader getBottomShader() {
        return this.mBottomShader;
    }

    /** Returns the extra bottom runtime shader. */
    @Nullable
    public RuntimeShader getExtraBottomShader() {
        return this.mExtraBottomShader;
    }

    /** Returns the extra top runtime shader. */
    @Nullable
    public RuntimeShader getExtraTopShader() {
        return this.mExtraTopShader;
    }

    /** Returns the top runtime shader. */
    @Nullable
    public RuntimeShader getTopShader() {
        return this.mTopShader;
    }

    /** Returns the active gradient shader for the top or bottom edge. */
    @Nullable
    public Shader getGradientForEdge(boolean isTop) {
        RuntimeShader shader;
        if (isTop) {
            return (!this.mExtendTopFadingEdge || (shader = this.mExtraTopShader) == null)
                    ? this.mTopShader
                    : shader;
        }
        return (!this.mExtendBottomFadingEdge || (shader = this.mExtraBottomShader) == null)
                ? this.mBottomShader
                : shader;
    }

    /** Initializes extra bottom runtime shader with specified color. */
    public void initializeExtraBottomShader(int color) {
        this.mExtraBottomShader = createRuntimeShader(color, DEFAULT_START_ALPHA, INTERPOLATOR_BOTTOM_EXTRA);
    }

    /** Initializes extra top runtime shader with specified color. */
    public void initializeExtraTopShader(int color) {
        this.mExtraTopShader = createRuntimeShader(color, DEFAULT_START_ALPHA, INTERPOLATOR_TOP_EXTRA);
    }

    /** Initializes top and bottom runtime shaders with specified color. */
    public void initializeShaders(int color) {
        this.mTopShader = createRuntimeShader(color, DEFAULT_START_ALPHA, INTERPOLATOR_TOP);
        this.mBottomShader = createRuntimeShader(color, DEFAULT_START_ALPHA, INTERPOLATOR_BOTTOM);
    }

    /** Returns whether bottom fading edge expansion is enabled. */
    public boolean isExtendBottomFadingEdge() {
        return this.mExtendBottomFadingEdge;
    }

    /** Returns whether top fading edge expansion is enabled. */
    public boolean isExtendTopFadingEdge() {
        return this.mExtendTopFadingEdge;
    }

    /** Sets bottom fading edge overrides. */
    public void setBottomFadingEdgeOverrides(@Nullable SeslBottomFadingEdgeOverrides overrides) {
        this.mBottomOverrides = overrides;
    }

    /** Sets top fading edge overrides. */
    public void setTopFadingEdgeOverrides(@Nullable SeslTopFadingEdgeOverrides overrides) {
        this.mTopOverrides = overrides;
    }

    /** Sets whether bottom fading edge should be expanded. */
    public void setExtendBottomFadingEdge(boolean extend) {
        this.mExtendBottomFadingEdge = extend;
    }

    /** Sets whether top fading edge should be expanded. */
    public void setExtendTopFadingEdge(boolean extend) {
        this.mExtendTopFadingEdge = extend;
    }

    /** Sets whether legacy transfer mode should be forced. */
    public void setForceLegacyXfermode(boolean force) {
        this.mForceLegacyXfermode = force;
    }

    /** Updates bottom shader parameters according to overlapping and taskbar state. */
    public void updateBottomShaderType(boolean overlapped, boolean isTaskBarAvailable) {
        RuntimeShader bottomShader = this.mBottomShader;
        if (bottomShader != null) {
            int slot = overlapped ? (isTaskBarAvailable ? 3 : 2) : 0;
            updateShaderInterpolator(bottomShader, getBottomInterpolatorForSlot(slot));
            updateShaderStartAlpha(bottomShader, overlapped ? DEFAULT_START_ALPHA : GESTURE_BAR_ONLY_START_ALPHA);
        }
        RuntimeShader extraBottomShader = this.mExtraBottomShader;
        if (extraBottomShader != null) {
            updateShaderInterpolator(extraBottomShader, getBottomInterpolatorForSlot(overlapped ? 4 : 1));
        }
    }

    /** Updates top shader parameters according to overlapping state. */
    public void updateTopShaderType(boolean overlapped) {
        RuntimeShader topShader = this.mTopShader;
        if (topShader != null) {
            updateShaderInterpolator(topShader, getTopInterpolatorForSlot(overlapped ? 2 : 0));
        }
        RuntimeShader extraTopShader = this.mExtraTopShader;
        if (extraTopShader != null) {
            updateShaderInterpolator(extraTopShader, getTopInterpolatorForSlot(overlapped ? 3 : 1));
        }
    }

    /** Updates shader uniform color. */
    public static void updateShaderColor(RuntimeShader shader, int color) {
        if (Build.VERSION.SDK_INT >= 33) {
            shader.setFloatUniform("color",
                    Color.red(color) / 255.0f, Color.green(color) / 255.0f,
                    Color.blue(color) / 255.0f, 1.0f);
        }
    }

    /** Updates shader dither strength uniform. */
    public static void updateShaderDitherStrength(RuntimeShader shader, float ditherStrength) {
        if (Build.VERSION.SDK_INT >= 33) {
            shader.setFloatUniform("ditherStrength", ditherStrength);
        }
    }

    /** Updates shader easing interpolator uniform. */
    public static void updateShaderInterpolator(RuntimeShader shader, float[] interpolator) {
        if (Build.VERSION.SDK_INT >= 33 && interpolator != null && interpolator.length >= 4) {
            shader.setFloatUniform("easing", interpolator[0], interpolator[1], interpolator[2], interpolator[3]);
        }
    }

    /** Updates shader start alpha uniform. */
    public static void updateShaderStartAlpha(RuntimeShader shader, float startAlpha) {
        if (Build.VERSION.SDK_INT >= 33) {
            shader.setFloatUniform("startAlpha", startAlpha);
        }
    }
}
