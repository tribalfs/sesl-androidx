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

import android.util.TypedValue
import android.view.View
import androidx.annotation.RestrictTo

//sesl9

/**
 * Whether the layout direction is right-to-left.
 */
@get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
val View.isLayoutRtl: Boolean
    get() = layoutDirection == View.LAYOUT_DIRECTION_RTL

/**
 * The resource entry name of this view's id.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun View.getResourceEntryName(): String? = resources?.getResourceEntryName(id)

/**
 * Resolves [id] into [out].
 *
 * @return true if the resolved value is a pixel value, otherwise false.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun View.getFloat(id: Int, out: TypedValue): Boolean {
    resources.getValue(id, out, true)
    return out.type == TypedValue.COMPLEX_UNIT_PX
}

/**
 * Resolves [id] and returns its value if it is a pixel value.
 *
 * @return the resolved float value, or [default] if [id] does not resolve to a pixel value.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun View.getFloat(id: Int, default: Float): Float {
    val value = TypedValue()
    resources.getValue(id, value, true)
    return if (value.type == TypedValue.COMPLEX_UNIT_PX) value.float else default
}

/**
 * Whether the view is scrolled to the top.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun View.isAtTop(): Boolean = !canScrollVertically(-1)

/**
 * Whether the view is scrolled to the bottom.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun View.isAtBottom(): Boolean = !canScrollVertically(1)
