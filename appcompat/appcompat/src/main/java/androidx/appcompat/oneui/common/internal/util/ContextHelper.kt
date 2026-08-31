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

package androidx.appcompat.oneui.common.internal.util

import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.util.TypedValue
import androidx.annotation.RestrictTo
import androidx.appcompat.R
import androidx.core.view.accessibility.SeslAccessibilityManagerCompat

//sesl9

/**
 * Screen width in pixels.
 */
@get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
val Context.screenWidth: Int
    get() = resources.displayMetrics.widthPixels

/**
 * Screen height in pixels.
 */
@get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
val Context.screenHeight: Int
    get() = resources.displayMetrics.heightPixels

/**
 * Resolves [id] into [out].
 *
 * @return true if the resolved value is a pixel value, otherwise false.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Context.getFloat(id: Int, out: TypedValue): Boolean {
    resources.getValue(id, out, true)
    return out.type == TypedValue.COMPLEX_UNIT_PX
}

/**
 * Resolves [id] and returns its value if it is a pixel value.
 *
 * @return the resolved float value, or [default] if [id] does not resolve to a pixel value.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Context.getFloat(id: Int, default: Float): Float {
    val value = TypedValue()
    resources.getValue(id, value, true)
    return if (value.type == TypedValue.COMPLEX_UNIT_PX) value.float else default
}

/**
 * Whether a screen reader (e.g. TalkBack) is currently enabled.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Context.isTalkbackOn(): Boolean = SeslAccessibilityManagerCompat.isScreenReaderEnabled(this)

/**
 * Whether the current theme is a light theme.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Context.isLightTheme(): Boolean {
    val value = TypedValue()
    theme.resolveAttribute(R.attr.isLightTheme, value, true)
    return value.data != 0
}

/**
 * Whether the default (non-custom) theme is active.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Context.isDefaultTheme(): Boolean =
    TextUtils.isEmpty(Settings.System.getString(contentResolver, "current_sec_active_themepackage"))
