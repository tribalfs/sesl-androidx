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

package androidx.appsearch.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Serializes some {@link T} to and from a String.
 *
 * @param <T> The custom type that can be serialized to a String.
 */
public interface StringSerializer<T> {
    /**
     * Serializes a {@link T} to a String.
     */
    @NonNull
    String serialize(@NonNull T instance);

    /**
     * Deserializes a {@link T} from a String. Returns null if deserialization failed.
     */
    @Nullable
    T deserialize(@NonNull String string);
}
