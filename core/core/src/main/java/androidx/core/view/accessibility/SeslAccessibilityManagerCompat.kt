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

package androidx.core.view.accessibility

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.annotation.RestrictTo
import androidx.reflect.view.accessibility.SeslAccessibilityManagerReflector

//sesl9
/**
 * Compatibility helper for querying accessibility states on Samsung devices.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
object SeslAccessibilityManagerCompat {

    /**
     * Checks whether a screen reader service (Voice Assistant / TalkBack) is active.
     *
     * @param context the [Context] to retrieve accessibility services from
     * @return `true` if a screen reader service is currently enabled, `false` otherwise
     */
    fun isScreenReaderEnabled(context: Context): Boolean =
        SeslAccessibilityManagerReflector.isScreenReaderEnabled(getAccessibilityManager(context), false)

    private fun getAccessibilityManager(context: Context): AccessibilityManager =
        context.getSystemService("accessibility") as AccessibilityManager
}
