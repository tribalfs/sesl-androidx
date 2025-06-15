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

/**
 * An abstract class that implements the [MutableState] interface and provides a base for
 * creating mutable state objects.
 *
 * This class is designed to be extended by concrete mutable state implementations. It
 * provides a `base` property that holds the underlying value of the state, as well as
 * methods for getting, setting, and updating the `base` value.
 *
 * Subclasses must implement the [getValue] and [setValue] methods to define how the
 * state value is accessed and modified.
 *
 * @param T The type of the base value.
 * @param R The type of the state value.
 * @property base The underlying value of the state.
 */
abstract class UpdateMutableState<T, R>(
    var base: T
) : MutableState<R?> {
    abstract override fun getValue(thisRef: Any?, prop: KProperty<*>): R?
    abstract override fun setValue(thisRef: Any?, prop: KProperty<*>, value: R?)

    open fun updateBase(newBase: T) {
        base = newBase
    }
}