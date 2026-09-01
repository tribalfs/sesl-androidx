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

package androidx.recyclerview.animation

import android.animation.ValueAnimator
import android.view.animation.Interpolator
import androidx.annotation.RestrictTo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

//Added in sesl7
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
abstract class SeslAnimatable<T : Any>(
    private val initialValue: T,
    private val defaultAnimationSpec: AnimationSpec
) : DisposableHandle {

    var animator: ValueAnimator? = null

    val value: T
        @Suppress("UNCHECKED_CAST")
        get() = animator?.animatedValue as? T ?: initialValue

    fun interface AnimationSpec {
        fun invoke(valueAnimator: ValueAnimator)
    }

    class SimpleAnimationSpec(
        val duration: Long,
        val interpolator: Interpolator
    ) : AnimationSpec {
        override fun invoke(valueAnimator: ValueAnimator) {
            valueAnimator.duration = duration
            valueAnimator.interpolator = interpolator
        }
    }

    object SnapAnimationSpec : AnimationSpec {
        override fun invoke(valueAnimator: ValueAnimator) {
            valueAnimator.duration = 0L
        }
    }

    abstract suspend fun animateTo(
        targetValue: T,
        animationSpec: AnimationSpec = defaultAnimationSpec
    )

    suspend fun snapTo(targetValue: T) {
        animateTo(targetValue, SnapAnimationSpec)
    }

    @JvmOverloads
    fun tryAnimateTo(
        targetValue: T,
        animationSpec: AnimationSpec = defaultAnimationSpec,
        dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
    ): Job {
        return CoroutineScope(dispatcher).launch {
            animateTo(targetValue, animationSpec)
        }
    }

    @JvmOverloads
    fun trySnapTo(
        targetValue: T,
        dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
    ): Job {
        return CoroutineScope(dispatcher).launch {
            snapTo(targetValue)
        }
    }

    override fun dispose() {
        animator?.removeAllListeners()
        animator?.cancel()
    }
}