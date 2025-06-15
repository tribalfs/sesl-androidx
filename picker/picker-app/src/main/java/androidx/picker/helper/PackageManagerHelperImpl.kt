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

package androidx.picker.helper

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.annotation.RestrictTo
import androidx.appcompat.R
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import androidx.picker.common.log.LogTag
import androidx.picker.common.log.debug
import androidx.picker.common.log.info
import androidx.picker.model.AppInfo
import androidx.reflect.app.SeslApplicationPackageManagerReflector
import androidx.reflect.content.SeslContextReflector
import androidx.reflect.os.SeslUserHandleReflector

/**
 * Implementation of [PackageManagerHelper] that provides functionality for interacting with the
 * Android Package Manager.
 *
 * This class is responsible for retrieving application information, such as labels and icons,
 * and handling Samsung-specific features related to icon trays. It also includes caching
 * mechanisms for `PackageManager` instances to improve performance.
 *
 * @param context The application context, used to access system services and resources.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class PackageManagerHelperImpl(
    val context: Context
) : PackageManagerHelper, LogTag {

    companion object {
        const val UNKNOWN = "Unknown"
    }

    override val logTag: String = "PackageManagerHelperImpl"

    private val iconSize =
        context.resources.getDimensionPixelSize(androidx.picker.R.dimen.picker_app_grid_icon_size)
    private val pmList: HashMap<Int, PackageManager> = HashMap()

    private fun getLabelFromPackageManager(packageName: String, userId: Int): String {
        val pm = getPackageManager(packageName, userId)
        return try {
            val appInfo: ApplicationInfo = pm.getApplicationInfo(packageName, 0)
            val label = pm.getApplicationLabel(appInfo)
            label as? String ?: label.toString()
        } catch (_: PackageManager.NameNotFoundException) {
            info("can't find label for $packageName")
            UNKNOWN
        }
    }

    private fun getLabelFromPackageManager(
        packageName: String,
        activityName: String,
        userId: Int
    ): String {
        val componentName = ComponentName(packageName, activityName)
        val pm = getPackageManager(packageName, userId)
        return try {
            val activityInfo: ActivityInfo = pm.getActivityInfo(componentName, 0)
            activityInfo.loadLabel(pm).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            info("can't find label for $componentName")
            UNKNOWN
        }
    }

    private fun getPackageManager(packageName: String, userId: Int): PackageManager {
        val cached = pmList[userId]
        if (cached != null) return cached

        return try {
            val userHandle = SeslUserHandleReflector.of(userId)
            @SuppressLint("RestrictedApi")
            SeslContextReflector.createPackageContextAsUser(
                context,
                packageName,
                0,
                userHandle
            ).packageManager
        } catch (_: Throwable) {
            context.packageManager
        }.also { pmList[userId] = it }
    }

    override fun getActivityIcon(
        packageName: String,
        activityName: String,
        userId: Int
    ): Drawable? =
        try {
            getPackageManager(packageName, userId).getActivityIcon(
                ComponentName(packageName, activityName)
            )
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }

    override fun getAppLabel(appInfo: AppInfo): String =
        if (appInfo.activityName.isNotBlank()) {
            getLabelFromPackageManager(appInfo.packageName, appInfo.activityName, appInfo.user)
        } else {
            getLabelFromPackageManager(appInfo.packageName, appInfo.user)
        }.also { debug("getAppLabel key=${appInfo.packageName}, value=$it") }

    override fun getApplicationIcon(packageName: String, userId: Int): Drawable? =
        try {
            getPackageManager(packageName, userId).getApplicationIcon(packageName)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }

    override fun getEmptyIcon(): Drawable =
        ContextCompat.getDrawable(context, R.drawable.sesl_search_icon_background_borderless)
            ?: throw IllegalStateException("Empty icon resource not found")

    override fun resizeDrawable(drawable: Drawable): Drawable =
        try {
            drawable.toBitmap().scale(iconSize, iconSize).toDrawable(context.resources)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            drawable
        }

    override fun semGetActivityIconForIconTray(
        packageName: String,
        activityName: String,
        mode: Int,
        userId: Int
    ): Drawable? =
        SeslApplicationPackageManagerReflector.semGetActivityIconForIconTray(
            getPackageManager(packageName, userId),
            ComponentName(packageName, activityName),
            mode
        )


    override fun semGetApplicationIconForIconTray(
        packageName: String,
        mode: Int,
        userId: Int
    ): Drawable? =
        SeslApplicationPackageManagerReflector.semGetApplicationIconForIconTray(
            getPackageManager(packageName, userId),
            packageName,
            mode
        )
}
