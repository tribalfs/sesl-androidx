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

package androidx.picker3.app;

import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.picker.R;
import androidx.picker.eyeDropper.SeslBitmapHolder;
import androidx.picker.eyeDropper.SeslEyeDropperActivity;
import androidx.picker3.widget.SeslColorPicker;
import androidx.picker3.widget.SeslColorPicker.PickerMode;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */
/**
 * A dialog that shows a {@link SeslColorPicker}.
 *
 * <p>This dialog allows users to select a color using various methods, including
 * a spectrum view, RGB/HSV input fields, and an eyedropper tool.
 * It provides options to set an initial color, recently used colors, and toggle
 * the visibility of an opacity bar.</p>
 *
 * <p>The dialog can be configured to use an eyedropper tool by providing an image
 * via {@link #setOnBitmapSetListener(OnBitmapSetListener)}.
 * When an image is set, the eyedropper tool becomes visible, allowing users to
 * pick colors directly from the image. If no image is provided, or if it's set
 * to {@code null}, the eyedropper tool is hidden, and a "last used color slot"
 * might be shown instead, depending on the {@link SeslColorPicker} configuration.</p>
 *
 * <p>The selected color can be retrieved through the {@link OnColorSetListener}
 * interface, which is invoked when the user confirms their selection.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * SeslColorPickerDialog colorPickerDialog = new SeslColorPickerDialog(
 *     context,
 *     new SeslColorPickerDialog.OnColorSetListener() {
 *         @Override
 *         public void onColorSet(int color) {
 *             // Handle the selected color
 *         }
 *     },
 *     initialColor, // Optional: set an initial color
 *     recentlyUsedColors, // Optional: provide an array of recently used colors
 *     true // Optional: show the opacity bar
 * );
 *
 * // To use the eyedropper with an image:
 * colorPickerDialog.setOnBitmapSetListener(new SeslColorPickerDialog.OnBitmapSetListener() {
 *     @Override
 *     public Bitmap onBitmapSet() {
 *         // Return the Bitmap to be used by the eyedropper
 *         // For example, load from resources:
 *         // return BitmapFactory.decodeResource(getResources(), R.drawable.my_image);
 *         return yourBitmap;
 *     }
 * });
 *
 * colorPickerDialog.show();
 * }
 * </pre>
 *
 * <p><b>Note:</b> To use the eye dropper tool, you must declare
 * {@code androidx.picker.eyeDropper.SeslEyeDropperActivity} in your app's {@code AndroidManifest.xml}:
 * <pre>
 * &lt;activity android:name="androidx.picker.eyeDropper.SeslEyeDropperActivity"
 *           android:exported="false" /&gt;
 * </pre>
 * @see SeslColorPicker
 * @see OnColorSetListener
 */
public class SeslColorPickerDialog extends AlertDialog
        implements DialogInterface.OnClickListener {
    private static final String TAG = "SeslColorPickerDialog";

    private static final String COLOR_PICKER_MODE = "color_picker_mode";
    private final SeslColorPicker mColorPicker;
    Integer mCurrentColor = null;
    private final OnColorSetListener mOnColorSetListener;

    /**
     * Interface used to indicate that the user has finished selecting a color.
     */
    public interface OnColorSetListener {
        void onColorSet(int color);
    }

    //Sesl7
    private Bitmap mBitmap = null;
    public interface OnBitmapSetListener {
        @NonNull
        Bitmap onBitmapSet();
    }
    //sesl7

    public SeslColorPickerDialog(@NonNull Context context, @Nullable OnColorSetListener listener) {
        super(context, resolveDialogTheme(context));
        // Ensure we are using the correctly themed context rather than the context that was
        // passed in.
        context = getContext();

        final LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.sesl_color_picker_oneui_3_dialog, null);
        setView(view);

        setButton(BUTTON_POSITIVE, context.getString(R.string.sesl_picker_done), this);
        setButton(BUTTON_NEGATIVE, context.getString(R.string.sesl_picker_cancel), this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        mOnColorSetListener = listener;
        mColorPicker = view.findViewById(R.id.sesl_color_picker_content_view);
        setupEyeDropper();
    }

    public SeslColorPickerDialog(@NonNull Context context, @Nullable OnColorSetListener listener,
                                int currentColor) {
        this(context, listener);
        mColorPicker.getRecentColorInfo().setCurrentColor(currentColor);
        mCurrentColor = currentColor;
        mColorPicker.updateRecentColorLayout();
    }

    public SeslColorPickerDialog(@NonNull Context context, @Nullable OnColorSetListener listener,
                                 int[] recentlyUsedColors) {
        this(context, listener);
        mColorPicker.getRecentColorInfo().initRecentColorInfo(recentlyUsedColors);
        mColorPicker.updateRecentColorLayout();
    }

    public SeslColorPickerDialog(@NonNull Context context, @Nullable OnColorSetListener onColorSetListener,
                                 int currentColor, int[] recentlyUsedColors, boolean showOpacityBar) {
        this(context, onColorSetListener);
        mColorPicker.getRecentColorInfo().initRecentColorInfo(recentlyUsedColors);
        mColorPicker.getRecentColorInfo().setCurrentColor(currentColor);
        mCurrentColor = currentColor;
        mColorPicker.updateRecentColorLayout();
        mColorPicker.initOpacitySeekBar(showOpacityBar);
    }

    @Override
    public void onClick(DialogInterface dialog, int whichButton) {
        switch (whichButton) {
            case BUTTON_NEGATIVE:
            default:
                return;
            case BUTTON_POSITIVE:
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                mColorPicker.saveSelectedColor();
                if (mOnColorSetListener != null) {
                    if (mColorPicker.isUserInputValid() || mCurrentColor == null) {
                        mOnColorSetListener.onColorSet(mColorPicker.getRecentColorInfo()
                                .getSelectedColor());
                    } else {
                        mOnColorSetListener.onColorSet(mCurrentColor);
                    }
                }
        }
    }

    /**
     * Returns the {@link SeslColorPicker} instance used by this dialog.
     *
     * @return The {@link SeslColorPicker} instance.
     */
    public @NonNull SeslColorPicker getColorPicker() {
        return mColorPicker;
    }

    /**
     * Sets the color picker to only show the spectrum mode.
     * <p>
     * When this mode is enabled, the color picker will hide the swatch and opacity bar,
     * presenting only the spectrum view for color selection. This simplifies the interface
     * for users who only need to pick colors from the spectrum without fine-tuning opacity
     * or using predefined swatches.
     * </p>
     */
    public void setOnlySpectrumMode() {
        mColorPicker.setOnlySpectrumMode();
    }

    /**
     * Sets the new color in the color picker.
     * <p>
     * This method updates the new color in the recent color information
     * of the color picker and refreshes the recent color layout to reflect the change.
     *
     * @param newColor The new color to set, represented as an Integer.
     *                 If {@code null}, the new color will be unset.
     */
    public void setNewColor(@Nullable Integer newColor) {
        mCurrentColor = newColor;
        mColorPicker.getRecentColorInfo().setNewColor(newColor);
        mColorPicker.updateRecentColorLayout();
    }

    /**
     * Sets whether the transparency control (opacity bar) is enabled in the color picker.
     *
     * <p>When enabled, the user can adjust the alpha value (transparency) of the selected color.
     * When disabled, the opacity bar is hidden, and the color will be fully opaque.</p>
     *
     * @param enabled {@code true} to enable the transparency control, {@code false} to disable it.
     */
    public void setTransparencyControlEnabled(boolean enabled) {
        mColorPicker.setOpacityBarEnabled(enabled);
    }

    @NonNull
    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(COLOR_PICKER_MODE, mColorPicker.getPickerMode());
        return state;
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mColorPicker.setPickerMode(savedInstanceState.getInt(COLOR_PICKER_MODE));
    }

    /**
     * Sets the display mode of the color picker.
     *
     * <p>This method allows you to control which views (e.g., spectrum, swatches, opacity bar)
     * are visible and how they are arranged within the color picker.</p>
     *
     * <p>The available modes are defined in {@link SeslColorPicker.PickerMode}.
     * For example, to show only the color spectrum, you can use
     * {@link SeslColorPicker#MODE_SPECTRUM}.</p>
     *
     * @param mode The desired display mode for the color picker. This should be one of the
     *             constants defined in {@link SeslColorPicker.PickerMode}.
     * @see SeslColorPicker#setPickerMode(int)
     * @see SeslColorPicker.PickerMode
     */
    public void setMode(@PickerMode int mode) {
        mColorPicker.setPickerMode(mode);
    }

    /**
     * Retrieves the current mode of the color picker.
     *
     * @return The current picker mode, which can be one of
     *         {@link SeslColorPicker#MODE_SPECTRUM},
     *         or {@link SeslColorPicker#MODE_SWATCH}.
     */
    @PickerMode
    public int getMode(){
        return mColorPicker.getPickerMode();
    }

    private static int resolveDialogTheme(Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(androidx.appcompat.R.attr.isLightTheme, outValue, true);
        return outValue.data != 0
                ? androidx.appcompat.R.style.ThemeOverlay_AppCompat_Light_Dialog :
                androidx.appcompat.R.style.ThemeOverlay_AppCompat_Dialog;
    }

    private static Activity scanForActivity(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        }
        if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    //Sesl7
    private void setupEyeDropper(){
        mColorPicker.setEyeDropperDisable(mBitmap == null);
        mColorPicker.setOnEyeDropperListener(() -> {
            SeslEyeDropperActivity.setOnColorPickListener(
                    color -> {
                        show();
                        setNewColor(color);
                    }
            );
            startEyeDropperActivity();
        });
    }

    private void startEyeDropperActivity() {
        SeslBitmapHolder.setBitmapWeakReference(mBitmap);

        Context context = getContext();
        Bundle bundle = ActivityOptions.makeCustomAnimation(
                getContext(), android.R.anim.fade_in,
                android.R.anim.fade_out).toBundle();
        Intent intent = new Intent(context, SeslEyeDropperActivity.class);
        if (scanForActivity(context) == null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent, bundle);
    }

    /**
     * Disables or enables the eye dropper tool.
     *
     * <p>When disabled, the eye dropper button in the {@link SeslColorPicker} will be hidden.</p>
     *
     * <p>When enabled, the eye dropper button will be visible, allowing the user to pick colors from the image.
     * Ensure that the image is set using {@link #setOnBitmapSetListener(OnBitmapSetListener)}.</p>
     *
     * @param disable {@code true} to disable the eye dropper, {@code false} to enable it.
     * @see SeslColorPicker#setEyeDropperDisable(boolean)
     */
    public void disableEyeDropper(boolean disable) {
        mColorPicker.setEyeDropperDisable(disable);
    }

    /**
     * Sets a listener to provide a {@link Bitmap} for the eye dropper tool.
     * <p>
     * When a listener is set, it will be invoked to retrieve a {@link Bitmap}.
     * If the listener returns a non-null {@link Bitmap}, the eye dropper tool
     * in the {@link SeslColorPicker} will be enabled, allowing users to pick
     * colors from the provided image.
     * </p>
     *
     * @param listener The listener to be invoked when a bitmap is needed for the
     *                 eye dropper
     * @see OnBitmapSetListener
     * @see #disableEyeDropper(boolean)
     */
    public void setOnBitmapSetListener(@Nullable OnBitmapSetListener listener) {
        if (listener != null) {
            mBitmap = listener.onBitmapSet();
            mColorPicker.setEyeDropperDisable(false);
        }
    }
    //sesl7
}
