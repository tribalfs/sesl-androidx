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

import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import android.graphics.Bitmap;

import java.lang.ref.WeakReference;


/**
 * A utility class to hold a Bitmap in a WeakReference.
 *
 * <p>This class is used to store a Bitmap that might be used across different components
 * or activities, allowing it to be garbage collected if no longer strongly referenced elsewhere.
 *
 * <p>The Bitmap is stored in a static WeakReference, meaning there's only one instance
 * of the Bitmap held by this class across the application.
 *
 * <p>This class is not meant to be instantiated. All methods are static.
 */
public class SeslBitmapHolder {
    private static WeakReference<Bitmap> sBitmapWeakReference;

    private SeslBitmapHolder() {
    }

    /**
     * Clears the Bitmap stored in the WeakReference.
     *
     * <p>This method sets the WeakReference to null, effectively releasing the Bitmap
     * if it's not strongly referenced elsewhere, allowing it to be garbage collected.
     */
    public static void clearBitmap() {
        sBitmapWeakReference = null;
    }

    /**
     * Retrieves the Bitmap stored in the WeakReference.
     *
     * <p>This method returns the Bitmap if it's still available (i.e., not garbage collected).
     * If the WeakReference is null or the Bitmap has been garbage collected, this method
     * returns null.
     *
     * @return The Bitmap if available, otherwise null.
     */
    public @Nullable static Bitmap getBitmap() {
        WeakReference<Bitmap> weakReference = sBitmapWeakReference;
        if (weakReference != null) {
            return weakReference.get();
        }
        return null;
    }

    /**
     * Sets the Bitmap to be held by this class in a WeakReference.
     *
     * <p>Any previously held Bitmap will be replaced. The Bitmap is stored in a static
     * WeakReference, so this method will affect all parts of the application that use
     * this class to access the Bitmap.
     *
     * @param bitmap The Bitmap to be stored. Must not be null.
     */
    public static void setBitmapWeakReference(@NonNull Bitmap bitmap) {
        sBitmapWeakReference = new WeakReference<>(bitmap);
    }
}