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

package androidx.picker.features.scs

import android.content.Context
import android.os.Build
import androidx.annotation.RestrictTo
import androidx.picker.common.log.LogTag
import androidx.picker.model.AppData.Companion.TYPE_ITEM_TEXT
import androidx.picker.model.AppData.ItemType
import androidx.picker.model.AppInfo
import androidx.picker.model.AppInfoData

@RestrictTo(RestrictTo.Scope.LIBRARY)
abstract class AbstractAppDataListFactory : LogTag {

    companion object {
        val EMPTY_FACTORY: AbstractAppDataListFactory = object : AbstractAppDataListFactory() {
            override fun getDataList(itemType: Int): List<AppInfoData> = emptyList()
            override val logTag: String get() = ""
        }

        /**
         * @param context The application's context.
         * @return An instance of a concrete implementation of [AbstractAppDataListFactory]
         * based on the Android SDK version. For Android SDK versions 30 and above,
         * it returns an [AppDataListSCSFactory]. Otherwise, it returns an [AppDataListBixbyFactory].
         */
        @JvmStatic
        fun getFactory(context: Context): AbstractAppDataListFactory {
            return if (Build.VERSION.SDK_INT >= 30) {
                AppDataListSCSFactory(context)
            } else {
                AppDataListBixbyFactory(context)
            }
        }
    }

    open fun getDataList(): List<AppInfoData> = getDataList(TYPE_ITEM_TEXT)

    /**
     * Retrieves a list of [AppInfoData] objects based on the specified item type.
     *
     * This is an abstract function that must be implemented by concrete subclasses to provide
     * the actual data retrieval logic.
     *
     * @param itemType An integer representing the type of items to retrieve.
     *                 For example, [TYPE_ITEM_TEXT].
     * @return A list of [AppInfoData] objects corresponding to the given item type.
     *         Returns an empty list if no data is found or if the item type is not supported.
     */
    abstract fun getDataList(@ItemType itemType: Int): List<AppInfoData>

    /**
     * Retrieves a map of [AppInfo] to their corresponding labels.
     *
     * This function calls [getDataList] to get a list of [AppInfoData] objects.
     * It then iterates through this list, creating a map where each [AppInfo]
     * is a key and its associated label (converted to a String) is the value.
     *
     * @return A [Map] where keys are [AppInfo] objects and values are their String labels.
     *         Returns an empty map if [getDataList] returns an empty list.
     */
    fun getLabelMap(): Map<AppInfo, String> {
        val dataList = getDataList()
        val map = HashMap<AppInfo, String>()
        for (appInfoData in dataList) {
            map[appInfoData.appInfo] = appInfoData.label.toString()
        }
        return map
    }

    /**
     * A String used for logging, which is the simple name of this class.
     */
    override val logTag: String = this::class.java.simpleName
}