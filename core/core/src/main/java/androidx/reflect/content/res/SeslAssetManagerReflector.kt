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

package androidx.reflect.content.res

import android.content.res.AssetManager
import androidx.annotation.RestrictTo
import androidx.reflect.SeslBaseReflector

//sesl9
/**
 * Reflection wrapper for [AssetManager] Samsung theme overlay methods.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
object SeslAssetManagerReflector {
    private val mClass: Class<*> = AssetManager::class.java

    /**
     * Retrieves list of Samsung theme overlay packages from the given [assetManager].
     *
     * @param assetManager the [AssetManager] to inspect
     * @return list of overlay package names, or `null` if reflection failed
     */
    fun getSamsungThemeOverlays(assetManager: AssetManager): ArrayList<String?>? {
        var method = SeslBaseReflector.getMethod(
            mClass.name, "getSamsungThemeOverlays")
        if (method == null) {
            method = SeslBaseReflector.getDeclaredMethod(
                mClass.name, "getSamsungThemeOverlays")
        }
        if (method == null) {
            return null
        }

        val result = SeslBaseReflector.invoke(assetManager, method) ?: return null
        if (result !is ArrayList<*>) {
            return null
        }

        val overlays = ArrayList<String?>(result.size)
        for (overlay in result) {
            if (overlay is String) {
                overlays.add(overlay)
            }
        }
        return overlays
    }
}
