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

import android.content.Context
import android.util.LayoutDirection
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.appcompat.R
import androidx.core.content.ContextCompat

@ColorInt
fun Context.getPrimaryColor(): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
    return if (typedValue.resourceId != 0) {
        ContextCompat.getColor(this, typedValue.resourceId)
    } else {
        typedValue.data
    }
}

@ColorInt
fun Context.getPrimaryDarkColor(): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true)
    return if (typedValue.resourceId != 0) {
        ContextCompat.getColor(this, typedValue.resourceId)
    } else {
        typedValue.data
    }
}

@ColorInt
fun Context.getTextSecondaryColor(): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(android.R.attr.textColorSecondary, typedValue, true)
    return if (typedValue.resourceId != 0) {
        ContextCompat.getColor(this, typedValue.resourceId)
    } else {
        typedValue.data
    }
}

@ColorInt
fun Context.getRoundedCornerColor(): Int {
    val typedValue = TypedValue()
    val resolved = theme.resolveAttribute(R.attr.roundedCornerColor, typedValue, true)
    return if (resolved && typedValue.resourceId != 0) {
        ContextCompat.getColor(this, typedValue.resourceId)
    } else if (resolved) {
        typedValue.data
    } else {
        ContextCompat.getColor(this, androidx.picker.R.color.picker_app_list_subheader_background_color)
    }
}

fun Context.isRTL(): Boolean {
    return resources.configuration.layoutDirection == LayoutDirection.RTL
}