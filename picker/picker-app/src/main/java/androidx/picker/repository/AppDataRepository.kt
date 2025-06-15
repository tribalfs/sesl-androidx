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

package androidx.picker.repository

import androidx.picker.features.scs.AbstractAppDataListFactory
import androidx.picker.model.AppInfoData

/**
 * Repository class responsible for providing application data.
 *
 * This class serves as a central point for accessing application information.
 * It utilizes an [AbstractAppDataListFactory] to retrieve the actual list of app data.
 *
 * @param appDataListFactory An instance of [AbstractAppDataListFactory] that will be used
 *                           to generate the list of [AppInfoData].
 */
class AppDataRepository(
    private val appDataListFactory: AbstractAppDataListFactory
) {
    /**
     * Retrieves the default list of application information.
     *
     * This function delegates the call to the `getDataList()` method of the
     * provided [appDataListFactory] to obtain the list of [AppInfoData].
     *
     * @return A list of [AppInfoData] objects representing the default application data.
     */
    fun getDefaultList(): List<AppInfoData> {
        return appDataListFactory.getDataList()
    }
}