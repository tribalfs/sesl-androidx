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

internal object AppWidgetManagerReflections {
    const val GET_APPWIDGET_SIZE_INFO = "hidden_semGetAppWidgetSizeInfo"
    const val GET_APPWIDGET_SIZE_INFOS = "hidden_semGetAppWidgetSizeInfos"
    const val GET_INSTALLED_PROVIDERS_FOR_PACKAGE_METHOD = "hidden_semGetInstalledProvidersForPackage"
    const val GET_TEMPLATE_WIDGET_PREVIEW_METHOD = "hidden_semGetTemplateWidgetPreview"
    const val GET_WIDGET_PREVIEW_FOR_HOST_METHOD = "hidden_semGetWidgetPreviewForHost"
    const val GET_WIDGET_PREVIEW_METHOD = "getWidgetPreview"
    const val IS_WIDGET_PREVIEW_FOR_HOST_UPDATE_AVAILABLE = "hidden_semIsPreviewForHostUpdateAvailable"
    const val REMOVE_TEMPLATE_WIDGET_PREVIEW_METHOD = "hidden_semRemoveTemplateWidgetPreview"
    const val REMOVE_WIDGET_PREVIEW_FOR_HOST_METHOD = "hidden_semRemoveWidgetPreviewForHost"
    const val REMOVE_WIDGET_PREVIEW_METHOD = "removeWidgetPreview"
    const val SET_APPWIDGET_SIZE_INFO = "hidden_semSetAppWidgetSizeInfo"
    const val SET_TEMPLATE_WIDGET_PREVIEW_METHOD = "hidden_semSetTemplateWidgetPreview"
    const val SET_WIDGET_PREVIEW_FOR_HOST_METHOD = "hidden_semSetWidgetPreviewForHost"
    const val SET_WIDGET_PREVIEW_METHOD = "setWidgetPreview"
}
