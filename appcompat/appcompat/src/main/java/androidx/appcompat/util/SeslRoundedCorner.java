/*
 * Copyright (C) 2022 The Android Open Source Project
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

package androidx.appcompat.util;

import static android.util.TypedValue.TYPE_FIRST_COLOR_INT;
import static android.util.TypedValue.TYPE_LAST_COLOR_INT;

import static androidx.core.graphics.PathParser.nodesToPath;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.RestrictTo;
import androidx.appcompat.R;
import androidx.core.graphics.Insets;
import androidx.core.graphics.PathParser;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */

/**
 * Utility class for managing and drawing rounded corners in SESL widgets.
 * This class provides methods to set which corners should be rounded,
 * the color of the rounded corners, and to draw these corners on a {@link Canvas}.
 *
 * <p>It defines constants for specifying different combinations of rounded corners:
 * <ul>
 *   <li>{@link #ROUNDED_CORNER_NONE}: No corners are rounded.
 *   <li>{@link #ROUNDED_CORNER_TOP_LEFT}: Only the top-left corner is rounded.
 *   <li>{@link #ROUNDED_CORNER_TOP_RIGHT}: Only the top-right corner is rounded.
 *   <li>{@link #ROUNDED_CORNER_BOTTOM_LEFT}: Only the bottom-left corner is rounded.
 *   <li>{@link #ROUNDED_CORNER_BOTTOM_RIGHT}: Only the bottom-right corner is rounded.
 *   <li>{@link #ROUNDED_CORNER_ALL}: All four corners are rounded.
 * </ul>
 * </p>
 *
 * <p>Starting with SESL9 (One UI 8.5), corner rounding supports:
 * <ul>
 *   <li>Selective corner path generation via {@link #getSmoothCornerRectPath(float, float, float, float, float, int)},
 *       allowing specific corners to remain sharp while others are smooth-rounded.
 *   <li>Color filtering via {@link PorterDuffColorFilter} applied directly to the individual corner drawables
 *       ({@link #mTopLeftRound}, {@link #mTopRightRound}, {@link #mBottomLeftRound}, {@link #mBottomRightRound}).
 *   <li>Path node caching in {@link SeslRoundedChunkingDrawable} to eliminate string parsing overhead
 *       on repeated draw operations.
 * </ul>
 * </p>
 *
 * <p>The class also includes a nested {@link SeslRoundedChunkingDrawable} class,
 * which is a {@link Drawable} responsible for rendering a single rounded corner.
 * </p>
 */
public class SeslRoundedCorner {
    public static final int ROUNDED_CORNER_ALL = 15;
    public static final int ROUNDED_CORNER_BOTTOM_LEFT = 4;
    public static final int ROUNDED_CORNER_BOTTOM_RIGHT = 8;
    public static final int ROUNDED_CORNER_NONE = 0;
    public static final int ROUNDED_CORNER_TOP_LEFT = 1;
    public static final int ROUNDED_CORNER_TOP_RIGHT = 2;
    private static final String TAG = "SeslRoundedCorner";

    static final Locale LOCALE = Locale.ENGLISH;
    private static final String CUBIC_BEZIER_CURVE_FORMAT = "C %f %f %f %f %f %f ";
    private static final String LINE_TO_FORMAT = "L %f %f ";
    private static final String PATH_SEGMENT_CLOSE = "Z";
    private static final String MOVE_TO_START = "M 0 0 ";

    private static final Float CONTROL_POINT_X_1 = 4.64f;
    private static final Float CONTROL_POINT_Y_1 = 67.45f;
    private static final Float CURVE_END_X = 13.36f;
    private static final Float CURVE_END_Y = 51.16f;
    private static final Float TANGENT_X_1 = 22.07f;
    private static final Float TANGENT_Y_1 = 34.86f;

    private static final float SHRINK_FACTOR_THRESHOLD = 0.5f;
    private static final float SHRINK_FACTOR_DENOMINATOR = 0.4f;
    private static final float SHRINK_FACTOR_MULTIPLIER = 0.13877845f;

    private static final float SCALE_FACTOR_THRESHOLD = 0.6f;
    private static final float SCALE_FACTOR_DENOMINATOR = 0.3f;
    private static final float SCALE_FACTOR_MULTIPLIER = 0.042454004f;

    //Sesl9
    @NonNull
    protected final SeslRoundedChunkingDrawable mTopLeftRound;
    @NonNull
    protected final SeslRoundedChunkingDrawable mTopRightRound;
    @NonNull
    protected final SeslRoundedChunkingDrawable mBottomLeftRound;
    //sesl9
    @NonNull
    protected final SeslRoundedChunkingDrawable mBottomRightRound;
    @ColorInt
    private int mTopLeftRoundColor;
    @ColorInt
    private int mTopRightRoundColor;
    @ColorInt
    private int mBottomLeftRoundColor;
    @ColorInt
    private int mBottomRightRoundColor;

    final Rect mRoundedCornerBounds = new Rect();
    final int mRoundRadius;
    int mRoundedCornerMode;
    @Nullable
    private Insets mInsets = null;

    /**
     * Creates a {@link SeslRoundedCorner} instance with theme-aware rounded corner colors and radius.
     * The corner color is automatically resolved from {@code R.attr.roundedCornerColor} or falls back
     * to light/dark theme defaults.
     *
     * @param context The context used to retrieve resources and theme attributes.
     */
    public SeslRoundedCorner(@NonNull Context context) {
        this(context, false);
    }

    /**
     * Creates a {@link SeslRoundedCorner} instance with theme-aware rounded corner colors and radius.
     *
     * @param context The context used to retrieve resources and theme attributes.
     * @param unused  Unused compatibility parameter.
     */
    public SeslRoundedCorner(@NonNull Context context, boolean unused) {
        Resources resources = context.getResources();

        mRoundRadius = resources.getDimensionPixelSize(R.dimen.sesl_rounded_corner_radius);

        final boolean isDarkMode = !SeslMisc.isLightTheme(context);

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(androidx.appcompat.R.attr.roundedCornerColor,
                typedValue, true);
        final int roundColor;

        if (typedValue.resourceId > 0 && isColorType(typedValue.type)) {
            roundColor = resources.getColor(typedValue.resourceId);
        } else if (typedValue.data > 0 && isColorType(typedValue.type)) {
            roundColor = typedValue.data;
        } else {
            if (isDarkMode) {
                roundColor = resources.getColor(R.color.sesl_round_and_bgcolor_dark);
            } else {
                roundColor = resources.getColor(R.color.sesl_round_and_bgcolor_light);
            }
        }
        mBottomRightRoundColor = roundColor;
        mBottomLeftRoundColor = roundColor;
        mTopRightRoundColor = roundColor;
        mTopLeftRoundColor = roundColor;

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);

        PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(roundColor,
                PorterDuff.Mode.SRC_IN);

        mTopLeftRound = new SeslRoundedChunkingDrawable(mRoundRadius, paint, 0f);
        mTopRightRound = new SeslRoundedChunkingDrawable(mRoundRadius, paint, 90f);
        mBottomLeftRound = new SeslRoundedChunkingDrawable(mRoundRadius, paint, 270f);
        mBottomRightRound = new SeslRoundedChunkingDrawable(mRoundRadius, paint, 180f);
        //Sesl9
        mTopLeftRound.setColorFilter(colorFilter);
        mTopRightRound.setColorFilter(colorFilter);
        mBottomLeftRound.setColorFilter(colorFilter);
        mBottomRightRound.setColorFilter(colorFilter);
        //sesl9
    }

    /**
     * Sets the rounded corners to be drawn.
     *
     * @param corners A bitmask of the corners to be rounded.
     *                Use {@link #ROUNDED_CORNER_NONE} for no rounded corners,
     *                {@link #ROUNDED_CORNER_TOP_LEFT} for the top-left corner,
     *                {@link #ROUNDED_CORNER_TOP_RIGHT} for the top-right corner,
     *                {@link #ROUNDED_CORNER_BOTTOM_LEFT} for the bottom-left corner,
     *                {@link #ROUNDED_CORNER_BOTTOM_RIGHT} for the bottom-right corner,
     *                or {@link #ROUNDED_CORNER_ALL} for all corners.
     *                You can also combine these flags using the bitwise OR operator.
     * @throws IllegalArgumentException if an invalid corner value is provided.
     */
    public void setRoundedCorners(int corners) {
        if ((corners & (-16)) == 0) {
            mRoundedCornerMode = corners;
        } else {
            throw new IllegalArgumentException("Use wrong rounded corners to the param, corners ="
                    + " " + corners);
        }
    }

    /**
     * Returns the current rounded corner mode.
     *
     * @return The current rounded corner mode, which can be a bitmask of
     *         {@link #ROUNDED_CORNER_NONE}, {@link #ROUNDED_CORNER_TOP_LEFT},
     *         {@link #ROUNDED_CORNER_TOP_RIGHT}, {@link #ROUNDED_CORNER_BOTTOM_LEFT},
     *         {@link #ROUNDED_CORNER_BOTTOM_RIGHT}, or {@link #ROUNDED_CORNER_ALL}.
     */
    public int getRoundedCorners() {
        return mRoundedCornerMode;
    }

    /**
     * Sets the color for the specified rounded corners using a {@link PorterDuffColorFilter}.
     *
     * @param corners A bitmask of the corners to set the color for.
     *                Use constants like {@link #ROUNDED_CORNER_TOP_LEFT},
     *                {@link #ROUNDED_CORNER_TOP_RIGHT}, etc., or {@link #ROUNDED_CORNER_ALL}
     *                to set all corners.
     * @param color   The color to set for the specified corners.
     * @throws IllegalArgumentException if {@code corners} is {@link #ROUNDED_CORNER_NONE}
     *                                  or if an invalid corner value is provided.
     */
    public void setRoundedCornerColor(int corners, @ColorInt int color) {
        if (corners == ROUNDED_CORNER_NONE) {
            throw new IllegalArgumentException("There is no rounded corner on = " + this);
        } else if ((corners & (-16)) == 0) {
            PorterDuffColorFilter pdcf = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
            if ((corners & ROUNDED_CORNER_TOP_LEFT) != 0) {
                mTopLeftRoundColor = color;
                mTopLeftRound.setColorFilter(pdcf);
            }
            if ((corners & ROUNDED_CORNER_TOP_RIGHT) != 0) {
                mTopRightRoundColor = color;
                mTopRightRound.setColorFilter(pdcf);
            }
            if ((corners & ROUNDED_CORNER_BOTTOM_LEFT) != 0) {
                mBottomLeftRoundColor = color;
                mBottomLeftRound.setColorFilter(pdcf);
            }
            if ((corners & ROUNDED_CORNER_BOTTOM_RIGHT) != 0) {
                mBottomRightRoundColor = color;
                mBottomRightRound.setColorFilter(pdcf);
            }
        } else {
            throw new IllegalArgumentException("Use wrong rounded corners to the param, corners ="
                    + " " + corners);
        }
    }

    /**
     * Retrieves the color of a specific rounded corner.
     *
     * @param corner The corner to get the color from. Must be one of
     *               {@link #ROUNDED_CORNER_TOP_LEFT}, {@link #ROUNDED_CORNER_TOP_RIGHT},
     *               {@link #ROUNDED_CORNER_BOTTOM_LEFT}, or {@link #ROUNDED_CORNER_BOTTOM_RIGHT}.
     * @return The color of the specified rounded corner.
     * @throws IllegalArgumentException if the corner is {@link #ROUNDED_CORNER_NONE} or an
     *                                  invalid corner value.
     */
    @ColorInt
    public int getRoundedCornerColor(int corner) {
        if (corner == ROUNDED_CORNER_NONE) {
            throw new IllegalArgumentException("There is no rounded corner on = " + this);
        } else if (corner != ROUNDED_CORNER_TOP_LEFT
                && corner != ROUNDED_CORNER_TOP_RIGHT
                && corner != ROUNDED_CORNER_BOTTOM_LEFT
                && corner != ROUNDED_CORNER_BOTTOM_RIGHT) {
            throw new IllegalArgumentException("Use multiple rounded corner as param on = " + this);
        }

        if ((corner & ROUNDED_CORNER_TOP_LEFT) != 0) {
            return mTopLeftRoundColor;
        }
        if ((corner & ROUNDED_CORNER_TOP_RIGHT) != 0) {
            return mTopRightRoundColor;
        }
        if ((corner & ROUNDED_CORNER_BOTTOM_LEFT) != 0) {
            return mBottomLeftRoundColor;
        }
        return mBottomRightRoundColor;
    }

    /**
     * Retrieves the radius of the rounded corners.
     *
     * @return The radius of the rounded corners in pixels.
     * @hide
     */
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public int getRoundedCornerRadius() {
        return mRoundRadius;
    }

    /**
     * Draws the rounded corners on the given canvas.
     * The bounds for drawing are obtained from the canvas's clip bounds.
     *
     * @param canvas The canvas to draw on. Must not be null.
     */
    public void drawRoundedCorner(@NonNull Canvas canvas) {
        canvas.getClipBounds(mRoundedCornerBounds);
        drawRoundedCornerInternal(canvas);
    }

    private void drawRoundedCornerInternal(Canvas canvas) {
        final int left = mRoundedCornerBounds.left + (mInsets != null ? mInsets.left : 0);
        final int right = mRoundedCornerBounds.right - (mInsets != null ? mInsets.right : 0);
        final int top = mRoundedCornerBounds.top + (mInsets != null ? mInsets.top : 0);
        final int bottom = mRoundedCornerBounds.bottom - (mInsets != null ? mInsets.bottom : 0);

        if ((mRoundedCornerMode & ROUNDED_CORNER_TOP_LEFT) != 0) {
            mTopLeftRound.setBounds(left, top, left + mRoundRadius, mRoundRadius + top);
            mTopLeftRound.draw(canvas);
        }

        if ((mRoundedCornerMode & ROUNDED_CORNER_TOP_RIGHT) != 0) {
            mTopRightRound.setBounds(right - mRoundRadius, top, right, mRoundRadius + top);
            mTopRightRound.draw(canvas);
        }

        if ((mRoundedCornerMode & ROUNDED_CORNER_BOTTOM_LEFT) != 0) {
            mBottomLeftRound.setBounds(left, bottom - mRoundRadius, mRoundRadius + left, bottom);
            mBottomLeftRound.draw(canvas);
        }

        if ((mRoundedCornerMode & ROUNDED_CORNER_BOTTOM_RIGHT) != 0) {
            mBottomRightRound.setBounds(right - mRoundRadius, bottom - mRoundRadius, right, bottom);
            mBottomRightRound.draw(canvas);
        }

        if (mTopLeftRoundColor == mTopRightRoundColor && mTopLeftRoundColor == mBottomLeftRoundColor && mTopLeftRoundColor == mBottomRightRoundColor) {
            Paint paint = new Paint();
            paint.setColor(mTopLeftRoundColor);

            if (mInsets != null && mInsets.top > 0) {
                canvas.drawRect(new Rect(left - mInsets.left, top - mInsets.top, mInsets.right + right, top), paint);
            }

            if (mInsets != null && mInsets.bottom > 0) {
                canvas.drawRect(new Rect(left - mInsets.left, bottom, mInsets.right + right, mInsets.bottom + bottom), paint);
            }

            if (mInsets != null && mInsets.left > 0) {
                canvas.drawRect(new Rect(left - mInsets.left, top - mInsets.top, left, mInsets.bottom + bottom), paint);
            }

            if (mInsets == null || mInsets.right <= 0) {
                return;
            }

            canvas.drawRect(new Rect(right, top - mInsets.top, mInsets.right + right, bottom + mInsets.bottom), paint);
        }
    }

    /**
     * Deprecated method for generating a smooth corner rectangle path starting from (0, 0).
     *
     * @param cornerRadius The desired smooth corner radius.
     * @param rectWidth    The width of the rectangle.
     * @param rectHeight   The height of the rectangle.
     * @return A {@link Path} representing the smooth-rounded rectangle.
     * @deprecated Use {@link #getSmoothCornerRectPath(float, float, float, float, float)} instead.
     */
    @NonNull
    @Deprecated
    public static Path getSmoothCornerRectPath(float cornerRadius, float rectWidth, float rectHeight) {
        Log.w(TAG, "This method is deprecated. Use getSmoothCornerRectPath(float, float, float, float, float) instead.");
        return getSmoothCornerRectPath(cornerRadius, 0f, 0f, rectWidth, rectHeight);
    }

    /**
     * Builds a smooth-rounded rectangle path where all corners are rounded according to the specified radius.
     *
     * @param cornerRadius The desired smooth corner radius in pixels.
     * @param left         The left coordinate of the rectangle.
     * @param top          The top coordinate of the rectangle.
     * @param width        The width of the rectangle.
     * @param height       The height of the rectangle.
     * @return A {@link Path} representing the smooth-rounded rectangle.
     */
    @NonNull
    public static Path getSmoothCornerRectPath(float cornerRadius, float left, float top,
            float width, float height) {
        return getSmoothCornerRectPath(cornerRadius, left, top, width, height,
                ROUNDED_CORNER_ALL);
    }

    /**
     * Builds a smooth-rounded rectangle path where only the corners selected by the
     * {@code corners} bitmask are rounded; unselected corners remain sharp.
     *
     * @param cornerRadius The desired smooth corner radius in pixels.
     * @param left         The left coordinate of the rectangle.
     * @param top          The top coordinate of the rectangle.
     * @param width        The width of the rectangle.
     * @param height       The height of the rectangle.
     * @param corners      A bitmask specifying which corners to round. Combination of
     *                     {@link #ROUNDED_CORNER_TOP_LEFT}, {@link #ROUNDED_CORNER_TOP_RIGHT},
     *                     {@link #ROUNDED_CORNER_BOTTOM_LEFT}, {@link #ROUNDED_CORNER_BOTTOM_RIGHT},
     *                     {@link #ROUNDED_CORNER_ALL}, or {@link #ROUNDED_CORNER_NONE}.
     * @return A {@link Path} representing the selectively smooth-rounded rectangle.
     */
    @NonNull
    public static Path getSmoothCornerRectPath(float cornerRadius, float left, float top,
            float width, float height, int corners) {
        Path path = new Path();

        if (width <= 0f || height <= 0f) {
            return new Path();
        }

        if (corners == ROUNDED_CORNER_NONE) {
            path.addRect(left, top, left + width, top + height, Path.Direction.CW);
            return path;
        }

        final float recCenterX = width / 2f;
        final float recCenterY = height / 2f;
        final float smallestHalfDimension = Math.min(recCenterX, recCenterY);

        final float clampedCornerRadius = Math.clamp(smallestHalfDimension, 0f, cornerRadius);
        final float cornerScaleFactor = clampedCornerRadius / smallestHalfDimension;

        final float smoothingFactor1 = cornerScaleFactor > SHRINK_FACTOR_THRESHOLD
                ? 1f - (Math.min(1f, (cornerScaleFactor - SHRINK_FACTOR_THRESHOLD)
                        / SHRINK_FACTOR_DENOMINATOR) * SHRINK_FACTOR_MULTIPLIER)
                : 1f;
        final float smoothingFactor2 = cornerScaleFactor > SCALE_FACTOR_THRESHOLD
                ? 1f + (Math.min(1f, (cornerScaleFactor - SCALE_FACTOR_THRESHOLD)
                        / SCALE_FACTOR_DENOMINATOR) * SCALE_FACTOR_MULTIPLIER)
                : 1f;

        final float radiusFactor = clampedCornerRadius / 100f;
        final float scaledCornerRadius = 128.19f * radiusFactor * smoothingFactor1;
        final float controlPointDistance = 83.62f * radiusFactor * smoothingFactor2;
        final float arcRadius = radiusFactor * 67.45f;
        final float controlPointOffsetY = radiusFactor * 4.64f;
        final float startControlPointX = radiusFactor * 51.16f;
        final float startControlPointY = radiusFactor * 13.36f;
        final float endControlPointX = radiusFactor * 34.86f;
        final float endControlPointY = radiusFactor * 22.07f;

        final float right = left + width;
        final float bottom = top + height;

        path.moveTo(left + recCenterX, top);

        if ((corners & ROUNDED_CORNER_TOP_RIGHT) != 0) {
            path.lineTo(Math.max(recCenterX, width - scaledCornerRadius) + left, top);
            path.cubicTo(right - controlPointDistance, top, right - arcRadius,
                    top + controlPointOffsetY, right - startControlPointX,
                    top + startControlPointY);
            path.cubicTo(right - endControlPointX, top + endControlPointY,
                    right - endControlPointY, top + endControlPointX,
                    right - startControlPointY, top + startControlPointX);
            path.cubicTo(right - controlPointOffsetY, top + arcRadius, right,
                    top + controlPointDistance, right,
                    Math.min(recCenterY, scaledCornerRadius) + top);
        } else {
            path.lineTo(right, top);
        }

        if ((corners & ROUNDED_CORNER_BOTTOM_RIGHT) != 0) {
            path.lineTo(right, Math.max(recCenterY, height - scaledCornerRadius) + top);
            path.cubicTo(right, bottom - controlPointDistance, right - controlPointOffsetY,
                    bottom - arcRadius, right - startControlPointY, bottom - startControlPointX);
            path.cubicTo(right - endControlPointY, bottom - endControlPointX,
                    right - endControlPointX, bottom - endControlPointY,
                    right - startControlPointX, bottom - startControlPointY);
            path.cubicTo(right - arcRadius, bottom - controlPointOffsetY,
                    right - controlPointDistance, bottom,
                    Math.max(recCenterX, width - scaledCornerRadius) + left, bottom);
        } else {
            path.lineTo(right, bottom);
        }

        if ((corners & ROUNDED_CORNER_BOTTOM_LEFT) != 0) {
            path.lineTo(Math.min(recCenterX, scaledCornerRadius) + left, bottom);
            path.cubicTo(left + controlPointDistance, bottom, left + arcRadius,
                    bottom - controlPointOffsetY, left + startControlPointX,
                    bottom - startControlPointY);
            path.cubicTo(left + endControlPointX, bottom - endControlPointY,
                    left + endControlPointY, bottom - endControlPointX,
                    left + startControlPointY, bottom - startControlPointX);
            path.cubicTo(left + controlPointOffsetY, bottom - arcRadius, left,
                    bottom - controlPointDistance, left,
                    Math.max(recCenterY, height - scaledCornerRadius) + top);
        } else {
            path.lineTo(left, bottom);
        }

        if ((corners & ROUNDED_CORNER_TOP_LEFT) != 0) {
            path.lineTo(left, Math.min(recCenterY, scaledCornerRadius) + top);
            path.cubicTo(left, top + controlPointDistance, left + controlPointOffsetY,
                    top + arcRadius, left + startControlPointY, top + startControlPointX);
            path.cubicTo(left + endControlPointX, top + endControlPointY,
                    left + endControlPointX, top + endControlPointY,
                    left + startControlPointX, top + startControlPointY);
            path.cubicTo(left + arcRadius, top + controlPointOffsetY,
                    left + controlPointDistance, top,
                    Math.min(recCenterX, scaledCornerRadius) + left, top);
        } else {
            path.lineTo(left, top);
        }

        path.close();
        return path;
    }

    private boolean isColorType(int i) {
        return i >= TYPE_FIRST_COLOR_INT && i <= TYPE_LAST_COLOR_INT;
    }

    /**
     * Draws the rounded corners for the specified view onto the given canvas.
     * Translates the canvas if the view has a non-zero translation Y.
     *
     * @param view   The view to draw the rounded corners for. Must not be null.
     * @param canvas The canvas to draw on. Must not be null.
     */
    public void drawRoundedCorner(@NonNull View view, @NonNull Canvas canvas) {
        int left;
        int top;
        if (view.getTranslationY() != 0f) {
            left = Math.round(view.getX());
            top = Math.round(view.getY());
            canvas.translate((view.getX() - left) + 0.5f, (view.getY() - top) + 0.5f);
        } else {
            left = view.getLeft();
            top = view.getTop();
        }
        mRoundedCornerBounds.set(left, top, view.getWidth() + left, view.getHeight() + top);
        drawRoundedCornerInternal(canvas);
    }

    /**
     * Represents a drawable for rendering a rounded corner "chunk" or segment.
     * This class is responsible for drawing a single rounded corner with a specified radius,
     * paint, and rotation angle. It utilizes path manipulation to create smooth corner
     * effects.
     *
     * <p>In SESL9, key performance and functional enhancements include:
     * <ul>
     *   <li>Path node caching via {@link PathParser.PathDataNode} arrays, avoiding redundant SVG path string parsing.
     *   <li>Color filter support via {@link PorterDuffColorFilter} for theme-aware dynamic tinting.
     *   <li>Reusable {@link Path} and {@link Matrix} instances for unit-space corner scaling and rotation.
     * </ul>
     * </p>
     */
    public static class SeslRoundedChunkingDrawable extends Drawable {
        private final float mAngle;
        private final Paint mPaint;
        private final int mRoundRadius;

        //Sesl9
        private ColorFilter mColorFilter;
        private PathParser.PathDataNode[] mPathDataNodes = null;
        private final Path mPath = new Path();

        public SeslRoundedChunkingDrawable(int radius, @NonNull Paint paint, float angle) {
            mRoundRadius = radius;
            mPaint = paint;
            mAngle = angle;
        }

        private Path getSmoothCornerRectPath(float cornerRadius, int rectWidth, int rectHeight) {
            if (rectWidth > 0 && rectHeight > 0) {
                // Maximum radius is half of the smaller dimension
                float maxCornerRadius = Math.min(rectWidth / 2f, rectHeight / 2f);
                // Adjust the provided corner radius to be within valid bounds
                float clampedCornerRadius = Math.min(Math.max(cornerRadius, 0f), maxCornerRadius);
                // Calculate the fraction of the adjusted radius relative to the maximum radius
                float cornerRadiusFraction = clampedCornerRadius / maxCornerRadius;

                // Calculate shrinkage factor for radius adjustment
                float shrinkageFactor;
                if (cornerRadiusFraction > SHRINK_FACTOR_THRESHOLD) {
                    float fractionExcess = cornerRadiusFraction - SHRINK_FACTOR_THRESHOLD;
                    float normalizedExcess = Math.min(1f, fractionExcess / SHRINK_FACTOR_DENOMINATOR);
                    shrinkageFactor = 1f - (normalizedExcess * SHRINK_FACTOR_MULTIPLIER);
                } else {
                    shrinkageFactor = 1f;
                }

                float scaleFactor;
                if (cornerRadiusFraction > SCALE_FACTOR_THRESHOLD) {
                    float fractionExcess = cornerRadiusFraction - SCALE_FACTOR_THRESHOLD;
                    float normalizedExcess = Math.min(1f, fractionExcess / SCALE_FACTOR_DENOMINATOR);
                    scaleFactor = 1f + (normalizedExcess * SCALE_FACTOR_MULTIPLIER);
                } else {
                    scaleFactor = 1f;
                }

                return getSmoothCornerRectPath(
                        clampedCornerRadius,
                        (float) rectWidth,
                        (float) rectHeight,
                        shrinkageFactor,
                        scaleFactor
                );
            }
            return new Path();
        }

        private Path getTopLeftSmoothCornerPath(float cornerRadius, float width, float height, float adjustScale, float scale) {
            if (mPathDataNodes == null) {
                final float horizontalRadiusFraction = ((width / 2f) / cornerRadius) * 100f;

                final float maxRadiusFraction = adjustScale * 128.19f;

                final String pathSegmentTop = String.format(LOCALE, LINE_TO_FORMAT, 0f,
                        Math.min(((height / 2f) / cornerRadius) * 100f, maxRadiusFraction));

                final Float controlPointOffsetY = scale * 83.62f;

                final String pathSegmentCurve1 = String.format(LOCALE, CUBIC_BEZIER_CURVE_FORMAT, 0f,
                        controlPointOffsetY,
                        CONTROL_POINT_X_1, CONTROL_POINT_Y_1, CURVE_END_X, CURVE_END_Y);

                final String pathSegmentCurve2 = String.format(LOCALE, CUBIC_BEZIER_CURVE_FORMAT,
                        TANGENT_X_1, TANGENT_Y_1,
                        TANGENT_Y_1, TANGENT_X_1, CURVE_END_Y, CURVE_END_X);

                final String pathSegmentBottom = String.format(LOCALE, CUBIC_BEZIER_CURVE_FORMAT,
                        CONTROL_POINT_Y_1,
                        CONTROL_POINT_X_1,
                        controlPointOffsetY, 0f, Math.min(horizontalRadiusFraction, maxRadiusFraction), 0f);

                final String pathSegmentEnd = String.format(
                        LOCALE, LINE_TO_FORMAT, Math.min(horizontalRadiusFraction, maxRadiusFraction), 0f);

                final String pathData = MOVE_TO_START
                        + pathSegmentTop
                        + pathSegmentCurve1
                        + pathSegmentCurve2
                        + pathSegmentBottom
                        + pathSegmentEnd
                        + PATH_SEGMENT_CLOSE;

                mPathDataNodes = PathParser.createNodesFromPathData(pathData);
            }

            mPath.reset();
            nodesToPath(mPathDataNodes, mPath);

            return mPath;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            mPaint.setColorFilter(mColorFilter);
            canvas.drawPath(getSmoothCornerRectPath(mRoundRadius, canvas.getWidth(), canvas.getHeight()), mPaint);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }

        @Override
        public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
            mPaint.setAlpha(alpha);
        }

        @Override
        public ColorFilter getColorFilter() {
            return mColorFilter;
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            mColorFilter = colorFilter;
        }

        @NonNull
        public Path getSmoothCornerRectPath(float cornerRadius, float width, float height, float adjustScale, float scale) {
            // Get the path for the top-left smooth corner of the rectangle
            final Path topLeftCornerPath = getTopLeftSmoothCornerPath(cornerRadius, width, height, adjustScale, scale);

            //Sesl9
            // Scale the unit-space corner path to the requested radius
            final Matrix scaleMatrix = new Matrix();
            final float scaleFactor = cornerRadius / 100.0f;
            scaleMatrix.setScale(scaleFactor, scaleFactor);
            topLeftCornerPath.transform(scaleMatrix);
            //sesl9

            // Get the bounding rectangle for transformations
            final Rect boundingRect = getBounds();

            // Create a matrix for rotation
            final Matrix rotationMatrix = new Matrix();
            rotationMatrix.setRotate(mAngle, boundingRect.width() / 2.0f, boundingRect.height() / 2.0f);

            // Apply rotation transformation to the top-left corner path
            topLeftCornerPath.transform(rotationMatrix);

            // Create a matrix for translation
            final Matrix translationMatrix = new Matrix();
            translationMatrix.setTranslate(boundingRect.left, boundingRect.top);

            // Apply translation transformation to the top-left corner path
            topLeftCornerPath.transform(translationMatrix);

            return topLeftCornerPath;
        }
    }

    /**
     * Draws the rounded corners on the given canvas with the specified insets.
     *
     * @param canvas The canvas to draw on.
     * @param insets The insets to apply to the rounded corners. If null, no insets are applied.
     */
    public void drawRoundedCorner(@NonNull Canvas canvas, @Nullable Insets insets) {
        mInsets = insets;
        drawRoundedCorner(canvas);
    }

    /**
     * Draws the rounded corners onto the provided canvas.
     * The corners are drawn within the bounds specified by the rect parameter.
     *
     * @param rect   The rectangular bounds within which the rounded corners will be drawn.
     * @param canvas The canvas on which to draw the rounded corners.
     */
    public void drawRoundedCorner(@NonNull Rect rect, @NonNull Canvas canvas) {
        mRoundedCornerBounds.set(rect);
        drawRoundedCornerInternal(canvas);
    }
}