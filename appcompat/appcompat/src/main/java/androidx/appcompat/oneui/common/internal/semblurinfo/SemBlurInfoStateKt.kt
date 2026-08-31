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

package androidx.appcompat.oneui.common.internal.semblurinfo

import androidx.annotation.RestrictTo
import androidx.core.view.SemBlurCompat
import androidx.core.view.SemBlurCompat.CurveParameter

//sesl9
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
val FIGMA_BLUR_COMPONENT_LIGHT_ZERO = CurveParameter(0, 0.0f, 0.0f, 0.0f, 255.0f, 0.0f, 255.0f)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
val FIGMA_BLUR_COMPONENT_DARK_ZERO = CurveParameter(0, 0.0f, 0.0f, 0.0f, 255.0f, 0.0f, 255.0f)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
val FIGMA_BLUR_COMPONENT_LIGHT_SM = CurveParameter(200, 0.5f, 15.0f, 15.0f, 235.0f, 122.4f, 249.9f)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
val FIGMA_BLUR_COMPONENT_DARK_SM = CurveParameter(200, 0.45f, -15.0f, 0.0f, 235.0f, 22.3f, 160.0f)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
val FIGMA_BLUR_COMPONENT_LIGHT_MD = CurveParameter(240, 0.4f, 20.0f, 15.0f, 235.0f, 173.1f, 249.6f)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
val FIGMA_BLUR_COMPONENT_DARK_MD = CurveParameter(240, 0.4f, -15.0f, 0.0f, 235.0f, 31.2f, 112.8f)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
val FIGMA_BLUR_COMPONENT_LIGHT_LG = CurveParameter(270, 0.35f, 25.0f, 15.0f, 235.0f, 204.9f, 250.8f)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
val FIGMA_BLUR_COMPONENT_DARK_LG = CurveParameter(270, 0.35f, -15.0f, 0.0f, 235.0f, 35.8f, 91.9f)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
val FIGMA_BLUR_COMPONENT_LIGHT_XL = CurveParameter(300, 0.3f, 30.0f, 15.0f, 235.0f, 217.1f, 252.8f)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
val FIGMA_BLUR_COMPONENT_DARK_XL = CurveParameter(300, 0.3f, -15.0f, 0.0f, 235.0f, 39.5f, 75.2f)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
val FIGMA_BLUR_COMPONENT_LIGHT_XS = CurveParameter(300, 0.75f, 25.0f, 15.0f, 235.0f, 214.6f, 252.8f)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
val FIGMA_BLUR_COMPONENT_DARK_XS = CurveParameter(300, 0.7f, -15.0f, 0.0f, 235.0f, 36.7f, 87.7f)