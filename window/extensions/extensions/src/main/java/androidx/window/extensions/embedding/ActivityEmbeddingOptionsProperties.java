/*
 * Copyright 2023 The Android Open Source Project
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

package androidx.window.extensions.embedding;

import androidx.window.extensions.RequiresVendorApiLevel;

/**
 * A class that contains activity embedding properties that puts to or retrieves from
 * {@link android.app.ActivityOptions}.
 */
public class ActivityEmbeddingOptionsProperties {

    private ActivityEmbeddingOptionsProperties() {}

    /**
     * The key of the unique identifier that put into {@link android.app.ActivityOptions}.
     * <p>
     * Type: {@link android.os.Bundle#putString(String, String) String}
     * <p>
     * An {@code OverlayCreateParams} property that represents the unique identifier of the overlay
     * container.
     */
    @RequiresVendorApiLevel(level = 5)
    public static final String KEY_OVERLAY_TAG =
            "androidx.window.extensions.embedding.OverlayTag";
}
