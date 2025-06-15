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
 * Represents the structure of a composable item by defining the different frames it can contain.
 *
 * Each property corresponds to a specific area within the composable item:
 * @property leftFrame The [ComposableFrame] for the left area.
 * @property iconFrame The [ComposableFrame] for the icon area.
 * @property titleFrame The [ComposableFrame] for the title area.
 * @property widgetFrame The [ComposableFrame] for the widget area.
 */
interface ComposableType {
    val leftFrame: ComposableFrame?
    val iconFrame: ComposableFrame?
    val titleFrame: ComposableFrame?
    val widgetFrame: ComposableFrame?

    companion object {

        fun isSame(a: ComposableType, b: ComposableType): Boolean {
            if (a === b) return true
            return a.leftFrame == b.leftFrame &&
                   a.iconFrame == b.iconFrame &&
                   a.titleFrame == b.titleFrame &&
                   a.widgetFrame == b.widgetFrame
        }
    }
}

