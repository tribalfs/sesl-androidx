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

package androidx.core.widget;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import androidx.annotation.RestrictTo;

import org.jspecify.annotations.NonNull;

//sesl9
/**
 * Abstract builder for creating instances of {@link SeslGoToTopController} subclasses.
 *
 * @param <T> controller type
 * @param <B> builder type
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public abstract class SeslGoToTopControllerBuilder<T extends SeslGoToTopController, B extends SeslGoToTopControllerBuilder<T, B>> {
    SeslGoToTopConfig config;
    SeslGoToTopController.Host host;

    /** Builds and returns a new {@link SeslGoToTopController} instance. */
    public abstract T build();

    /** Sets the {@link SeslGoToTopConfig} configuration for the controller. */
    @SuppressWarnings("unchecked")
    public B setConfig(@NonNull SeslGoToTopConfig config) {
        this.config = config;
        return (B) this;
    }

    /** Sets the {@link SeslGoToTopController.Host} for the controller. */
    @SuppressWarnings("unchecked")
    public B setHost(SeslGoToTopController.@NonNull Host host) {
        this.host = host;
        return (B) this;
    }

    /** Validates that required builder parameters are supplied. */
    public void validate() {
        if (this.host == null) {
            throw new IllegalStateException("host required");
        }
        if (this.config == null) {
            throw new IllegalStateException("config required");
        }
    }
}
