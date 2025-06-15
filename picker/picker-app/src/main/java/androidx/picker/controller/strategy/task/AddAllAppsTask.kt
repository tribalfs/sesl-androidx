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

import androidx.picker.model.viewdata.AllAppsViewData
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData

/**
 * This task prepends an [AllAppsViewData] instance to the provided input list.
 * The [AllAppsViewData] is created using the provided [createAllAppsViewData] function,
 * which takes a list of all [AppInfoViewData] instances found in the input list.
 *
 * @param createAllAppsViewData A function lambda to transform a list of [AppInfoViewData] and into
 *   an [AllAppsViewData].
 */
class AddAllAppsTask(
    private val createAllAppsViewData: (appInfoViewDataList: List<AppInfoViewData>) -> AllAppsViewData
) : Task<List<ViewData>, List<ViewData>> {

    /**
     * @param input The list of [ViewData] to process.
     * @return A new list with an [AllAppsViewData] added at the beginning,
     *   containing all [AppInfoViewData] from the input list.
     */
    override operator fun invoke(input: List<ViewData>): List<ViewData> {
        val mutableList = input.toMutableList()
        val appInfoList = mutableList.filterIsInstance<AppInfoViewData>()
        mutableList.add(0, createAllAppsViewData(appInfoList))
        return mutableList
    }

}