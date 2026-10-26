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

package androidx.glance.oneui.common.sizepolicy.multifold

import androidx.glance.oneui.common.sizepolicy.SizeRatioPolicy

abstract class MultiFoldablePolicy : SizeRatioPolicy() {
    companion object {
        private const val ORIENTATION_STATE_KEY = 0
        private const val EASY_MODE_STATE_KEY = 1

        internal fun makeStateKey(isPortrait: Boolean, isEasyMode: Boolean): Int {
            val i = if (isPortrait) 0 else 1
            return if (isEasyMode) i or 2 else i
        }
    }
}
