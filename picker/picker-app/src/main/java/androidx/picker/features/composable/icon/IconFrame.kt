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

package androidx.picker.features.composable.icon

import androidx.annotation.LayoutRes
import androidx.picker.R
import androidx.picker.features.composable.ComposableFrame
import androidx.picker.features.composable.ComposableViewHolder
import androidx.picker.features.composable.icon.ComposableIconViewHolder
import kotlin.jvm.java

/**
 * Represents the available frames for displaying icons within the picker.
 *
 * Each enum constant defines a specific layout and associated ViewHolder
 * for rendering an icon.
 *
 * @property layoutResId The layout resource ID for this frame.
 * @property viewHolderClass The class of the ViewHolder responsible for binding data to this frame.
 */
enum class IconFrame(
    @LayoutRes override val layoutResId: Int,
    override val viewHolderClass: Class<out ComposableViewHolder>
) : ComposableFrame {
    Icon(
        R.layout.picker_app_composable_frame_icon,
        ComposableIconViewHolder::class.java
    )
}