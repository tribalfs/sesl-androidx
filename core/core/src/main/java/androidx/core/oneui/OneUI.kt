/*
 * Copyright (C) 2026 The Android Open Source Project
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

package androidx.core.oneui

import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX
import androidx.reflect.os.SeslBuildReflector

//sesl9
/**
 * Provides the current One UI (SESL) platform version and helpers to gate features
 * by platform version. Enum constants are limited to those present in the
 * reconstructed build.
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
object OneUI {

    /**
     * The raw integer SDK version of the current device's Samsung Extension Platform (SEP/One UI).
     */
    @JvmField
    val currentSepSdkVersion: Int =
        SeslBuildReflector.SeslVersionReflector.getField_SEM_PLATFORM_INT()

    /**
     * Known One UI versions for version gating checks.
     */
    enum class Version(val displayName: String, val sepSdkVersion: Int) {
        UNKNOWN("Unknown", Integer.MIN_VALUE),
        ONEUI_8_0("8.0", 170000),
        ONEUI_8_5("8.5", 170500);

        companion object {
            /**
             * Returns the [Version] corresponding to the given raw [semPlatformInt],
             * or [UNKNOWN] if no matching version is defined.
             */
            @JvmStatic
            fun fromSemPlatformInt(semPlatformInt: Int): Version =
                entries.firstOrNull { it.sepSdkVersion == semPlatformInt } ?: UNKNOWN
        }
    }

    /**
     * Returns whether the device's One UI platform version is greater than or equal to [version].
     *
     * @param version the target [Version] to check against
     * @return `true` if current SEP SDK version is at least the target version, `false` otherwise
     */
    @JvmStatic
    fun isGreaterOrEqual(version: Version): Boolean =
        currentSepSdkVersion >= version.sepSdkVersion
}
