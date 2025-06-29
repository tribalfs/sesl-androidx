/*
 * Copyright 2025 The Android Open Source Project
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

package androidx.picker.eyeDropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.picker.R;

/**
 * A custom View that displays a magnified portion of a Bitmap, typically used for an eyedropper
 * tool.
 *
 * <p>This view draws a circular magnified area of a provided screenshot Bitmap. It includes
 * a grid overlay and a central square to indicate the exact pixel being selected. The border
 * of the magnified area and the central square can be customized with different colors and stroke
 * widths.
 *
 * <p>The magnification is centered around the touch position ({@code mTouchPosX},
 * {@code mTouchPosY}) on the original screenshot. The view itself is circular, and the magnified
 * content is clipped to this circular shape.
 *
 * <p>Key visual elements:
 * <ul>
 *     <li><b>Magnified Screenshot:</b> A zoomed-in portion of the {@code mScreenShotBitmap}.</li>
 *     <li><b>Grid Lines:</b> A 15x15 grid is drawn over the magnified area to help with
 *         alignment and pixel identification.</li>
 *     <li><b>Center Square:</b> A small square at the very center of the magnifier, highlighting
 *         the pixel whose color is being picked.</li>
 *     <li><b>Inner Border:</b> A circular border just inside the main magnifier boundary.</li>
 *     <li><b>Color Border:</b> The outermost circular border, whose color can be dynamically
 *         updated ({@code mColorBorderColor}).</li>
 * </ul>
 *
 * <p>Initialization of paint objects for drawing these elements occurs in the constructor,
 * using dimension resources for stroke widths. The actual drawing logic is handled in the
 * {@link #onDraw(Canvas)} method.
 *
 * <p>Usage typically involves:
 * <ol>
 *     <li>Providing a {@link Bitmap} to {@code mScreenShotBitmap}.</li>
 *     <li>Setting the touch coordinates {@code mTouchPosX} and {@code mTouchPosY} to indicate
 *         the center of the desired magnified area.</li>
 *     <li>Optionally, setting {@code mColorBorderColor} to change the color of the outer
 *         border.</li>
 *     <li>Adding this view to a layout. The view will then automatically draw the magnified
 *         region.</li>
 * </ol>
 */
public class SeslMagnifyingView extends View {
    private final Paint mBitmapPaint;
    private final Paint mCenterSquarePaint;
    int mColorBorderColor;
    private final Paint mColorBorderPaint;
    private final int mColorBorderStrokeWidth;
    private final Paint mDividersPaint;
    private final Paint mInnerBorderPaint;
    private final int mInnerBorderStrokeWidth;
    Bitmap mScreenShotBitmap;
    float mTouchPosX;
    float mTouchPosY;

    public SeslMagnifyingView(@NonNull Context context) {
        this(context, null);
    }

    @Override
    public final void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (mScreenShotBitmap == null) {
            return;
        }
        float width = getWidth();
        float height = getHeight();
        float magnifierRadius = Math.min(width, height) / 2.0f;
        float touchPositionX = mTouchPosX;
        float magnifierWidthOffset = (width / 3.0f) / 2.0f;
        float touchPositionY = mTouchPosY;
        float magnifierHeightOffset = (height / 3.0f) / 2.0f;

        RectF magnifierSourceRect = new RectF(touchPositionX - magnifierWidthOffset,
                touchPositionY - magnifierHeightOffset, touchPositionX
                + magnifierWidthOffset, touchPositionY + magnifierHeightOffset);
        RectF magnifierDestinationRect = new RectF(0.0f, 0.0f, width, height);
        Rect roundedMagnifierSourceRect = new Rect();
        magnifierSourceRect.round(roundedMagnifierSourceRect);
        Rect roundedMagnifierDestinationRect = new Rect();
        magnifierDestinationRect.round(roundedMagnifierDestinationRect);
        canvas.save();

        Path circularClipPath = new Path();
        float viewCenterX = width / 2.0f;
        float viewCenterY = height / 2.0f;
        circularClipPath.addCircle(viewCenterX, viewCenterY, magnifierRadius, Path.Direction.CW);
        canvas.clipPath(circularClipPath);
        canvas.drawBitmap(mScreenShotBitmap, roundedMagnifierSourceRect,
                roundedMagnifierDestinationRect, mBitmapPaint);
        float gridLineCount = 15;
        float horizontalGridSpacing = width / gridLineCount;
        float verticalGridSpacing = height / gridLineCount;
        int horizontalLineIndex = 0;
        while (horizontalLineIndex < 15) {
            float currentHorizontalLineX = horizontalLineIndex * horizontalGridSpacing;
            canvas.drawLine(currentHorizontalLineX, 0.0f, currentHorizontalLineX, height,
                    mDividersPaint);
            horizontalLineIndex++;
        }
        for (int verticalLineIndex = 0; verticalLineIndex < 15; verticalLineIndex++) {
            float currentVerticalLineY = verticalLineIndex * verticalGridSpacing;
            canvas.drawLine(0.0f, currentVerticalLineY, width, currentVerticalLineY,
                    mDividersPaint);
        }
        float centerSquareWidthOffset = (width / 15.0f) / 2.0f;
        float centerSquareHeightOffset = (height / 15.0f) / 2.0f;
        float innerBorderStrokeWidth = mInnerBorderStrokeWidth;
        canvas.drawRoundRect(
                viewCenterX - centerSquareWidthOffset, viewCenterY - centerSquareHeightOffset,
                viewCenterX
                        + centerSquareWidthOffset, centerSquareHeightOffset + viewCenterY,
                innerBorderStrokeWidth, innerBorderStrokeWidth, mCenterSquarePaint);
        canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f,
                magnifierRadius - ((mInnerBorderStrokeWidth / 2.0f) + mColorBorderStrokeWidth),
                mInnerBorderPaint);
        mColorBorderPaint.setColor(mColorBorderColor);
        canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f,
                magnifierRadius - (mColorBorderStrokeWidth / 2.0f), mColorBorderPaint);
        canvas.restore();
    }

    public SeslMagnifyingView(@NonNull Context context, @NonNull AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SeslMagnifyingView(@NonNull Context context, @NonNull AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Paint.Style style = Paint.Style.STROKE;
        int colorBorderStrokeWidthDimension = getResources().getDimensionPixelSize(
                R.dimen.sesl_eyedropper_color_border_stroke_width);
        int innerBorderStrokeWidthDimension = getResources().getDimensionPixelSize(
                R.dimen.sesl_eyedropper_inner_border_stroke_width);
        int dividersStrokeWidthDimension = getResources().getDimensionPixelSize(
                R.dimen.sesl_eyedropper_dividers_stroke_width);
        int centerSquareStrokeWidthDimension = getResources().getDimensionPixelSize(
                R.dimen.sesl_eyedropper_center_square_stroke_width);

        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setStyle(Paint.Style.FILL);

        mColorBorderPaint = new Paint();
        mColorBorderPaint.setStyle(style);
        mColorBorderPaint.setAntiAlias(true);
        mColorBorderPaint.setStrokeWidth(colorBorderStrokeWidthDimension);


        mInnerBorderPaint = new Paint();
        mInnerBorderPaint.setStyle(style);
        mInnerBorderPaint.setColor(getResources().getColor(R.color.sesl_color_picker_cursor_stroke_color));
        mInnerBorderPaint.setAntiAlias(true);
        mInnerBorderPaint.setStrokeWidth(innerBorderStrokeWidthDimension);

        mDividersPaint = new Paint();
        mDividersPaint.setStyle(style);
        mDividersPaint.setColor(getResources().getColor(R.color.sesl_color_picker_swatch_cursor_color));
        mDividersPaint.setAntiAlias(true);
        mDividersPaint.setStrokeWidth(dividersStrokeWidthDimension);

        mCenterSquarePaint = new Paint();
        mCenterSquarePaint.setStyle(style);
        mCenterSquarePaint.setColor(getResources().getColor(R.color.sesl_color_picker_cursor_stroke_color));
        mCenterSquarePaint.setAntiAlias(true);
        mCenterSquarePaint.setStrokeWidth(centerSquareStrokeWidthDimension);

        mColorBorderStrokeWidth = colorBorderStrokeWidthDimension;
        mInnerBorderStrokeWidth = innerBorderStrokeWidthDimension;
    }
}