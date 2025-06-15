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

import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData


/**
 * Task to sort a list of {@link AppInfoViewData} objects.
 *
 * This task takes a list of {@link AppInfoViewData} as input and returns a new list containing
 * the same elements, sorted according to the provided {@link Comparator}.
 *
 * If no comparator is provided, the original order of the list is preserved.
 */
class SortAppInfoViewDataTask(
) : Task<List<AppInfoViewData>, List<AppInfoViewData>> {

    operator fun invoke(input: List<AppInfoViewData>, comparator: Comparator<ViewData>?): List<AppInfoViewData> =
        comparator?.let { input.toMutableList().apply { sortWith(it) } } ?: input

    override fun invoke(input: List<AppInfoViewData>): List<AppInfoViewData> =
        this(input, null)
}