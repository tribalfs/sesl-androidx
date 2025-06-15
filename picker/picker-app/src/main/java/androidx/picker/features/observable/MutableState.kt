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
 * A mutable value holder that can be observed for changes.
 *
 * This interface allows for creating properties that, when their value changes,
 * can trigger updates or reactions in other parts of the application.
 *
 * It utilizes Kotlin's delegated property mechanism, allowing for concise syntax
 * when declaring and using observable properties.
 *
 * Example usage:
 * ```kotlin
 * class MyViewModel {
 *     var name: String by mutableStateOf("Initial Name")
 * }
 *
 * // To observe changes (implementation details would depend on the specific
 * // observation mechanism used with MutableState):
 * // viewModel.observeName { newName -> println("Name changed to: $newName") }
 *
 * // To change the value:
 * // viewModel.name = "New Name" // This would trigger observers.
 * ```
 *
 * @param T The type of the value held by this state.
 */
interface MutableState<T> {
    operator fun getValue(thisRef: Any?, prop: KProperty<*>): T
    operator fun setValue(thisRef: Any?, prop: KProperty<*>, value: T)
}