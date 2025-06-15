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

package androidx.picker.features.composable

/**
 * Default implementation of the [ComposableType] interface.
 *
 * This data class holds nullable references to different [ComposableFrame] instances,
 * representing the optional frames within a composable item.
 *
 * @property leftFrame The [ComposableFrame] for displaying checkBox to the left.
 * @property iconFrame The [ComposableFrame] for displaying the app icon on the left.
 * @property titleFrame The [ComposableFrame] for the main title, subtitle and extra info.
 * @property widgetFrame The [ComposableFrame] for interactive elements or widgets on the right.
 */
data class ComposableTypeImpl(
    override val leftFrame: ComposableFrame? = null,
    override val iconFrame: ComposableFrame? = null,
    override val titleFrame: ComposableFrame? = null,
    override val widgetFrame: ComposableFrame? = null
) : ComposableType