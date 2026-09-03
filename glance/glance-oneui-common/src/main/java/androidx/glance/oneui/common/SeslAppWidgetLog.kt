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

import android.util.Log

class SeslAppWidgetLog {
    companion object {
        const val PREFIX = "GWT:"
        var caller: String = "[,common-1.1.4]"

        fun init(callerClass: String) {
            caller = "[${callerClass.substringAfterLast('.')},common-1.1.4]"
        }

        fun d(tag: String, msg: String) {
            Log.d(PREFIX + tag, "$caller $msg")
        }

        fun d(tag: String, msg: String, tr: Throwable) {
            // Log.d(PREFIX + tag, "$caller $msg", tr)
        }

        fun e(tag: String, msg: String) {
            Log.e(PREFIX + tag, "$caller $msg")
        }

        fun e(tag: String, msg: String, tr: Throwable) {
            Log.e(PREFIX + tag, "$caller $msg", tr)
        }

        fun i(tag: String, msg: String) {
            Log.i(PREFIX + tag, "$caller $msg")
        }

        fun i(tag: String, msg: String, tr: Throwable) {
            Log.i(PREFIX + tag, "$caller $msg", tr)
        }

        fun v(tag: String, msg: String) {
            Log.v(PREFIX + tag, "$caller $msg")
        }

        fun v(tag: String, msg: String, tr: Throwable) {
            Log.v(PREFIX + tag, "$caller $msg", tr)
        }

        fun w(tag: String, msg: String) {
            Log.w(PREFIX + tag, "$caller $msg")
        }

        fun w(tag: String, msg: String, tr: Throwable) {
            Log.w(PREFIX + tag, "$caller $msg", tr)
        }
    }
}
