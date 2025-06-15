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
import kotlin.reflect.KProperty
import kotlinx.coroutines.DisposableHandle


/**
 * A generic observable property that allows for value observation and modification.
 *
 * This class encapsulates a value of type [T] and provides mechanisms to:
 * - Get and set the value.
 * - Register listeners that are notified before or after the value changes.
 * - Bind to a callback that is invoked when the value changes or when the binding is established.
 *
 * It uses a [MutableState] to manage the underlying state, making it compatible with
 * Compose's state management system.
 *
 * @param T The type of the value held by this observable property.
 * @property state The underlying [MutableState] that holds the actual value.
 * @property onUpdated An optional callback that is invoked after the value has been successfully
 *   updated (and `beforeChange` listeners allowed the change).
 */
@Keep
open class ObservableProperty<T>(
    private val state: MutableState<T>,
    private val onUpdated: ((T) -> Unit)? = null
) {
    private val onAfterChangeListenerList = arrayListOf<(T, T) -> Unit>()
    private val onBeforeChangeListenerList = arrayListOf<(T, T) -> Boolean>()
    private var onBindCallback: ((T) -> Unit)? = null

    /**
     * Sets the value of the observable property using property delegation.
     * This is a convenience method that delegates to [MutableState.setValue].
     *
     * Note: This method does **not** invoke the `beforeChange` or `afterChange` listeners,
     * nor does it trigger the `onUpdated` or `onBindCallback` callbacks. It directly
     * modifies the underlying `state`. For behavior that includes these callbacks and listeners,
     * use the [setValue] method.
     *
     * @param value The new value to set for the property.
     */
    fun setState(value: T) = state.setValue(this, ::state, value)

    /**
     * Retrieves the current value of the observable property.
     *
     * This function delegates to the underlying [state]'s `getValue` method.
     *
     * @return The current value of type [T].
     */
    fun getState(): T = state.getValue(this, ::state)

    /**
     * Sets the value of the observable property.
     *
     * This function attempts to update the `state` with the new `value`.
     * - If the new `value` is the same as the current `state`, no action is taken, but the
     *   `onBindCallback` is still invoked.
     * - If the `value` is different:
     *     - It first calls all registered `beforeChange` listeners. If any of these listeners
     *       return `false`, the update is aborted, and the function returns.
     *     - If all `beforeChange` listeners allow the change (or if there are no such listeners),
     *       the `state` is updated to the new `value`.
     *     - After the `state` is updated, all registered `afterChange` listeners are invoked
     *       with the old and new values.
     *     - The `onUpdated` callback (if provided during construction) is invoked with the new
     *       `value`.
     * - Regardless of whether the value changed or not, the `onBindCallback` (if set via `bind`)
     *   is invoked with the new (or current) `value`.
     *
     * @param value The new value to set for the property.
     */
    fun setValue(value: T) {
        if (getState() != value) {
            if (!beforeChange(getState(), value)) return
            val state: T = getState()
            setState(value)
            afterChange(state, value)
            onUpdated?.invoke(value)
        }
        onBindCallback?.invoke(value)
    }

    fun setValue(thisRef: Any?, prop: KProperty<*>, value: T) {
        setValue(value)
    }

    /**
     * Sets the value of the property without invoking the `beforeChange` and `afterChange`
     * listeners. However, the `onUpdated` callback (if set) and `onBindCallback` callback
     * will still be triggered.
     *
     * This method is useful when you want to update the state internally without triggering
     * external change notifications, but still want to execute the primary update logic and
     * binding callbacks.
     *
     * @param value The new value to set.
     */
    fun setValueSilence(value: T) {
        setState(value)
        onBindCallback?.invoke(value)
    }

    private fun afterChange(oldValue: T, newValue: T) {
        for (listener in onAfterChangeListenerList) {
            listener.invoke(oldValue, newValue)
        }
    }

    private fun beforeChange(oldValue: T, newValue: T): Boolean {
        if (onBeforeChangeListenerList.isEmpty()) return true
        for (listener in onBeforeChangeListenerList) {
            if (!listener.invoke(oldValue, newValue)) return false
        }
        return true
    }

    /**
     * Binds a callback to this observable property.
     *
     * The provided [callback] will be invoked immediately with the current [state] of the property
     * when this function is called. Subsequently, the [callback] will also be invoked whenever the
     * property's [state] is updated via the [setValue] method.
     *
     * This method sets the internal `onBindCallback`. If a previous callback was set by this
     * method, it will be replaced.
     *
     * @param callback An optional lambda function that takes the new value of type [T] as input
     *   and returns [Unit]. If `null`, any existing bound callback will be cleared.
     * @return A [DisposableHandle] that can be used to remove the registered callback.
     *   Calling `dispose()` on the returned handle will clear the callback *only if* it is the
     *   same callback instance that was originally passed to this `bind` function. This prevents
     *   accidental removal of callbacks set by other parts of the code.
     */
    fun bind(callback: ((T) -> Unit)? = null): DisposableHandle {
        setOnBindCallback(callback)
        return object : DisposableHandle {
            override fun dispose() {
                if (onBindCallback == callback) {
                    setOnBindCallback(null)
                }
            }
        }
    }

    internal fun setOnBindCallback(callback: ((T) -> Unit)?) {
        onBindCallback = callback
        callback?.invoke(getState())
    }

    /**
     * Registers a listener that is notified after the value of this property has changed.
     *
     * The listener will be invoked with the old and new values.
     *
     * @param onValueUpdateListener The callback to be invoked after the value changes. It receives
     *   the old value (T) and the new value (T) as parameters.
     * @return A [DisposableHandle] that can be used to unregister the listener by calling its
     *   [DisposableHandle.dispose] method.
     */
    fun registerAfterChangeUpdateListener(
        onValueUpdateListener: (T, T) -> Unit
    ): DisposableHandle {
        onAfterChangeListenerList.add(onValueUpdateListener)
        return object : DisposableHandle {
            override fun dispose() {
                onAfterChangeListenerList.remove(onValueUpdateListener)
            }
        }
    }

    /**
     * Registers a listener that is invoked *before* the property's value changes.
     *
     * The listener receives the old and new values as arguments and should return `true` to allow
     * the change, or `false` to prevent it.
     *
     * @param onValueUpdateListener The listener function to register. It takes the old value (T)
     *   and the new value (T) as input and returns a Boolean indicating whether the change should
     *   proceed.
     * @return A [DisposableHandle] that can be used to unregister the listener by calling
     *   [DisposableHandle.dispose].
     */
    fun registerBeforeChangeUpdateListener(
        onValueUpdateListener: (T, T) -> Boolean
    ): DisposableHandle {
        onBeforeChangeListenerList.add(onValueUpdateListener)
        return object : DisposableHandle {
            override fun dispose() {
                onBeforeChangeListenerList.remove(onValueUpdateListener)
            }
        }
    }
}