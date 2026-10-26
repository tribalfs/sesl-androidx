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

package androidx.core.widget;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.util.Log;

import androidx.annotation.RestrictTo;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

//sesl9
/**
 * Creates {@link SeslGoToTopController} instances for the requested widget type.
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public abstract class SeslGoToTopControllerFactory {

    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public enum ControllerType {
        RECYCLERVIEW,
        NESTEDSCROLLVIEW
    }

    @Nullable
    public static SeslGoToTopController createController(@NonNull ControllerType type,
            @Nullable SeslGoToTopConfig config,
            SeslGoToTopController.@Nullable Host host, @NonNull String tag) {
        try {
            switch (type.ordinal()) {
                case 0: // RECYCLERVIEW
                    if (host == null) {
                        throw new IllegalStateException("host required");
                    }
                    if (config == null) {
                        throw new IllegalStateException("config required");
                    }
                    return new SeslGoToTopController(host, config);

                case 1: // NESTEDSCROLLVIEW
                    if (host == null) {
                        throw new IllegalStateException("host required");
                    }
                    if (config == null) {
                        throw new IllegalStateException("config required");
                    }

                    SeslNestedGoToTopController controller =
                            new SeslNestedGoToTopController(host, config);
                    controller.setSupportGoToTop(false);
                    return controller;

                default:
                    Log.e(tag, "Unknown controller type: " + type);
                    return null;
            }
        } catch (Throwable t) {
            Log.e(tag, "Failed to initialize GoToTopController", t);
            return null;
        }
    }
}
