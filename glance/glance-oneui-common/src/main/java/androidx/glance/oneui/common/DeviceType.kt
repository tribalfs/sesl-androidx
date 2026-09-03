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

enum class DeviceType(val type: Int) {
    Phone(-1),
    FoldMain(0),
    FoldSub(5),
    Flip(100),
    Tablet(101),
    MultiFoldMain(102),
    MultiFoldSub(103);

    override fun toString(): String {
        return when (this) {
            FoldMain -> "fold-main"
            FoldSub -> "fold-sub"
            Flip -> "flip"
            Tablet -> "tablet"
            MultiFoldMain -> "multi-fold-main"
            MultiFoldSub -> "multi-fold-sub"
            else -> "phone"
        }
    }
}
