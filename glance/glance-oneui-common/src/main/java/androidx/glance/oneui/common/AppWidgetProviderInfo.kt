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
import android.content.Context
import android.content.res.XmlResourceParser
import android.os.Build
import android.util.Xml
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

internal object AppWidgetProviderInfoReflections {
    const val GENERATED_COLORFUL_PREVIEW_STATES = "hidden_semGeneratedColorfulPreviewStates"
    const val GENERATED_MONOTONE_PREVIEW_STATES = "hidden_semGeneratedMonotonePreviewStates"
    const val GENERATED_PREVIEW_PREVIEW_HOST_KEYS = "hidden_semGeneratedPreviewHostKeys"
}

fun AppWidgetProviderInfo.extractHostType(context: Context): AppWidgetHostType {
    if (Build.VERSION.SDK_INT < 31) {
        return AppWidgetHostType.Unknown
    }
    val metadata = getMetadata(context) ?: return AppWidgetHostType.All
    return try {
        metadata.moveToAttributeSet()
        val attributeValue = metadata.getAttributeValue(StyleableAttributes.TargetHost.attributeName)
        if (attributeValue != null) AppWidgetHostType.get(attributeValue) else AppWidgetHostType.All
    } finally {
        metadata.close()
    }
}

fun AppWidgetProviderInfo.extractPreviewSize(context: Context): AppWidgetSize {
    return filterPreviewSize(extractSize(context), extractPreviewSizeInner(context))
}

private fun AppWidgetProviderInfo.extractPreviewSizeInner(context: Context): AppWidgetSize {
    if (Build.VERSION.SDK_INT < 31) {
        return AppWidgetSize.Unknown
    }
    val metadata = getMetadata(context) ?: return AppWidgetSize.Unknown
    return try {
        metadata.moveToAttributeSet()
        val attributeValue = metadata.getAttributeValue(StyleableAttributes.PreviewSize.attributeName)
        if (attributeValue != null) AppWidgetSize(attributeValue) else AppWidgetSize.Unknown
    } finally {
        metadata.close()
    }
}

fun AppWidgetProviderInfo.extractSize(context: Context): AppWidgetSize {
    if (Build.VERSION.SDK_INT < 31) {
        return AppWidgetSize.Unknown
    }
    val metadata = getMetadata(context) ?: return AppWidgetSize.Unknown
    return try {
        metadata.moveToAttributeSet()
        val attributeValue = metadata.getAttributeValue(StyleableAttributes.WidgetSize.attributeName)
        if (attributeValue != null) AppWidgetSize(attributeValue) else AppWidgetSize.All
    } finally {
        metadata.close()
    }
}

fun filterPreviewSize(widgetSize: AppWidgetSize, previewSize: AppWidgetSize): AppWidgetSize {
    val result = ArrayList<AppWidgetSize>()
    val widgetSizes = widgetSize.toArrayList()
    val previewSizes = previewSize.toArrayList()

    SeslAppWidgetLog.d("ProviderInfo", "filtering widgetSize : $widgetSizes / previewSize : $previewSizes")

    if (previewSizes.isEmpty()) {
        result.addAll(widgetSizes.filter { it < AppWidgetSize.ExtraLarge })
    } else {
        previewSizes.forEach {
            val mask = it.mask
            if (mask != AppWidgetSize.ExtraLarge.mask) {
                val targetMask = if (mask == AppWidgetSize.ExtraLargeLong.mask) AppWidgetSize.ExtraLarge.mask else mask
                if (widgetSize.contains(AppWidgetSize(targetMask))) {
                    result.add(AppWidgetSize(mask))
                }
            }
        }
    }

    var finalSize = AppWidgetSize.Unknown
    result.forEach { finalSize += it }
    return finalSize
}

val AppWidgetProviderInfo.generatedColorfulPreviewStates: Int
    get() = try {
        val field = AppWidgetProviderInfo::class.java.getDeclaredField(AppWidgetProviderInfoReflections.GENERATED_COLORFUL_PREVIEW_STATES)
        field.isAccessible = true
        field.get(this) as Int
    } catch (e: Exception) {
        0
    }

val AppWidgetProviderInfo.generatedMonotonePreviewStates: Int
    get() = try {
        val field = AppWidgetProviderInfo::class.java.getDeclaredField(AppWidgetProviderInfoReflections.GENERATED_MONOTONE_PREVIEW_STATES)
        field.isAccessible = true
        field.get(this) as Int
    } catch (e: Exception) {
        0
    }

val AppWidgetProviderInfo.generatedPreviewHostKeys: Int
    get() = try {
        val field = AppWidgetProviderInfo::class.java.getDeclaredField(AppWidgetProviderInfoReflections.GENERATED_PREVIEW_PREVIEW_HOST_KEYS)
        field.isAccessible = true
        field.get(this) as Int
    } catch (e: Exception) {
        0
    }

private fun XmlResourceParser.getAttributeValue(attributeName: String): Int? {
    val attributeSet = Xml.asAttributeSet(this) ?: return null
    for (i in 0 until attributeSet.attributeCount) {
        if (attributeSet.getAttributeName(i) == attributeName) {
            return attributeSet.getAttributeIntValue(i, AppWidgetSize.All.mask)
        }
    }
    return null
}

private fun AppWidgetProviderInfo.getMetadata(context: Context): XmlResourceParser? {
    val pm = context.packageManager
    return try {
        @Suppress("NewApi")
        activityInfo?.loadXmlMetaData(pm, "android.appwidget.provider")
    } catch (e: Exception) {
        SeslAppWidgetLog.d("ProviderInfo", "Exception $e occurred during parsing $provider meta data")
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val providers = appWidgetManager.installedAllProviders(provider.packageName)
        val providerInfo = providers.firstOrNull { 
            it.provider.className == provider.className && it.provider.packageName == provider.packageName 
        }
        @Suppress("NewApi")
        providerInfo?.activityInfo?.loadXmlMetaData(pm, "android.appwidget.provider")
    }
}

@Throws(XmlPullParserException::class, IOException::class)
private fun XmlResourceParser.moveToAttributeSet(): XmlResourceParser {
    var type: Int
    while (next().also { type = it } != XmlResourceParser.END_DOCUMENT && type != XmlResourceParser.START_TAG) {
        // continue
    }
    return this
}
