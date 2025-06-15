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

package androidx.picker.di

import android.content.Context
import androidx.picker.features.scs.AbstractAppDataListFactory
import androidx.picker.helper.PackageManagerHelper
import androidx.picker.helper.PackageManagerHelperImpl
import androidx.picker.loader.DataLoader
import androidx.picker.loader.select.SelectStateLoader
import androidx.picker.repository.AppDataRepository
import androidx.picker.repository.ViewDataRepository

/**
 * A dependency injection container that provides instances of various components
 * required by the app picker.
 *
 * This class is designed to be easily extendable for testing or customization purposes.
 * It uses lazy initialization for some of its properties to optimize performance.
 *
 * @param context The application context, used for accessing system services like
 *                PackageManager.
 */
open class AppPickerContext(context: Context) {

    /**
     * Provides an instance of [PackageManagerHelper] for interacting with the Android
     * PackageManager. This is used to retrieve information about installed applications.
     */
    val packageManagerHelper: PackageManagerHelper = PackageManagerHelperImpl(context)

    /**
     * Provides an instance of [AbstractAppDataListFactory] which is responsible for creating
     * lists of application data. The specific implementation of the factory is determined
     * by calling [AbstractAppDataListFactory.getFactory].
     */
    val appDataListFactory = AbstractAppDataListFactory.getFactory(context)

    /**
     * Provides a [DataLoader] instance, responsible for loading and processing application
     * data. It utilizes [appDataListFactory] to create lists of app data and
     * [packageManagerHelper] for package-related operations. This property is lazily
     * initialized.
     */
    val dataLoader: DataLoader by lazy {
        DataLoader(appDataListFactory, packageManagerHelper)
    }

    /**
     * Provides a [SelectStateLoader] instance, responsible for managing the selection state
     * of items in the picker. This property is lazily initialized.
     */
    val selectStateLoader: SelectStateLoader by lazy {
        SelectStateLoader()
    }

    /**
     * Provides an [AppDataRepository] instance, responsible for providing access
     * to application data. It utilizes [appDataListFactory] to obtain the underlying data.
     * This property is lazily initialized.
     */
    val appDataRepository: AppDataRepository by lazy {
        AppDataRepository(appDataListFactory)
    }

    /**
     * Provides a [ViewDataRepository] instance, responsible for managing and providing access
     * to view-specific data. It utilizes [dataLoader] to load application data and
     * [selectStateLoader] to manage selection states. This property is lazily initialized.
     */
    val viewDataRepository: ViewDataRepository by lazy {
        ViewDataRepository(dataLoader, selectStateLoader)
    }
}