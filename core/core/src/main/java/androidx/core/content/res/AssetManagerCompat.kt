/*
 * Copyright (C) 2026 The Android Open Source Project
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

package androidx.core.content.res

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import androidx.annotation.RestrictTo
import androidx.reflect.content.res.SeslAssetManagerReflector

//sesl9
/**
 * Compatibility helper for checking Samsung theme overlay configuration on an [AssetManager].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
object AssetManagerCompat {

    /**
     * Checks whether any Samsung theme overlays are currently present in the given [assetManager].
     *
     * @param assetManager the [AssetManager] to inspect
     * @return `true` if Samsung theme overlays are present or if overlay state cannot be determined,
     *   `false` if overlays are confirmed to be empty
     */
    fun hasSamsungThemeOverlays(assetManager: AssetManager): Boolean {
        val overlays = SeslAssetManagerReflector.getSamsungThemeOverlays(assetManager)
            ?: return true
        return overlays.isNotEmpty()
    }

    /**
     * Checks whether any Samsung theme overlays are currently present in the given [resources].
     *
     * @param resources the [Resources] whose assets should be inspected
     * @return `true` if Samsung theme overlays are present, `false` otherwise
     */
    @JvmStatic
    fun hasSamsungThemeOverlays(resources: Resources): Boolean {
        return hasSamsungThemeOverlays(resources.assets)
    }

    /**
     * Checks whether any Samsung theme overlays are currently present in the given [context].
     *
     * @param context the [Context] whose resources should be inspected
     * @return `true` if Samsung theme overlays are present, `false` otherwise
     */
    fun hasSamsungThemeOverlays(context: Context): Boolean {
        return hasSamsungThemeOverlays(context.resources)
    }
}
