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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.picker.R;
import androidx.picker3.app.SeslColorPickerDialog;

//Added in sesl7
/**
 * Activity for the Eye Dropper tool.
 *
 * <p>This activity allows users to pick a color from an image displayed on the screen.
 * It uses a magnifying view to help users accurately select the desired color.
 * The selected color is then passed back to the calling activity or component
 * through the {@link ColorPickListener#onColorPicked(Integer)}.
 *
 * <p>The activity handles various aspects such as:
 * <ul>
 *     <li>Displaying the image from which the color needs to be picked.
 *     <li>Providing a touch interface to select a pixel.
 *     <li>Showing a magnifying glass and a pointer for precise color selection.
 *     <li>Handling screen orientation changes and activity lifecycle events gracefully.
 *     <li>Dismissing the keyguard if it's locked when the activity starts.
 *     <li>Communicating the selected color back via a listener.
 * </ul>
 *
 * @see #setOnColorPickListener(ColorPickListener)
 * @see SeslColorPickerDialog
 * @see SeslMagnifyingView
 */
public class SeslEyeDropperActivity extends AppCompatActivity {

    /**
     * Interface definition for a callback to be invoked when a color is picked.
     * Implement this interface to receive the selected color from the Eye Dropper tool.
     */
    public interface ColorPickListener {
         void onColorPicked(@NonNull Integer color);
    }

    private static final long ANIMATION_DURATION = 400;
    private static final String TAG = "SeslEyeDropper";

    @Nullable
    private static ColorPickListener mOnColorPickListener;

    private ImageView mBitmapView;
    private int mCurrentPixelColor;
    private Bitmap mImageBitmap;
    private SeslMagnifyingView mMagnifyingView;
    private View mPointerView;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAfterTransition();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration configuration) {
        super.onConfigurationChanged(configuration);
        finishAfterTransition();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (keyguardManager != null && keyguardManager.isKeyguardLocked()) {
            if (Build.VERSION.SDK_INT >= 26) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        }

        getWindow().setFlags(512, 512);
        setContentView(R.layout.activity_eye_dropper);

        mBitmapView = findViewById(R.id.screenshotView);
        mBitmapView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        mMagnifyingView = findViewById(R.id.magnifierView);
        mPointerView = findViewById(R.id.pointerView);
        captureAndSetupBitmap();
        initializeBitmapViewAnimation();
        setupTouchListener();
    }

    private void captureAndSetupBitmap() {
        mBitmapView.post(() -> {
            mImageBitmap = addBackground(SeslBitmapHolder.getBitmap());
            setupBitmapView();
        });
    }

    private Bitmap addBackground(@Nullable Bitmap bitmap) {
        int width = mBitmapView.getWidth();
        int height = mBitmapView.getHeight();
        Bitmap createBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        canvas.drawColor(0xFF000000);
        if (bitmap != null) {
            float scale = Math.min((float) width / bitmap.getWidth(), (float) height / bitmap.getHeight());
            Bitmap scaledBitmap = bitmap;
            if (bitmap.getWidth() > width || bitmap.getHeight() > height) {
                scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                        (int) (bitmap.getWidth() * scale),
                        (int) (bitmap.getHeight() * scale),
                        false);
            }
            canvas.drawBitmap(scaledBitmap,
                    (width - scaledBitmap.getWidth()) / 2f,
                    (height - scaledBitmap.getHeight()) / 2f,
                    null);
        }
        return createBitmap;
    }


    private void setupBitmapView() {
        final int centerX = mImageBitmap.getWidth() / 2;
        final int centerY = mImageBitmap.getHeight() / 2;
        mCurrentPixelColor = mImageBitmap.getPixel(centerX, centerY);

        mBitmapView.post(() -> {
            mBitmapView.setImageBitmap(mImageBitmap);
            mMagnifyingView.mScreenShotBitmap = mImageBitmap;
            mMagnifyingView.mTouchPosX = centerX;
            mMagnifyingView.mTouchPosY = centerY;
            mMagnifyingView.mColorBorderColor = mCurrentPixelColor;
            mMagnifyingView.invalidate();
            positionMagnifierAndPointer(centerX, centerY, mImageBitmap.getPixel(centerX, centerY));
        });
    }

    private void positionMagnifierAndPointer(int x, int y, int color) {
        if (mMagnifyingView == null || mPointerView == null || mImageBitmap == null) return;

        mMagnifyingView.mTouchPosX = x;
        mMagnifyingView.mTouchPosY = y;
        mMagnifyingView.mColorBorderColor = color;
        mMagnifyingView.invalidate();

        int pointerHeight = mPointerView.getHeight();
        int magnifierHeight = mMagnifyingView.getHeight();
        int yOffset = getResources().getDimensionPixelSize(R.dimen.sesl_eyedropper_y_offset);

        if (y <= mImageBitmap.getHeight() * 0.2) {
            mMagnifyingView.setY((pointerHeight / 2.0f) + y + yOffset);
        } else {
            mMagnifyingView.setY(y - ((pointerHeight / 2.0f) + magnifierHeight + yOffset));
        }
        mMagnifyingView.setX(x - (mMagnifyingView.getWidth() / 2.0f));
        mPointerView.setX(x - (mPointerView.getWidth() / 2.0f));
        mPointerView.setY(y - (pointerHeight / 2.0f));
    }

    private void initializeBitmapViewAnimation() {
        mBitmapView.setClickable(false);
        mBitmapView.setEnabled(false);
        int yAnimOffset = getResources().getDimensionPixelSize(R.dimen.sesl_eyedropper_y_animation_offset);
        PathInterpolator interpolator = new PathInterpolator(0.22f, 0.25f, 0.0f, 1.0f);

        ObjectAnimator pointerScaleX = ObjectAnimator.ofFloat(mPointerView, "scaleX", 0.0f, 1.0f);
        ObjectAnimator pointerScaleY = ObjectAnimator.ofFloat(mPointerView, "scaleY", 0.0f, 1.0f);
        ObjectAnimator magnifierScaleX = ObjectAnimator.ofFloat(mMagnifyingView, "scaleX", 0.0f, 1.0f);
        ObjectAnimator magnifierScaleY = ObjectAnimator.ofFloat(mMagnifyingView, "scaleY", 0.0f, 1.0f);
        ObjectAnimator pointerTransY = ObjectAnimator.ofFloat(mPointerView, "translationY", 0.0f, yAnimOffset);

        pointerScaleX.setDuration(ANIMATION_DURATION);
        pointerScaleY.setDuration(ANIMATION_DURATION);
        magnifierScaleX.setDuration(ANIMATION_DURATION);
        magnifierScaleY.setDuration(ANIMATION_DURATION);
        pointerTransY.setDuration(ANIMATION_DURATION);

        pointerScaleX.setInterpolator(interpolator);
        pointerScaleY.setInterpolator(interpolator);
        magnifierScaleX.setInterpolator(interpolator);
        magnifierScaleY.setInterpolator(interpolator);
        pointerTransY.setInterpolator(interpolator);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(magnifierScaleX, magnifierScaleY, pointerScaleX, pointerScaleY, pointerTransY);

        mPointerView.setVisibility(View.VISIBLE);
        mMagnifyingView.setVisibility(View.VISIBLE);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mBitmapView.setClickable(true);
                mBitmapView.setEnabled(true);
            }
        });
        animatorSet.start();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchListener() {
        mBitmapView.setOnTouchListener((view, event) -> {
            int x = (int) event.getX();
            int y = (int) event.getY();

            if (x >= 0 && x < mImageBitmap.getWidth()) {
                if ((float) y > mPointerView.getHeight() / 2.0f &&
                        (float) y < mImageBitmap.getHeight() - (mPointerView.getHeight() / 2.0f)) {
                    int action = event.getActionMasked();
                    if (action == MotionEvent.ACTION_UP) {
                        if (mOnColorPickListener != null) {
                            mCurrentPixelColor = mImageBitmap.getPixel(x, y);
                            mOnColorPickListener.onColorPicked(mCurrentPixelColor);
                            mOnColorPickListener = null;
                        }
                        finishAfterTransition();
                    }
                    positionMagnifierAndPointer(x, y, mCurrentPixelColor);
                }
            }
            return true;
        });
    }

    @Override
    public void finishAfterTransition() {
        super.finishAfterTransition();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Sets the listener for color pick events.
     * <p>
     * This listener will be notified when a color is picked by the user.
     * The listener is stored as a static field and should be cleared (set to null)
     * when it's no longer needed, typically in the onDestroy method of the activity
     * or component that sets it, to prevent memory leaks.
     *
     * @param colorPickListener The listener to be notified of color pick events.
     *                          Can be null to remove the listener.
     */
    public static void setOnColorPickListener(@Nullable ColorPickListener colorPickListener) {
        mOnColorPickListener = colorPickListener;
    }
}