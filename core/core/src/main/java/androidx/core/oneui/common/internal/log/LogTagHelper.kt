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

@file:JvmName("LogTagHelperKt")

package androidx.core.oneui.common.internal.log

import android.os.Build
import android.util.Log
import androidx.annotation.RestrictTo
import java.util.Locale

@JvmField
internal val IS_DEBUG_DEVICE: Boolean = run {
    val type = Build.TYPE.lowercase(Locale.ROOT)
    type == "eng" || type == "userdebug"
}

private fun LogTag.buildTag(): String {
    val ver = if (isDebugVersion) version else ""
    return "$ver."
}

//sesl9
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun LogTag.verbose(msg: String) {
    if (IS_DEBUG_DEVICE) {
        Log.v(buildTag(), msg)
    }
}

//sesl9
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun LogTag.debug(msg: String) {
    if (IS_DEBUG_DEVICE) {
        Log.d(buildTag(), msg)
    }
}

//sesl9
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun LogTag.info(msg: String) {
    Log.i(buildTag(), msg)
}

//sesl9
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun LogTag.warn(msg: String) {
    Log.w(buildTag(), msg)
}

//sesl9
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun LogTag.error(msg: String) {
    Log.e(buildTag(), msg)
}

//sesl9
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun LogTag.debugTouch(msg: String) {
    if (isDebugTouch) {
        debug(msg)
    }
}
