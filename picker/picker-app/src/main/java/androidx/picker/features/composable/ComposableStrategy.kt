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

import androidx.annotation.Keep
import androidx.picker.model.viewdata.ViewData

/**
 * A strategy to pick a [ComposableType] for composing a [ViewHolder][ComposableViewHolder] for a given [ViewData].
 *
 * This interface defines how to select the appropriate [ComposableType] based on the provided
 * [ViewData]. It utilizes lists of [ComposableFrame] objects, where each frame represents a
 * [ComposableType] along with its associated constraints.
 *
 * The selection process iterates through these lists in a specific order. When a [ViewData]
 * instance satisfies all the constraints defined within a [ComposableFrame], the corresponding
 * [ComposableType] from that frame is chosen.
 *
 * If no [ComposableFrame] in any of the lists matches the given [ViewData], the
 * [selectComposableType] method will return null, indicating that no suitable [ComposableType]
 * could be determined.
 */
@Keep
interface ComposableStrategy {
    /**
     * A list of [ComposableFrame] objects used to determine the [ComposableType] for the icon
     * section.
     *
     * This list contains frames that define different [ComposableType] options for displaying
     * icons, along with the constraints that must be met for each option. The selection process
     * iterates through this list, and the first frame whose constraints are satisfied by the
     * [ViewData] will determine the [ComposableType] used for the icon.
     */
    val iconFrameList: List<ComposableFrame>
    /**
     * A list of [ComposableFrame] objects used to determine the [ComposableType] for the left
     * section.
     *
     * This list contains frames that define different [ComposableType] options for displaying
     * content in the left area, along with the constraints that must be met for each option. The
     * selection process iterates through this list, and the first frame whose constraints are
     * satisfied by the [ViewData] will determine the [ComposableType] used for the left section.
     */
    val leftFrameList: List<ComposableFrame>
    /**
     * A list of [ComposableFrame] objects used to determine the [ComposableType] for the title
     * section.
     *
     * This list contains frames that define different [ComposableType] options for displaying
     * titles, along with the constraints that must be met for each option. The selection process
     * iterates through this list, and the first frame whose constraints are satisfied by the
     * [ViewData] will determine the [ComposableType] used for the title.
     */
    val titleFrameList: List<ComposableFrame>
    /**
     * A list of [ComposableFrame] objects used to determine the [ComposableType] for the widget
     * section.
     *
     * This list contains frames that define different [ComposableType] options for displaying
     * widgets, along with the constraints that must be met for each option. The selection process
     * iterates through this list, and the first frame whose constraints are satisfied by the
     * [ViewData] will determine the [ComposableType] used for the widget.
     */
    val widgetFrameList: List<ComposableFrame>

    /**
     * Selects a [ComposableType] based on the provided [ViewData].
     *
     * This method iterates through the [iconFrameList], [leftFrameList], [titleFrameList], and
     * [widgetFrameList] in that order. For each list, it checks if the given [viewData] satisfies
     * all the constraints defined in any of its [ComposableFrame] objects.
     *
     * The first [ComposableFrame] whose constraints are met by the [viewData] will determine the
     * [ComposableType] to be returned.
     *
     * If no [ComposableFrame] in any of the lists matches the [viewData], this method returns
     * `null`.
     *
     * @param viewData The [ViewData] for which to select a [ComposableType].
     * @return The selected [ComposableType], or `null` if no suitable type is found.
     */
    fun selectComposableType(viewData: ViewData): ComposableType?
}