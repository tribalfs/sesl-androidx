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

package androidx.picker.adapter.viewholder

import kotlinx.coroutines.DisposableHandle

/**
 * Interface for an item where its animation can be `induced`.
 */
interface Inducible {
    /**
     * This method is part of the [Inducible] interface and is used to trigger an animation
     * on the item view.
     *
     * @return A [DisposableHandle] to control the animation's lifecycle.
     */
    fun induce(): DisposableHandle
}