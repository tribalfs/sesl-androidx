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
import kotlin.Lazy
import kotlin.LazyThreadSafetyMode
import kotlin.collections.Map
import kotlin.collections.mutableMapOf
import kotlin.jvm.functions.Function0
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jetbrains.annotations.NotNull

/**
 * Interface for loading app data such as labels and icons.
 *
 * This interface provides methods to retrieve labels and load icons for applications.
 * It abstracts the data loading process, allowing for different implementations
 * such as caching or network requests.
 */
interface DataLoader {
    fun getLabel(key: AppInfo): String
    fun loadIcon(key: AppInfo): Flow<Drawable>

    companion object {
        /**
         * Creates an instance of [DataLoader].
         *
         * This function serves as a factory method for creating [DataLoader] instances,
         * specifically returning an instance of [DataLoaderImpl]. It simplifies the creation
         * process by abstracting the underlying implementation details.
         *
         * @param factory The [AbstractAppDataListFactory] used to retrieve app data.
         * @param packageManagerHelper The [PackageManagerHelper] used for package manager
         * interactions.
         * @return An instance of [DataLoader].
         */
        operator fun invoke(
            factory: AbstractAppDataListFactory,
            packageManagerHelper: PackageManagerHelper
        ): DataLoader = DataLoaderImpl(factory, packageManagerHelper)
    }
}
