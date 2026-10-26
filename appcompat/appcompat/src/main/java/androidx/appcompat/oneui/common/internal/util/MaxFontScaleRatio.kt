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

package androidx.appcompat.oneui.common.internal.util

import androidx.annotation.RestrictTo

//sesl9

/**
 * Maximum font scale ratios.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class MaxFontScaleRatio(val ratio: Float) {
    SMALL(1.0f),
    MEDIUM(1.15f),
    LARGE(1.3f),
    EXTRA_LARGE(1.5f),
    HUGE(1.7f),
    EXTRA_HUGE(2.0f),
}
