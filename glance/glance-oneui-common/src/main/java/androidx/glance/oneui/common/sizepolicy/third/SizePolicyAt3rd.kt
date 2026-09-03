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

package androidx.glance.oneui.common.sizepolicy.third

import androidx.glance.oneui.common.sizepolicy.SizePolicy

abstract class SizePolicyAt3rd : SizePolicy() {
    object PortraitPolicy : SizePolicyAt3rd() {
        override val widthLevel0 = Float.MIN_VALUE
        override val widthLevel1 = 118.0f
        override val widthLevel2 = 268.0f
        override val widthLevel3 = Float.MAX_VALUE
        override val heightLevel0 = Float.MIN_VALUE
        override val heightLevel1 = 131.0f
        override val heightLevel2 = 354.0f
        override val heightLevel3 = 600.0f
        override val heightLevel4 = Float.MAX_VALUE
    }

    object LandscapePolicy : SizePolicyAt3rd() {
        override val widthLevel0 = Float.MIN_VALUE
        override val widthLevel1 = 109.0f
        override val widthLevel2 = 345.0f
        override val widthLevel3 = Float.MAX_VALUE
        override val heightLevel0 = Float.MIN_VALUE
        override val heightLevel1 = 120.0f
        override val heightLevel2 = 241.0f
        override val heightLevel3 = Float.MAX_VALUE
        override val heightLevel4 = heightLevel3
    }
}
