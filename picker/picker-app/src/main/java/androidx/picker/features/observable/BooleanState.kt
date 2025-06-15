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

package androidx.picker.features.observable

import kotlin.reflect.KProperty

class BooleanState(
    var value: Boolean
) : MutableState<Boolean> {

    override operator fun getValue(thisRef: Any?, prop: KProperty<*>): Boolean {
        return value
    }

    override operator fun setValue(thisRef: Any?, prop: KProperty<*>, value: Boolean) {
        this.value = value
    }
}