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

package androidx.glance.oneui.common.devicefeature

import androidx.reflect.feature.SeslFloatingFeatureReflector
import androidx.reflect.os.SeslSystemPropertiesReflector

object DeviceFeature {
    val isFlipModel: Boolean
        get() = SeslFloatingFeatureReflector.getBoolean("SEC_FLOATING_FEATURE_FRAMEWORK_SUPPORT_FOLDABLE_TYPE_FLIP")

    val isFoldModel: Boolean
        get() = SeslFloatingFeatureReflector.getBoolean("SEC_FLOATING_FEATURE_FRAMEWORK_SUPPORT_FOLDABLE_TYPE_FOLD")

    val isMultiFoldModel: Boolean
        get() = SeslFloatingFeatureReflector.getBoolean("SEC_FLOATING_FEATURE_FRAMEWORK_SUPPORT_MULTI_FOLD")

    val isTabletModel: Boolean
        get() = SeslSystemPropertiesReflector.getStringProperties("ro.build.characteristics")?.contains("tablet") == true
}
