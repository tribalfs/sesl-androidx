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

sealed class StyleableAttributes(var attributeName: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StyleableAttributes) return false
        return attributeName == other.attributeName
    }

    override fun hashCode(): Int {
        return attributeName.hashCode()
    }

    object ConfigureCoverScreen2x2 : StyleableAttributes("configureCoverScreen2x2")
    object ConfigureHomeScreen2x2 : StyleableAttributes("configureHomeScreen2x2")
    object ConfigureLockScreen : StyleableAttributes("configureLockScreen")
    object DimViewColor : StyleableAttributes("dimViewColor")
    object DimViewColorAtFullScreen : StyleableAttributes("dimViewColorAtFullScreen")
    object FeaturedWidget : StyleableAttributes("featuredWidget")
    object InitialLayoutExtraLarge : StyleableAttributes("initialLayoutExtraLarge")
    object InitialLayoutExtraLargeLong : StyleableAttributes("initialLayoutExtraLargeLong")
    object InitialLayoutLarge : StyleableAttributes("initialLayoutLarge")
    object InitialLayoutMedium : StyleableAttributes("initialLayoutMedium")
    object InitialLayoutSmall : StyleableAttributes("initialLayoutSmall")
    object InitialLayoutTiny : StyleableAttributes("initialLayoutTiny")
    object InitialLayoutWideSmall : StyleableAttributes("initialLayoutWideSmall")
    object LockWidgetSize : StyleableAttributes("lockWidgetSize")
    object MonotoneInitialLayoutMedium : StyleableAttributes("monotoneInitialLayoutMedium")
    object MonotoneInitialLayoutSmall : StyleableAttributes("monotoneInitialLayoutSmall")
    object MonotoneInitialLayoutTiny : StyleableAttributes("monotoneInitialLayoutTiny")
    object MonotonePreviewLayoutMedium : StyleableAttributes("monotonePreviewLayoutMedium")
    object MonotonePreviewLayoutSmall : StyleableAttributes("monotonePreviewLayoutSmall")
    object MonotonePreviewLayoutTiny : StyleableAttributes("monotonePreviewLayoutTiny")
    object MonotonePreviewSize : StyleableAttributes("monotonePreviewSize")
    object MonotoneWidgetSize : StyleableAttributes("monotoneWidgetSize")
    object PreviewLayoutExtraLarge : StyleableAttributes("previewLayoutExtraLarge")
    object PreviewLayoutExtraLargeLong : StyleableAttributes("previewLayoutExtraLargeLong")
    object PreviewLayoutLarge : StyleableAttributes("previewLayoutLarge")
    object PreviewLayoutMedium : StyleableAttributes("previewLayoutMedium")
    object PreviewLayoutSmall : StyleableAttributes("previewLayoutSmall")
    object PreviewLayoutTiny : StyleableAttributes("previewLayoutTiny")
    object PreviewLayoutWideSmall : StyleableAttributes("previewLayoutWideSmall")
    object PreviewSize : StyleableAttributes("previewSize")
    object TargetHost : StyleableAttributes("targetHost")
    object WidgetFeatures : StyleableAttributes("semWidgetFeatures")
    object WidgetSize : StyleableAttributes("widgetSize")
    object WidgetStyle : StyleableAttributes("widgetStyle")
}
