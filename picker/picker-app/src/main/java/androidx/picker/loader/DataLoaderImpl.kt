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

package androidx.picker.loader

import android.graphics.drawable.Drawable
import androidx.picker.features.scs.AbstractAppDataListFactory
import androidx.picker.helper.PackageManagerHelper
import androidx.picker.model.AppInfo
import kotlin.collections.getOrPut
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of [DataLoader] that uses a [AbstractAppDataListFactory] to load initial data
 * and [PackageManagerHelper] to load data on demand.
 *
 * It maintains a lazy-loaded map for labels and uses a [CachedLoader] for icons to optimize
 * performance by caching previously loaded data.
 *
 * @property factory The factory used to get the initial label map.
 * @property packageManagerHelper The helper class used to load application labels and icons from
 * the package manager.
 */
class DataLoaderImpl(
    private val factory: AbstractAppDataListFactory,
    private val packageManagerHelper: PackageManagerHelper
) : DataLoader {

    private val labelMap: MutableMap<AppInfo, String> by lazy(LazyThreadSafetyMode.NONE) {
        factory.getLabelMap().toMutableMap()
    }

    private val iconLoader by lazy(LazyThreadSafetyMode.NONE) {
        object: CachedLoader<AppInfo, Drawable>() {
            override fun createValue(key: AppInfo): Drawable = createAppIcon(key)
        }
    }

    /**
     * Creates an application icon [Drawable] for the given [AppInfo].
     *
     * This function attempts to retrieve the icon in the following order:
     * 1. If `activityName` is not blank:
     *    a. Tries to get the activity icon using `semGetActivityIconForIconTray`.
     *    b. If (a) fails, tries to get the activity icon using `getActivityIcon`.
     * 2. If `activityName` is blank:
     *    a. Tries to get the application icon using `semGetApplicationIconForIconTray`.
     *    b. If (a) fails, tries to get the application icon using `getApplicationIcon`.
     * 3. If all above attempts fail, returns an empty icon using `getEmptyIcon`.
     *
     * Finally, the retrieved icon is resized using `resizeDrawable`.
     *
     * @param appInfo The [AppInfo] for which to create the icon.
     * @return The created application icon as a [Drawable].
     */
    fun createAppIcon(appInfo: AppInfo): Drawable {
        val activityName = appInfo.activityName
        val packageName = appInfo.packageName
        val user = appInfo.user

        val icon: Drawable = if (activityName.isNotBlank()) {
            packageManagerHelper.semGetActivityIconForIconTray(packageName, activityName, 1, user)
                ?: packageManagerHelper.getActivityIcon(packageName, activityName, user)
        } else {
            packageManagerHelper.semGetApplicationIconForIconTray(packageName, 1, user)
                ?: packageManagerHelper.getApplicationIcon(packageName, user)
        } ?: packageManagerHelper.getEmptyIcon()

        return packageManagerHelper.resizeDrawable(icon)
    }

    /**
     * Gets the label for the given [AppInfo] key.
     *
     * If the label is not found in the cached map, it fetches the label using
     * [PackageManagerHelper.getAppLabel] and stores it in the map for future use.
     *
     * @param key The [AppInfo] for which to retrieve the label.
     * @return The label string for the given [AppInfo].
     */
    override fun getLabel(key: AppInfo): String =
        labelMap.getOrPut(key) { packageManagerHelper.getAppLabel(key) }


    /**
     * Loads the icon for the given [AppInfo] as a [Flow] of [Drawable].
     *
     * This method utilizes a [CachedLoader] to efficiently load and cache icons.
     * If the icon is already cached, it's returned directly. Otherwise, it's loaded
     * using [createAppIcon], cached, and then returned.
     *
     * @param key The [AppInfo] for which to load the icon.
     * @return A [Flow] emitting the [Drawable] icon.
     */
    override fun loadIcon(key: AppInfo): Flow<Drawable> = iconLoader.load(key)

}