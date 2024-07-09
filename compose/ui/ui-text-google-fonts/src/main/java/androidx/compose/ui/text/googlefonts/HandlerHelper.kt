/*
 * Copyright 2022 The Android Open Source Project
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

package androidx.compose.ui.text.googlefonts

import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi

/**
 * Handler Helper helps make handlers.
 *
 * (with Async support API28+)
 */
internal object HandlerHelper {

    /** @return handler, with createAsync if API level supports it. */
    fun createAsync(looper: Looper): Handler {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Handler28Impl.createAsync(looper)
        } else {
            Handler(looper)
        }
    }

    @RequiresApi(28)
    internal object Handler28Impl {
        fun createAsync(looper: Looper): Handler {
            return Handler.createAsync(looper)
        }
    }
}
