/*
 * Copyright 2026 The Android Open Source Project
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

package androidx.glance.oneui.common

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import android.widget.RemoteViews
import androidx.glance.oneui.common.appwidgetsize.HostKey
import java.lang.reflect.Method

private const val TAG = "AppWidgetManager"
private const val APPWIDGET_PREVIEW_REMOTEVIEWS_KEY = "previewRemoteViews"
private const val APPWIDGET_PREVIEW_STATES_KEY = "previewStates"

fun AppWidgetManager.getAppWidgetSizeInfo(appWidgetId: Int): List<Bundle> {
    return try {
        val method = javaClass.getDeclaredMethod(AppWidgetManagerReflections.GET_APPWIDGET_SIZE_INFO, Int::class.javaPrimitiveType)
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        method.invoke(this, appWidgetId) as List<Bundle>
    } catch (e: Exception) {
        SeslAppWidgetLog.e(TAG, "getAppWidgetSizeInfo $e")
        emptyList()
    }
}

fun AppWidgetManager.getAppWidgetSizeInfos(): List<Bundle> {
    return try {
        val method = javaClass.getDeclaredMethod(AppWidgetManagerReflections.GET_APPWIDGET_SIZE_INFOS)
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        method.invoke(this) as List<Bundle>
    } catch (e: Exception) {
        SeslAppWidgetLog.e(TAG, "getAppWidgetSizeInfos $e")
        emptyList()
    }
}

fun AppWidgetManager.getBindAppWidgetSize(appWidgetId: Int, defaultSize: AppWidgetSize): AppWidgetSize {
    val options = getAppWidgetOptions(appWidgetId)
    val explicitSize = options.explicitWidgetSize()
    SeslAppWidgetLog.d(TAG, "(id=$appWidgetId) mode=$explicitSize from options")
    if (explicitSize != AppWidgetSize.Unknown) return explicitSize
    
    SeslAppWidgetLog.d(TAG, "(id=$appWidgetId) default=$defaultSize")
    return if (defaultSize == AppWidgetSize.Unknown) AppWidgetSize.Medium else defaultSize
}

fun AppWidgetManager.getInstalledProvidersForPackage(categoryFilter: Int, packageName: String?): List<AppWidgetProviderInfo> {
    return try {
        val method = javaClass.getDeclaredMethod(AppWidgetManagerReflections.GET_INSTALLED_PROVIDERS_FOR_PACKAGE_METHOD, Int::class.javaPrimitiveType, String::class.java)
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        method.invoke(this, categoryFilter, packageName) as List<AppWidgetProviderInfo>
    } catch (e: Exception) {
        SeslAppWidgetLog.e(TAG, "getInstalledProvidersForPackage $e")
        emptyList()
    }
}

fun AppWidgetManager.installedAllProviders(packageName: String? = null): List<AppWidgetProviderInfo> {
    SeslAppWidgetLog.d(TAG, "installedAllProviders $packageName")
    var providers = getInstalledProvidersForPackage(8705, packageName)
    if (providers.isEmpty()) {
        SeslAppWidgetLog.d(TAG, "no getInstalledProvidersForPackage method")
        try {
            val method = javaClass.getDeclaredMethod("getInstalledProviders", Int::class.javaPrimitiveType)
            method.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            providers = method.invoke(this, 8705) as List<AppWidgetProviderInfo>
        } catch (e: Exception) {
            SeslAppWidgetLog.e(TAG, "ex=$e")
        }
    }
    return providers
}

fun AppWidgetManager.getPreview(provider: ComponentName, category: Int): RemoteViews? {
    return try {
        val method = javaClass.getDeclaredMethod(AppWidgetManagerReflections.GET_WIDGET_PREVIEW_METHOD, ComponentName::class.java, Int::class.javaPrimitiveType)
        method.isAccessible = true
        method.invoke(this, provider, category) as? RemoteViews
    } catch (e: Exception) {
        null
    }
}

fun AppWidgetManager.setPreview(provider: ComponentName, category: Int, preview: RemoteViews): Any? {
    return try {
        val method = javaClass.getDeclaredMethod(AppWidgetManagerReflections.SET_WIDGET_PREVIEW_METHOD, ComponentName::class.java, Int::class.javaPrimitiveType, RemoteViews::class.java)
        method.isAccessible = true
        method.invoke(this, provider, category, preview)
    } catch (e: Exception) {
        null
    }
}

fun AppWidgetManager.removePreview(provider: ComponentName, category: Int, preview: RemoteViews): Any? {
    return try {
        val method = javaClass.getDeclaredMethod(AppWidgetManagerReflections.REMOVE_WIDGET_PREVIEW_METHOD, ComponentName::class.java, Int::class.javaPrimitiveType, RemoteViews::class.java)
        method.isAccessible = true
        method.invoke(this, provider, category, preview)
    } catch (e: Exception) {
        null
    }
}

fun AppWidgetManager.isSupportPreviewForHost(): Boolean {
    return try {
        javaClass.getDeclaredMethod(AppWidgetManagerReflections.GET_WIDGET_PREVIEW_FOR_HOST_METHOD, ComponentName::class.java, Bundle::class.java, Int::class.javaPrimitiveType)
        true
    } catch (e: Exception) {
        false
    }
}

fun AppWidgetManager.isPreviewForHostUpdateAvailable(provider: ComponentName): Boolean {
    return try {
        val method = javaClass.getDeclaredMethod(AppWidgetManagerReflections.IS_WIDGET_PREVIEW_FOR_HOST_UPDATE_AVAILABLE, ComponentName::class.java)
        method.isAccessible = true
        method.invoke(this, provider) as Boolean
    } catch (e: Exception) {
        true
    }
}

fun AppWidgetManager.getWidgetPreviewForHost(context: Context, providerInfo: AppWidgetProviderInfo, hostType: AppWidgetHostType, style: AppWidgetStyle): RemoteViews? {
    val hostKey = HostKey.makeHostKey(hostType.toInt(), DisplayDeviceType.from(context).toInt(), style.toInt())
    val preview = try {
        val method = javaClass.getDeclaredMethod(AppWidgetManagerReflections.GET_WIDGET_PREVIEW_FOR_HOST_METHOD, ComponentName::class.java, Bundle::class.java, Int::class.javaPrimitiveType)
        method.isAccessible = true
        method.invoke(this, providerInfo.provider, null, hostKey) as? RemoteViews
    } catch (e: Exception) {
        null
    }
    SeslAppWidgetLog.d(TAG, "Get $hostType-$style-${providerInfo.provider} preview : $preview")
    return preview
}

fun AppWidgetManager.setWidgetPreviewForHost(provider: ComponentName, previews: SparseArray<RemoteViews>): Any? {
    return try {
        val method = javaClass.getDeclaredMethod(AppWidgetManagerReflections.SET_WIDGET_PREVIEW_FOR_HOST_METHOD, ComponentName::class.java, SparseArray::class.java)
        method.isAccessible = true
        method.invoke(this, provider, previews)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun AppWidgetManager.removeWidgetPreviewForHost(provider: ComponentName, hostKey: Int): Any? {
    return try {
        val method = javaClass.getDeclaredMethod(AppWidgetManagerReflections.REMOVE_WIDGET_PREVIEW_FOR_HOST_METHOD, ComponentName::class.java, Int::class.javaPrimitiveType)
        method.isAccessible = true
        method.invoke(this, provider, hostKey)
    } catch (e: Exception) {
        null
    }
}

fun AppWidgetManager.isSupportTemplatePreview(): Boolean? {
    return try {
        javaClass.getDeclaredMethod(AppWidgetManagerReflections.GET_TEMPLATE_WIDGET_PREVIEW_METHOD, ComponentName::class.java, Bundle::class.java, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        true
    } catch (e: Exception) {
        null
    }
}

fun AppWidgetManager.getTemplatePreview(providerInfo: AppWidgetProviderInfo, templateSize: AppWidgetSize, templateStyle: AppWidgetStyle): List<Pair<AppWidgetSize, RemoteViews>> {
    return try {
        val bundle = getTemplateCachedBundle(providerInfo, templateSize, templateStyle)
        val data = bundle.convertToPreviewData()
        SeslAppWidgetLog.d(TAG, "converted data : ${data.size} / $data")
        val result = mutableListOf<Pair<AppWidgetSize, RemoteViews>>()
        listOf(AppWidgetStyle.Colorful, AppWidgetStyle.Monotone).forEach { style ->
            if (style in templateStyle) {
                result.addAll(data.filter { it.first == style }.map { Pair(it.second, it.third) })
            }
        }
        SeslAppWidgetLog.d(TAG, "filtered preview : ${result.size} / $result")
        result
    } catch (e: Throwable) {
        e.printStackTrace()
        emptyList()
    }
}

private fun AppWidgetManager.getTemplateCachedBundle(providerInfo: AppWidgetProviderInfo, templateSize: AppWidgetSize, templateStyle: AppWidgetStyle): Bundle {
    return try {
        val method = javaClass.getDeclaredMethod(AppWidgetManagerReflections.GET_TEMPLATE_WIDGET_PREVIEW_METHOD, ComponentName::class.java, Bundle::class.java, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        method.isAccessible = true
        method.invoke(this, providerInfo.provider, null, templateSize.toInt(), templateStyle.toInt()) as? Bundle ?: Bundle.EMPTY
    } catch (e: Exception) {
        Bundle.EMPTY
    }
}

fun AppWidgetManager.setTemplatePreview(provider: ComponentName, preview: List<Triple<AppWidgetSize, AppWidgetStyle, RemoteViews>>) {
    val sortedPreviews = sortPreview(preview)
    var sizeMask = 0
    var styleMask = 0
    preview.forEach {
        sizeMask = sizeMask or it.first.toInt()
        styleMask = styleMask or it.second.toInt()
    }
    SeslAppWidgetLog.d(TAG, "setTemplatePreview / $styleMask, $sizeMask, ${sortedPreviews.size}")
    setTemplatePreview(provider, AppWidgetSize(sizeMask), AppWidgetStyle(styleMask), sortedPreviews)
}

private fun AppWidgetManager.setTemplatePreview(provider: ComponentName, size: AppWidgetSize, style: AppWidgetStyle, previews: Array<RemoteViews>): Any? {
    return try {
        val method = javaClass.getDeclaredMethod(AppWidgetManagerReflections.SET_TEMPLATE_WIDGET_PREVIEW_METHOD, ComponentName::class.java, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Array<RemoteViews>::class.java)
        method.isAccessible = true
        method.invoke(this, provider, size.toInt(), style.toInt(), previews)
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }
}

fun AppWidgetManager.removeTemplatePreview(provider: ComponentName, templateSize: AppWidgetSize, templateStyle: AppWidgetStyle): Any? {
    return try {
        val method = javaClass.getDeclaredMethod(AppWidgetManagerReflections.REMOVE_TEMPLATE_WIDGET_PREVIEW_METHOD, ComponentName::class.java, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        method.isAccessible = true
        method.invoke(this, provider, templateSize.toInt(), templateStyle.toInt())
    } catch (e: Exception) {
        null
    }
}

fun AppWidgetManager.setAppWidgetSizeInfo(packageName: String, appWidgetId: Int, appWidgetSizeInfos: List<Bundle>) {
    try {
        val method = javaClass.getDeclaredMethod(AppWidgetManagerReflections.SET_APPWIDGET_SIZE_INFO, String::class.java, Int::class.javaPrimitiveType, List::class.java)
        method.isAccessible = true
        method.invoke(this, packageName, appWidgetId, appWidgetSizeInfos)
        SeslAppWidgetLog.i(TAG, "setAppWidgetSizeInfo - invoke")
    } catch (e: Exception) {
        SeslAppWidgetLog.e(TAG, "setAppWidgetSizeInfo $e")
    }
}

fun sortPreview(preview: List<Triple<AppWidgetSize, AppWidgetStyle, RemoteViews>>): Array<RemoteViews> {
    return preview.sortedBy { it.toState() }.map { it.third }.toTypedArray()
}

private fun Triple<AppWidgetSize, AppWidgetStyle, RemoteViews>.toState(): Int {
    return first.toInt() or (second.toInt() shl AppWidgetSize.All.toArrayList().size)
}

private fun Bundle.convertToPreviewData(): List<Triple<AppWidgetStyle, AppWidgetSize, RemoteViews>> {
    val states = getIntArray(APPWIDGET_PREVIEW_STATES_KEY) ?: return emptyList()
    val remoteViews = if (Build.VERSION.SDK_INT >= 33) {
        getParcelableArray(APPWIDGET_PREVIEW_REMOTEVIEWS_KEY, RemoteViews::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableArray(APPWIDGET_PREVIEW_REMOTEVIEWS_KEY) as? Array<RemoteViews>
    } ?: return emptyList()

    val result = mutableListOf<Triple<AppWidgetStyle, AppWidgetSize, RemoteViews>>()
    val sizeListSize = AppWidgetSize.All.toArrayList().size
    states.forEachIndexed { index, state ->
        if (index < remoteViews.size) {
            result.add(Triple(AppWidgetStyle(state shr sizeListSize), AppWidgetSize(state and ((1 shl sizeListSize) - 1)), remoteViews[index] as RemoteViews))
        }
    }
    return result
}
