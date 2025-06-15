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
import androidx.picker.model.AppInfoData
import androidx.picker.model.appdata.CategoryAppData
import androidx.picker.model.appdata.GroupAppData
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.CategoryViewData
import androidx.picker.model.viewdata.GroupTitleViewData
import androidx.picker.model.viewdata.ViewData

/**
 * This function takes a list of different types of [AppData] objects and
 * transforms it into a list of concrete [ViewData] objects.
 *
 * It processes [GroupAppData] items first. If there are both [GroupAppData] items and
 * other types of [AppData] (non-group data), the non-group data will be wrapped in a
 * synthetic group with an empty title. Otherwise, if only non-group data is present,
 * it will be processed directly by [createViewDatas].
 *
 * @property createAppInfoViewDatas A function lambda to convert a list of [AppInfoData] to a list of [AppInfoViewData].
 * @property createGroupTitleViewData A function lambda to convert [GroupAppData] to [GroupTitleViewData].
 * @property createCategoryViewData A function lambda to convert [CategoryAppData] with the corresponding list of [AppInfoViewData]
 * to [CategoryViewData].
 */
class ParseAppDataTask(
    private val createAppInfoViewDatas: (appInfoDataList: List<AppInfoData>) -> List<AppInfoViewData>,
    private val createGroupTitleViewData: (groupAppData: GroupAppData) -> GroupTitleViewData,
    private val createCategoryViewData: (catAppData: CategoryAppData, appInfoViewDataList: List<AppInfoViewData>) -> CategoryViewData
) : Task<List<AppData>, List<ViewData>> {

    companion object {
        private const val EMPTY_STRING = ""

        /**
         * Provides a factory function to create [ParseAppDataTask] instances.
         * This allows for dependency injection of the creation logic for
         * [GroupTitleViewData] and [CategoryViewData].
         *
         * @param createAppInfoViewDatas A function lambda to convert a list of [AppInfoData] to a list of [AppInfoViewData].
         * @param createGroupTitleViewData A function lambda to convert [GroupAppData] to [GroupTitleViewData].
         * @param createCategoryViewData A function lambda to convert [CategoryAppData] with the corresponding list of [AppInfoViewData]
         */
        fun provide(
            createAppInfoViewDatas: (appInfoDataList: List<AppInfoData>) -> List<AppInfoViewData>,
            createGroupTitleViewData: (groupAppData: GroupAppData) -> GroupTitleViewData,
            createCategoryViewData: (catAppData: CategoryAppData, appInfoViewDataList: List<AppInfoViewData>) -> CategoryViewData,
        ): ParseAppDataTask {
            return ParseAppDataTask(createAppInfoViewDatas, createGroupTitleViewData, createCategoryViewData)
        }
    }

    private fun createCategory(categoryAppData: CategoryAppData): List<ViewData> {
        val appInfoViewDataList = createAppInfoViewDatas(categoryAppData.appInfoDataList)
        return listOf(createCategoryViewData(categoryAppData, appInfoViewDataList)) + appInfoViewDataList
    }

    private fun createGroupHeader(groupAppData: GroupAppData): List<ViewData> {
        val result = mutableListOf<ViewData>()
        result.add(createGroupTitleViewData(groupAppData))
        result.addAll(createViewDatas(groupAppData.appDataList))
        return result
    }

    private fun createViewDatas(appData: List<AppData>): List<ViewData> {
        val result = mutableListOf<ViewData>()
        val categories = appData.filterIsInstance<CategoryAppData>()
        val appInfos = appData.filterIsInstance<AppInfoData>()

        for (category in categories) {
            result.addAll(createCategory(category))
        }

        if (categories.isNotEmpty() && appInfos.isNotEmpty()) {
            // Wrap remaining AppInfoData in a new CategoryAppData
            val syntheticCategory = AppData.CategoryAppDataBuilder(EMPTY_STRING)
                .setAppDatas(appInfos)
                .build()
            result.addAll(createCategory(syntheticCategory))
        } else {
            result.addAll(createAppInfoViewDatas(appInfos))
        }
        return result
    }

    override fun invoke(input: List<AppData>): List<ViewData> {
        val result = mutableListOf<ViewData>()
        val groupAppDataList = input.filterIsInstance<GroupAppData>()
        val nonGroupAppData = input - groupAppDataList.toSet()

        for (group in groupAppDataList) {
            result.addAll(createGroupHeader(group))
        }

        if (groupAppDataList.isNotEmpty() && nonGroupAppData.isNotEmpty()) {
            // Wrap remaining non-group data in a new GroupAppData
            val syntheticGroup = AppData.GroupAppDataBuilder(EMPTY_STRING)
                .setAppDatas(nonGroupAppData)
                .build()
            result.addAll(createGroupHeader(syntheticGroup))
        } else {
            result.addAll(createViewDatas(nonGroupAppData))
        }
        return result
    }
}