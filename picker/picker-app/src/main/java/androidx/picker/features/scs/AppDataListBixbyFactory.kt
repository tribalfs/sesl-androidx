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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.UserHandle
import android.os.UserManager
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo
import androidx.picker.common.log.error
import androidx.picker.common.log.info
import androidx.picker.common.log.warn
import androidx.picker.features.search.InitialSearchUtils.AUTHORITY_BIXBY
import androidx.picker.features.search.InitialSearchUtils.AUTH_VERSION
import androidx.picker.model.AppData.ItemType
import androidx.picker.model.AppInfo
import androidx.picker.model.AppInfoData
import androidx.picker.model.AppInfoDataImpl
import androidx.reflect.DeviceInfo
import androidx.reflect.os.SeslUserHandleReflector
import androidx.core.net.toUri

/**
 * A factory class for creating lists of application data.
 *
 * This class extends [AbstractAppDataListFactory] and provides methods to retrieve application
 * information from both the Samsung Content Service (SCS) using the provider
 * using `com.samsung.android.scs.ai.search/v1` in querying the apps and from Package Manager.
 *
 * It prioritizes SCS for data retrieval and falls back to the Package Manager if SCS data is
 * unavailable.
 *
 * This class is intended for library use only, as indicated by the `@RestrictTo` annotation.
 *
 * @param context The application context, used for accessing system services like
 *   PackageManager and ContentResolver.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
open class AppDataListBixbyFactory(
    private val context: Context
) : AbstractAppDataListFactory() {

    companion object {
        private const val INVALID_IDX = -1
        private const val KEY_COMPONENT_NAME = "componentName"
        private const val KEY_LABEL = "label"
        private const val KEY_PACKAGE_NAME = "packageName"
        private const val SERVICE_USER = "user"
        private const val MAX_APP_LIST_COUNT = 10_000
    }

    override val logTag: String = "AppDataListBixbyFactory"

    private fun createAppInfoData(
        userId: Int,
        resolveInfo: ResolveInfo,
        @ItemType itemType: Int
    ): AppInfoData {
        val activityInfo: ActivityInfo = resolveInfo.activityInfo
        val appInfoDataImpl = AppInfoDataImpl(
            AppInfo(activityInfo.applicationInfo.packageName, activityInfo.name, userId),
            itemType
        )
        appInfoDataImpl.label = resolveInfo.loadLabel(context.packageManager).toString()
        return appInfoDataImpl
    }

    private fun getDataListFromPackageManager(@ItemType itemType: Int): List<AppInfoData> {
        info("getDataListFromPackageManager")

        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        if (DeviceInfo.isOneUI()) {
            try {
                val appInfoDataList = ArrayList<AppInfoData>()
                val userManager = context.getSystemService(SERVICE_USER) as UserManager
                val userProfiles = userManager.userProfiles as List<UserHandle>
                val packageManager = context.packageManager
                for (userHandle in userProfiles) {
                    val userId = UserHandle::class.java.getMethod("semGetIdentifier").invoke(userHandle) as Int
                    @Suppress("UNCHECKED_CAST")
                    val semQueryIntentActivitiesAsUser = PackageManager::class.java.getMethod(
                        "semQueryIntentActivitiesAsUser",
                        Intent::class.java,
                        Integer.TYPE,
                        Integer.TYPE
                    ).invoke(packageManager, intent, 0, userId) as List<ResolveInfo>

                    for (resolveInfo in semQueryIntentActivitiesAsUser) {
                        appInfoDataList.add(createAppInfoData(userId, resolveInfo, itemType))
                    }
                }
                return appInfoDataList
            } catch (_: Throwable) {
                warn("Failed to call semGetIdentifier and semQueryIntentActivitiesAsUser, " +
                    "fallback to PackageManager.queryIntentActivities AOSP api.")
            }
        }

        val appInfoDataList = ArrayList<AppInfoData>()
        val myUserId = SeslUserHandleReflector.myUserId()
        @SuppressLint("QueryPermissionsNeeded")
        val queryIntentActivities = context.packageManager.queryIntentActivities(intent, 0)
        for (resolveInfo in queryIntentActivities) {
            appInfoDataList.add(createAppInfoData(myUserId, resolveInfo, itemType))
        }
        return appInfoDataList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDataListFromSCS(@ItemType itemType: Int): List<AppInfoData> {
        info("getDataListFromSCS")
        val arrayList = ArrayList<AppInfoData>()
        val withAppendedPath =
            Uri.withAppendedPath("content://${getAuthority()}".toUri(), "application")
        val bundle = Bundle().apply {
            putString("android:query-arg-sql-selection", "*")
            putBoolean("query-arg-all-apps", true)
            putInt("android:query-arg-limit", MAX_APP_LIST_COUNT)
        }
        var query: Cursor? = null
        try {
            query = context.contentResolver.query(withAppendedPath, null, bundle, null)
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
        if (query == null) {
            query?.close()
            return arrayList
        }
        try {
            if (!query.moveToFirst()) {
                query.close()
                return arrayList
            }
            do {
                val columnIndexLabel = query.getColumnIndex(KEY_LABEL)
                val columnIndexComponentName = query.getColumnIndex(KEY_COMPONENT_NAME)
                val columnIndexPackageName = query.getColumnIndex(KEY_PACKAGE_NAME)
                val columnIndexUser = query.getColumnIndex(SERVICE_USER)
                if (columnIndexLabel == INVALID_IDX || columnIndexComponentName == INVALID_IDX || columnIndexPackageName == INVALID_IDX) {
                    error(
                        "Can't find columnIndex (" +
                        "$KEY_LABEL : $columnIndexLabel, " +
                        "$KEY_COMPONENT_NAME : $columnIndexComponentName, " +
                        "$KEY_PACKAGE_NAME : $columnIndexPackageName, " +
                        "$SERVICE_USER : $columnIndexUser)"
                    )
                } else {
                    val label = query.getString(columnIndexLabel)
                    val packageName = query.getString(columnIndexPackageName)
                    val componentName = query.getString(columnIndexComponentName)
                    val userId = query.getString(columnIndexUser)?.toIntOrNull() ?: 0
                    val appInfoDataImpl = AppInfoDataImpl(
                        AppInfo(packageName, componentName, userId),
                        itemType
                    )
                    appInfoDataImpl.label = label
                    arrayList.add(appInfoDataImpl)
                }
            } while (query.moveToNext())
        } finally {
            query.close()
        }
        return arrayList
    }

    open fun getAuthority(): String = "$AUTHORITY_BIXBY/$AUTH_VERSION"

    override fun getDataList(@ItemType itemType: Int): List<AppInfoData> {
        var dataListFromSCS = if (Build.VERSION.SDK_INT >= 26) {
            getDataListFromSCS(itemType)
        } else {
            emptyList()
        }
        if (dataListFromSCS.isEmpty()) {
            dataListFromSCS = getDataListFromPackageManager(itemType)
        }
        info("getDataList=${dataListFromSCS.size}")
        return dataListFromSCS
    }
}


