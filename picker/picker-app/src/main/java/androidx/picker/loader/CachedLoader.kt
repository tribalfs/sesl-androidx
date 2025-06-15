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

package androidx.picker.loader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * A generic class for loading and caching values.
 *
 * This class provides a mechanism to load values associated with keys and cache them for future
 * use. It uses a HashMap to store the cached values.
 *
 * @param K The type of the keys used for caching.
 * @param V The type of the values being cached.
 */
abstract class CachedLoader<K, V> {

    private val cachedMap = HashMap<K, V>()

    fun clear(key: K) { cachedMap.remove(key) }

    fun clear() { cachedMap.clear() }

    protected abstract fun createValue(key: K): V

    /**
     * Loads the value associated with the given key.
     *
     * If the value is already cached, it is returned from the cache. Otherwise, the value is
     * created using the `createValue` function, cached, and then returned.
     *
     * @param key The key for which to load the value.
     * @return A Flow that emits the loaded value.
     */
    fun load(key: K): Flow<V> = flow {
        val cached = cachedMap[key]
        if (cached != null) {
            emit(cached)
        } else {
            val value = createValue(key)
            cachedMap[key] = value
            emit(value)
        }
    }.flowOn(Dispatchers.Default)
}