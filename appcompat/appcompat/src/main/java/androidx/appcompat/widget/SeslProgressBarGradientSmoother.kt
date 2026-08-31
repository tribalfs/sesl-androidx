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

package androidx.appcompat.widget

import android.graphics.Color
import androidx.annotation.RestrictTo
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

//sesl9
/**
 * Smooths color and position stops for progress bar gradients introduced in SESL9 (One UI 8.5).
 *
 * <p>This utility uses Catmull-Rom cubic spline interpolation in sRGB/linear color space to eliminate
 * sharp color transitions between discrete stop colors, generating a higher-density smoothed stop
 * array used by {@link SeslProgressBar#MODE_GRADIENT_HORIZONTAL} and {@link SeslProgressBar#MODE_GRADIENT_CIRCLE}.</p>
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
object SeslProgressBarGradientSmoother {

    class Result(val colors: IntArray?, val positions: FloatArray?)

    @JvmStatic
    fun smoothStopsCubic(stopColors: IntArray?, stopPos01: FloatArray?, samplesPerSegment: Int): Result {
        if (stopColors == null || stopPos01 == null) {
            return Result(stopColors, stopPos01)
        }
        if (stopColors.size < 2 || stopColors.size != stopPos01.size) {
            return Result(stopColors, stopPos01)
        }
        val samples = if (samplesPerSegment < 0) 0 else samplesPerSegment
        val numStops = stopColors.size
        val numSegments = numStops - 1
        val samplesPerSegPlus1 = samples + 1
        val totalSamples = numSegments * samplesPerSegPlus1 + 1

        val resultColors = IntArray(totalSamples)
        val resultPositions = FloatArray(totalSamples)

        var resultIdx = 0
        for (i in 0 until numSegments) {
            val c0 = stopColors[clampIndex(i - 1, numStops)]
            val c1 = stopColors[i]
            val nextI = i + 1
            val c2 = stopColors[nextI]
            val c3 = stopColors[clampIndex(i + 2, numStops)]

            val p1 = stopPos01[i]
            val p2 = stopPos01[nextI]

            for (j in 0 until samplesPerSegPlus1) {
                val t = j.toFloat() / samplesPerSegPlus1.toFloat()
                resultColors[resultIdx] = catmullRomColor(c0, c1, c2, c3, t)
                resultPositions[resultIdx] = lerp(p1, p2, t)
                resultIdx++
            }
        }
        resultColors[resultIdx] = stopColors[numStops - 1]
        resultPositions[resultIdx] = stopPos01[numStops - 1]

        normalizePositions(resultPositions)
        return Result(resultColors, resultPositions)
    }

    private fun clampIndex(idx: Int, n: Int): Int {
        if (idx < 0) return 0
        return if (idx >= n) n - 1 else idx
    }

    private fun catmullRomColor(c0: Int, c1: Int, c2: Int, c3: Int, t: Float): Int {
        val a0 = Color.alpha(c0) / 255.0f
        val a1 = Color.alpha(c1) / 255.0f
        val a2 = Color.alpha(c2) / 255.0f
        val a3 = Color.alpha(c3) / 255.0f

        val r0 = srgbToLinear(Color.red(c0) / 255.0f)
        val g0 = srgbToLinear(Color.green(c0) / 255.0f)
        val b0 = srgbToLinear(Color.blue(c0) / 255.0f)

        val r1 = srgbToLinear(Color.red(c1) / 255.0f)
        val g1 = srgbToLinear(Color.green(c1) / 255.0f)
        val b1 = srgbToLinear(Color.blue(c1) / 255.0f)

        val r2 = srgbToLinear(Color.red(c2) / 255.0f)
        val g2 = srgbToLinear(Color.green(c2) / 255.0f)
        val b2 = srgbToLinear(Color.blue(c2) / 255.0f)

        val r3 = srgbToLinear(Color.red(c3) / 255.0f)
        val g3 = srgbToLinear(Color.green(c3) / 255.0f)
        val b3 = srgbToLinear(Color.blue(c3) / 255.0f)

        val a = catmullRom(a0, a1, a2, a3, t)
        val r = catmullRom(r0, r1, r2, r3, t)
        val g = catmullRom(g0, g1, g2, g3, t)
        val b = catmullRom(b0, b1, b2, b3, t)

        return Color.argb(
            (clamp01(a) * 255.0f).roundToInt(),
            (linearToSrgb(clamp01(r)) * 255.0f).roundToInt(),
            (linearToSrgb(clamp01(g)) * 255.0f).roundToInt(),
            (linearToSrgb(clamp01(b)) * 255.0f).roundToInt()
        )
    }

    private fun catmullRom(p0: Float, p1: Float, p2: Float, p3: Float, t: Float): Float {
        val t2 = t * t
        val t3 = t2 * t
        return 0.5f * (
            (3.0f * p1 - p0 - 3.0f * p2 + p3) * t3 +
            (2.0f * p0 - 5.0f * p1 + 4.0f * p2 - p3) * t2 +
            (p2 - p0) * t +
            2.0f * p1
        )
    }

    private fun srgbToLinear(c: Float): Float =
        if (c <= 0.04045f) c / 12.92f else ((c + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()

    private fun linearToSrgb(c: Float): Float =
        if (c <= 0.0031308f) c * 12.92f else (1.055f * c.toDouble().pow(1.0 / 2.4) - 0.055f).toFloat()

    private fun clamp01(v: Float): Float =
        if (v < 0.0f) 0.0f else if (v > 1.0f) 1.0f else v

    private fun lerp(start: Float, stop: Float, amount: Float): Float =
        start + (stop - start) * amount

    private fun normalizePositions(pos: FloatArray?) {
        if (pos == null || pos.isEmpty()) return
        pos[0] = clamp01(pos[0])
        for (i in 1 until pos.size) {
            var clamped = clamp01(pos[i])
            val prev = pos[i - 1]
            if (clamped <= prev) {
                clamped = min(1.0f, prev + 1.0e-6f)
            }
            pos[i] = clamped
        }
        for (i in pos.size - 2 downTo 0) {
            val curr = pos[i]
            val next = pos[i + 1]
            if (curr >= next) {
                pos[i] = max(0.0f, next - 1.0e-6f)
            }
        }
        pos[0] = max(0.0f, pos[0])
        pos[pos.size - 1] = min(1.0f, pos[pos.size - 1])
    }
}
