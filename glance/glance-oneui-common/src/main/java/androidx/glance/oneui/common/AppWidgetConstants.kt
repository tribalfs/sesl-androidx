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

import androidx.annotation.RestrictTo

object AppWidgetConstants {
    private const val KEY_BACKGROUND_IMAGE_VIEW = 1003
    private const val KEY_DYNAMIC_LABEL = 1001
    private const val KEY_KEEP_CURRENT_COLOR = 1002

    const val OPTION_APPWIDGET_COLUMN_SPAN = "semAppWidgetColumnSpan"
    const val OPTION_APPWIDGET_DISPLAY_DENSITY = "semDisplayDensity"
    const val OPTION_APPWIDGET_DISPLAY_ID = "semDisplayId"
    const val OPTION_APPWIDGET_HOME_GRID = "hsHomeGrid"
    const val OPTION_APPWIDGET_HOME_MODE = "hsMode"
    const val OPTION_APPWIDGET_HOST_TYPE = "semHostType"
    const val OPTION_APPWIDGET_ICON_LABEL_ON = "hsIconLabelEnabled"
    const val OPTION_APPWIDGET_ROW_SPAN = "semAppWidgetRowSpan"
    const val OPTION_APPWIDGET_SCALE_RATIO = "semScaleRatio"
    const val OPTION_APPWIDGET_SIZE = "semWidgetSize"
    const val OPTION_APPWIDGET_STYLE = "semWidgetStyle"
    const val OPTION_APPWIDGET_STYLE_SHOW_SHADOW = "setShadow"
    const val OPTION_APPWIDGET_WIDGET_LABEL_ON = "hsWidgetLabelEnabled"

    internal const val SEM_WIDGET_CATEGORY_HIDDEN_FROM_3P = 8192
    internal const val SEM_WIDGET_CATEGORY_ONLY_SAMSUNG = 512

    const val VIEW_ID_ICON_SHADOW = "glance:templateIconShadow"
    const val VIEW_TAG_DYNAMIC_LABEL = -385875968
    const val VIEW_TAG_KEY_BACKGROUND_IMAGE_VIEW = -352321536
    const val VIEW_TAG_KEY_KEEP_CURRENT_COLOR = -369098752
}
