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

package androidx.picker.helper

import android.graphics.drawable.Drawable
import androidx.picker.model.AppInfo

/**
 * A helper interface that provides utility methods to interact with the PackageManager.
 * This can be implemented by different classes to provide platform-specific implementations.
 */
interface PackageManagerHelper {
    fun getActivityIcon(
        packageName: String,
        activityName: String,
        userId: Int
    ): Drawable?

    fun getAppLabel(
        appInfo: AppInfo
    ): String

    fun getApplicationIcon(
        packageName: String,
        userId: Int
    ): Drawable?

    fun getEmptyIcon(): Drawable

    fun resizeDrawable(
        drawable: Drawable
    ): Drawable

    fun semGetActivityIconForIconTray(
        packageName: String,
        activityName: String,
        mode: Int,
        userId: Int
    ): Drawable?

    fun semGetApplicationIconForIconTray(
        packageName: String,
        mode: Int,
        userId: Int
    ): Drawable?
}