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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import androidx.annotation.RestrictTo
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
class SeslFloatAnimatable(
    initialValue: Float,
    defaultAnimationSpec: AnimationSpec,
    private val onValueUpdated: (Float) -> Unit
) : SeslAnimatable<Float>(initialValue, defaultAnimationSpec), DisposableHandle {

    override suspend fun animateTo(
        targetValue: Float,
        animationSpec: AnimationSpec
    ) {
        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                dispose()
            }
            dispose()
            if (value != targetValue) {
                animator = ValueAnimator.ofFloat(value, targetValue).apply {
                    animationSpec.invoke(this)
                    addUpdateListener { animator ->
                        val animatedValue = animator.animatedValue as Float
                        onValueUpdated(animatedValue)
                    }
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animator: Animator) {}
                        override fun onAnimationEnd(animator: Animator) {
                            continuation.resume(Unit)
                        }
                        override fun onAnimationCancel(animator: Animator) {}
                        override fun onAnimationRepeat(animator: Animator) {}
                    })
                    start()
                }
            } else {
                continuation.resume(Unit)
            }
        }
    }
}
