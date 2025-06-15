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

import androidx.annotation.Keep
import kotlin.Unit

/**
 * Represents an observable property whose value is derived from a base value of type [T]
 * and can be updated by changing the base value.
 *
 * This class extends [ObservableProperty] and uses an [UpdateMutableState] to manage
 * the underlying base value and the derived value. When the base value is updated
 * via the [update] method, the [UpdateMutableState] recomputes the derived value,
 * and if the derived value changes, it notifies any registered observers.
 *
 * @param T The type of the base value.
 * @param R The type of the derived value.
 * @param mutableState The [UpdateMutableState] that holds the base value and the logic to
 *                     derive the observable value.
 * @param onUpdated An optional callback function that is invoked when the derived value changes.
 *                  It receives the new derived value (or null if it hasn't been computed yet)
 *                  as its argument.
 */
@Keep
open class UpdateObservableProperty<T, R>(
    private val mutableState: UpdateMutableState<T, R>,
    onUpdated: ((R?) -> Unit)? = null
) : ObservableProperty<R?>(mutableState, onUpdated) {

    /**
     * Updates the base value of this observable property.
     *
     * This will trigger a recomputation of the derived value and notify any observers
     * if the derived value changes.
     *
     * @param newBase The new base value to set.
     */
    fun update(newBase: T) {
        mutableState.updateBase(newBase)
    }
}