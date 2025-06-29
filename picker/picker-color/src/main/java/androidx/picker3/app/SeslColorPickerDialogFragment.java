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

import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.util.SeslMisc;
import androidx.fragment.app.FragmentActivity;
import androidx.picker.R;
import androidx.picker.eyeDropper.SeslBitmapHolder;
import androidx.picker.eyeDropper.SeslEyeDropperActivity;
import androidx.picker3.widget.SeslColorPicker;
import androidx.picker3.widget.SeslColorPicker.OnColorChangedListener;

import java.io.Serializable;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */

/**
 * A DialogFragment that shows a color picker.
 *
 * <p>The basic {@link SeslColorPickerDialogFragment} is created using
 * {@link SeslColorPickerDialogFragment#newInstance(OnColorSetListener)}
 * and the {@link SeslColorPickerDialogFragment#show} method.
 *
 * <p>The {@link SeslColorPickerDialogFragment#newInstance(OnColorSetListener, int)} constructor can
 * be used to provide an initial color for the dialog.
 *
 * <p>The {@link SeslColorPickerDialogFragment#newInstance(OnColorSetListener, int[])} constructor can
 * be used to provide an array of recently used colors.
 *
 * <p>The {@link SeslColorPickerDialogFragment#newInstance(OnColorSetListener, int, int[], boolean)}
 * constructor can be used to provide an initial color, an array of recently used colors and
 * whether to show the opacity bar.
 *
 * <p>The {@link SeslColorPickerDialogFragment#newInstance(OnColorSetListener, int, int[], boolean, boolean)}
 * constructor can be used to provide an initial color, an array of recently used colors,
 * whether to show the opacity bar, and whether to show only the spectrum view.
 *
 * <p><b>Note:</b> You should pay attention to fragment lifecycle. When the activity is recreated
 * (e.g. on configuration change), the fragment will also be recreated. In this case, you should
 * use the {@link SeslColorPickerDialogFragment#setOnColorChangedListener(OnColorChangedListener)}
 * method to set the listener again in the {@link #onCreate(Bundle)} method of your activity.
 *
 * <p>If you want to use the eye dropper feature, your Activity must implement
 * {@link SeslColorPickerDialogFragment.OnBitmapSetListener}. The
 * {@link SeslColorPickerDialogFragment.OnBitmapSetListener#onBitmapSet()} method will be called
 * to get the Bitmap to be used for the eye dropper.
 *
 * <p>Example:
 * <pre>
 * public class MyActivity extends AppCompatActivity implements SeslColorPickerDialogFragment.OnBitmapSetListener {
 *     private SeslColorPickerDialogFragment mColorPickerDialogFragment;
 *
 *     &#64;Override
 *     protected void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         setContentView(R.layout.activity_main);
 *
 *         // Create the color picker dialog fragment with a color set listener
 *         mColorPickerDialogFragment = SeslColorPickerDialogFragment.newInstance(
 *             new SeslColorPickerDialogFragment.OnColorSetListener() {
 *                 &#64;Override
 *                 public void onColorSet(int color) {
 *                     // Handle the selected color
 *                     findViewById(R.id.colorPreview).setBackgroundColor(color);
 *                 }
 *             },
 *             Color.RED, // initial color
 *             new int[] {Color.RED, Color.GREEN, Color.BLUE}, // recently used colors
 *             true, // show opacity bar
 *             false // show only spectrum
 *         );
 *
 *         // Optionally set a color changed listener (e.g., for live preview)
 *         mColorPickerDialogFragment.setOnColorChangedListener(new SeslColorPicker.OnColorChangedListener() {
 *             &#64;Override
 *             public void onColorChanged(int color) {
 *                 // Live update preview
 *                 findViewById(R.id.colorPreview).setBackgroundColor(color);
 *             }
 *         });
 *
 *         // Show the dialog when needed, e.g., on button click
 *         findViewById(R.id.showColorPickerButton).setOnClickListener(v -> {
 *             mColorPickerDialogFragment.show(getSupportFragmentManager(), "color_picker");
 *         });
 *     }
 *
 *     // Implement the OnBitmapSetListener for the eye dropper feature
 *     &#64;NonNull
 *     &#64;Override
 *     public Bitmap onBitmapSet() {
 *        return BitmapFactory.decodeResource(getResources(), R.drawable.your_drawable)
 *     }
 * }
 * </pre>
 *
 * <p>Replace <code>R.id.colorPreview</code> and <code>R.id.showColorPickerButton</code> with actual view IDs from your layout.
 * The <code>onBitmapSet()</code> method should return the bitmap you want to use for the eye dropper feature.
 * Remember to set the listeners again after configuration changes if needed, as described above.
 */
public class SeslColorPickerDialogFragment extends AppCompatDialogFragment
        implements DialogInterface.OnClickListener {
    private static final String TAG = "SeslColorPickerDialogFragment";

    private static final String KEY_CURRENT_COLOR = "current_color";
    private static final String KEY_RECENTLY_USED_COLORS = "recently_used_colors";
    private static final String KEY_OPACITY_BAR_ENABLED = "opacity_bar_enabled";
    private static final String KEY_SHOW_OPACITY = "show_opacity_bar";
    private static final String KEY_SHOW_ONLY_SPECTRUM = "show_only_spectrum";
    private static final String KEY_COLOR_SET_LISTENER = "color_set_listener";
    private static final String KEY_SHOW_EYE_DROPPER = "disable_eye_dropper";//sesl7

    private AlertDialog mAlertDialog;
    private SeslColorPicker mColorPicker;
    private OnColorChangedListener mOnColorChangedListener;
    private OnColorSetListener mOnColorSetListener;
    private Integer mCurrentColor = null;
    private Integer mNewColor = null;
    private int[] mRecentlyUsedColors = null;
    private boolean mShowOpacity = false;
    private boolean mIsTransparencyControlEnabled = false;
    private boolean mIsOnlySpectrumMode = false;

    /**
     * Interface definition for a callback to be invoked when a color is set in the dialog.
     */
    public interface OnColorSetListener extends Serializable {
        void onColorSet(int color);
    }

    /**
     * Sets the listener to be called when the color is changed.
     *
     * @param listener The listener to be called when the color is changed.
     */
    public void setOnColorChangedListener(@Nullable OnColorChangedListener listener) {
        mOnColorChangedListener = listener;
    }

    //Sesl7
    private OnBitmapSetListener mOnBitmapSetListener;
    private boolean mIsEyeDropperDisable = true;
    private Bitmap mBitmap = null;
    /** Interface definition for a callback to be invoked to get the Bitmap for the eye dropper. */
    public interface OnBitmapSetListener {
        /** This method is used to get the Bitmap to be used for the eye dropper feature. */
        @NonNull
        Bitmap onBitmapSet();
    }
    //sesl7

    @NonNull
    public static SeslColorPickerDialogFragment newInstance(@Nullable OnColorSetListener listener) {
        SeslColorPickerDialogFragment instance = new SeslColorPickerDialogFragment();

        final Bundle args = new Bundle();
        args.putSerializable(KEY_COLOR_SET_LISTENER, listener);

        instance.setArguments(args);
        return instance;
    }

    @NonNull
    public static SeslColorPickerDialogFragment newInstance(@Nullable OnColorSetListener listener,
                                                            int currentColor) {
        SeslColorPickerDialogFragment instance = new SeslColorPickerDialogFragment();

        final Bundle args = new Bundle();
        args.putSerializable(KEY_COLOR_SET_LISTENER, listener);
        args.putSerializable(KEY_CURRENT_COLOR, currentColor);

        instance.setArguments(args);
        return instance;
    }

    @NonNull
    public static SeslColorPickerDialogFragment newInstance(@Nullable OnColorSetListener onColorSetListener,
                                                            int[] recentlyUsedColors) {
        SeslColorPickerDialogFragment instance = new SeslColorPickerDialogFragment();

        final Bundle args = new Bundle();
        args.putSerializable(KEY_COLOR_SET_LISTENER, onColorSetListener);
        args.putIntArray(KEY_RECENTLY_USED_COLORS, recentlyUsedColors);

        instance.setArguments(args);
        return instance;
    }

    @NonNull
    public static SeslColorPickerDialogFragment newInstance(@Nullable OnColorSetListener onColorSetListener,
                                                            int currentColor,
            int[] recentlyUsedColors, boolean showOpacityBar) {
        SeslColorPickerDialogFragment instance = new SeslColorPickerDialogFragment();

        final Bundle args = new Bundle();
        args.putSerializable(KEY_COLOR_SET_LISTENER, onColorSetListener);
        args.putSerializable(KEY_CURRENT_COLOR, currentColor);
        args.putIntArray(KEY_RECENTLY_USED_COLORS, recentlyUsedColors);
        args.putBoolean(KEY_SHOW_OPACITY, showOpacityBar);

        instance.setArguments(args);
        return instance;
    }

    @NonNull
    public static SeslColorPickerDialogFragment newInstance(@Nullable OnColorSetListener onColorSetListener,
            int currentColor, int[] recentlyUsedColors, boolean showOpacityBar, boolean showOnlySpectrum) {
        SeslColorPickerDialogFragment instance = new SeslColorPickerDialogFragment();

        final Bundle args = new Bundle();
        args.putSerializable(KEY_COLOR_SET_LISTENER, onColorSetListener);
        args.putSerializable(KEY_CURRENT_COLOR, currentColor);
        args.putIntArray(KEY_RECENTLY_USED_COLORS, recentlyUsedColors);
        args.putBoolean(KEY_SHOW_OPACITY, showOpacityBar);
        args.putBoolean(KEY_SHOW_ONLY_SPECTRUM, showOnlySpectrum);

        instance.setArguments(args);
        return instance;
    }

    /** @noinspection deprecation*/
    @Override
    public void onActivityCreated(@Nullable Bundle bundle) {
        super.onActivityCreated(bundle);
        if (getActivity() instanceof OnBitmapSetListener onBitmapSetListener) {
            mOnBitmapSetListener = onBitmapSetListener;
            mBitmap = onBitmapSetListener.onBitmapSet();
            mColorPicker.setEyeDropperDisable(false);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mRecentlyUsedColors
                    = savedInstanceState.getIntArray(KEY_RECENTLY_USED_COLORS);
            mCurrentColor
                    = (Integer) savedInstanceState.getSerializable(KEY_CURRENT_COLOR);
            mIsTransparencyControlEnabled
                    = savedInstanceState.getBoolean(KEY_OPACITY_BAR_ENABLED);
            mIsEyeDropperDisable = savedInstanceState.getBoolean(KEY_SHOW_EYE_DROPPER);
        }
    }


    @Nullable
    @Override
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        mColorPicker = (SeslColorPicker) inflater.inflate(R.layout.sesl_color_picker_oneui_3_dialog, null);

        final Window window;
        if (getDialog() != null && (window = getDialog().getWindow()) != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.getDecorView().setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    @NonNull
                    public WindowInsets onApplyWindowInsets(@NonNull View view, @NonNull WindowInsets windowInsets) {
                        WindowManager.LayoutParams attributes = window.getAttributes();
                        attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
                        window.setAttributes(attributes);
                        return windowInsets.consumeSystemWindowInsets();
                    }
                });
            }
        }

        final Bundle args = getArguments();
        if (args != null) {
            mOnColorSetListener = (OnColorSetListener) args.getSerializable(KEY_COLOR_SET_LISTENER);
            mCurrentColor = (Integer) args.getSerializable(KEY_CURRENT_COLOR);
            mRecentlyUsedColors = args.getIntArray(KEY_RECENTLY_USED_COLORS);
            mShowOpacity = args.getBoolean(KEY_SHOW_OPACITY);
            mIsOnlySpectrumMode = args.getBoolean(KEY_SHOW_ONLY_SPECTRUM);
        }

        if (mCurrentColor != null) {
            mColorPicker.getRecentColorInfo().setCurrentColor(mCurrentColor);
        }
        if (mNewColor != null) {
            mColorPicker.getRecentColorInfo().setNewColor(mNewColor);
        }
        if (mRecentlyUsedColors != null) {
            mColorPicker.getRecentColorInfo().initRecentColorInfo(mRecentlyUsedColors);
        }
        if (mIsOnlySpectrumMode) {
            mColorPicker.setOnlySpectrumMode();
        }

        mColorPicker.setOpacityBarEnabled(mIsTransparencyControlEnabled);
        mColorPicker.updateRecentColorLayout();
        mColorPicker.setOnColorChangedListener(mOnColorChangedListener);
        mColorPicker.initOpacitySeekBar(mShowOpacity);

        mAlertDialog.setView(mColorPicker);

        FragmentActivity activity = getActivity();
        if (activity != null) {
            mColorPicker.setOnEyeDropperListener(
                    () -> {
                        SeslEyeDropperActivity.setOnColorPickListener(
                                new SeslEyeDropperActivity.ColorPickListener() {
                                    @Override
                                    public void onColorPicked(@NonNull Integer color) {
                                        SeslBitmapHolder.clearBitmap();
                                        SeslColorPickerDialogFragment.showNewInstance(activity, color, args, mOnColorChangedListener);
                                    }
                                }
                        );
                        mAlertDialog.dismiss();
                        startEyeDropperActivity();
                    }
            );
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    static void showNewInstance(@NonNull FragmentActivity activity, Integer newColor, Bundle args,
            @Nullable OnColorChangedListener onColorChangedListener){
        SeslColorPickerDialogFragment instance = new SeslColorPickerDialogFragment();
        if (args != null) {
            instance.setArguments(args);
        }

        if (onColorChangedListener != null) {
            instance.setOnColorChangedListener(onColorChangedListener);
        }
        instance.setNewColor(newColor);

        if (!activity.getSupportFragmentManager().isStateSaved()) {
            instance.show(activity.getSupportFragmentManager(), SeslColorPickerDialogFragment.TAG);
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    try {
                        activity.getSupportFragmentManager()
                                .beginTransaction()
                                .add(instance, SeslColorPickerDialogFragment.TAG)
                                .setReorderingAllowed(true)
                                .commitAllowingStateLoss();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }, 500L);
        }

    }

    private void startEyeDropperActivity() {
        Context context = requireContext();
        SeslBitmapHolder.setBitmapWeakReference(mBitmap);
        Bundle bundle = ActivityOptions.makeCustomAnimation(
                context, android.R.anim.fade_in,
                android.R.anim.fade_out).toBundle();
        Intent intent = new Intent(context, SeslEyeDropperActivity.class);
        context.startActivity(intent, bundle);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        mAlertDialog = new ColorPickerDialog(getActivity());
        mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                context.getString(R.string.sesl_picker_done), this);
        mAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                context.getString(R.string.sesl_picker_cancel), this);
        return mAlertDialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int whichButton) {
        switch (whichButton) {
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.dismiss();
                break;
            case DialogInterface.BUTTON_POSITIVE:
                getDialog().getWindow()
                        .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                mColorPicker.saveSelectedColor();
                if (mOnColorSetListener != null) {
                    if (mCurrentColor == null || mColorPicker.isUserInputValid()) {
                        mOnColorSetListener.onColorSet(mColorPicker.getRecentColorInfo()
                                .getSelectedColor());
                    } else {
                        mOnColorSetListener.onColorSet(mCurrentColor);
                    }
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mColorPicker.getRecentColorInfo()
                .setCurrentColor(mColorPicker.getRecentColorInfo().getSelectedColor());
        outState.putIntArray(KEY_RECENTLY_USED_COLORS, mRecentlyUsedColors);
        outState.putSerializable(KEY_CURRENT_COLOR, mCurrentColor);
        outState.putBoolean(KEY_OPACITY_BAR_ENABLED, mIsTransparencyControlEnabled);
        outState.putBoolean(KEY_SHOW_EYE_DROPPER, mIsEyeDropperDisable);
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    @NonNull
    public SeslColorPicker getColorPicker() {
        return mColorPicker;
    }

    public void setNewColor(@Nullable Integer newColor) {
        mNewColor = newColor;
    }

    public void setTransparencyControlEnabled(boolean enabled) {
        mIsTransparencyControlEnabled = enabled;
    }

    public void setOnlySpectrumMode() {
        mIsOnlySpectrumMode = true;
    }

    private class ColorPickerDialog extends AlertDialog {
        ColorPickerDialog(Context context) {
            super(context,
                    SeslMisc.isLightTheme(context) ?
                            androidx.appcompat.R.style.ThemeOverlay_AppCompat_Light_Dialog :
                            androidx.appcompat.R.style.ThemeOverlay_AppCompat_Dialog);
        }
    }

    //Sesl7
    public void disableEyeDropper(boolean disable) {
        mIsEyeDropperDisable = disable;
    }

}
