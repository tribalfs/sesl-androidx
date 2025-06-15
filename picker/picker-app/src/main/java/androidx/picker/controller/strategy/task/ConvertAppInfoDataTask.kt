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

package androidx.picker.controller.strategy.task

import androidx.picker.model.AppData
import androidx.picker.model.AppInfo
import androidx.picker.model.AppInfoData
import androidx.picker.model.viewdata.AppInfoViewData

/**
 * Converts a list of [AppInfoData] into a list of [AppInfoViewData] by either creating new
 * [AppInfoViewData] or updating existing ones using a cache to preserve existing object
 * identities keyed with the [AppInfo].
 *
 * This approach helps in efficiently updating UI elements by reusing existing view data objects
 * when possible.
 *
 * @property createAppInfoViewData A lambda function to convert [AppInfoData] to [AppInfoViewData].
 * This function is called when a new view data object needs to be created.
 */
class ConvertAppInfoDataTask(
    private val createAppInfoViewData: (AppInfoData) -> AppInfoViewData
) : Task<List<AppInfoData>, List<AppInfoViewData>> {

    private val cachedAppInfoViewDataMap: MutableMap<AppInfo, AppInfoViewData> = LinkedHashMap()

    /**
     * A lambda function that retrieves an existing [AppInfoViewData] from the cache or creates a
     * new one if it doesn't exist using the [AppInfoData] as input.
     */
    private val createOrGetAppInfoViewData: (AppInfoData) -> AppInfoViewData = { appInfoData ->
        val cached = cachedAppInfoViewDataMap[appInfoData.appInfo]
        val updated = cached?.update(appInfoData) ?: createAppInfoViewData(appInfoData)
        cachedAppInfoViewDataMap[appInfoData.appInfo] = updated
        updated
    }

    private fun clearUnusedCacheData(input: List<AppData>) {
        val validKeys = input.map { it.appInfo }.toSet()
        val unusedKeys = cachedAppInfoViewDataMap.keys.filter { it !in validKeys }
        for (key in unusedKeys) {
            cachedAppInfoViewDataMap.remove(key)
        }
    }

    override fun invoke(input: List<AppInfoData>): List<AppInfoViewData> {
        clearUnusedCacheData(input)
        return input.map(createOrGetAppInfoViewData)
    }
}