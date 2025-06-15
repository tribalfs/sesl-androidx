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

package androidx.picker.helper

import android.widget.ImageView
import androidx.picker.loader.AppIconFlow
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Loads an icon into an ImageView using a coroutine and shows a shimmer effect while loading.
 * Returns a DisposableHandle to cancel the loading and hide the shimmer.
 */
fun ImageView.loadIcon(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    iconFlow: AppIconFlow,
    shimmerLayout: ShimmerFrameLayout
): DisposableHandle {
    shimmerLayout.visibility = android.view.View.VISIBLE
    shimmerLayout.startShimmer()
    val job = CoroutineScope(dispatcher).launch { loadIconJob(iconFlow, dispatcher, this@loadIcon, shimmerLayout) }
    return object : DisposableHandle {
        override fun dispose() {
            shimmerLayout.visibility = android.view.View.GONE
            shimmerLayout.stopShimmer()
            job.cancel()
        }
    }
}

/**
 * Coroutine lambda for loading an icon into an ImageView with shimmer effect.
 * This is typically launched in a coroutine scope.
 */
suspend fun loadIconJob(
    iconFlow: AppIconFlow,
    dispatcher: CoroutineDispatcher,
    imageView: ImageView,
    shimmerLayout: ShimmerFrameLayout
) {
    iconFlow
        .flowOn(dispatcher)
        .collect { drawable ->
            withContext(Dispatchers.Main) {
                imageView.setImageDrawable(drawable)
                shimmerLayout.visibility = android.view.View.GONE
                shimmerLayout.stopShimmer()
            }
        }
}

