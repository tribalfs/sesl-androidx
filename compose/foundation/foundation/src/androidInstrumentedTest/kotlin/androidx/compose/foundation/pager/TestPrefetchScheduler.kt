/*
 * Copyright 2024 The Android Open Source Project
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

package androidx.compose.foundation.pager

import androidx.compose.foundation.lazy.layout.PrefetchRequest
import androidx.compose.foundation.lazy.layout.PrefetchRequestScope
import androidx.compose.foundation.lazy.layout.PrefetchScheduler

internal class TestPrefetchScheduler : PrefetchScheduler {

    private var activeRequests = mutableListOf<PrefetchRequest>()

    override fun schedulePrefetch(prefetchRequest: PrefetchRequest) {
        activeRequests.add(prefetchRequest)
    }

    fun executeActiveRequests() {
        activeRequests.forEach { with(it) { scope.execute() } }
        activeRequests.clear()
    }

    private val scope =
        object : PrefetchRequestScope {
            override fun availableTimeNanos(): Long = Long.MAX_VALUE
        }
}
